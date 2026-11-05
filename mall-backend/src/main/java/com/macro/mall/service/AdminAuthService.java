package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.auth.TokenService;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.AdminInfo;
import com.macro.mall.entity.Admin;
import com.macro.mall.entity.Menu;
import com.macro.mall.entity.Resource;
import com.macro.mall.entity.Role;
import com.macro.mall.mapper.AdminMapper;
import com.macro.mall.mapper.LinkMapper;
import com.macro.mall.mapper.MenuMapper;
import com.macro.mall.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminMapper adminMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final LinkMapper linkMapper;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> login(String username, String password) {
        if (username == null || password == null) {
            throw new BizException("请输入用户名和密码");
        }
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        }
        admin.setLoginTime(LocalDateTime.now());
        adminMapper.updateById(admin);
        String token = tokenService.createAdminToken(admin.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("info", getInfo(admin.getId()));
        return result;
    }

    public void logout(String token) {
        tokenService.removeAdminToken(token);
    }

    public AdminInfo getInfo(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BizException("管理员不存在");
        }
        List<Long> roleIds = linkMapper.selectRoleIdsByAdmin(adminId);
        List<Role> roles = roleIds.isEmpty() ? List.of() : roleMapper.selectBatchIds(roleIds);
        boolean superAdmin = roleIds.contains(1L);

        List<Menu> menus;
        List<String> permissions;
        if (superAdmin) {
            menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSort));
            permissions = linkMapper.selectAllResources().stream()
                    .map(r -> r.getId() + ":" + r.getName()).collect(Collectors.toList());
        } else {
            Set<Long> menuIds = new LinkedHashSet<>();
            Set<Long> resourceIds = new LinkedHashSet<>();
            for (Long roleId : roleIds) {
                menuIds.addAll(linkMapper.selectMenuIdsByRole(roleId));
                resourceIds.addAll(linkMapper.selectResourceIdsByRole(roleId));
            }
            menus = menuIds.isEmpty() ? List.of() : menuMapper.selectBatchIds(menuIds);
            menus.sort(Comparator.comparing(Menu::getSort));
            List<Resource> resources = resourceIds.isEmpty() ? List.of() : linkMapper.selectAllResources().stream()
                    .filter(r -> resourceIds.contains(r.getId())).toList();
            permissions = resources.stream().map(r -> r.getId() + ":" + r.getName()).collect(Collectors.toList());
        }
        AdminInfo info = new AdminInfo();
        info.setAdmin(admin);
        info.setRoles(roles);
        info.setMenus(buildTree(menus));
        info.setPermissions(permissions);
        return info;
    }

    public static List<Menu> buildTree(List<Menu> menus) {
        Map<Long, Menu> map = new LinkedHashMap<>();
        for (Menu menu : menus) {
            menu.setChildren(new ArrayList<>());
            map.put(menu.getId(), menu);
        }
        List<Menu> roots = new ArrayList<>();
        for (Menu menu : menus) {
            Menu parent = map.get(menu.getParentId());
            if (menu.getParentId() == null || menu.getParentId() == 0 || parent == null) {
                roots.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }
        roots.sort(Comparator.comparing(m -> m.getSort() == null ? 0 : m.getSort()));
        roots.forEach(AdminAuthService::sortChildren);
        return roots;
    }

    private static void sortChildren(Menu menu) {
        menu.getChildren().sort(Comparator.comparing(m -> m.getSort() == null ? 0 : m.getSort()));
        menu.getChildren().forEach(AdminAuthService::sortChildren);
    }
}
