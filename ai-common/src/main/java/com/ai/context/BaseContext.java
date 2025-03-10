package com.ai.context;

import java.util.Map;

public class BaseContext {
    public static ThreadLocal<Map<String,Object>> threadLocal = new ThreadLocal<>();

    public static void set(Map<String,Object> map){
        threadLocal.set(map);
    }
    public static Map<String,Object> get(){
        return threadLocal.get();
    }
    public static void remove(){
        threadLocal.remove();
    }
}
