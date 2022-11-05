package com.marssenger.common.sql.generator;

import com.marssenger.common.sql.domain.*;
import com.marssenger.common.sql.exception.TableNotFoundException;
import com.marssenger.common.sql.executor.StandardSqlExecutor;
import com.marssenger.common.sql.service.SchemaQueryService;
import com.marssenger.common.sql.utils.SpringUtilsForSql;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ligaosheng
 * 一个简单的sql语句生成器,url参数的格式为：
 * select
 * distinct/distinct
 * from t1
 * join t2
 * where [and|or]
 * groupBy
 * having
 * orderBy [desc]
 */
@Slf4j
public class JSqlGenerator implements SqlGenerator {

    //sql语句所使用的关键字如下:
    //数组：模糊匹配
    public static final String[] vagueSymbol = new String[]{"*"}; // 用于like语句，替换为%
    public static final String[] select = new String[]{"select", "selectItems", "column"};//select语句查哪些列
    public static final String[] distinct = new String[]{"distinct"}; //sql语句中的distinct关键字
    public static final String[] function = new String[]{"function"};//聚合函数avg,max使用
    public static final String[] from = new String[]{"from", "table", "tableName", "fromTable"}; //本次查询从哪个表开始查询，对应sql语句中的from关键字
    public static final String[] innerJoin = new String[]{"innerJoin", "join"}; //sql语句的inner join关键字
    public static final String[] leftJoin = new String[]{"leftJoin"}; //sql语句的left join关键字
    public static final String[] rightJoin = new String[]{"rightJoin"};//sql语句的right join关键字
    public static final String[] fullJoin = new String[]{"fullJoin"};//sql语句的full join关键字
    public static final String[] where = new String[]{"where"};//sql语句中的where关键字
    public static final String[] having = new String[]{"having"};//having过滤条件
    public static final String[] groupBy = new String[]{"groupBy"};//sql语句的groupBy关键字
    public static final String[] orderBy = new String[]{"orderBy"};//sql语句的orderBy关键字
    public static final String desc = "desc";//sql语句的desc关键字
    public static final String and = ",and,";//sql语句的or关键字
    public static final String or = ",or,";//sql语句的or关键字
    //对分页语句的支持
    public static final String start = "start";
    public static final String limit = "limit";
    public static final String pageNum = "pageNum";
    public static final String pageSize = "pageSize";
    //参数分割符
    public static final String spliter = ",";
    //数据库、表名、字段分割符号
    public static final String dot = ".";
    //代码中使用的常量
    public static final String sqlCode = "sqlCode";
    public static final String identifier = "identifier";

    /**
     * 根据sqlType和params生成对应的sql语句
     *
     * @param sqlType sql类型
     * @param params  参数类型
     * @return SqlObject
     */
    @Override
    public SqlObject generate(SqlType sqlType, Map<String, Object> params) {
        if (sqlType.getCode().equals(SqlType.SELECT_ONE_TABLE.getCode())) {
            return getR1Object(params);
        }
        if (sqlType.getCode().equals(SqlType.SELECT_LOTS_TABLE.getCode())) {
            return getR2Object(params);
        }
        return null;
    }

    /**
     * 方法定义:
     *
     * @param params
     * @return
     */
    public static SqlObject getR1Object(Map<String, Object> params) {
        SelectBody selectBody = R1(params);
        String sql = selectBody.toString();
        //在xml中使用，需要转义
        sql = "<![CDATA[" + "\n" + sql + "\n" + "]]>";
        SqlObject sqlObject = new SqlObject();
        sqlObject.setSqlType(StandardSqlExecutor.SELECT);
        sqlObject.setSql(sql);
        sqlObject.setSqlCode(getUUID(sqlCode, params));
        sqlObject.setIdentifier(getUUID(identifier, params));
        return sqlObject;
    }

    public static SqlObject getR2Object(Map<String, Object> params) {
        SelectBody selectBody = R2(params);
        String sql = selectBody.toString();
        //在xml中使用，需要转义
        sql = "<![CDATA[" + "\n" + sql + "\n" + "]]>";
        SqlObject sqlObject = new SqlObject();
        sqlObject.setSqlType(StandardSqlExecutor.SELECT);
        sqlObject.setSql(sql);
        sqlObject.setSqlCode(getUUID(sqlCode, params));
        sqlObject.setIdentifier(getUUID(identifier, params));
        return sqlObject;
    }

