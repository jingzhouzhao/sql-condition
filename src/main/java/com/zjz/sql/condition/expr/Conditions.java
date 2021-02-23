package com.zjz.sql.condition.expr;

/**
 * Conditions SQL条件组合定义
 * 示例:
 * //where (id = 1 or name = aa) and age in(1,2,3)
 *          //第一个条件
 *         Condition condition1 = new Condition("id", ConditionOperator.EQ, 1);
 *         //第二个条件
 *         Condition condition2 = new Condition("name", ConditionOperator.EQ, "aa");
 *         //两个条件组合
 *         Conditions twoConditions = new Conditions(condition1, ConditionOperator.OR, condition2);
 *         //组合后的条件再次与一个条件组合
 *         Conditions finalCondition = new Conditions(twoConditions, ConditionOperator.AND, new Condition("age", ConditionOperator.IN, Lists.newArrayList(1, 2, 3)));
 *         System.out.println(JSON.toJSONString(finalCondition));
 *         //最终输出的结果：
 * {
 *     "conditionOperator":"AND",
 *     "left":{
 *         "conditionOperator":"OR",
 *         "left":{
 *             "name":"id",
 *             "operator":"EQ",
 *             "val":1
 *         },
 *         "right":{
 *             "name":"name",
 *             "operator":"EQ",
 *             "val":"aa"
 *         }
 *     },
 *     "right":{
 *         "name":"age",
 *         "operator":"IN",
 *         "val":[
 *             1,
 *             2,
 *             3]
 *     }
 * }
 *
 * @author zhaojingzhou
 * @date 2020/6/23 13:18
 */
public class Conditions extends ConditionExpr {

    private ConditionExpr left;
    private ConditionExpr right;

    public Conditions(ConditionExpr left, ConditionOperator operator, ConditionExpr right) {
        this(left, operator, right, ConditionPosition.RIGHT);
    }

    public Conditions(ConditionExpr left, ConditionOperator operator, ConditionExpr right,ConditionPosition position) {
        this.left = left;
        this.right = right;
        this.operator = operator;
        this.position = position;
    }


    public ConditionExpr getLeft() {
        return left;
    }

    public void setLeft(ConditionExpr left) {
        this.left = left;
    }

    public ConditionExpr getRight() {
        return right;
    }

    public void setRight(ConditionExpr right) {
        this.right = right;
    }


}
