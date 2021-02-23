package com.zjz.sql.condition.handler;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQLHandlerRegistrar
 *
 * @author zhaojingzhou
 * @date 2020/6/23 17:44
 */
public class SqlHandlerRegistrar {

    private static final Map<Class<? extends Annotation>, SqlHandler> SQL_HANDLER_MAP = new ConcurrentHashMap<>();

    private SqlHandlerRegistrar(){}

    /**
     * 注册注解解析器
     * @param clazz
     * @param sqlHandler
     */
    public static void register(Class<? extends Annotation> clazz, SqlHandler sqlHandler){
        SQL_HANDLER_MAP.put(clazz, sqlHandler);
    }

    /**
     *
     * @param clazz
     * @return
     */
    public static SqlHandler getSQLHandler(Class<? extends Annotation> clazz) {
        return SQL_HANDLER_MAP.get(clazz);
    }

    public static Set<Class<? extends Annotation>> getAnnotations() {
        return SQL_HANDLER_MAP.keySet();
    }

}
