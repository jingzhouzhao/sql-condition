## 全局参数

## 自动条件补全

### 使用步骤

1. 升级stone版本至1.5.0-SNAPSHOT
2. 程序入口类添加@EnableAutoSqlCondition
3. 目前内置条件注解:
   1. @Domain，将给SQL添加上domain_id=xxx条件，可定义domain_id别名
   2. @Org， 将给SQL添加上org_id=xxx条件，可定义org_id别名
   3. @DataAuth，将给SQL添加上(domain_id=xxx and org_id=xxx and data_org_id in(x,x,x))条件，字段名均可定义别名
4. 注解可加在mapper上以及mapper内的method上，相同注解优先级为 method 大于 mapper。

### 取值说明

目前存在两个Context：SqlConditionContext、AuthContext。

将优先取SQLConditionContext中当前条件字段同名（如有别名使用别名）的值，例如@DataAuth：

```java
Object dataOrgIds = SqlConditionContext.getValue(dataAuth.dataOrgIdAlias())
```

如果未获取到值将从全局环境变量AuthContext中取值：

```java
AuthContext.getAuthPropertiesBean().getFilterDataOrgIdList()
```

### 自定义条件处理器
1. 定义一个注解
   ```java
   @Target({ElementType.METHOD, ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   public @interface Customer {

     String columnNameAlias() default "column_test";

   }
   ```
   
2. 定义一个注解处理器
    ```java
      public class CustomerSqlHandler extends AbstractSqlHandler {
          @Override
          public ConditionExpr doHandle(SqlHandlerContext ctx) {
              Customer customer = (Customer) ctx.getAnnotation();
              //SqlConditionContext
              //AuthContext
              Condition columnTest = new Condition(customer.columnNameAlias(), ConditionOperator.EQ,"从上下文中取值");
            //组合条件可参考DataAuthSqlHandler
              return columnTest;
          }
      }
    ```

3. 注册处理器
  
    ``` java
    //可在程序任意初始化过程中调用下面代码
      SqlHandlerRegistrar.register(Customer.class, new CustomerSqlHandler());
    ```
