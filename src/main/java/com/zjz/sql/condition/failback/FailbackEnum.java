package com.zjz.sql.condition.failback;


/**
 * FailbackEnum
 *
 * @author zhaojingzhou
 * @date 2020/6/29 18:07
 */
public enum FailbackEnum {

    THROW_EXCEPTION("抛出异常"),
    EXEC_RAW_SQL("执行原始SQL");

    private String desc;

    FailbackEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
