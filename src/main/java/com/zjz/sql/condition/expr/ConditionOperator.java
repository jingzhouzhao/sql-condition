package com.zjz.sql.condition.expr;

/**
 * ConditionOperator
 *
 * @author zhaojingzhou
 * @date 2020/6/23 11:56
 */
public enum ConditionOperator {
    EQ("="),
    NE("!="),
    IN("in"),
    IS("is"),
    IS_NOT("is not"),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    LIKE("like"),
    NOT_LIKE("not like"),
    AND("and"),
    OR("or");

    private String desc;

    ConditionOperator(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
