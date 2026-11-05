package com.macro.mall.dto;

import lombok.Data;

@Data
public class OrderNoteParam {
    private Long orderId;
    private String note;
}
