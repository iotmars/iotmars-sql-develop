# How to use

### 提供的接口

请您打开源码，找到com.marssenger.common.sql.controller.StandardController。

您可以看出本项目提供了3个接口

```java
/common/standard/sql/list            需要有事先写好的sql才能执行的接口                    
```

```java
/common/standard/sql/selectOneTable  单表查询接口,无需事先写好sql   
```

```java
/common/standard/sql/commonSelect    多表join查询接口,无需事先写好sql 
```

### 1. 手写sql然后查询   

#### 1.1请求路径

```
/common/standard/sql/list 
```

#### 1.2请求说明

GET请求。

根据sqlCode或者seriesCode来查询到cc_base_sql的一个或者多个sql，然后按照顺序执行它，返回具体的结果。

核心是收集从url中获取的参数后，调用如下方法：

```java
com.marssenger.common.sql.utils.ControllerUtil#getResult(StandardQueryObject standardQueryObject) throws NoSqlExistsException, BeanNotFoundException, ClassNotFoundException
```

StandardQueryObject封装了如下属性。

| 字段名        | 字段意义                                                     | 是否可空 |
| :------------ | ------------------------------------------------------------ | -------- |
| sqlCode       | cc_base_sql表主键sql_code，用来唯一对应一个sql               | Y        |
| seriesCode    | cc_base_sql_series表的series_code字段，对应cc_base_sql的多个sql_code | Y        |
| params        | params自动封装了从url中所有的参数，mybaits框架会使用这些参数 | Y        |
| handler       | SqlExecuteHandler的实现类的名字，是spring中该实现类的bean的名字。用来自定义sql语句查询前后怎么处理环境变量和查询结果 | Y        |
| handlerParams | handler类会使用的参数，多个参数请用字符串隔开                | Y        |
| resultType    | 返回结果的格式，有如下四个数值rawlist、multilist、multiSumInfoAndOneList、multiListAndOneList，其中rawlist是原始结果，multilist返回的是多个列表，列表位置相同，multiSumInfoAndOneList是一个列表外加上多个统计值，multiListAndOneList是一个列表再加上不同的列表，位置不同. | Y        |
| route         | 路由，也可以称之为一组方法的组合，参阅1.3.2 route(路由)的使用 |          |

StandardQueryObject的其余属性均为框架内部使用属性。

###### 示例1:

在数据库里执行如下sql

```sql
INSERT INTO `compass_dev`.`cc_base_sql` (`sql_code`, `sql_type`, `sql`, `total_sql`, `sql_result_name`, `total_sql_result_name`, `jexl`, `map`, `order_no`, `remark`) VALUES ('exampleSql', 'select', 'select  count(1) con from cc_base_sql', NULL, NULL, NULL, NULL, NULL, NULL, '用来测试的sql');
```

这条sql向数据库里插入了一条数据，主键sql_code为exampleSql，对应的sql语句为: select  count(1) con from cc_base_sql。

运行项目，在浏览器里输入如下地址：

http://localhost:8080/common/standard/sql/list?sqlCode=exampleSql

会得到如下的返回结果:

{
    "records": [
        {
            "con": 45
        }
    ]
}

这里面con的具体数值取决于cc_base_sql里到底有多少数据。

上面的请求根据sqlCode=exampleSql的条件，在cc_base_sql里查到了一条sql，sql具体的数值为：select  count(1) con from cc_base_sql，然后调用mybaits框架，执行该sql。

###### 示例2:

```
INSERT INTO `compass_dev`.`cc_base_sql` (`sql_code`, `sql_type`, `sql`, `total_sql`, `sql_result_name`, `total_sql_result_name`, `jexl`, `map`, `order_no`, `remark`) VALUES ('exampleSql2', 'select', 'select  * from cc_base_sql where sql_code=#{pk}', NULL, NULL, NULL, NULL, NULL, NULL, '用来测试的sql2');
```

