package com.example.entity;

import lombok.Data;

/**
 * 云申监测点解析实体：拆分dlwz四级行政地址
 */
@Data
public class MonitorPoint {
    // 源表code = 三维模型编号
    private String code;
    // 源表name = 三维模型点名称
    private String name;
    // 拆分dlwz得来
    private String city;
    private String county;
    private String town;
    private String village;
}