package com.macro.mall.dto;

import com.macro.mall.entity.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleParam extends Role {
    private List<Long> menuIds;
    private List<Long> resourceIds;
}
