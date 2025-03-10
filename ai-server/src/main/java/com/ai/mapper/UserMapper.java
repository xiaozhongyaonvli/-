package com.ai.mapper;

import com.ai.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    /**
     * 根据phone查找用户
     * @param phone 电话号码
     * @return      返回与该号码绑定的用户
     */
    User selectByPhone(String phone);

    /**
     * 插入用户
     * @param user 新用户信息
     */
    void insert(User user);
}
