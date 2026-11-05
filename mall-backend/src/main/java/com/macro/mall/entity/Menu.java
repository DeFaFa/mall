package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("ums_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String title;
    private Integer level;
    private Integer sort;
    private String name;
    private String icon;
    private Integer hidden;

    @TableField(exist = false)
    private List<Menu> children;
}
