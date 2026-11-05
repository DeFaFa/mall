package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sms_flash_promotion")
public class FlashPromotion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private LocalDateTime createTime;
}
