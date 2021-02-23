package com.zjz.sql.condition;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.zjz.sql.condition.expr.Condition;
import com.zjz.sql.condition.expr.ConditionOperator;
import com.zjz.sql.condition.expr.Conditions;

import java.util.List;

/**
 * ParseTest
 *
 * @author zhaojingzhou
 * @date 2020/7/2 14:24
 */
public class ParseTest {

    public static void main(String[] args) {
        String sql = "select count(1) from ((select name from t1,t2 where t1.id =t2.out_id) union (select test from t1,t2 where t1.id = t2.out_id)) t";
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        SQLStatement sqlStatement = sqlStatements.get(0);
        sqlStatement.accept(new MySqlConditionsVisitor(new Conditions(new Condition("t1.owner_id", ConditionOperator.AND, 123), ConditionOperator.OR, new Condition("t1.owner_id", ConditionOperator.IS, null))));
        System.out.println(SQLUtils.toSQLString(sqlStatement));
    }
}
