package com.marssenger.common.sql.domain;

import lombok.Data;

/**
 * @author ligaosheng
 */
@Data
public class CommandObject {

    public static final String SQL_COMMAND = "SQL";


    public static final String COMMON_COMMAND = "COMMAND";

    /**
     * 命令名称
     */
    private String commandName;

    /**
     * 命令类型
     */
    private String commandType = SQL_COMMAND;

    /**
     * 命令
     */
    private Object command;

    /**
     * 命令参数
     */
    private Object commandParams;
}
