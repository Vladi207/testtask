package com.game.controller;


import com.game.entity.Player;
import com.game.exception.NotValidValueException;
import com.game.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {
    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @GetMapping
    public List<Player> getPlayersList(@RequestParam Map<String, String> params) {
        return service.getPlayerList(params);
    }

    @PostMapping
    public Player createPlayer(@RequestBody Map<String, String> params) {
        if (PlayerService.validateParams(params, true)) {
            Player player = new Player();
            PlayerService.applyParams(player, params);
            return service.savePlayer(player);
        }
        throw new NotValidValueException();
    }

    @GetMapping("/count")
    public long getPlayersCount(@RequestParam Map<String, String> params) {
        return service.getPlayersCount(params);
    }

    @GetMapping("/{id}")
    public Player getPlayer(@PathVariable String id) {
        return service.getPlayer(id);
    }

    @PostMapping("/{id}")
    public Player updatePlayer(@PathVariable String id, @RequestBody Map<String, String> params) {
        if (PlayerService.validateParams(params, false)) {
            Player player = service.getPlayer(id);
            PlayerService.applyParams(player, params);
            return service.savePlayer(player);
        }
        throw new NotValidValueException();
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable String id) {
        service.deletePlayer(service.getPlayer(id));
    }
}