这条sql向数据库里插入了一条数据，主键sql_code为exampleSql，对应的sql语句为: select  count(1) con from cc_base_sql。

运行项目，在浏览器里输入如下地址：

http://localhost:8080/common/standard/sql/list?sqlCode=exampleSql2&pk=exampleSql2

会得到如下的返回结果:

{
    "records": [
        {
            "sqlType": "select",
            "sqlCode": "exampleSql2",
            "remark": "用来测试的sql2",
            "sql": "select  * from cc_base_sql where sql_code=#{pk}"
        }
    ]
}

在这里，将一行记录返回了过来，你可以猜出#{pk}这种写法是mybaits的独有写法，因此您可以在sql中任意使用标签，如<if/>,<foreach/>等。pk的数值是从params这个Map<String,Object>中获取的，这是mybatis运行的上下文，这个参数会总动收集url中的所有参数。

当您需要执行一批查询而不是一个查询时，您可以使用seriesCode代替sqlCode。在  cc_base_sql_series表的series_code字段，对应cc_base_sql的多个sql_code，也就是对应多条sql。

在您的数据库里执行如下sql:

```sql
INSERT INTO `cc_base_sql` (`sql_code`, `sql_type`, `sql`, `total_sql`, `sql_result_name`, `total_sql_result_name`, `jexl`, `map`, `order_no`, `remark`) VALUES ('exampleCodeA', 'select', 'select 1 result1', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `compass_dev`.`cc_base_sql` (`sql_code`, `sql_type`, `sql`, `total_sql`, `sql_result_name`, `total_sql_result_name`, `jexl`, `map`, `order_no`, `remark`) VALUES ('exampleCodeB', 'select', 'select 2 result2', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `cc_base_sql_series` (`id`, `series_code`, `sql_code`, `identifier`, `order_no`) VALUES (null, 'testSeries', 'exampleCodeA', 'exampleCodeA', 1);
INSERT INTO `cc_base_sql_series` (`id`, `series_code`, `sql_code`, `identifier`, `order_no`) VALUES (null, 'testSeries', 'exampleCodeB', 'exampleCodeB', 2);
```

上述sql先在cc_base_sql表里插入两条sql记录，主键为exampleCodeA、exampleCodeB，对应的两条sql分别为select 1 result1 和 select 2 result2

然后在cc_base_sql_series也插入了两条记录，指定exampleCodeA、exampleCodeB这两条sql对应的series_code为testSeries。

运行项目，在浏览器输入：

http://localhost:8080/common/standard/sql/list?seriesCode=testSeries&resultType=multiListAndOneList

返回结果：

{
    "records": [
        {
            "result1": 1
        }
    ],
    "result2": 2
}

您看出来了吧，result1和result2都返回了，两条sql都执行了。





#### 1.3高级模块

##### 1.3.1 handler的编写。

handler指的是com.marssenger.common.sql.handler.SqlExecuteHandler这个类的实现类，您可以通过实现这个类来定制您的模板，里面有这么几个方法。



```
/**
 * 设置环境的方法
 *
 * @param env
 */
void setEnv(Map<String, Object> env);

/**
 * 获取环境的方法
 *
 * @return
 */
Map<String, Object> getEnv();


/**
 * 获取handler的参数
 *
 * @return
 */
String getHandlerParams();

/**
 * 设置handler的参数
 *
 * @param handlerParams
 */
void setHandlerParams(String handlerParams);

/**
 * 初始情况下设置环境变量
 */
void initEnv();


/**
 * 初始化sql列表
 * @param list
 */
List<SqlObject> initSqlList(List<SqlObject> list);


/**
 * 返回缓存的结果
 *
 * @return 返回的结果
 */
Object getCacheResult();

/**
 * 每次执行sql前必然执行的，设置环境变量
 *
 * @param sqlObject sql参数
 */
void setEnvBeforeExecuteEachSql(SqlObject sqlObject);

/**
 * 每次执行sql后必然执行的
 *
 * @param sqlObject sql参数
 * @param result    本次执行的结果
 */
public void setEnvAfterExecuteEachSql(SqlObject sqlObject, Result result);

/**
 * 对最终执行的sql进行处理
 *
 * @param standardQueryObject 初始的查询参数
 * @param sqlObjects          sql列表
 * @param resultMap           对象列表
 * @return 最终的返回结果
 */
public Object setFinalResult(StandardQueryObject standardQueryObject, List<SqlObject> sqlObjects, Map<String, Result> resultMap);
```



