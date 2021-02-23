package com.zjz.sql.condition.handler;

import com.zjz.sql.condition.expr.ConditionExpr;

/**
 * SQLHandler
 *
 * @author zhaojingzhou
 * @date 2020/6/23 17:46
 */
public interface SqlHandler {

    /**
     * SQL处理注解对应处理方法
     * 参数从Context中获取
     *
     * @param ctx 环境变量，用于扩展
     * @exception
     * @return 返回SQL条件表达式
     */
    ConditionExpr handle(SqlHandlerContext ctx) throws Exception;


}
