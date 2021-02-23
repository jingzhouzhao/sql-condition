package com.zjz.sql.condition;

/**
 * Constants
 *
 * @author zhaojingzhou
 * @date 2020/7/1 16:56
 */
public class Constants {

    private Constants() {
    }

    /**
     * constants
     */
    public static final int CACHE_EXECUTOR_QUERY_MAX_PARAM_LENGTH = 6;
    public static final String SP_CHAR = ".";
    public static final String SPLIT_CHAR = "\\.";

    /**
     * error
     */
    public static final String CTX_ERROR_MSG_TEMPLETE = "Could not get the value of [%s],Please confirm whether the value exists！";

    /**
     * cat
     */
    public static final String TRANS_SQL_CONDITION_INTERCEPTOR = "SqlConditionInterceptor";
    public static final String EVENT_TYPE_ABSTRACT_SQL_HANDLER = "AbstractSqlHandler";
    public static final String EVENT_TYPE_SQL_CONDITION_INTERCEPTOR = "SqlConditionInterceptor";
    public static final String EVENT_RAW_SQL = "rawSql";
    public static final String EVENT_NEW_SQL = "newSql";
    public static final String EVENT_TIME_CONSUMING_REPORT = "TimeConsumingReport";
    public static final String EVENT_CONDITION_EXPR = "ConditionExpr";
    public static final String EVENT_AUTH_CONTEXT = "AuthContext";
    public static final String EVENT_SQL_CONDITION_CONTEXT = "SqlConditionContext";
    public static final String EVENT_CONTEXT_MISSING = "ContextMissing";

    /**
     * stopwatch
     */
    public static final String TASK_SQL_CONDITION_INTERCEPTOR = "SqlConditionInterceptor";
    public static final String TASK_GET_SUPPORTED_SQL_HANDLER = "get Supported SQLHandler";
    public static final String TASK_PARSING_SQL = "Parsing SQL";
    public static final String TASK_EXECUTE_NEW_SQL = "Execute New SQL";
    public static final String TASK_EXECUTE_FAILBACK = "Execute Failback";
    public static final String OWNER_ID = "owner_id";
}
