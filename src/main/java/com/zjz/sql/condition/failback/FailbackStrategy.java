package com.zjz.sql.condition.failback;

import java.lang.annotation.*;

/**
 * FailbackStrategy
 *
 * @author zhaojingzhou
 * @date 2020/6/29 17:45
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FailbackStrategy {

    FailbackEnum failback() default FailbackEnum.THROW_EXCEPTION;
}
