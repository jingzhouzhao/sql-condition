package com.zjz.sql.condition;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.zjz.sql.condition.expr.ConditionExpr;
import com.zjz.sql.condition.failback.FailbackStrategy;
import com.zjz.sql.condition.handler.SqlHandler;
import com.zjz.sql.condition.handler.SqlHandlerContext;
import com.zjz.sql.condition.handler.SqlHandlerRegistrar;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zjz.sql.condition.Constants.SP_CHAR;

/**
 * ParseUtil
 *
 * @author zhaojingzhou
 * @date 2020/6/30 10:55
 */
public final class ParseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseUtil.class);

    private static final LoadingCache<String, Map<Annotation, SqlHandler>> SUPPORTED_SQL_HANDLER_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(Runtime.getRuntime().availableProcessors()).initialCapacity(100).maximumSize(200).expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new SupportedSqlHandlerLoader());

    private ParseUtil() {
    }

    /**
     * 获取降级策略
     *
     * @param msId
     * @return
     */
    public static FailbackStrategy getFailbackStrategy(String msId) {
        Class<?> mapper = getMapper(msId);
        Method method = getMethod(msId, mapper);
        if (method == null) {
            LOGGER.error("解析method失败");
            return null;
        }
        FailbackStrategy failbackStrategy = method.getAnnotation(FailbackStrategy.class);
        if (failbackStrategy == null) {
            LOGGER.info("failbackStrategy from mapper:{}", mapper.getName());
            failbackStrategy = mapper.getAnnotation(FailbackStrategy.class);
            return failbackStrategy != null ? failbackStrategy : null;
        }
        LOGGER.info("failbackStrategy from method:{}", method.getName());
        return failbackStrategy;
    }


    /**
     * 解析原始SQL并生成新SQL
     *
     * @param supportedSQLHandler
     * @param sql
     * @return
     */
    public static String parseSQL(Map<Annotation, SqlHandler> supportedSQLHandler, String sql) throws Exception {
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        if (CollectionUtils.isEmpty(sqlStatements)) {
            throw new IllegalStateException(String.format("SQLStatement 解析失败,Raw SQL:%s", sql));
        }
        //不支持批量执行SQL
        SQLStatement sqlStatement = sqlStatements.get(0);
        SqlHandlerContext context = new SqlHandlerContext();
        context.setRawSql(sql);
        for (Map.Entry<Annotation, SqlHandler> sqlHandlerEntry : supportedSQLHandler.entrySet()) {
            context.setAnnotation(sqlHandlerEntry.getKey());
            ConditionExpr conditionExpr = sqlHandlerEntry.getValue().handle(context);
            MySqlConditionsVisitor visitor = new MySqlConditionsVisitor(conditionExpr);
            sqlStatement.accept(visitor);
        }
        return SQLUtils.toMySqlString(sqlStatement);
    }

    /**
     * 合并method和class注解（优先method）
     *
     * @param methodSupportedAnnotation
     * @param classSupportedAnnotation
     * @return
     */
    public static Set<Annotation> mergeSupportedAnnotation(Set<Annotation> methodSupportedAnnotation, Set<Annotation> classSupportedAnnotation) {
        Set<? extends Class<? extends Annotation>> methodAnnotationClasses = methodSupportedAnnotation.stream().map(Annotation::annotationType).collect(Collectors.toSet());
        Set<Annotation> classSupportedFiltered = classSupportedAnnotation.stream().filter(annotation -> !methodAnnotationClasses.contains(annotation.annotationType())).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(classSupportedFiltered)) {
            return methodSupportedAnnotation;
        }
        if (methodSupportedAnnotation.isEmpty()) {
            //EMPTY_SET UnsupportedOperationException
            methodSupportedAnnotation = Sets.newHashSet(classSupportedFiltered);
        } else {
            methodSupportedAnnotation.addAll(classSupportedFiltered);
        }
        return methodSupportedAnnotation;
    }

    /**
     * 获取支持的SQL条件handler
     *
     * @param msId
     * @return
     */
    public static Map<Annotation, SqlHandler> getSupportedSQLHandler(String msId) throws Exception {
        return SUPPORTED_SQL_HANDLER_CACHE.get(msId);
    }

    /**
     * 获取所有支持的注解
     *
     * @param msId mappedStatement id
     * @return
     */
    public static Set<Annotation> getSupportedAnnotation(String msId) {
        //获取方法上支持的注解
        Class<?> mapper = getMapper(msId);
        Method method = getMethod(msId, mapper);
        Set<Annotation> methodSupportedAnnotation = getSupportedAnnotation(method);
        //获取class上支持的注解
        Set<Annotation> classSupportedAnnotation = getSupportedAnnotation(mapper);
        //合并注解优先使用method上的注解
        return mergeSupportedAnnotation(methodSupportedAnnotation, classSupportedAnnotation);
    }

    /**
     * 获取支持的注解（注册在SQLHandlerRegistrar中的）
     *
     * @param t
     * @return
     */
    public static <T extends AnnotatedElement> Set<Annotation> getSupportedAnnotation(T t) {
        if (t != null && t.getAnnotations().length > 0) {
            Set<Class<? extends Annotation>> registeredAnnotations = SqlHandlerRegistrar.getAnnotations();
            HashSet<Annotation> annotations = Sets.newHashSet(t.getAnnotations());
            Iterator<Annotation> iterator = annotations.iterator();
            while (iterator.hasNext()) {
                if (!registeredAnnotations.contains(iterator.next().annotationType())) {
                    iterator.remove();
                }
            }
            return annotations;
        }
        return Collections.emptySet();
    }


    /**
     * 获取当前执行的Mapper
     *
     * @param msId mappedStatement id
     * @return
     */
    public static Class<?> getMapper(String msId) {
        try {
            String className = msId.substring(0, msId.lastIndexOf(SP_CHAR));
            return Class.forName(className);
        } catch (Exception e) {
            LOGGER.warn("解析Class失败:", e);
        }
        return null;
    }

    /**
     * 获取当前执行的Method
     *
     * @param msId mappedStatement id
     * @return
     */
    public static Method getMethod(String msId, Class<?> mapper) {
        try {
            if (mapper != null) {
                String methodName = msId.substring(msId.lastIndexOf(SP_CHAR) + 1);
                for (Method method : mapper.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        return method;
                    }
                }
                LOGGER.warn("未找到method:{},class:{}", methodName, mapper.getName());
            }
        } catch (Exception e) {
            LOGGER.warn("解析Method失败:", e);
        }
        return null;
    }

    /**
     * supportedHandlerLoader 用于加载ms对应的可用handler
     */
    private static class SupportedSqlHandlerLoader extends CacheLoader<String, Map<Annotation, SqlHandler>> {
        @Override
        public Map<Annotation, SqlHandler> load(String msId) {
            Set<Annotation> supportedAnnotation = getSupportedAnnotation(msId);
            if (CollectionUtils.isEmpty(supportedAnnotation)) {
                return Collections.emptyMap();
            }
            return supportedAnnotation.stream().collect(HashMap::new, (m, v) -> m.put(v, SqlHandlerRegistrar.getSQLHandler(v.annotationType())), HashMap::putAll);
        }
    }

}
