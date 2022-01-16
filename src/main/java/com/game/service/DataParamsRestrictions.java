package com.game.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

public interface DataParamsRestrictions {
    HashSet<String> REQUIRED_PARAMS = new HashSet<String>() {{
        add("name");
        add("title");
        add("race");
        add("profession");
        add("birthday");
        add("experience");
    }};
    int MAX_NAME_LENGTH = 12;
    int MAX_TITLE_LENGTH = 30;
    int MIN_EXPERIENCE = 0;
    int MAX_EXPERIENCE = 10_000_000;
    long MIN_BIRTHDAY_IN = new GregorianCalendar(2000, Calendar.JANUARY, 1).getTimeInMillis();
    long MAX_BIRTHDAY_EX = new GregorianCalendar(3001, Calendar.JANUARY, 1).getTimeInMillis();
}
