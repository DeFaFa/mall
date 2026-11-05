package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalTime;

@Data
@TableName("sms_flash_promotion_session")
public class FlashPromotionSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
}
