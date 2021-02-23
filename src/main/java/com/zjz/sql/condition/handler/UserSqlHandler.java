package com.zjz.sql.condition.handler;

import com.google.common.collect.Lists;
import com.zjz.sql.condition.expr.Condition;
import com.zjz.sql.condition.expr.ConditionExpr;
import com.zjz.sql.condition.expr.ConditionOperator;
import com.zjz.sql.condition.expr.Conditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrgSqlHandler
 *
 * @author zhaojingzhou
 * @date 2020/7/1 10:07
 */
public class UserSqlHandler extends AbstractSqlHandler {

    private static final Logger log = LoggerFactory.getLogger(UserSqlHandler.class);

    @Override
    public ConditionExpr doHandle(SqlHandlerContext ctx) throws Exception {
        User org = (User) ctx.getAnnotation();
//        Long userId = null;
        //优先使用本地上下文参数
        /*Object localValue = SqlConditionContext.getValue(org.userIdAlias());
        if (localValue == null) {
            log.info("SqlConditionContext.getValue is null");
            return null;
        }*/
        Condition condition1 = new Condition(org.userIdAlias(), ConditionOperator.IN, Lists.newArrayList(1, 2, 3));
        Condition condition2 = new Condition(org.userIdAlias(), ConditionOperator.IS, null);
        return new Conditions(condition1, ConditionOperator.OR, condition2);

    }
}