    /**
     * 单表查询
     *
     * @param params map参数
     * @return 返回查询对象体
     */
    public static SelectBody R1(Map<String, Object> params) {
        String _tableName = getValueVague(params, from);
        //生成查询对象
        Table table = generateTableByName(_tableName);
        PlainSelect plainSelect = new PlainSelect();
        plainSelect.setFromItem(table);
        //处理查询的项目
        setSelectColumns(plainSelect, params);
        //开始设置where条件
        setWhere(Arrays.asList(table), plainSelect, params);
        //开始设置groupBy条件
        setGroupBy(plainSelect, params);
        //开始设置having条件
        setHaving(Arrays.asList(table), plainSelect, params);
        //开始设置orderBy
        setOrderBy(plainSelect, params);
        //处理分页
        setPage(plainSelect, params);
        return plainSelect;
    }

    /**
     * 多表查询
     *
     * @param params map参数
     * @return
     */
    public static SelectBody R2(Map<String, Object> params) {
        PlainSelect plainSelect = new PlainSelect();
        String _tableName = getValueVague(params, from);
        //生成查询对象,主表
        Table table = generateTableByName(_tableName);
        //设置join条件，形成视图
        List<Table> tables = new ArrayList<>();
        tables.add(table);
        List<Join> joins = getJoins(tables, params);
        if (joins.size() == 0) {
            throw new IllegalArgumentException("多表查询必须包含如下join条件之一:join(innerJoin),leftJoin,rightJoin,fullJoin");
        }
        plainSelect.setFromItem(table);
        plainSelect.withJoins(joins);
        setSelectColumns(plainSelect, params);
        setWhere(tables, plainSelect, params);
        //开始设置groupBy条件
        setGroupBy(plainSelect, params);
        //开始设置having条件
        setHaving(tables, plainSelect, params);
        //开始设置orderBy
        setOrderBy(plainSelect, params);
        //处理分页
        setPage(plainSelect, params);
        return plainSelect;
    }


    /**
     * 得到join的表达式
     *
     * @param listTable
     * @param params
     * @return
     */
    public static List<Join> getJoins(List<Table> listTable, Map<String, Object> params) {
        List<String> innerJoinExprs = getExpr(innerJoin, params);
        List<String> leftJoinExprs = getExpr(leftJoin, params);
        List<String> rightJoinExprs = getExpr(rightJoin, params);
        List<String> fullJoinExprs = getExpr(fullJoin, params);
        List<Join> joinList = new ArrayList<>();
        //join和inner join都代指 inner join
        if (innerJoinExprs.size() > 0) {
            for (int i = 0; i < innerJoinExprs.size(); i++) {
                String expr = innerJoinExprs.get(i);
                Join join = new Join();
                join.setInner(true);
                setJoin(listTable, join, expr);
                joinList.add(join);
            }
        }
        if (leftJoinExprs.size() > 0) {
            for (int i = 0; i < leftJoinExprs.size(); i++) {
                String expr = leftJoinExprs.get(i);
                Join join = new Join();
                join.setLeft(true);
                setJoin(listTable, join, expr);
                joinList.add(join);
            }
        }
        if (rightJoinExprs.size() > 0) {
            for (int i = 0; i < rightJoinExprs.size(); i++) {
                String expr = rightJoinExprs.get(i);
                Join join = new Join();
                join.setRight(true);
                setJoin(listTable, join, expr);
                joinList.add(join);
            }
        }
        if (fullJoinExprs.size() > 0) {
            for (int i = 0; i < fullJoinExprs.size(); i++) {
                String expr = fullJoinExprs.get(i);
                Join join = new Join();
                join.setFull(true);
                setJoin(listTable, join, expr);
                joinList.add(join);
            }
        }
        return joinList;
    }


    /**
     * 根据通用字符串生成表名
     *
     * @param _tableName
     * @return
     */
    public static Table generateTableByName(String _tableName) {
        String _schemaName = null;
        if (_tableName.indexOf(dot) > 0) {
            _schemaName = _tableName.split(dot)[0];
            _tableName = _tableName.split(dot)[1];
        }
        if (_tableName == null || _tableName.trim().length() == 0) {
            throw new IllegalArgumentException("表名[table]不能为空");
        }
        Table table = new Table(_schemaName, _tableName);
        return table;
    }


    /**
     * 比较表是否相同
     *
     * @param table1
     * @param table2
     * @return
     */
    public static boolean tableEquals(Table table1, Table table2) {
        if (table1 == table2) {
            return true;
        }
        if (table1 == null || table2 == null) {
            return false;
        }
        if ((table1.getName().equals(table2.getName()))) {
            return true;
        }
        return false;
    }


