package com.bing.bean;

import lombok.Data;

@Data
public class TableProcess {
    private String sourceTable;
    private String sinkTable;
    private String sinkColumns;
    private String sinkPk;
    private String sinkExtend;
}
