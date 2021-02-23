package com.zjz.sql.condition.expr;

/**
 * Condition
 *
 * @author zhaojingzhou
 * @date 2020/6/23 11:54
 */
public class Condition extends ConditionExpr {

    private String name;
    private Object val;

    public Condition(String name, ConditionOperator operator, Object val) {
        this(name, operator, val, ConditionPosition.RIGHT);
    }

    public Condition(String name, ConditionOperator operator, Object val, ConditionPosition position) {
        this.name = name;
        this.val = val;
        this.operator = operator;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }

}
