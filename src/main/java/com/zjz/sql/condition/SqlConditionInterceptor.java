package com.zjz.sql.condition;

import com.zjz.sql.condition.failback.FailbackEnum;
import com.zjz.sql.condition.failback.FailbackStrategy;
import com.zjz.sql.condition.handler.SqlHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.zjz.sql.condition.Constants.*;

/**
 * SQLConditionInterceptor
 *
 * @author zhaojingzhou
 * @date 2020/6/29 16:49
 */
@Intercepts({@Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
), @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
), @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
)})
public class SqlConditionInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlConditionInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Exception {
//        Transaction transaction = Cat.newTransaction(CatConstants.TYPE_SQL, TRANS_SQL_CONDITION_INTERCEPTOR);
        try {
            StopWatch stopWatch = new StopWatch(TASK_SQL_CONDITION_INTERCEPTOR);
            Object[] args = invocation.getArgs();
            MappedStatement mappedStatement = (MappedStatement) args[0];
            stopWatch.start(TASK_GET_SUPPORTED_SQL_HANDLER);
            //获取所有支持的handler
            Map<Annotation, SqlHandler> supportedSQLHandler = ParseUtil.getSupportedSQLHandler(mappedStatement.getId());
            stopWatch.stop();
            //无需处理
            if (CollectionUtils.isEmpty(supportedSQLHandler)) {
                return invocation.proceed();
            }
            Object parameter = args[1];
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            String rawSql = boundSql.getSql();
            LOGGER.debug("Raw SQL:{}", rawSql);
//            Cat.logEvent(EVENT_TYPE_SQL_CONDITION_INTERCEPTOR, EVENT_RAW_SQL, Message.SUCCESS, rawSql);
            try {
                stopWatch.start(TASK_PARSING_SQL);
                String newSql = ParseUtil.parseSQL(supportedSQLHandler, rawSql);
                stopWatch.stop();
                LOGGER.debug("New SQL:{}", newSql);
//                Cat.logEvent(EVENT_TYPE_SQL_CONDITION_INTERCEPTOR, EVENT_NEW_SQL, Message.SUCCESS, newSql);
                stopWatch.start(TASK_EXECUTE_NEW_SQL);
                replaceArgs(args, mappedStatement, boundSql, newSql, parameter);
                return invocation.proceed();
            } catch (Exception e) {
                stopWatch.stop();
                Throwable throwable = ExceptionUtil.unwrapThrowable(e);
                LOGGER.warn("SQL execution failed:", throwable);
//                Cat.logError(throwable);
//                transaction.setStatus(throwable);
                stopWatch.start(TASK_EXECUTE_FAILBACK);
                return failback(e, rawSql, invocation);
            } finally {
                stopWatch.stop();
                String timeReport = stopWatch.toString();
                LOGGER.info(timeReport);
//                Cat.logEvent(EVENT_TYPE_SQL_CONDITION_INTERCEPTOR, EVENT_TIME_CONSUMING_REPORT, Message.SUCCESS, timeReport);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(stopWatch.prettyPrint());
                }
            }
        }catch (Exception e){
//            transaction.setStatus(e);
            LOGGER.error("SqlConditionInterceptor",e);
            return invocation.proceed();
        }finally {
//            transaction.complete();
        }
    }

    /**
     * 异常降级
     *
     * @param e
     * @param rawSql
     * @param invocation
     * @return
     * @throws Exception
     */
    private Object failback(Exception e, String rawSql, Invocation invocation) throws Exception {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        FailbackStrategy failbackStrategy = ParseUtil.getFailbackStrategy(mappedStatement.getId());
        Object parameter = args[1];
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        LOGGER.warn("Start the failback strategy:{}", failbackStrategy);
//        Cat.logEvent(EVENT_TYPE_SQL_CONDITION_INTERCEPTOR, "failbackStrategy", Message.SUCCESS, String.valueOf(failbackStrategy));
        if (failbackStrategy == null || FailbackEnum.THROW_EXCEPTION.equals(failbackStrategy.failback())) {
            throw e;
        }
        if (FailbackEnum.EXEC_RAW_SQL.equals(failbackStrategy.failback())) {
            replaceArgs(args, mappedStatement, boundSql, rawSql, parameter);
            return invocation.proceed();

        }
        return null;
    }

    /**
     * 替换statement中sql
     *
     * @param args
     * @param mappedStatement
     * @param boundSql
     * @param newSql
     * @param parameter
     */
    private void replaceArgs(Object[] args, MappedStatement mappedStatement, BoundSql boundSql, String newSql, Object parameter) {
        args[0] = newMappedStatement(mappedStatement, boundSql, newSql);
        if (args.length == CACHE_EXECUTOR_QUERY_MAX_PARAM_LENGTH) {
            args[5] = copyFromBoundSql(mappedStatement, boundSql, newSql, boundSql.getParameterMappings(), parameter);
        }
    }

    /**
     * 替换MappedStatement
     *
     * @param ms
     * @param oldBound
     * @param sqlStr
     * @return
     */
    private MappedStatement newMappedStatement(final MappedStatement ms, BoundSql oldBound, String sqlStr) {
        MappedStatement newStatement = copyFromMappedStatement(ms, new BoundSqlSqlSource(oldBound));
        MetaObject msObject = MetaObject.forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        msObject.setValue("sqlSource.boundSql.sql", sqlStr);
        return newStatement;
    }

    /**
     * 替换 boundsql
     *
     * @param ms
     * @param boundSql
     * @param sql
     * @param parameterMappings
     * @param parameter
     * @return
     */
    private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql,
                                      String sql, List<ParameterMapping> parameterMappings, Object parameter) {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, parameterMappings, parameter);
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    /**
     * copyFromMappedStatement
     *
     * @param ms
     * @param newSqlSource
     * @return
     */
    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource,
                ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        throw new UnsupportedOperationException();
    }


    static class BoundSqlSqlSource implements SqlSource {
        private final BoundSql boundSql;

        BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