对应于sql 的执行周期

| 序号 | 生命周期                   | 注释                                                         |
| ---- | -------------------------- | ------------------------------------------------------------ |
| 1    | setEnv                     | 初始化mybatis的运行环境Map<String,Object>                    |
| 2    | setHandlerParams           | 将页面传来的handlerParams字符串设置到该类中                  |
| 3    | initEnv                    | 再次设置您的运行环境，这里您可以调用getEnv()获取当前环境，然后再度加入您的参数。 |
| 4    | initSqlList                | 对sql列表进行设置，比如您可list.add一个新的SqlObject         |
| 5    | getCacheResult             | 如果您需要读取缓存，可以使用该方法，该方法返回的结果如果非空，便是最终结果。 |
| 6    | setEnvBeforeExecuteEachSql | List<SqlObject> list中的sql每调用一次前，便会调用该方法，该方法可以对mybatis当次执行所使用的参数进行调整，比如您可以调用getEnv()获取当前的执行环境，这是一个Map<String,Object>，你可以随意更改里面的key和value |
| 7    | setEnvAfterExecuteEachSql  | List<SqlObject> list中的sql每调用一次后，便会调用该方法，该方法可以对该sql的执行结果进行处理。 |
| 8    | setFinalResult             | 设置最终结果，resultMap有list中所有sql语句的执行结果，您可以汇总这些结果得到新的一个结果 |

本系统默认的handler是com.marssenger.common.sql.executor.StandardSqlExecutor，当您传入您的handler时，本框架会使用您的handler。

一个简单的handler示例如下:

```java
package com.marsenger.demo;


import com.marssenger.common.sql.handler.StandardSqlExecuteHandler;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Component("testHandler")
public class TestHandler extends StandardSqlExecuteHandler {

    /**
     * 初始化环境，当您的handlerParams包含setDateRange这个参数时，为您设置当前月份的开始时间和当前月份的结束时间，您可以在使用mybatis的#{startDay}、#{endDay}符号调用
     */
    @Override
    public void initEnv() {
        Map<String, Object> env = getEnv();
        String handlerParams = getHandlerParams();
        Set<String> handlerParamsSet = getHandlerParamsSet(handlerParams);
        if (handlerParamsSet.contains("setDateRange")) {
            Calendar calendar = Calendar.getInstance();
            //获取当月第一天
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String yearMonth = sdf.format(new Date());
            env.put("startDay", yearMonth + "-" + "01");
            //获取当月最后一天
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            env.put("endDay", yearMonth + "-" + getDays(year, month));
        }
    }

    /**
     * 获取当前月份有多少天，可能28、29、30、31天
     *
     * @param year
     * @param month
     * @return
     */
    public static int getDays(int year, int month) {
        int days = 0;
        if (month != 2) {
            switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    days = 31;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    days = 30;

            }
        } else {
            // 闰年
            if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                days = 29;
            } else {
                days = 28;
            }
        }
        return days;
    }


}
```

上面的handler继承了StandardSqlExecuteHandler，重新了initEnv()方法，在执行sql查询前，为您设置了两个参数startDay和endDay，分别是当月的第一天和最后一天，您可以在您的sql里使用#{startDay}、#{endDay}引用这些变量。

在数据库里执行如下sql：

