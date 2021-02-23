package com.zjz.sql.condition.handler;

import com.alibaba.fastjson.JSON;
import com.zjz.sql.condition.expr.ConditionExpr;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zjz.sql.condition.Constants.*;

/**
 * AbstractSqlHandler
 *
 * @author zhaojingzhou
 * @date 2020/7/1 18:41
 */
public abstract class AbstractSqlHandler implements SqlHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSqlHandler.class);

    @Override
    public ConditionExpr handle(SqlHandlerContext ctx) throws Exception {
        logContext();
        ConditionExpr expr = doHandle(ctx);
//        Cat.logEvent(EVENT_TYPE_ABSTRACT_SQL_HANDLER, EVENT_CONDITION_EXPR, Message.SUCCESS, JSON.toJSONString(expr));
        return expr;
    }

    public abstract ConditionExpr doHandle(SqlHandlerContext ctx) throws Exception;

    /**
     * 本地打印上下文参数以及上报cat
     */
    public void logContext() {
        String localCtx = JSON.toJSONString(SqlConditionContext.getAll());
//        LOGGER.info("AuthContext：{}，SqlConditionContext：{}", globalCtx, localCtx);
//        Cat.logEvent(EVENT_TYPE_ABSTRACT_SQL_HANDLER, EVENT_AUTH_CONTEXT, Message.SUCCESS, globalCtx);
//        Cat.logEvent(EVENT_TYPE_ABSTRACT_SQL_HANDLER, EVENT_SQL_CONDITION_CONTEXT, Message.SUCCESS, localCtx);
    }

    /**
     * 抛出上下文参数缺失异常
     *
     * @param key
     */
    public void throwContextError(String key) {
        String errorMsg = String.format(CTX_ERROR_MSG_TEMPLETE, key);
        LOGGER.error(errorMsg);
//        Cat.logEvent(EVENT_TYPE_ABSTRACT_SQL_HANDLER, EVENT_CONTEXT_MISSING, Message.SUCCESS, errorMsg);
        throw new PersistenceException(errorMsg);
    }
}
