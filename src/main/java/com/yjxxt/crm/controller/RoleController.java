package com.yjxxt.crm.controller;

import com.yjxxt.crm.annotation.RequiredPermission;
import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.bean.Role;
import com.yjxxt.crm.query.RoleQuery;
import com.yjxxt.crm.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;

    @RequestMapping("findRoles")
    @ResponseBody
    public List<Map<String,Object>> sayRoles(Integer userId){
        return roleService.findRoles(userId);
    }

    //角色模块前端查询列表
    @RequestMapping("index")
    public String index(){
        return "role/role";
    }

    //角色模块后端查询列表
    @RequestMapping("list")
    @ResponseBody
    @RequiredPermission(code="60")
    public Map<String,Object> list(RoleQuery roleQuery){
        return roleService.findRoleByParam(roleQuery);
    }

    //角色模块添加角色
    @RequestMapping("save")
    @ResponseBody
    public ResultInfo save(Role role){
        roleService.addRole(role);
        return success("角色添加成功");
    }

    //角色模块修改数据
    @RequestMapping("update")
    @ResponseBody
    public ResultInfo update(Role role){
        roleService.changeRole(role);
        return success("角色修改成功");
    }

    //角色模块前端——添加
    @RequestMapping("toAddOrUpdate")
    public String addOrUpdate(Integer roleId, Model model){
        if (roleId!=null){
            Role role= roleService.selectByPrimaryKey(roleId);
            model.addAttribute("role",role);
        }
        return "role/add_update";
    }

    //角色模块前端——删除
    @RequestMapping("delete")
    @ResponseBody
    public ResultInfo delete(Role role){
        roleService.removeRoleById(role);
        return success("角色删除成功");
    }

    //角色模块前端——授权
    @RequestMapping("toRoleGrantPage")
    public String toRoleGrantPage(Integer roleId, Model model){
        model.addAttribute("roleId",roleId);
        return "role/grant";
    }

    //角色模块——拿去资源信息与用户的ID值
    @RequestMapping("addGrant")
    @ResponseBody
    public ResultInfo grand(Integer roleId,Integer[] mids){
        roleService.addGrant(roleId,mids);
        return success("授权成功了");
    }
}