```sql
INSERT INTO `compass_dev`.`cc_base_sql` (`sql_code`, `sql_type`, `sql`, `total_sql`, `sql_result_name`, `total_sql_result_name`, `jexl`, `map`, `order_no`, `remark`) VALUES ('exampleSql3', 'select', 'select #{startDay} startDay ,#{endDay} endDay', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
```

这条sql向数据库里插入了一条数据，主键sql_code为exampleSql3，对应的sql语句为: select #{startDay} startDay ,#{endDay} endDay。您可以看出，这里#{startDay}、#{endDay}是mybatis的写法。

运行项目，在浏览器输入:

http://localhost:8080/common/standard/sql/list?sqlCode=exampleSql3&handler=testHandler&handlerParams=setDateRange

这里指定的handler=testHandler，对应的是代码的@Component("testHandler")，这个注解说明您的handler是一个spring的bean，名字叫做testHandler。框架汇总spring的bean工厂中得到这个bean，然后使用您的bean完成对数据库的查询。

返回结果为:

{
    "records": [
        {
            "startDay": "2022-11-01",
            "endDay": "2022-11-30"
        }
    ]
}

##### 1.3.2 route(路由)的使用

当您定制模板时，您可能追求代码的简洁，不想让不同需求代码分割开（至少要分割成不同的方法），您可以继承com.marssenger.common.sql.handler.StandardSqlExecuteDispatcherHandler类，然后传递一个route参数，来指定您需要访问的方法。

举个例子，您的项目有如下的handler

```java
package com.marsenger.demo;


import com.marssenger.common.sql.annotation.Route;
import com.marssenger.common.sql.handler.StandardSqlExecuteDispatcherHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Slf4j
@Route(route = "r1", source = "getCacheResult", target = "r1GetCacheResult")
@Route(route = "r1", source = "initEnv", target = "r1InitEnv")
@Route(route = "r2", source = "getCacheResult", target = "r2GetCacheResult")
@Route(route = "r2", source = "initEnv", target = "r2InitEnv")
@Component("myDispatcherHandler")
public class MyDispatcherHandler extends StandardSqlExecuteDispatcherHandler {
    public Object r1GetCacheResult() {
        log.info("enter myGetCacheResult1");
        return null;
    }

    public void r1InitEnv() {
        log.info("enter myInitEnv1");
        getEnv().put("pageSize", "1");
    }

    public Object r2GetCacheResult() {
        log.info("enter myGetCacheResult2");
        return null;
    }

    public void r2InitEnv() {
        log.info("enter myInitEnv2");
        getEnv().put("pageSize", "2");
    }

}

```

这个handler使用@Route指定了两条路由，r1和r2。路由r1将标准生命周期的getCacheResult方法映射为r1GetCacheResult方法，将标准生命周期的initEnv方法映射为r1InitEnv方法。路由r2将标准生命周期的getCacheResult方法映射为r2GetCacheResult方法，将标准生命周期的initEnv方法映射为r2InitEnv方法。也就是您的参数route=r1时，会调用r1GetCacheResult、r1InitEnv方法。您的参数route=r2时，框架会调用r2GetCacheResult、r2InitEnv方法。当您不传route参数时，会调用默认的getCacheResult、initEnv方法。

r1InitEnv将pageSize设置为1，r2InitEnv将pageSize设置为2

这种路由的方式，实现了不同需求在方法层面上的隔离。您可以使用一个模板实现不同的需求，达到了复用的目的。

在您的数据库执行如下sql:

```sql
INSERT INTO `compass_dev`.`cc_base_sql` (`sql_code`, `sql_type`, `sql`, `total_sql`, `sql_result_name`, `total_sql_result_name`, `jexl`, `map`, `order_no`, `remark`) VALUES ('exampleSql4', 'select', 'select  ${pageSize} pageSize', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

```

这条sql向数据库里插入了一条数据，主键sql_code为exampleSql4，对应的sql语句为: select  ${pageSize} pageSize。

