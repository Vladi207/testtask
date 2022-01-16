package com.game.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.Date;

@StaticMetamodel(Player.class)
public abstract class Player_ {
    public static volatile SingularAttribute<Player, Long> id;
    public static volatile SingularAttribute<Player, String> name;
    public static volatile SingularAttribute<Player, String> title;
    public static volatile SingularAttribute<Player, Race> race;
    public static volatile SingularAttribute<Player, Profession> profession;
    public static volatile SingularAttribute<Player, Date> birthday;
    public static volatile SingularAttribute<Player, Boolean> banned;
    public static volatile SingularAttribute<Player, Integer> experience;
    public static volatile SingularAttribute<Player, Integer> level;
    public static volatile SingularAttribute<Player, Integer> untilNextLevel;
}