    /**
     * 设置join条件，并收集表,方便起见，一个expr中只能有2个表
     * 范式:t2,t1.a,t2.b,t1.c,t2.d
     * 与t2表join,join条件是 t1.a=t2.b and t1.c=t2.d
     *
     * @param listTable
     * @param join
     * @param expr
     */
    public static void setJoin(List<Table> listTable, Join join, String expr) {
        String[] array = expr.split(spliter);
        if (array.length == 0 || array.length % 2 != 1) {
            throw new IllegalArgumentException("join条件:[" + expr + "]的参数个数(用逗号分割后)必须是奇数！第一个参数是要join的条件，之后每隔两个是join的条件,格式为t2,t1.a,t2.b,t1.c,t2.d...");
        }
        List<Expression> list = new ArrayList<>();
        for (int i = 1; i < array.length; i = i + 2) {
            EqualsTo equalsTo = new EqualsTo();
            Table table1 = generateTableByFieldName(array[i + 0]);
            Table table2 = generateTableByFieldName(array[i + 1]);
            boolean containTable1 = false;
            boolean containTable2 = false;
            for (int j = 0; j < listTable.size(); j++) {
                //比较表名
                Table table = listTable.get(j);
                if (tableEquals(table, table1)) {
                    containTable1 = true;
                }
                if (tableEquals(table, table2)) {
                    containTable2 = true;
                }
                if (containTable1 && containTable2) {
                    break;
                }
            }
            if (!containTable1) {
                listTable.add(table1);
            }
            if (!containTable2) {
                listTable.add(table2);
            }
            equalsTo.setLeftExpression(getColumn(array[i + 0]));
            equalsTo.setRightExpression(getColumn(array[i + 1]));
            list.add(equalsTo);
        }
        Expression andExpression = getAndExpression(list);
        join.setOnExpressions(Arrays.asList(andExpression));
        join.setRightItem(generateTableByName(array[0]));
    }

    //根据字段名判断表名
    public static Table generateTableByFieldName(String fieldName) {
        Column column = getColumn(fieldName);
        return column.getTable();
    }

    /**
     * 处理select语句的具体查询项目
     *
     * @param plainSelect
     * @param params
     */
    public static void setSelectColumns(PlainSelect plainSelect, Map<String, Object> params) {
        boolean isReturn = false;
        //处理function,得到function表达式列表
        List<String> exprs = getExpr(function, params);
        if (exprs != null && exprs.size() > 0) {
            List<Function> functions = new ArrayList<>();
            for (int i = 0; i < exprs.size(); i++) {
                String expr = exprs.get(i);
                if (expr != null) {
                    expr = expr.replaceAll("\\s*", "");
                    if (expr.length() > 0) {
                        String[] functionData = expr.split(spliter);
                        if (functionData.length >= 1) {
                            String functionName = functionData[0];
                            Function f = new Function();
                            f.setName(functionName);
                            ExpressionList paramsList = new ExpressionList();
                            List<Expression> list = new ArrayList<>();
                            if (functionData.length > 1) {
                                for (int j = 1; j < functionData.length; j++) {
                                    //这里只能传列，不可以传固定的值
                                    list.add(getColumn(functionData[j]));
                                }
                                paramsList.setExpressions(list);
                                f.setParameters(paramsList);
                            }
                            functions.add(f);
                        }
                    }
                }
            }
            if (functions.size() > 0) {
                List<SelectItem> list = functions.stream().map(
                        e -> {
                            SelectExpressionItem selectExpressionItem = new SelectExpressionItem(e);
                            ExpressionList parameters = e.getParameters();
                            String alias = e.getName();
                            if (parameters != null) {
                                List<Expression> expressions = parameters.getExpressions();
                                if (expressions != null && expressions.size() > 0) {
                                    alias += "_" + expressions.stream().map(exp -> exp.toString().replace(dot, "")).collect(Collectors.joining("_"));
                                }
                            }
                            selectExpressionItem.setAlias(new Alias(alias));
                            return selectExpressionItem;
                        }
                ).collect(Collectors.toList());
                plainSelect.addSelectItems(list);
                isReturn = true;
            }
        }
        //处理单表distinct查询
        String _distinct = getValueVague(params, distinct);
        if (_distinct != null) {
            _distinct = _distinct.replaceAll("\\s*", "");
            if (_distinct.length() > 0) {
                List<SelectItem> list = Arrays.stream(_distinct.split(spliter)).map(e -> new SelectExpressionItem(getColumn(e))).collect(Collectors.toList());
                Distinct distinct = new Distinct();
                distinct.setOnSelectItems(list);
                plainSelect.withDistinct(distinct);
                isReturn = true;
            }
        }

        String selectItems = getValueVague(params, select);
        //处理通常select
        if (selectItems != null) {
            selectItems = selectItems.replaceAll("\\s*", "");
            if (selectItems.length() > 0) {
                if (selectItems.lastIndexOf(spliter) == selectItems.length() - 1) {
                    selectItems = selectItems.substring(0, selectItems.length() - 1);
                }
                List<SelectItem> list = Arrays.stream(selectItems.split(spliter)).map(e -> new SelectExpressionItem(getColumn(e))).collect(Collectors.toList());
                plainSelect.addSelectItems(list);
                isReturn = true;
            }
        }
        if (isReturn) {
            return;
        } else {
            //处理默认情况
            plainSelect.addSelectItems(Arrays.asList(new SelectExpressionItem()
                    .withExpression(new AllColumns())));
        }
    }

