package com.yjxxt.crm.mapper;

import com.yjxxt.crm.base.BaseMapper;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.query.UserQuery;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public interface UserMapper extends BaseMapper<User,Integer> {

    User selectUserByName(String userName);

    @MapKey("")
    List<Map<String,Object>> selectSales();

}