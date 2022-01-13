package com.yjxxt.crm.controller;

import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.service.UserService;
import com.yjxxt.crm.utils.LoginUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    //前端修改密码，页面的跳转
    @RequestMapping("toPasswordPage")
    public String updatePwd(){
        return "user/password";
    }

    //基本信息-setting后传参，页面中才可以显示数据
    @RequestMapping("toSettingPage")
    public String setting(HttpServletRequest req){
        //获取用户的ID
        int userId = LoginUserUtil.releaseUserIdFromCookie(req);
        //调用方法
        User user = (User) userService.selectByPrimaryKey(userId);
        //存储
        req.setAttribute("user",user);
        //转发
        return "user/setting";
    }

    @RequestMapping("login")
    @ResponseBody
    public ResultInfo say(User user){
        ResultInfo resultInfo=new ResultInfo();
        //try{
            UserModel userModel= userService.userLogin(user.getUserName(),user.getUserPwd());
            resultInfo.setResult(userModel);
        /*}catch (ParamsException e){
            e.printStackTrace();
            //设置状态码和提示信息
            resultInfo.setCode(e.getCode());
            resultInfo.setMsg(e.getMsg());
        }catch (Exception e){
            e.printStackTrace();
            resultInfo.setCode(500);
            resultInfo.setMsg("操作失败！");
        }*/
        return resultInfo;
    }

    @RequestMapping("setting")
    @ResponseBody
    public ResultInfo sayUpdate(User user){
        ResultInfo resultInfo=new ResultInfo();
        //修改信息-BaseMapper中有update……该方法，代码生成器也自动生成了根据用户进行更改的sql语句
        userService.updateByPrimaryKeySelective(user);
        //返回目标对象
        return resultInfo;
    }

    @PostMapping("updatePwd")
    @ResponseBody
    public ResultInfo updatePwd(HttpServletRequest req,String oldPassword,String newPassword,String confirmPwd){
        ResultInfo resultInfo=new ResultInfo();
        //获取Cookie中的userId
        int userId= LoginUserUtil.releaseUserIdFromCookie(req);
        //修改密码操作
        //try{
            userService.changeUserPwd(userId,oldPassword,newPassword,confirmPwd);
        /*}catch (ParamsException pe){
            pe.printStackTrace();
            resultInfo.setCode(pe.getCode());
            resultInfo.setResult(pe.getMsg());
        }catch (Exception ex){
            ex.printStackTrace();
            resultInfo.setCode(500);
            resultInfo.setMsg("操作失败！");
        }*/

        return resultInfo;

    }

    //传输指派人选的下拉框
    @RequestMapping("sales")
    @ResponseBody
    public List<Map<String,Object>> findSales(){
        List<Map<String,Object>> list = userService.querySales();
        return list;
    }


    //用户管理模块列表
    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> list(UserQuery userQuery){

        return userService.findUserByParams(userQuery);
    }

    //用户管理模块，前端显示
    @RequestMapping("index")
    public String index(){
        return "user/user";
    }

    //用户管理模块——前端增加更新
    @RequestMapping("addOrUpdatePage")
    public String addOrUpdatePage(Integer id, Model model){
        if (id!=null){
            User user = userService.selectByPrimaryKey(id);
            model.addAttribute("user",user);
        }
        return "user/add_update";
    }


    @RequestMapping("save")
    @ResponseBody
    public ResultInfo save(User user){
        //用户的添加
        userService.addUser(user);
        //返回目标对象
        return success("用户添加成功了");
    }

    @RequestMapping("update")
    @ResponseBody
    public ResultInfo update(User user){
        //用户的添加
        userService.changeUser(user);
        //返回目标对象
        return success("用户修改成功了");
    }

    //批量删除
    @RequestMapping("delete")
    @ResponseBody
    public ResultInfo delete(Integer[] ids){
        //用户的删除
        userService.removeUserIds(ids);
        //返回目标对象
        return success("批量删除成功了");
    }

}
