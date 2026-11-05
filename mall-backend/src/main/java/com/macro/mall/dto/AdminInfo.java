package com.macro.mall.dto;

import com.macro.mall.entity.Admin;
import com.macro.mall.entity.Menu;
import com.macro.mall.entity.Role;
import lombok.Data;

import java.util.List;

@Data
public class AdminInfo {
    private Admin admin;
    private List<Role> roles;
    private List<Menu> menus;
    private List<String> permissions;
}