    /**
     * where语句的生成
     *
     * @param tables
     * @param plainSelect
     * @param params
     */
    public static void setWhere(List<Table> tables, PlainSelect plainSelect, Map<String, Object> params) {
        List<Expression> wheres = new ArrayList<>();
        if (tables != null && tables.size() > 0) {
            for (int i = 0; i < tables.size(); i++) {
                Table table = tables.get(i);
                List<TableSchema> schemeInfo = getFieldsInfo(table.getSchemaName(), table.getName());
                if (schemeInfo == null) {
                    throw new TableNotFoundException(table.getName() + "表不存在!");
                }
                List<String[]> equalsTofields = getEqualsToFields(params, schemeInfo);
                //开始where语句的等值比较
                if (equalsTofields.size() > 0) {
                    for (int j = 0; j < equalsTofields.size(); j++) {
                        String vagueKey = equalsTofields.get(j)[0];
                        String columnName = equalsTofields.get(j)[1];
                        if (params.get(vagueKey) != null) {
                            String columnValue = "" + params.get(vagueKey);
                            EqualsTo expr = new EqualsTo();
                            // 设置表达式左边值
                            expr.setLeftExpression(getColumn(columnName));
                            // 设置表达式右边值
                            expr.setRightExpression(new StringValue(columnValue));
                            wheres.add(expr);
                        }
                    }
                }
                //开始通常的where语句
                List<Expression> expressions = getExpressions(where, table, params, schemeInfo);
                if (expressions.size() > 0) {
                    wheres.addAll(expressions);
                }
            }
        }
        if (wheres.size() > 0) {
            Expression andExpression = getAndExpression(wheres);
            if (andExpression != null) {
                plainSelect.setWhere(andExpression);
            }
        }
    }

    //解析出具体的字段

    /**
     * 根据一个字符串(格式为:nameSpace.tableName.columnName 或 tableName.columnName 或 columnName)来解析出一个字段，
     *
     * @param field
     * @return
     */
    public static Column getColumn(String field) {
        if (field == null) {
            return null;
        }
        field = field.replaceAll("\\s*", "");
        field = field.replaceAll("^\\.*|\\.*$", "");
        if (field.length() == 0) {
            return null;
        }
        if (field.indexOf(dot) > 0) {
            String[] split = field.split("\\" + dot);
            if (split.length == 3) {
                return new Column(new Table(split[0], split[1]), split[2]);
            }
            if (split.length == 2) {
                return new Column(new Table(split[0]), split[1]);
            }
            if (split.length == 1) {
                return new Column(split[0]);
            }
        }
        return new Column(field);
    }

    /**
     * groupBy语句的生成
     *
     * @param plainSelect
     * @param params
     */
    public static void setGroupBy(PlainSelect plainSelect, Map<String, Object> params) {
        String _groupBy = getValueVague(params, groupBy);
        List<Expression> groupBys = new ArrayList<>();
        if (_groupBy != null) {
            _groupBy = _groupBy.replaceAll("\\s*", "");
            for (String e : _groupBy.split(spliter)) {
                if (e.length() > 0) {
                    groupBys.add(getColumn(e));
                }
            }
        }
        if (groupBys.size() > 0) {
            GroupByElement groupByElement = new GroupByElement();
            ExpressionList expressionList = new ExpressionList(groupBys);
            groupByElement.setGroupByExpressionList(expressionList);
            plainSelect.setGroupByElement(groupByElement);
        }
    }


