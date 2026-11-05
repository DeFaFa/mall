package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ums_member")
public class Member {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberLevelId;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String nickname;
    private String phone;
    private Integer status;
    private String icon;
    private Integer gender;
    private LocalDate birthday;
    private String city;
    private Integer integration;
    private Integer growth;
    private Integer source;
    private LocalDateTime createTime;
}
