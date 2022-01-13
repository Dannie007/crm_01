package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.bean.UserRole;
import com.yjxxt.crm.mapper.UserMapper;
import com.yjxxt.crm.mapper.UserRoleMapper;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.Md5Util;
import com.yjxxt.crm.utils.PhoneUtil;
import com.yjxxt.crm.utils.UserIDBase64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User,Integer> {


    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    public UserModel userLogin(String userName,String userPwd){
        checkUserLoginParam(userName,userPwd);
        //用户是否存在
        User temp=userMapper.selectUserByName(userName);
        AssertUtil.isTrue(temp==null,"用户不存在");
        //用户密码是否正确-userPwd:用户输入密码；temp.getUserPwd-数据库中存入的密码
        checkUserPwd(userPwd,temp.getUserPwd());
        //构建返回对象
        return builderUserInfo(temp);
    }


    /*
    * 构建返回目标对象
    * */
    private UserModel builderUserInfo(User user) {
        //实例化目标对象
        UserModel userModel=new UserModel();
        //加密
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        //返回目标对象
        return userModel;
    }


    /*
    * 校验用户名与密码
    * */
    private void checkUserLoginParam(String userName, String userPwd) {
        //用户非空
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");
        //密码非空
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"密码不能为空");
    }


    /*
    * 验证密码
    * */
    private void checkUserPwd(String userPwd, String userPwd1) {
        //对输入的密码进行加密
        userPwd = Md5Util.encode(userPwd);
        //将加密的密码与数据库中的密码进行比较
        AssertUtil.isTrue(!userPwd.equals(userPwd1),"用户密码不正确");
    }

    /*
    * 修改密码的方法
    * */
    public void changeUserPwd(Integer userId,String oldPassword,String newPassword,String confirmPwd){
        //用户登录了修改
        User user=userMapper.selectByPrimaryKey(userId);
        //密码验证
        checkPasswordParams(user,oldPassword,newPassword,confirmPwd);
        //修改密码
        user.setUserPwd(Md5Util.encode(newPassword));
        //确认密码是否修改成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)<1,"修改失败了");
    }

    /*
    * 修改密码的验证
    * */
    private void checkPasswordParams(User user, String oldPassword, String newPassword, String confirmPwd) {
        AssertUtil.isTrue(user==null,"用户未登录或不存在");
        //原始密码非空
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"请输入原始密码");
        //原始密码是否正确
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(oldPassword))),"原始密码不正确");
        //新密码非空
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"新密码不能为空");
        //新密码不能与原密码一致
        AssertUtil.isTrue(newPassword.equals(oldPassword),"新密码与旧密码不能相同");
        //确认密码非空
        AssertUtil.isTrue(StringUtils.isBlank(confirmPwd),"确认密码不能为空");
        //确认密码与新密码一致
        AssertUtil.isTrue(!confirmPwd.equals(newPassword),"确认密码与新密码要一致");

    }

    //查询所有的销售人员
    public List<Map<String,Object>> querySales(){
        return userMapper.selectSales();
    }

    /*
    * 用户模块的列表查询
    * */
    public Map<String,Object> findUserByParams(UserQuery userQuery){
        //实例化map
        Map<String,Object> map = new HashMap<String,Object>();
        //初始化分页单位
        PageHelper.startPage(userQuery.getPage(),userQuery.getLimit());
        //开始分页
        PageInfo<User> plist=new PageInfo<User>(selectByParams(userQuery));


        //准备数据
        map.put("code",0);
        map.put("msg","success");
        map.put("count",plist.getTotal());
        map.put("data",plist.getList());

        return map;
    }

    //用户管理模块后台实现——添加功能
    /*
    * 一·验证
    * 1·用户非空，且唯一
    * 2·邮箱非空
    * 3·手机号非空，格式正确
    * 二·设定默认值
    * 1·is_valid=1
    * 2·createData 系统时间
    * 3·updateDate 系统时间 
    * 密码：123456——加密
    * 三·判断添加是否成功
    *
    * */

    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user){
        //一·验证
        //1·用户非空，且唯一
        //2·邮箱非空
        //3·手机号非空，格式正确
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());
        //用户名唯一
        User temp = userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp != null, "用户名已经存在");

        //二·设定默认值
        //1·is_valid=1
        //2·createData 系统时间
        //3·updateDate 系统时间 
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        //密码：123456——加密
        user.setUserPwd(Md5Util.encode("123456"));
        //三·判断添加是否成功
        //获取自动增长列的方法与配置，拿到用户ID与角色ID的方式：insertSelective或insertHashkey
        AssertUtil.isTrue(insertSelective(user)<1,"添加失败了");
        System.out.println(user.getId()+"<<<<<"+user.getRoleIds());
        //批量添加--关联两张表
        relationUseRole(user.getId(),user.getRoleIds());
    }


    //操作中间表
    /*
    * userId：用户ID
    * roleId：角色ID 1,3,5……
    *       进行用户编辑时：要先判断角色ID是否存在：
    *                       存在：先删除，在添加
    * */
    private void relationUseRole(Integer userId, String roleIds) {
        //准备集合存储对象
        List<UserRole> urlist=new ArrayList<UserRole>();
        //userId，roleId
        AssertUtil.isTrue(StringUtils.isBlank(roleIds),"请先选择角色");
        //统计当前用户有多少个角色
        int count = userRoleMapper.countUserRoleNum(userId);
        //删除用户角色信息
        if (count>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色删除失败");
        }
        //删除原来的角色
        String[] RoleStrId = roleIds.split(",");

        //遍历
        for (String rid: RoleStrId) {
            //准备对象
            UserRole userRole=new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(Integer.parseInt(rid));
            userRole.setCreateDate(new Date());
            userRole.setUpdateDate(new Date());
            //存放到集合
            urlist.add(userRole);
        }

        //批量添加
        AssertUtil.isTrue(userRoleMapper.insertBatch(urlist)!=urlist.size(),"用户角色分配失败");
    }


    private void checkUser(String userName, String email, String phone) {
        //一·验证
        //1·用户非空，
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");

        //2·邮箱非空
        AssertUtil.isTrue(StringUtils.isBlank(email),"邮箱不能为空");

        //3·手机号非空，格式正确
        AssertUtil.isTrue(StringUtils.isBlank(phone),"手机号不能为空");
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone),"请输入合法的手机号");

    }

    //用户管理模块后台实现——更新功能
    /*
     * 一·验证
     * 用户有ID，没有不能修改
     * 1·用户非空，且唯一
     * 2·邮箱非空
     * 3·手机号非空，格式正确
     * 二·设定默认值
     * 1·is_valid=1
     *
     * 3·updateDate 系统时间
     * 密码：123456——加密
     * 三·判断更新是否成功
     *
     * */

    @Transactional(propagation = Propagation.REQUIRED)
    public void changeUser(User user){
        //根据ID获取用户的信息
        User temp = userMapper.selectByPrimaryKey(user.getId());
        //判断
        AssertUtil.isTrue(temp == null, "待修改的记录不存在");
        //验证参数
        checkUser(user.getUserName(), user.getEmail(), user.getPhone());
        //修改用户名已经存在问题
        User temp2 = userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp2 != null && !(temp2.getId().equals(user.getId())), "用户名称已经存在");
        //设定默认值
        user.setUpdateDate(new Date());
        //判断修改是否成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(user) < 1, "修改失败了");
        //
        relationUseRole(user.getId(),user.getRoleIds());

    }


    //批量删除
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeUserIds(Integer[] ids){
        //验证
        AssertUtil.isTrue(ids == null || ids.length == 0,"请选择要删除的项目");

        //删除用户角色信息
        for (Integer userId: ids) {
            //统计当前用户有多少个角色
            int count = userRoleMapper.countUserRoleNum(userId);
            //删除用户角色信息
            if (count>0){
                AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色删除失败");
            }
        }
        //判断删除成功与否
        AssertUtil.isTrue(deleteBatch(ids)<1,"删除失败了");
    }
}
