package com.zjz.sql.condition.handler;

import com.google.common.collect.Sets;
import com.zjz.sql.condition.expr.Condition;
import com.zjz.sql.condition.expr.ConditionExpr;
import com.zjz.sql.condition.expr.ConditionOperator;
import com.zjz.sql.condition.expr.Conditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Set;


/**
 * DataAuthSQLHandler
 *
 * @author zhaojingzhou
 * @date 2020/6/23 17:50
 */
public class DataAuthSqlHandler extends AbstractSqlHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAuthSqlHandler.class);

    @Override
    public ConditionExpr doHandle(SqlHandlerContext ctx) throws Exception {
        DataAuth dataAuth = (DataAuth) ctx.getAnnotation();
        Long domainId = null;
        //优先使用本地上下文参数
        Object localValue = SqlConditionContext.getValue(dataAuth.domainIdAlias());
        if (localValue != null) {
            domainId = Long.valueOf(localValue.toString());
        }
        if (domainId == null) {
            throwContextError(dataAuth.domainIdAlias());
        }
        //orgId 租户
        Long orgId = null;
        //优先使用本地上下文参数
        localValue = SqlConditionContext.getValue(dataAuth.orgIdAlias());
        if (localValue != null) {
            orgId = Long.valueOf(localValue.toString());
        }
        if (orgId == null) {
            throwContextError(dataAuth.orgIdAlias());
        }
        //要包含的表(不指定则所有表将添加条件)
        Set<String> includeTables = null;
        if (dataAuth.includeTables().length != 0) {
            includeTables = Sets.newHashSet(dataAuth.includeTables());
        }
        //domain_id = xxx
        Condition domainIdCondition = new Condition(dataAuth.domainIdAlias(), ConditionOperator.EQ, domainId);
        //org_id = xxx
        Condition orgIdCondition = new Condition(dataAuth.orgIdAlias(), ConditionOperator.EQ, orgId);
        //(domain_id = xxx and org_id = xxx)
        Conditions domainAndOrgIdCondition = new Conditions(domainIdCondition, ConditionOperator.AND, orgIdCondition);
        domainAndOrgIdCondition.setIncludeTables(includeTables);

        //数据权限orgid
        Collection<Long> dataOrgId = null;
        localValue = SqlConditionContext.getValue(dataAuth.dataOrgIdAlias());
        if (localValue != null) {
            dataOrgId = (Collection) localValue;
        }
        if (CollectionUtils.isEmpty(dataOrgId)) {
            throwContextError(dataAuth.dataOrgIdAlias());
        }
        //data_org_id in(x,x,x)
        Condition dataOrgIdCondition = new Condition(dataAuth.dataOrgIdAlias(), ConditionOperator.IN, dataOrgId);
        //(domain_id = xxx and org_id = xxx and and data_org_id in(x,x,x))
        return new Conditions(domainAndOrgIdCondition, ConditionOperator.AND, dataOrgIdCondition);
    }

}
