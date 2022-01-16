package com.game.service;

import com.game.controller.PlayerOrder;

import java.util.HashSet;

interface PageRequestParamsDefault {
    int PAGE_NUMBER = 0;
    int PAGE_SIZE = 3;
    String ORDER = PlayerOrder.ID.getFieldName();

    HashSet<String> NAMES = new HashSet<String>() {{
        add("pageNumber");
        add("pageSize");
        add("order");
    }};
}
