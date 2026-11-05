package com.macro.mall.dto;

import com.macro.mall.entity.Admin;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminParam extends Admin {
    private List<Long> roleIds;
}
