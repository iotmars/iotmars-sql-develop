```
# Installation
```

#### 1.使用git下载sql-devlop项目

项目地址:https://github.com/iotmars/iotmars-sql-develop.git

进去该项目的sql-devlop文件夹，执行mvn install命令，将如下核心模块安装到了maven本地仓库

```xml
 <dependency>
            <groupId>com.marssenger</groupId>
            <artifactId>sql-develop</artifactId>
            <version>0.0.12-SNAPSHOT</version>
 </dependency>
```



#### 2.准备数据库

选一个数据库，执行如下脚本

```sql
CREATE TABLE `cc_base_sql` (
  `sql_code` varchar(64) NOT NULL,
  `sql_type` varchar(12) NOT NULL DEFAULT 'select' COMMENT 'insert,delete,update,select',
  `sql` text NOT NULL COMMENT '具体的sql语句',
  `total_sql` varchar(2550) DEFAULT NULL COMMENT '分页所对应的总数sql',
  `sql_result_name` varchar(64) DEFAULT NULL COMMENT 'sql所对应的返回结果的名字',
  `total_sql_result_name` varchar(64) DEFAULT NULL COMMENT '总数所对应的结果的名字',
  `jexl` varchar(2550) DEFAULT NULL COMMENT 'java表达式（待扩展）',
  `map` varchar(2550) DEFAULT NULL COMMENT 'sql运行的上下文',
  `order_no` int(11) DEFAULT NULL COMMENT '排序号',
  `remark` varchar(200) DEFAULT NULL COMMENT 'sql的说明',
  PRIMARY KEY (`sql_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
CREATE TABLE `cc_base_sql_series` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `series_code` varchar(64) NOT NULL,
  `sql_code` varchar(64) NOT NULL,
  `identifier` varchar(64) DEFAULT NULL COMMENT '结果的标识符',
  `order_no` int(11) DEFAULT NULL COMMENT '排序号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sql_code` (`sql_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4;
```

其中cc_base_sql是用来根据sql_code查到一条sql来执行。cc_base_sql_series是用来根据series_code查到一系列sql来按照顺序执行。



#### 3.配置您的项目。

##### 3.1 创建一个springboot项目

​		在idea创建一个springboot的maven项目。

##### 3.2 引入依赖

首先sql-devlop本身引入了springboot的诸多模块，您要在pom.xml以如下的方式引入sql-develop并排除依赖：

```
<dependency>
    <groupId>com.marssenger</groupId>
    <artifactId>sql-develop</artifactId>
    <version>0.0.12-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

排除了spring-boot-starter-web、spring-boot-starter、spring-boot-autoconfigure、spring-beans、spring-core、slf4j-api

这里假设了您也使用的springboot项目，因此我们会有springboot的jar包版本上的冲突。



在下面列举了两种项目的pom.xml

springboot项目的xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.0.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>com.marssenger</groupId>
    <artifactId>sql-develop-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.marssenger</groupId>
            <artifactId>sql-develop</artifactId>
            <version>0.0.12-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-web</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-autoconfigure</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-beans</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-core</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-api</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

</project>
```

springcloud项目的pom.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.0.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>com.marssenger</groupId>
    <artifactId>sql-develop-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <spring-cloud.version>Hoxton.SR1</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>com.marssenger</groupId>
            <artifactId>sql-develop</artifactId>
            <version>0.0.12-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-web</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-autoconfigure</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-beans</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-core</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-api</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>
```



###### 3.3 修改项目数据源

您打开bootstrap.yml或者application.yml，引入您的数据库配置。bootstrap-dev.yml里仅仅是普通的yml配置，因为项目最基本的是数据库，如果您不需要springcloud的注册中心的繁琐配置，您可以单纯参考一般的springboot项目引入mysql数据库的写法，不再赘述。

典型配置如下：

```properties
spring:
  datasource:
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: fayfox
    password: 123456
```

数据源配置好后，您可以运行一个这个项目，一切都大功告成。



#### 3.4 调整代码

请在您springboot项目的启动类上加上如下注解

```
@ComponentScan(basePackages = {"com.*"})
@MapperScan(basePackages ={"com.marssenger.common.sql.mapper"} )
```

这个注解会扫描到sql-devlop的jar包里的bean，这些bean是标准的controller、service、mapper，以便执行标准的SSM跳转流程

通常情况下，一个引入了sql-devlop标准的springboot项目的主类，大概是如下的样子

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.*"})
@MapperScan(basePackages ={"com.marssenger.common.sql.mapper"} )
public class Demo {
    public static void main(String[] args) {
        SpringApplication.run(Demo.class, args);
    }
}
```

如果您是springcloud项目而不是springboot项目，请加上@EnableDiscoveryClient这个与配置中心相关的注解。





#### 备注

在您从github上下载的项目里有一个sql-devlop-demo的文件夹，里面是springboot版本的实例，如您有疑惑的，可以完全照抄该模板。

