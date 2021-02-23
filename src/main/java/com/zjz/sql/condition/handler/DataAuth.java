package com.zjz.sql.condition.handler;

import java.lang.annotation.*;

/**
 * DataAuth
 *
 * @author zhaojingzhou
 * @date 2020/6/23 17:47
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataAuth {

    String dataOrgIdAlias() default "data_org_id";

    String domainIdAlias() default "domain_id";

    String orgIdAlias() default "org_id";

    String[] includeTables() default {};


}
