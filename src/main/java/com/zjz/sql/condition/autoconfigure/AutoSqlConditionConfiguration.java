package com.zjz.sql.condition.autoconfigure;

import com.zjz.sql.condition.SqlConditionInterceptor;
import com.zjz.sql.condition.handler.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.List;

/**
 * ConditionAutoConfiguration
 *
 * @author zhaojingzhou
 * @date 2020/6/30 11:45
 */
@ConditionalOnClass({SqlSessionFactory.class})
@AutoConfigureAfter({SqlSessionFactory.class})
public class AutoSqlConditionConfiguration implements InitializingBean {
    @Autowired
    private List<SqlSessionFactory> sqlSessionFactories;

    @Override
    public void afterPropertiesSet() {
        sqlSessionFactories.forEach(sqlSessionFactory -> {
            if (sqlSessionFactory != null) {
                sqlSessionFactory.getConfiguration().addInterceptor(new SqlConditionInterceptor());
            }
        });
        //注册默认内置的处理器
        SqlHandlerRegistrar.register(Domain.class, new DomainSqlHandler());
        SqlHandlerRegistrar.register(Org.class, new OrgSqlHandler());
        SqlHandlerRegistrar.register(DataAuth.class, new DataAuthSqlHandler());
        SqlHandlerRegistrar.register(User.class, new UserSqlHandler());
    }
}
