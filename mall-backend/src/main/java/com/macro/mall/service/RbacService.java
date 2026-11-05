package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.AdminParam;
import com.macro.mall.dto.RoleParam;
import com.macro.mall.entity.Admin;
import com.macro.mall.entity.Menu;
import com.macro.mall.entity.Resource;
import com.macro.mall.entity.Role;
import com.macro.mall.mapper.AdminMapper;
import com.macro.mall.mapper.LinkMapper;
import com.macro.mall.mapper.MenuMapper;
import com.macro.mall.mapper.ResourceMapper;
import com.macro.mall.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RbacService {
    private final AdminMapper adminMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final ResourceMapper resourceMapper;
    private final LinkMapper linkMapper;
    private final PasswordEncoder passwordEncoder;

    public Page<Admin> adminPage(int pageNum, int pageSize, String keyword) {
        return adminMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Admin>()
                .like(keyword != null && !keyword.isBlank(), Admin::getUsername, keyword)
                .orderByDesc(Admin::getId));
    }

    public Map<String, Object> adminDetail(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BizException("管理员不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("admin", admin);
        result.put("roleIds", linkMapper.selectRoleIdsByAdmin(id));
        return result;
    }

    @Transactional
    public void createAdmin(AdminParam param) {
        if (param.getUsername() == null || param.getUsername().isBlank()) {
            throw new BizException("用户名不能为空");
        }
        if (param.getPassword() == null || param.getPassword().length() < 6) {
            throw new BizException("密码至少 6 位");
        }
        long exists = adminMapper.selectCount(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, param.getUsername()));
        if (exists > 0) {
            throw new BizException("用户名已存在");
        }
        Admin admin = new Admin();
        admin.setUsername(param.getUsername());
        admin.setPassword(passwordEncoder.encode(param.getPassword()));
        admin.setNickName(param.getNickName());
        admin.setEmail(param.getEmail());
        admin.setNote(param.getNote());
        admin.setIcon(param.getIcon());
        admin.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        admin.setCreateTime(LocalDateTime.now());
        adminMapper.insert(admin);
        assignRoles(admin.getId(), param.getRoleIds());
    }

    @Transactional
    public void updateAdmin(Long id, AdminParam param) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BizException("管理员不存在");
        }
        Admin update = new Admin();
        update.setId(id);
        update.setUsername(param.getUsername());
        update.setNickName(param.getNickName());
        update.setEmail(param.getEmail());
        update.setNote(param.getNote());
        update.setIcon(param.getIcon());
        update.setStatus(param.getStatus());
        if (param.getPassword() != null && !param.getPassword().isBlank()) {
            update.setPassword(passwordEncoder.encode(param.getPassword()));
        }
        adminMapper.updateById(update);
        linkMapper.deleteAdminRoles(id);
        assignRoles(id, param.getRoleIds());
    }

    private void assignRoles(Long adminId, List<Long> roleIds) {
        if (roleIds == null) {
            return;
        }
        for (Long roleId : roleIds) {
            linkMapper.insertAdminRole(adminId, roleId);
        }
    }

    @Transactional
    public void deleteAdmin(Long id) {
        if (id == 1L) {
            throw new BizException("不能删除超级管理员");
        }
        linkMapper.deleteAdminRoles(id);
        adminMapper.deleteById(id);
    }

    // ---------------- 角色 ----------------
    public List<Role> roles(String keyword) {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .like(keyword != null && !keyword.isBlank(), Role::getName, keyword)
                .orderByAsc(Role::getSort));
    }

    public RoleParam roleDetail(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        RoleParam param = new RoleParam();
        org.springframework.beans.BeanUtils.copyProperties(role, param);
        param.setMenuIds(linkMapper.selectMenuIdsByRole(id));
        param.setResourceIds(linkMapper.selectResourceIdsByRole(id));
        return param;
    }

    @Transactional
    public void createRole(RoleParam param) {
        if (param.getName() == null || param.getName().isBlank()) {
            throw new BizException("角色名称不能为空");
        }
        Role role = new Role();
        role.setName(param.getName());
        role.setDescription(param.getDescription());
        role.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        role.setSort(param.getSort() == null ? 0 : param.getSort());
        role.setAdminCount(0);
        role.setCreateTime(LocalDateTime.now());
        roleMapper.insert(role);
        saveRoleRelations(role.getId(), param);
    }

    @Transactional
    public void updateRole(Long id, RoleParam param) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        role.setName(param.getName());
        role.setDescription(param.getDescription());
        role.setStatus(param.getStatus());
        role.setSort(param.getSort());
        roleMapper.updateById(role);
        linkMapper.deleteRoleMenus(id);
        linkMapper.deleteRoleResources(id);
        saveRoleRelations(id, param);
    }

    private void saveRoleRelations(Long roleId, RoleParam param) {
        if (param.getMenuIds() != null) {
            for (Long menuId : param.getMenuIds()) {
                linkMapper.insertRoleMenu(roleId, menuId);
            }
        }
        if (param.getResourceIds() != null) {
            for (Long resourceId : param.getResourceIds()) {
                linkMapper.insertRoleResource(roleId, resourceId);
            }
        }
    }

    @Transactional
    public void deleteRole(Long id) {
        if (id == 1L) {
            throw new BizException("不能删除超级管理员角色");
        }
        linkMapper.deleteRoleMenus(id);
        linkMapper.deleteRoleResources(id);
        linkMapper.deleteAdminRolesByRole(id);
        roleMapper.deleteById(id);
    }

    // ---------------- 菜单 ----------------
    public List<Menu> menuTree() {
        List<Menu> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSort));
        return AdminAuthService.buildTree(menus);
    }

    public void createMenu(Menu menu) {
        if (menu.getTitle() == null || menu.getTitle().isBlank()) {
            throw new BizException("菜单标题不能为空");
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        menu.setLevel(menu.getParentId() == 0 ? 0 : 1);
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getHidden() == null) {
            menu.setHidden(0);
        }
        menuMapper.insert(menu);
    }

    public void updateMenu(Menu menu) {
        if (menu.getId() == null) {
            throw new BizException("菜单不存在");
        }
        if (menu.getParentId() != null) {
            menu.setLevel(menu.getParentId() == 0 ? 0 : 1);
        }
        menuMapper.updateById(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        long children = menuMapper.selectCount(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id));
        if (children > 0) {
            throw new BizException("请先删除子菜单");
        }
        linkMapper.deleteRoleMenusByMenu(id);
        menuMapper.deleteById(id);
    }

    // ---------------- 资源 ----------------
    public Page<Resource> resourcePage(int pageNum, int pageSize, String keyword) {
        return resourceMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Resource>()
                .like(keyword != null && !keyword.isBlank(), Resource::getName, keyword)
                .orderByAsc(Resource::getId));
    }

    public List<Resource> allResources() {
        return linkMapper.selectAllResources();
    }

    public void createResource(Resource resource) {
        if (resource.getName() == null || resource.getUrl() == null) {
            throw new BizException("资源名称和 URL 不能为空");
        }
        if (resource.getMethod() == null || resource.getMethod().isBlank()) {
            resource.setMethod("*");
        }
        resource.setCreateTime(LocalDateTime.now());
        resourceMapper.insert(resource);
    }

    public void updateResource(Resource resource) {
        resourceMapper.updateById(resource);
    }

    @Transactional
    public void deleteResource(Long id) {
        linkMapper.deleteRoleResourcesByResource(id);
        resourceMapper.deleteById(id);
    }
}
