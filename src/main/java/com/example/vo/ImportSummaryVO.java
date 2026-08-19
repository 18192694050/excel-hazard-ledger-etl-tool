package com.example.vo;

import jdk.jfr.DataAmount;
import lombok.Data;

@Data
public class ImportSummaryVO{
    private Integer totalRows;
    private Integer successCount;
    private Integer failCount;
}