    /**
     * having语句的生成
     *
     * @param tables
     * @param plainSelect
     * @param params
     */
    public static void setHaving(List<Table> tables, PlainSelect plainSelect, Map<String, Object> params) {
        List<Expression> expressions = new ArrayList<>();
        if (tables != null && tables.size() > 0) {
            for (int i = 0; i < tables.size(); i++) {
                Table table = tables.get(i);
                List<TableSchema> schemeInfo = getFieldsInfo(table.getSchemaName(), table.getName());
                if (schemeInfo == null) {
                    throw new TableNotFoundException(table.getName() + "表不存在!");
                }
                List<Expression> tmp = getExpressions(having, table, params, schemeInfo);
                if (tmp != null) {
                    expressions.addAll(tmp);
                }
            }
        }
        if (expressions != null && expressions.size() > 0) {
            Expression andExpression = getAndExpression(expressions);
            if (andExpression != null) {
                plainSelect.setHaving(andExpression);
            }
        }
    }

    /**
     * 设置orderBy语句，方法为[orderBy*=field,order]
     *
     * @param plainSelect
     * @param params
     */
    public static void setOrderBy(PlainSelect plainSelect, Map<String, Object> params) {
        List<String> exprs = getExpr(orderBy, params);
        List<OrderByElement> list = new ArrayList<>();
        for (int i = 0; i < exprs.size(); i++) {
            String fo = exprs.get(i);
            if (fo != null) {
                fo = fo.replaceAll("\\s*", "");
                if (fo.length() > 0) {
                    OrderByElement orderByElement = new OrderByElement();
                    if (fo.indexOf(spliter) > 0) {
                        String field = fo.split(spliter)[0];
                        String order = fo.split(spliter)[1];
                        // 设置排序字段
                        orderByElement.setExpression(getColumn(field));
                        if (vagueEquals(desc, order)) {
                            orderByElement.setAsc(false);
                        } else {
                            orderByElement.setAsc(true);
                        }
                    } else {
                        // 设置排序字段
                        orderByElement.setExpression(getColumn(fo));
                        orderByElement.setAsc(true);
                    }
                    list.add(orderByElement);
                }
            }
        }
        list.forEach(e -> {
            plainSelect.addOrderByElements(list);
        });
    }


    /**
     * 分页条件的生成
     *
     * @param plainSelect
     * @param params
     */
    public static void setPage(PlainSelect plainSelect, Map<String, Object> params) {
        Page page = getPage(params);
        if (page != null) {
            plainSelect.setLimit(new Limit()
                    .withOffset(new LongValue(page.getStart()))
                    .withRowCount(new LongValue(page.getLimit())));
        }
    }


    /**
     * 将多个Expression合并成一个Expression
     *
     * @param expressions
     * @return
     */
    public static Expression getAndExpression(List<Expression> expressions) {
        if (expressions.size() == 0) {
            return null;
        }
        if (expressions.size() == 1) {
            return expressions.get(0);
        } else {
            AndExpression expression = new AndExpression();
            Expression exp = expressions.get(expressions.size() - 1);
            expressions.remove(expressions.size() - 1);
            expression.setLeftExpression(getAndExpression(expressions));
            expression.setRightExpression(exp);
            return expression;
        }
    }