运行项目（sql-devlop-demo或者您自己的项目，您自己的项目的话，要复制这个类，修改包名）。

在浏览器里输入：

http://localhost:8080/common/standard/sql/list?sqlCode=exampleSql4&route=r1&handler=myDispatcherHandler

在这里，指定了route=r1，使用的bean的名字是myDispatcherHandler

返回结果为：

{
    "records": [
        {
            "pageSize": 1
        }
    ]
}

控制台打印：

2022-11-11 16:14:23.196  INFO 1328 --- [nio-8080-exec-1] com.marsenger.demo.MyDispatcherHandler   : enter myInitEnv1
2022-11-11 16:14:23.196  INFO 1328 --- [nio-8080-exec-1] com.marsenger.demo.MyDispatcherHandler   : enter myGetCacheResult1

在浏览器里输入：

http://localhost:8080/common/standard/sql/list?sqlCode=exampleSql4&route=r2&handler=myDispatcherHandler

在这里，指定了route=r2，使用的bean的名字是myDispatcherHandler

返回结果为：

{
    "records": [
        {
            "pageSize": 2
        }
    ]
}

控制台打印：

2022-11-11 16:16:43.540  INFO 1328 --- [nio-8080-exec-6] com.marsenger.demo.MyDispatcherHandler   : enter myInitEnv2
2022-11-11 16:16:43.540  INFO 1328 --- [nio-8080-exec-6] com.marsenger.demo.MyDispatcherHandler   : enter myGetCacheResult2

如上，您可以看出，当您在url中指定不同的route时，会调用不同的方法，从而实现了需求在方法层面上的隔离，修改一个需求的代码时，不会影响到另外一个。



### 2.自动生成sql 的单表查询

#### 2.1 请求路径 

```
/common/standard/sql/selectOneTable
```

#### 2.2 请求说明

GET请求

sql中**select**语句的结构为：

```sql
SELECT select_list
FROM table_name
[ WHERE search_condition]
[ GROUP BY group_by_expression]
[ HAVING search_condition]
[ ORDER BY order_expression [ ASC|DESC ] ]
```

基于这个结构，框架定义了一项基于url的数据库查询接口

```
/common/standard/sql/selectOneTable?select=...&from=...&where=...&groupBy=...&having=...&orderBy=...&fieldName1=fieldValue1&fieldName2=fieldValue2
```

其中sql语句的关键字select、from、where、group by、having、order by 都以驼峰的格式嵌入到了url中，您指定了这些，框架会自动为您拼接sql，然后执行返回给您结果。

各个关键字的说明:

| 关键字    | 解释                                                         | 示例                                                      | 是否可空 |
| --------- | ------------------------------------------------------------ | --------------------------------------------------------- | -------- |
| select    | 对应sql语句里的select,这里要指定一个字段列表，要查哪些字段，多个字段用英文逗号分隔。可以省略，如果省略代表查询全部字段。 | select=sql_code,sql_type,`sql`                            | Y        |
| from      | 对应sql语句里的from,这里要指定一个表。                       | from=cc_base_sql                                          | N        |
| where     | 对应sql语句里的where,判断条件，也就是sql里面鱼的条件，可以有多个where条件，分别是where1,where2,where3...,这些条件都要以where开头。 | where0=sql_code,eq,exampleSql4                            | Y        |
| groupBy   | 对应sql语句里的group by,配合聚合函数(function)使用，多个字段用英文逗号分隔。 | groupBy=sql_type                                          | Y        |
| having    | 对应sql语句里的having,这里指定的是过滤条件，如where一样，可以有多个having条件，如having1,having2,having3...这些条件都要以having开头。 | having1=sql_type,eq,select                                | Y        |
| orderBy   | 对应sql语句里的order by,指定查询结果的排序。可以有多个orderBy条件，如orderBy1,orderBy2,orderBy3...这些条件都要以orderBy开头。 | orderBy1=sql_code,desc                                    | Y        |
| fieldName | 和其他的关键字不同，fieldName是您要查询的表的具体的字段，fieldValue是该字段对应的值，描述了一种特殊的where条件，也就是等值条件。 | sql_code=exampleSql4;等价于where0=sql_code,eq,exampleSql4 | Y        |

