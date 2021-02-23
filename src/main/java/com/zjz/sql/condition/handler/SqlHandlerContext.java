package com.zjz.sql.condition.handler;

import java.lang.annotation.Annotation;

/**
 * SQLContext
 *
 * @author zhaojingzhou
 * @date 2020/6/28 13:04
 */
public class SqlHandlerContext {

    /**
     * 原始SQL
     */
    private String rawSql;

    /**
     * 当前Handler对应的注解，可获取注解属性实现条件判断等（需要类型转换）
     */
    private Annotation annotation;

    public String getRawSql() {
        return rawSql;
    }

    public void setRawSql(String rawSql) {
        this.rawSql = rawSql;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }
}
