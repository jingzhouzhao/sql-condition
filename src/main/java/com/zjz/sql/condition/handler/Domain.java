package com.zjz.sql.condition.handler;

import java.lang.annotation.*;

/**
 * Domain
 *
 * @author zhaojingzhou
 * @date 2020/6/30 21:21
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Domain {

    String domainIdAlias() default "domain_id";
}
