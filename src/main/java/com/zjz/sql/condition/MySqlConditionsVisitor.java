package com.zjz.sql.condition;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.zjz.sql.condition.expr.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Set;

import static com.zjz.sql.condition.Constants.SPLIT_CHAR;
import static com.zjz.sql.condition.Constants.SP_CHAR;

/**
 * MySqlConditionsVisitor
 *
 * @author zhaojingzhou
 * @date 2020/6/23 11:07
 */

public class MySqlConditionsVisitor extends MySqlASTVisitorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlConditionsVisitor.class);
    private static final EnumMap<ConditionOperator, SQLBinaryOperator> SQL_BINARY_OP_MAPPING = new EnumMap<>(ConditionOperator.class);

    static {
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.EQ, SQLBinaryOperator.Equality);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.NE, SQLBinaryOperator.NotEqual);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.GT, SQLBinaryOperator.GreaterThan);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.GE, SQLBinaryOperator.GreaterThanOrEqual);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.LT, SQLBinaryOperator.LessThan);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.LE, SQLBinaryOperator.LessThanOrEqual);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.IS, SQLBinaryOperator.Is);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.IS_NOT, SQLBinaryOperator.IsNot);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.LIKE, SQLBinaryOperator.Like);
        SQL_BINARY_OP_MAPPING.put(ConditionOperator.NOT_LIKE, SQLBinaryOperator.NotLike);
    }

    private final ConditionExpr conditionExpr;

    public MySqlConditionsVisitor(ConditionExpr conditionExpr) {
        this.conditionExpr = conditionExpr;
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock x) {
        SQLExpr newWhere = visitCondition(x.getFrom(), x.getWhere());
        if (newWhere != null) {
            x.setWhere(newWhere);
        }
        return super.visit(x);
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock x) {
        commonEndVisit(x);
        super.endVisit(x);
    }

    @Override
    public boolean visit(MySqlUpdateStatement x) {
        SQLExpr newWhere = visitCondition(x.getTableSource(), x.getWhere());
        if (newWhere != null) {
            x.setWhere(newWhere);
        }
        return super.visit(x);
    }

    @Override
    public void endVisit(MySqlUpdateStatement x) {
        commonEndVisit(x);
        super.endVisit(x);
    }

    @Override
    public boolean visit(MySqlDeleteStatement x) {
        SQLExpr newWhere = visitCondition(x.getTableSource(), x.getWhere());
        if (newWhere != null) {
            x.setWhere(newWhere);
        }
        return super.visit(x);
    }

    @Override
    public void endVisit(MySqlDeleteStatement x) {
        commonEndVisit(x);
        super.endVisit(x);
    }

    /**
     * commonEndVisit
     *
     * @param x
     */
    private void commonEndVisit(SQLObjectImpl x) {
        String finalSQL = SQLUtils.toSQLString(x, JdbcConstants.MYSQL);
        LOGGER.debug("SQL处理结果：{}", finalSQL);
    }

    /**
     * 条件访问入口
     *
     * @param sqlTableSource
     * @param where
     * @return
     */
    public SQLExpr visitCondition(SQLTableSource sqlTableSource, SQLExpr where) {
        if (conditionExpr == null) {
            LOGGER.info("conditionExpr is null");
            return null;
        }
        Set<Table> tables = getTables(sqlTableSource);
        SQLExpr sqlExpr = where;
        for (Table table : tables) {
            sqlExpr = this.handleCondition(sqlExpr, table);
        }
        return sqlExpr;
    }

    /**
     * 获取SQL中所有表及别名
     *
     * @param sqlTableSource
     * @return
     */
    public Set<Table> getTables(SQLTableSource sqlTableSource) {
        Set<Table> tables = Sets.newHashSet();
        if (sqlTableSource instanceof SQLExprTableSource) {
            tables.add(new Table(((SQLIdentifierExpr) ((SQLExprTableSource) sqlTableSource).getExpr()).getName(), sqlTableSource.getAlias()));
        } else if ((sqlTableSource instanceof SQLJoinTableSource)) {
            SQLJoinTableSource joinTable = (SQLJoinTableSource) sqlTableSource;
            tables.addAll(getTables(joinTable.getLeft()));
            tables.addAll(getTables(joinTable.getRight()));
        }
        return tables;
    }

    /**
     * 处理新增条件
     *
     * @param where
     * @param table
     * @return
     */
    public SQLExpr handleCondition(SQLExpr where, Table table) {
        SQLExpr sqlExpr = conditionExpr2SqlBinaryOpExpr(this.conditionExpr, table);
        if (where == null) {
            return sqlExpr;
        }
        if (sqlExpr == null) {
            return where;
        }
        SQLBinaryOpExpr newWhere = new SQLBinaryOpExpr();
        newWhere.setOperator(SQLBinaryOperator.BooleanAnd);
        if (ConditionPosition.LEFT.equals(this.conditionExpr.getPosition())) {
            newWhere.setLeft(sqlExpr);
            newWhere.setRight(where);
        } else {
            newWhere.setLeft(where);
            newWhere.setRight(sqlExpr);
        }
        return newWhere;
    }

    /**
     * 自定义的ConditionExpr转druid SQLExpr
     *
     * @param conditionExpr
     * @param table
     * @return
     */
    public SQLExpr conditionExpr2SqlBinaryOpExpr(ConditionExpr conditionExpr, Table table) {
        Set<String> includeTables = conditionExpr.getIncludeTables();
        //只添加条件到指定表上，不指定时默认所有表添加条件
        if (!CollectionUtils.isEmpty(includeTables) && !includeTables.contains(table.getName())) {
            return null;
        }
        //单个条件
        if (conditionExpr instanceof Condition) {
            Condition condition = (Condition) conditionExpr;
            //解析列表
            SQLExpr column = parseColumn(condition.getName(), table.getAlias());
            if (column == null) {
                return null;
            }
            return conditionExpr2SqlBinaryOpExpr(condition, column);
            //组合条件
        } else if (conditionExpr instanceof Conditions) {
            SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr();
            Conditions conditions = (Conditions) conditionExpr;
            SQLExpr sqlExpr = conditionExpr2SqlBinaryOpExpr(conditions.getLeft(), table);
            if (sqlExpr == null) {
                return null;
            }
            sqlBinaryOpExpr.setLeft(sqlExpr);
            if (ConditionOperator.AND.equals(conditions.getOperator())) {
                sqlBinaryOpExpr.setOperator(SQLBinaryOperator.BooleanAnd);
            } else if (ConditionOperator.OR.equals(conditions.getOperator())) {
                sqlBinaryOpExpr.setOperator(SQLBinaryOperator.BooleanOr);
            } else {
                throw new IllegalArgumentException("Combination conditions must specify Operator");
            }
            sqlExpr = conditionExpr2SqlBinaryOpExpr(conditions.getRight(), table);
            if (sqlExpr == null) {
                return null;
            }
            sqlBinaryOpExpr.setRight(sqlExpr);
            return sqlBinaryOpExpr;
        }
        LOGGER.warn("未正确转换表达式，ConditionExpr:{}", JSON.toJSON(conditionExpr));
        return null;
    }

    /**
     * 条件表达式转换
     *
     * @param column
     * @param condition
     * @return
     */
    public SQLExpr conditionExpr2SqlBinaryOpExpr(Condition condition, SQLExpr column) {
        SQLBinaryOperator sqlBinaryOperator = SQL_BINARY_OP_MAPPING.get(condition.getOperator());
        //通用key * value的操作符
        if (sqlBinaryOperator != null) {
            SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr();
            sqlBinaryOpExpr.setLeft(column);
            sqlBinaryOpExpr.setOperator(sqlBinaryOperator);
            sqlBinaryOpExpr.setRight(val2SQLExpr(condition.getVal()));
            return sqlBinaryOpExpr;
        }
        //特殊操作符
        if (ConditionOperator.IN.equals(condition.getOperator())) {
            //in操作value必须是集合
            if (!(condition.getVal() instanceof Collection)) {
                throw new IllegalArgumentException("If the operator is IN, value must be a collection type");
            }
            SQLInListExpr sqlInListExpr = new SQLInListExpr();
            sqlInListExpr.setExpr(column);
            SQLListExpr sqlListExpr = (SQLListExpr) val2SQLExpr(condition.getVal());
            sqlInListExpr.setTargetList(sqlListExpr.getItems());
            return sqlInListExpr;
        }
        return null;
    }

    /**
     * 解析字段名
     *
     * @param column
     * @param tableAlias
     * @return
     */
    public SQLExpr parseColumn(String column, String tableAlias) {
        //表达式中指定了别名
        if (column.indexOf(SP_CHAR) != -1) {
            String[] columnAndAlias = column.split(SPLIT_CHAR);
            String alias = columnAndAlias[0];
            if (!alias.equals(tableAlias)) {
                return null;
            }
            return new SQLPropertyExpr(new SQLIdentifierExpr(alias), columnAndAlias[1]);
        }
        //未指定别名使用从from中解析来的别名
        return StringUtils.isEmpty(tableAlias) ? new SQLIdentifierExpr(column) : new SQLPropertyExpr(new SQLIdentifierExpr(tableAlias), column);
    }

    /**
     * 值转druid SQLExpr
     *
     * @param val
     * @return
     */
    public SQLExpr val2SQLExpr(Object val) {
        if (val == null) {
            return new SQLNullExpr();
        }
        if (val instanceof Number) {
            return new SQLNumberExpr((Number) val);
        }
        if (val instanceof String) {
            return new SQLCharExpr((String) val);
        }
        if (val instanceof Collection) {
            SQLListExpr sqlListExpr = new SQLListExpr();
            Collection<?> collection = (Collection) val;
            Iterator<?> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                sqlListExpr.getItems().add(val2SQLExpr(next));
            }
            return sqlListExpr;
        }
        LOGGER.warn("不支持的值类型，默认使用String");
        return new SQLCharExpr(String.valueOf(val));
    }

    /**
     * 数据库表
     */
    private static class Table {
        private String name;
        private String alias;

        public Table(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }
    }

}
