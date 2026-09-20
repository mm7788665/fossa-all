package com.txy.mapper;

import com.txy.entity.AdminPerm;
import com.txy.entity.AdminRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 角色与权限 */
public interface AdminRoleMapper {

    List<AdminRole> selectRoles();

    AdminRole selectRole(@Param("roleKey") String roleKey);

    int insertRole(AdminRole r);

    int updateRole(AdminRole r);

    int softDeleteRole(@Param("roleKey") String roleKey);

    /** 全量替换某角色的权限（先删后插） */
    int deletePerms(@Param("roleKey") String roleKey);

    int insertPerms(@Param("roleKey") String roleKey, @Param("perms") List<String> perms);

    List<AdminPerm> selectPerms();

    List<String> selectPermsOfRole(@Param("roleKey") String roleKey);

    /** 有多少账号在用这个角色（防止误删） */
    int countAccountByRole(@Param("roleKey") String roleKey);
}
