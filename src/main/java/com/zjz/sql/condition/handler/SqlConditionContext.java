package com.zjz.sql.condition.handler;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * SqlConditionContext
 *
 * @author zhaojingzhou
 * @date 2020/7/1 15:06
 */
public class SqlConditionContext {

    private SqlConditionContext() {
    }

    private static final ThreadLocal<Map<String, Object>> DELEGATE = ThreadLocal.withInitial(Maps::newHashMap);

    public static void setValue(String key, Object value) {
        DELEGATE.get().put(key, value);
    }

    public static Object getValue(String key) {
        return DELEGATE.get().get(key);
    }

    public static Map<String,Object> getAll() {
        return DELEGATE.get();
    }

    /**
     * 建议确认context不再使用了调用此方法
     */
    public void unload() {
        DELEGATE.remove();
    }

}