    /**
     * 将多个Expression合并成一个OR Expression
     *
     * @param expressions
     * @return
     */
    public static Expression getORExpression(List<Expression> expressions) {
        Expression exp = getORExpressionTrue(expressions);
        try {
            return CCJSqlParserUtil.parseCondExpression("(" + exp.toString() + ")");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 以递归的模式得出or语句
     *
     * @param expressions
     * @return
     */
    public static Expression getORExpressionTrue(List<Expression> expressions) {
        if (expressions.size() == 0) {
            return null;
        }
        if (expressions.size() == 1) {
            return expressions.get(0);
        } else {
            OrExpression expression = new OrExpression();
            Expression exp = expressions.get(expressions.size() - 1);
            expressions.remove(expressions.size() - 1);
            expression.setLeftExpression(getORExpressionTrue(expressions));
            expression.setRightExpression(exp);
            return expression;
        }
    }

    /**
     * 从url中获取页面起止信息
     *
     * @param params 上下文
     * @return
     */
    public static Page getPage(Map<String, Object> params) {
        Page page = new Page();
        if (params.get(start) != null) {
            page.setStart(Integer.parseInt(params.get(start).toString()));
        }
        if (params.get(limit) != null) {
            page.setLimit(Integer.parseInt(params.get(limit).toString()));
        }
        if (params.get(pageSize) != null) {
            page.setLimit(Integer.parseInt(params.get(pageSize).toString()));
        }
        if (params.get(pageNum) != null && params.get(pageSize) != null) {
            int pNum = Integer.parseInt(params.get(pageSize).toString());
            int pSize = Integer.parseInt(params.get(pageNum).toString());
            page.setStart((pNum - 1) * pSize);
        }
        if (page.getStart() == null && page.getLimit() == null) {
            return null;
        } else {
            return page;
        }
    }

    public static List<TableSchema> getFieldsInfo(String tableSchema, String tableName) {
        if (tableName == null || tableName.trim().length() == 0) {
            throw new IllegalArgumentException("表名[tableName]不能为空");
        }
        if (tableName.indexOf(dot) > 0) {
            String[] split = tableName.split(dot);
            tableSchema = split[0];
            tableName = split[1];
        }
        SchemaQueryService schemaQueryService = SpringUtilsForSql.getBean(SchemaQueryService.class);
        return schemaQueryService.getSchemaInfo(tableSchema, tableName);
    }

    public static List<Expression> getExpressions(String[] base, Table table, Map<String, Object> params, List<TableSchema> schemeInfo) {
        List<String> exprs = getExpr(base, params);
        List<Expression> ret = new ArrayList<>();
        for (int i = 0; i < exprs.size(); i++) {
            //这里对or语句进行处理,范式:where,0=a,in,1,2,3,or,b,in,2,3,4
            String exprLine = exprs.get(i);
            exprLine = exprLine.replace("\\s*", "");
            boolean isOr;
            String innerSpliter;
            if (exprs.get(i).indexOf(or) > 0) {
                isOr = true;
                innerSpliter = or;
            } else {
                isOr = false;
                innerSpliter = and;
            }
            String[] splitExps = exprLine.split(innerSpliter);
            List<Expression> curExps = new ArrayList<>();
            for (int j = 0; j < splitExps.length; j++) {
                Expression curExp = getSitutions(table, splitExps[j], schemeInfo);
                if (curExp != null) {
                    curExps.add(curExp);
                }
            }
            Expression exp;
            if (curExps.size() > 0) {
                if (isOr) {
                    exp = getORExpression(curExps);
                } else {
                    exp = getAndExpression(curExps);
                }
                if (exp != null) {
                    ret.add(exp);
                }
            }
        }
        return ret;
    }

    public static List<String> getExpr(String[] bases, Map<String, Object> params) {
        List<String> exprs = new ArrayList<>();
        for (int i = 0; i < bases.length; i++) {
            List<String> expr = getExpr(bases[i], params);
            if (expr != null) {
                exprs.addAll(expr);
            }
        }
        return exprs;
    }

    /**
     * 获取以指定前缀开头的表达式列表，并排序。如以where开头的，where0,where1,where2并按照0,1,2的顺序排序。
     *
     * @param base
     * @param params
     * @return
     */
    public static List<String> getExpr(String base, Map<String, Object> params) {
        List<String[]> exprs = new ArrayList<>();
        params.forEach((k, v) -> {
            if (k.toLowerCase().indexOf(base.toLowerCase()) == 0) {
                if (v != null) {
                    exprs.add(new String[]{k, v + ""});
                }
            }
        });
        exprs.sort(new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                String s1 = o1[0].toLowerCase().replaceAll("\\s*", "").replace(base.toLowerCase(), "").replace(spliter, "");
                String s2 = o2[0].toLowerCase().replaceAll("\\s*", "").replace(base.toLowerCase(), "").replace(spliter, "");
                if (s1.length() == 0) {
                    s1 = "0";
                }
                if (s2.length() == 0) {
                    s2 = "0";
                }
                int r = Integer.parseInt(s1) - Integer.parseInt(s2);
                return r;
            }
        });
        List<String> sortExprs = exprs.stream().map(array -> array[1]).collect(Collectors.toList());
        return sortExprs;
    }