上表中的eq是=的意思。

一个简单查询示例

http://localhost:8080/common/standard/sql/selectOneTable?from=cc_base_sql

这里省略了其他所有的东西，仅仅给您来了一个全表查询，用from字段指定了要查询cc_base_sql表。系统为你自动生成的sql

是**SELECT * FROM cc_base_sql**



如果您仅仅要查询sql_code,sql_type两个字段，可以这样写：

http://localhost:8080/common/standard/sql/selectOneTable?select=sql_code,sql_type&from=cc_base_sql

系统为您生成的sql是**SELECT sql_code, sql_type FROM cc_base_sql**



如果您要指定查询sql_code为exampleSql4的数据，您有两种写法

http://localhost:8080/common/standard/sql/selectOneTable?from=cc_base_sql&sql_code=exampleSql4

或

http://localhost:8080/common/standard/sql/selectOneTable?from=cc_base_sql&where0=sql_code,eq,exampleSql4

系统为你自动生成的sql是:

**SELECT * FROM cc_base_sql WHERE cc_base_sql.sql_code = 'exampleSql4'**

第一种方法是fieldName=fieldValue的格式，因为您已经通过from条件把表名指定了，框架读取表的元数据，是可以识别出这个sql_code=exampleSql4的条件的。第二种方法，是解析出来的where0=sql_code,eq,exampleSql4被解析成sql_code=exampleSql4，如果您熟悉shell脚本，应该知道eq(=),gt(>),lt(<),le(<=),ge(>=)的意思。

#### 2.3 where和having语句的语法

where和having都是做过滤的，语法是一致的，在url中的语法为

```
where[0-9]*=fieldName,operator,params

having[0-9]*=fieldName,operator,params
```

其中fieldName是表中的字段名，比如cc_base_sql.sql_code。

operator是运算符，params 是operator的参数

示例:

```
where0=sql_code,eq,exampleSql4
```

where后面加数字(where0,where1,where2,where3...)表示这是一个where条件，sql_code是表中的字段名，eq是运算符operator，exampleSql4是operator的参数，这句话的意思是有一个where条件是sql_code=exampleSql4。

运算符有如下

| 运算符(operator) | 注释                                          | 参数值（params）                               | 示例                      | 示例含义                  |
| ---------------- | --------------------------------------------- | ---------------------------------------------- | ------------------------- | ------------------------- |
| eq               | EqualsTo,和sql语句中的=对应                   | 1个具体的值，比如5                             | where1=id,eq,5            | id=5                      |
| _eq              | NotEqualsTo,和sql语句中的<>对应               | 1个具体的值，比如5                             | where1=id,_eq,5           | id!=5                     |
| gt               | GreaterThan,和sql语句中的>对应                | 1个具体的值，比如5                             | where1=id,gt,5            | id>5                      |
| ge               | GreaterThanEquals,和sql语句中的>=对应         | 1个具体的值，比如5                             | where1=id,ge,5            | id>=5                     |
| lt               | LessThan,和sql语句中的<对应                   | 1个具体的值，比如5                             | where1=id,lt,5            | id<5                      |
| le               | LessThanEquals,和sql语句中的<=对应            | 1个具体的值，比如5                             | where1=id,le,5            | id<=5                     |
| bt               | Between,和sql语句中的Between...And对应        | 2个具体的值，比如5,10                          | where1=id,bt,5,10         | id between 5 and 10       |
| _bt              | NotBetween,和sql语句中的Not Between...And对应 | 2个具体的值，比如5,10                          | where1=id,_bt,5,10        | id not between 5 and 10   |
| in               | in,和sql语句中的in对应                        | 多个具体的值，比如5,6,9,10                     | where1=id,in,5,6,9,10     | id in (5,6,9,10)          |
| _in              | NotIn,和sql语句中的not in对应                 | 多个具体的值，比如5,6,9,10                     | where1=id,_in,5,6,9,10    | id not in (5,6,9,10)      |
| nl               | IsNull,和sql语句中的is null对应               | 没有params                                     | where1=identifier,nl      | identifier is null        |
| _nl              | IsNotNull,和sql语句中的is not null对应        | 没有params                                     | where1=identifier,_nl     | identifier is not null    |
| lk               | like,和sql语句中的like对应                    | 一个具体的数值，比如exam*，其中的符号雪花表示% | where1=sql_code,lk,exam*  | sql_code like 'exam%'     |
| _lk              | like,和sql语句中的not like对应                | 一个具体的数值，比如exam*，其中的符号雪花表示% | where1=sql_code,_lk,exam* | sql_code not like 'exam%' |

