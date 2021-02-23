package com.zjz.sql.condition.handler;

import com.zjz.sql.condition.expr.Condition;
import com.zjz.sql.condition.expr.ConditionExpr;
import com.zjz.sql.condition.expr.ConditionOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DomainSqlHandler
 *
 * @author zhaojingzhou
 * @date 2020/6/30 21:22
 */
public class DomainSqlHandler extends AbstractSqlHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainSqlHandler.class);

    @Override
    public ConditionExpr doHandle(SqlHandlerContext ctx) throws Exception {
        Domain domain = (Domain) ctx.getAnnotation();
        Long domainId = null;
        //优先使用本地上下文参数
        Object localValue = SqlConditionContext.getValue(domain.domainIdAlias());
        if (localValue != null) {
            domainId = Long.valueOf(localValue.toString());
        }
        if (domainId == null) {
            throwContextError(domain.domainIdAlias());
        }
        return new Condition(domain.domainIdAlias(), ConditionOperator.EQ, domainId);
    }
}
