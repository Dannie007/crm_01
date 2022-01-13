package com.yjxxt.crm.mapper;

import com.yjxxt.crm.base.BaseMapper;
import com.yjxxt.crm.bean.Permission;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission,Integer> {

    int countRoleModulesByRoleId(Integer roleId);

    int deleteRoleModuleByRoleId(Integer roleId);

    List<Integer> selectModelByRoleId(Integer roleId);

    List<String> selectUserHasRolesHasPermissions(Integer userId);

}