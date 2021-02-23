package com.zjz.sql.condition.handler;

import com.zjz.sql.condition.expr.Condition;
import com.zjz.sql.condition.expr.ConditionExpr;
import com.zjz.sql.condition.expr.ConditionOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrgSqlHandler
 *
 * @author zhaojingzhou
 * @date 2020/7/1 10:07
 */
public class OrgSqlHandler extends AbstractSqlHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrgSqlHandler.class);

    @Override
    public ConditionExpr doHandle(SqlHandlerContext ctx) throws Exception {
        Org org = (Org) ctx.getAnnotation();
        Long orgId = null;
        //优先使用本地上下文参数
        Object localValue = SqlConditionContext.getValue(org.orgIdAlias());
        if (localValue != null) {
            orgId = Long.valueOf(localValue.toString());
        }
        return new Condition(org.orgIdAlias(), ConditionOperator.EQ, orgId);
    }
}
