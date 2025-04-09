package com.ai.mapper;

import com.ai.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对应user表
 */
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

    /**
     * 根据用户名查用户
     * @param userName 用户名
     * @return         对应的用户
     */
    User selectByUserName(String userName);

    /**
     * 更新用户数据
     * @param user 用户新数据
     */
    void update(User user);
}