    public static Expression getSitutions(Table table, String[] datas, List<TableSchema> schemeInfo) {
        if (datas == null || datas.length == 0) {
            return null;
        }
        if (datas.length < 2) {
            throw new IllegalArgumentException("expr里的参数不许大于等于2个");
        }
        String columnName = datas[0];
        String operator = datas[1];
        Column column = getColumn(columnName);
        if (column == null || column.getColumnName() == null) {
            return null;
        }
        Table columnTable = column.getTable();
        if (columnTable != null) {
            if (columnTable.getName() != null) {
                if (!columnTable.getName().equals(table.getName())) {
                    //表名不匹配,跳出的条件1
                    return null;
                }
            }
        }
        //不包含.号的column
        columnName = column.getColumnName();
        boolean containThisColumn = false;
        //无表名或者表名匹配
        if (schemeInfo != null && schemeInfo.size() > 0) {
            for (int i = 0; i < schemeInfo.size(); i++) {
                if (vagueEquals(columnName, schemeInfo.get(i).getColumnName())) {
                    containThisColumn = true;
                    columnName = schemeInfo.get(i).getColumnName();
                    log.info(Arrays.toString(datas) + "对应的表为:" + table.getName() + ",对应的字段为:" + schemeInfo.get(i).getColumnName());
                    break;
                }
            }
        }

        //该表不包含该字段
        if (!containThisColumn) {
            return null;
        }

        //EqualsTo 比较相等
        if (SqlOperateor.EqualsTo.vagueEquals(operator)) {
            EqualsTo equalsTo = new EqualsTo();
            // 设置表达式左边值
            equalsTo.setLeftExpression(new Column(table, columnName));
            // 设置表达式右边值
            equalsTo.setRightExpression(new StringValue(datas[2]));
            return equalsTo;
        }

        //NotEqualsTo 不相等
        if (SqlOperateor.NotEqualsTo.vagueEquals(operator)) {
            NotEqualsTo notEqualsTo = new NotEqualsTo();
            // 设置表达式左边值
            notEqualsTo.setLeftExpression(new Column(table, columnName));
            // 设置表达式右边值
            notEqualsTo.setRightExpression(new StringValue(datas[2]));
            return notEqualsTo;
        }

        //GreaterThan 大于
        if (SqlOperateor.GreaterThan.vagueEquals(operator)) {
            GreaterThan greaterThan = new GreaterThan();
            // 设置表达式左边值
            greaterThan.setLeftExpression(new Column(table, columnName));
            // 设置表达式右边值
            greaterThan.setRightExpression(new StringValue(datas[2]));
            return greaterThan;
        }

        //GreaterThanEquals 大于等于
        if (SqlOperateor.GreaterThanEquals.vagueEquals(operator)) {
            GreaterThanEquals greaterThanEquals = new GreaterThanEquals();
            // 设置表达式左边值
            greaterThanEquals.setLeftExpression(new Column(table, columnName));
            // 设置表达式右边值
            greaterThanEquals.setRightExpression(new StringValue(datas[2]));
            return greaterThanEquals;
        }

        //MinorThan 小于
        if (SqlOperateor.MinorThan.vagueEquals(operator)) {
            MinorThan minorThan = new MinorThan();
            // 设置表达式左边值
            minorThan.setLeftExpression(new Column(table, columnName));
            // 设置表达式右边值
            minorThan.setRightExpression(new StringValue(datas[2]));
            return minorThan;
        }

        //MinorThanEquals 小于等于
        if (SqlOperateor.MinorThanEquals.vagueEquals(operator)) {
            MinorThanEquals minorThanEquals = new MinorThanEquals();
            // 设置表达式左边值
            minorThanEquals.setLeftExpression(new Column(table, columnName));
            // 设置表达式右边值
            minorThanEquals.setRightExpression(new StringValue(datas[2]));
            return minorThanEquals;
        }

        //Between 在两者之间
        if (SqlOperateor.Between.vagueEquals(operator) || SqlOperateor.NotBetween.vagueEquals(operator)) {
            Between between = new Between();
            // 设置起点值
            between.setBetweenExpressionStart(new StringValue(datas[2]));
            // 设置终点值
            between.setBetweenExpressionEnd(new StringValue(datas[3]));
            // 设置左边的表达式，一般为列
            between.setLeftExpression(new Column(table, columnName));
            if (SqlOperateor.NotBetween.vagueEquals(operator)) {
                between.setNot(true);
            }
            return between;
        }

        //IN NotIn (不)在什么之中
        if (SqlOperateor.In.vagueEquals(operator) || SqlOperateor.NotIn.vagueEquals(operator)) {
            List<Expression> expressions = new ArrayList<>();
            for (int i = 2; i < datas.length; i++) {
                expressions.add(new StringValue(datas[i]));
            }
            ItemsList itemsList = new ExpressionList(expressions);
            InExpression inExpression = new InExpression(new Column(columnName), itemsList);
            if (SqlOperateor.NotIn.vagueEquals(operator)) {
                inExpression.setNot(true);
            }
            return inExpression;
        }

        //IsNull,IsNotNull (不)为空
        if (SqlOperateor.IsNull.vagueEquals(operator) || SqlOperateor.IsNotNull.vagueEquals(operator)) {
            IsNullExpression expression = new IsNullExpression();
            expression.setLeftExpression(new Column(table, columnName));
            if (SqlOperateor.IsNotNull.vagueEquals(operator)) {
                expression.setNot(true);
            }
            return expression;
        }

        //Like NotLike  (不)包含
        if (SqlOperateor.Like.vagueEquals(operator) || SqlOperateor.NotLike.vagueEquals(operator)) {
            LikeExpression expression = new LikeExpression();
            expression.setLeftExpression(new Column(table, columnName));
            for (int i = 0; i < vagueSymbol.length; i++) {
                datas[2] = datas[2].replace(vagueSymbol[i], "%");
            }
            expression.setRightExpression(new StringValue(datas[2]));
            if (SqlOperateor.NotLike.vagueEquals(operator)) {
                expression.setNot(true);
            }
            return expression;
        }

        //throws noSqlException

        return null;
    }

