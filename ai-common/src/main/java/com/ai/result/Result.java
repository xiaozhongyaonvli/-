package com.ai.result;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 返回前端包装
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;//成功返回200，失败返回其他code
    private T data;      //成功返回数据
    private String msg;  //失败返回信息


    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.setCode(200);
        return result;
    }

    public static <T> Result<T> success(T obj){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setData(obj);
        return result;
    }

    public static <T> Result<T> success(T data, String msg){
        Result<T> result = new Result<>();
        result.setData(data);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> error(Integer code){
        Result<T> result = new Result<>();
        result.setCode(code);
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg, T data){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }
}
