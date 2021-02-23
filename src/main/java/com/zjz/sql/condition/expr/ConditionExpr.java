package com.zjz.sql.condition.expr;

import java.util.Set;

/**
 * ConditionExpr
 *
 * @author zhaojingzhou
 * @date 2020/6/23 13:20
 */
public class ConditionExpr {

    /**
     * position
     */
    protected ConditionPosition position;
    /**
     * operator
     */
    protected ConditionOperator operator;
    /**
     * includeTables
     */
    protected Set<String> includeTables;

    public ConditionPosition getPosition() {
        return position;
    }

    public void setPosition(ConditionPosition position) {
        this.position = position;
    }

    public ConditionOperator getOperator() {
        return operator;
    }

    public void setOperator(ConditionOperator operator) {
        this.operator = operator;
    }

    public Set<String> getIncludeTables() {
        return includeTables;
    }

    public void setIncludeTables(Set<String> includeTables) {
        this.includeTables = includeTables;
    }
}
