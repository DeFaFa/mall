package com.macro.mall.dto;

import com.macro.mall.entity.Subject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectParam extends Subject {
    private List<Long> productIds;
}