### 3.自动生成sql 的多表查询

#### 3.1请求路径

```
/common/standard/sql/commonSelect
```

#### 3.2请求说明



```
public static final String[] innerJoin = new String[]{"innerJoin", "join"}; //sql语句的inner join关键字
public static final String[] leftJoin = new String[]{"leftJoin"}; //sql语句的left join关键字
public static final String[] rightJoin = new String[]{"rightJoin"};//sql语句的right join关键字
public static final String[] fullJoin = new String[]{"fullJoin"};//sql语句的full join关键字
```

GET请求。

语法和单表查询一致，

```
/common/standard/sql/commonSelect?select=...&from=...&innerJoin|leftJoin|rightJoin|fullJoin=...&where=...&groupBy=...&having=...&orderBy=...&fieldName1=fieldValue1&fieldName2=fieldValue2
```

增加了如下4个请求字段

| 请求字段  | 说明                                   | 参数值           | 示例                                                         | 示例含义                       |
| --------- | -------------------------------------- | ---------------- | ------------------------------------------------------------ | ------------------------------ |
| innerJoin | 与sql语句中 inner join对应，是驼峰写法 | 表名,字段1,字段2 | cc_base_sql,innerJoin=cc_base_sql_series.sql_code,cc_base_sql.sql_code | inner join cc_base_sql on  ... |
| leftJoin  | 与sql语句中 left join对应，是驼峰写法  | 表名,字段1,字段2 | cc_base_sql,leftJoin=cc_base_sql_series.sql_code,cc_base_sql.sql_code | left join cc_base_sql on  ...  |
| rightJoin | 与sql语句中 right join对应，是驼峰写法 | 表名,字段1,字段2 | cc_base_sql,rightJoin=cc_base_sql_series.sql_code,cc_base_sql.sql_code | right join cc_base_sql on  ... |
| fullJoin  | 与sql语句中 full join对应，是驼峰写法  | 表名,字段1,字段2 | cc_base_sql,fullJoin=cc_base_sql_series.sql_code,cc_base_sql.sql_code | full join cc_base_sql on  ...  |

参数值中的表名是join的表名，字段1,字段2是join的条件,可以有多个join条件，比如说:表名,字段1,字段2,字段3，字段4...

示例：

http://localhost:8080/common/standard/sql/commonSelect?from=cc_base_sql_series&rightJoin=cc_base_sql,cc_base_sql_series.sql_code,cc_base_sql.sql_code

上述sql里from=cc_base_sql_series，生命了查询的主表是cc_base_sql_series，这个表right join 了 cc_base_sql 表，join条件是cc_base_sql_series.sql_code=cc_base_sql.sql_code，通过查看控制台，最终的对应的系统为您生成的sql是

```sql
SELECT * FROM cc_base_sql_series RIGHT JOIN cc_base_sql ON cc_base_sql_series.sql_code = cc_base_sql.sql_code
```













