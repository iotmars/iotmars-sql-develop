# Getting started

一个用sql语句实现敏捷开发的组件，项目地址为:https://github.com/iotmars/iotmars-sql-develop.git

该项目具有如下特性:

#### 纯SQL实现功能开发

传统ssm项目有繁琐又模板式的跳转（controller->service-mapper）,本项目的原意是让您省略这些模板式的跳转，直接写sql实现功能的开发。一些简单的查询sql可以由系统自动生成然后拼接。这样连sql语句这一步都省略了。希望本项目可以让拧螺丝的您感到快乐和轻松。

#### mybatis支持

引入了mybatis框架，可以按照mybaitis的方式书写sql，使用如<foreach/>、<if/>等标签，对于程序员来说，学习成本比较低。

#### springboot支持

本项目是面向springboot框架开发的，可以方便的将该模块引入您的已有的springboot或者springcloud项目。

#### sql自动生成

本项目使用jsqlparser来自动生成sql。单表和多表的查询可以自动生成sql。您只需要传入指定的参数，就能自动生成sql，并生成相应的结果。

jsqlparser是github上的一个开源项目，地址为：https://github.com/JSQLParser/JSqlParser。

#### sql按照顺序执行

本项目可以实现一批sql按照先后顺序执行，后执行的sql可以使用先执行的sql的结果，这是一个按照先后顺序执行的流水线。





















