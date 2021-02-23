package com.zjz.sql.condition.handler;

import java.lang.annotation.*;

/**
 * Org 租户ORG_ID
 *
 * @author zhaojingzhou
 * @date 2020/7/1 09:57
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Org {

    String orgIdAlias() default "org_id";
}
