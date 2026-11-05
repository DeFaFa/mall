package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_subject")
public class Subject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String subTitle;
    private Long categoryId;
    private String pic;
    private String mainTitle;
    private String description;
    private Integer showStatus;
    private Integer sort;
    private Integer views;
    private LocalDateTime createTime;
}
