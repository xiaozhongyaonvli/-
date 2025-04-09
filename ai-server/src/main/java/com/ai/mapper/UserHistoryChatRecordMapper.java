package com.ai.mapper;

import com.ai.entity.UserHistoryChatRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 对应bk_user_chat_record表
 */
@Mapper
public interface UserHistoryChatRecordMapper {

    /**
     * 根据用户id查询所有的对话
     * @param userId 用户id
     * @return       返回用户列表
     */
    List<UserHistoryChatRecord> selectByUserId(Integer userId);
}
