package com.zjz.sql.condition.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableSqlCondition
 *
 * @author zhaojingzhou
 * @date 2020/6/30 11:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({AutoSqlConditionConfiguration.class})
public @interface EnableAutoSqlCondition {
}
