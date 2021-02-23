package com.zjz.sql.condition.expr;

/**
 * ConditionPosition
 *
 * @author zhaojingzhou
 * @date 2020/6/23 13:36
 */
public enum ConditionPosition {
    LEFT(0,"left"),
    RIGHT(1, "right");
    private int position;
    private String desc;

    ConditionPosition(int position,String desc) {
        this.position = position;
        this.desc = desc;
    }

    public int getPosition() {
        return position;
    }

    public String getDesc() {
        return desc;
    }
}
