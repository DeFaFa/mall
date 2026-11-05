package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cms_prefrence_area")
public class PrefrenceArea {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String pic;
    private Integer sort;
    private String subTitle;
    private Integer showStatus;
}
