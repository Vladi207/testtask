package com.game.service;


import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Player_;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception.NotFoundElementException;
import com.game.exception.NotValidValueException;
import com.game.repository.PlayerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerService {
    private final PlayerRepository repository;

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Player> getPlayerList(Map<String, String> params) {
        HashSet<String> prParamsKeys = PageRequestParamsDefault.NAMES;

        Map<String, String> filterParams = params.entrySet().stream()
                .filter(entry -> !prParamsKeys.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //without filtering
        if (filterParams.isEmpty())
            return repository.findAll(createPageRequest(params)).toList();

        //with filtering
        return repository.findAll(createSpecification(filterParams), createPageRequest(params)).toList();
    }

    @Transactional(readOnly = true)
    public long getPlayersCount(Map<String, String> params) {
        if (params.isEmpty())
            return repository.count();
        return repository.count(createSpecification(params));
    }

    @Transactional(readOnly = true)
    public Player getPlayer(String id) {
        if (!validateId(id))
            throw new NotValidValueException();

        Optional<Player> optionalPlayer = repository.findById(Long.parseLong(id));
        if (!optionalPlayer.isPresent())
            throw new NotFoundElementException();

        return optionalPlayer.get();
    }

    @Transactional
    public Player savePlayer(Player player) {
        beforeSave(player);
        return repository.save(player);
    }

    @Transactional
    public void deletePlayer(Player player) {
        repository.delete(player);
    }

    public static void applyParams(Player player, Map<String, String> params) {
        fixNullValues(params);
        for(Map.Entry<String, String> param : params.entrySet()) {
            if (param.getValue() == null) continue;
            try {
                switch (param.getKey()) {
                    case "name" :
                        player.setName(param.getValue()); break;
                    case "title" :
                        player.setTitle(param.getValue()); break;
                    case "race" :
                        player.setRace(Race.valueOf(param.getValue())); break;
                    case "profession" :
                        player.setProfession(Profession.valueOf(param.getValue()));break;
                    case "birthday" :
                        player.setBirthday(new Date(Long.parseLong(param.getValue())));break;
                    case "banned" :
                        player.setBanned("true".equals(param.getValue()));break;
                    case "experience" :
                        player.setExperience(Integer.parseInt(param.getValue()));break;
                }
            }catch (IllegalArgumentException e) {
                throw new NotValidValueException();
            }
        }
    }

    public static boolean validateParams(Map<String, String> params, boolean checkRequired) {
        if (!checkRequired && params.isEmpty())
            return true;

        params.keySet().forEach(String :: toLowerCase);
        if (checkRequired) {
            if (!params.keySet().containsAll(DataParamsRestrictions.REQUIRED_PARAMS))
                throw new NotValidValueException();
        }

        fixNullValues(params);
        boolean valid = true;
        for (Map.Entry<String, String> param : params.entrySet()) {
            try {
                switch (param.getKey()) {
                    case "name" :
                        String name = param.getValue();
                        valid = !name.isEmpty()
                                && (name.length() <= DataParamsRestrictions.MAX_NAME_LENGTH);
                        break;
                    case "title" :
                        valid = (param.getValue().length() <= DataParamsRestrictions.MAX_TITLE_LENGTH);
                        break;
                    case "experience" :
                        int experience = Integer.parseInt(param.getValue());
                        valid = (experience >= DataParamsRestrictions.MIN_EXPERIENCE)
                                && (experience <= DataParamsRestrictions.MAX_EXPERIENCE);
                        break;
                    case "birthday" :
                        long birthday = Long.parseLong(param.getValue());
                        valid = (birthday >= DataParamsRestrictions.MIN_BIRTHDAY_IN)
                                && (birthday < DataParamsRestrictions.MAX_BIRTHDAY_EX);
                        break;
                }
            }catch (NumberFormatException e) {
                throw new NotValidValueException();
            }
            if (!valid) throw new NotValidValueException();
        }
        return true;
    }

    private static void beforeSave(Player player) {
        // calc and set current level
        player.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));

        // calc and set experience until next level
        player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());
    }

    private static void fixNullValues(Map<String, String> params) {

    }

    private static String fixNullValue(String check, String result) {
        if (check == null || check.isEmpty())
            return result;
        return check;
    }

    private static boolean validateId(String id) {
        try {
            return (Long.parseLong(id) > 0);
        }catch (NumberFormatException e) {
            return false;
        }
    }

    private static Specification<Player> createSpecification(Map<String, String> filters) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            for (Map.Entry<String, String> filter : filters.entrySet()) {
                Predicate p = null;
                switch (filter.getKey()) {
                    case "name" :
                        p = criteriaBuilder.like(root.get(Player_.name), "%"+filter.getValue()+"%"); break;
                    case "title" :
                        p = criteriaBuilder.like(root.get(Player_.title), "%"+filter.getValue()+"%"); break;
                    case "race" :
                        p = criteriaBuilder.equal(root.get(Player_.race), Race.valueOf(filter.getValue())); break;
                    case "profession" :
                        p = criteriaBuilder.equal(root.get(Player_.profession), Profession.valueOf(filter.getValue())); break;
                    case "banned" :
                        p = criteriaBuilder.equal(root.get(Player_.banned), "true".equals(filter.getValue())); break;
                    case "after" :
                        p = criteriaBuilder.greaterThan(root.get(Player_.birthday), new Date(Long.parseLong(filter.getValue()))); break;
                    case "before" :
                        p = criteriaBuilder.lessThan(root.get(Player_.birthday), new Date(Long.parseLong(filter.getValue()))); break;
                    case "minExperience" :
                        p = criteriaBuilder.greaterThanOrEqualTo(root.get(Player_.experience), Integer.parseInt(filter.getValue())); break;
                    case "maxExperience" :
                        p = criteriaBuilder.lessThanOrEqualTo(root.get(Player_.experience), Integer.parseInt(filter.getValue())); break;
                    case "minLevel" :
                        p = criteriaBuilder.greaterThanOrEqualTo(root.get(Player_.level), Integer.parseInt(filter.getValue())); break;
                    case "maxLevel" :
                        p = criteriaBuilder.lessThanOrEqualTo(root.get(Player_.level), Integer.parseInt(filter.getValue())); break;
                }
                if (p != null)
                    predicate = criteriaBuilder.and(predicate, p);
            }
            return predicate;
        };
    }

    private static PageRequest createPageRequest(Map<String, String> params) {
        int pageNumber = params.containsKey("pageNumber") ?
                Integer.parseInt(params.get("pageNumber")) :
                PageRequestParamsDefault.PAGE_NUMBER;
        int pageSize = params.containsKey("pageSize") ?
                Integer.parseInt(params.get("pageSize")) :
                PageRequestParamsDefault.PAGE_SIZE;
        String order = params.containsKey("order") ?
                PlayerOrder.valueOf(params.get("order")).getFieldName() :
                PageRequestParamsDefault.ORDER;

        return PageRequest.of(pageNumber, pageSize, Sort.by(order));
    }
}