    /**
     * 增加一个where条件，方法为[where*=field,operator,value...]
     *
     * @param expr 表达式
     * @return where0=a,b
     * where1=c,d
     * where2=e,f
     * where3=g,h
     */
    public static Expression getSitutions(Table table, String expr, List<TableSchema> schemeInfo) {
        String[] datas = expr.split(spliter);
        return getSitutions(table, datas, schemeInfo);
    }

    /**
     * @param params     map参数
     * @param schemeInfo 表的结构信息
     * @return
     */
    public static List<String[]> getEqualsToFields(Map<String, Object> params, List<TableSchema> schemeInfo) {
        List<String[]> ret = new ArrayList<>();
        if (schemeInfo != null && schemeInfo.size() > 0) {
            for (int i = 0; i < schemeInfo.size(); i++) {
                TableSchema tableSchema = schemeInfo.get(i);
                String columnName1 = (tableSchema.getTableSchema() + dot + tableSchema.getTableName() + dot + tableSchema.getColumnName()).replaceAll("\\s*", "");
                String columnName2 = (tableSchema.getTableName() + dot + tableSchema.getColumnName()).replaceAll("\\s*", "");
                String columnName3 = tableSchema.getColumnName();
                String vagueKey = getVagueKey(columnName1, params);
                if (vagueKey != null) {
                    ret.add(new String[]{vagueKey, columnName1});
                    continue;
                }
                vagueKey = getVagueKey(columnName2, params);
                if (vagueKey != null) {
                    ret.add(new String[]{vagueKey, columnName2});
                    continue;
                }
                vagueKey = getVagueKey(columnName3, params);
                if (vagueKey != null) {
                    ret.add(new String[]{vagueKey, columnName3});
                    continue;
                }
            }
        }
        return ret;
    }

    /**
     * 不区分大小写的匹配
     *
     * @param name
     * @param params
     * @return
     */
    public static String getVagueKey(String name, Map<String, Object> params) {
        Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
        String vagueKey = null;
        while (iterator.hasNext()) {
            Map.Entry<String, Object> next = iterator.next();
            String key = next.getKey();
            String key1 = key.replace("_", "").replaceAll("\\s*", "").toLowerCase();
            String key2 = name.replace("_", "").replaceAll("\\s*", "").toLowerCase();
            if (key1.equals(key2)) {
                vagueKey = key;
                break;
            }
        }
        return vagueKey;
    }

    /**
     * 从模糊中获取参数
     *
     * @param params map参数
     * @param keys   可能的键值列表
     * @param <T>
     * @return
     */
    public static <T> T getValueVague(Map<String, Object> params, String[] keys) {
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            T t = getValueVague(params, key);
            if (t != null) {
                return t;
            }
        }
        return null;
    }


    public static <T> T getValueVague(Map<String, Object> params, String key) {
        Object val = params.get(key);
        if (val != null) {
            return (T) val;
        }
        return null;
    }

    /**
     * 宽松条件下的相等，下划线模式与驼峰模式的比较
     *
     * @param base
     * @param comp
     * @return
     */
    public static boolean vagueEquals(String base, String comp) {
        if (base == comp) {
            return true;
        }
        if (base != null && comp != null) {
            return base.replace("_", "").replaceAll("\\s*", "").toLowerCase().equals(comp.replace("_", "").replaceAll("\\s*", "").toLowerCase());
        } else {
            return false;
        }
    }

    public static String getUUID(String key, Map<String, Object> params) {
        if (key == null || key.length() == 0 || params == null) {
            return UUID.randomUUID().toString();
        }
        if (params.get(key) == null) {
            return UUID.randomUUID().toString();
        }
        return (String) params.get(key);
    }

}
