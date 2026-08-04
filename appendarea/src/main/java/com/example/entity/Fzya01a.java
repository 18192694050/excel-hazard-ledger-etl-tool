package com.example.entity; // 定义包名，该类属于实体层（entity），对应数据库表或业务对象

import java.math.BigDecimal; // 导入高精度小数类，用于精确表示面积数值（避免浮点误差）

/**
 * 主数据表 FXQ_FZYA01A
 * 该实体类对应数据库中的主数据表，用于存储风险区（或类似业务）的主记录信息，
 * 并额外扩展了一个面积字段（从 shp 导入）。
 */
public class Fzya01a { // 类名 Fzya01a，与表名 FXQ_FZYA01A 对应（通常 Java 类名采用驼峰命名，表名用下划线）


    private String fzya01a001; // 私有字符串类型，对应数据库列 FZYA01A001


    private String fzya01a002; // 对应数据库列 FZYA01A002


    private String fzya01a003; // 对应数据库列 FZYA01A003

    private String fzya01a004; // 对应数据库列 FZYA01A004

    private BigDecimal area; // 面积值，可存小数（如 123.45）

    // ========== 以下是所有字段的 getter 和 setter 方法 ==========
   //映射的是FXQ_FZYA01ANew这个表中的每一列

    public String getFzya01a001() {
        return fzya01a001; // 返回当前对象的 fzya01a001 属性值
    }

    //获得可以更改FXQ_FZYA01ANew表中第一列数据的权限
    public void setFzya01a001(String fzya01a001) {
        this.fzya01a001 = fzya01a001; // 将参数赋值给当前对象的 fzya01a001 属性
    }


    public String getFzya01a002() {
        return fzya01a002; // 返回当前对象的 fzya01a002 属性值
    }


    public void setFzya01a002(String fzya01a002) {
        this.fzya01a002 = fzya01a002; // 将参数赋值给当前对象的 fzya01a002 属性
    }


    public String getFzya01a003() {
        return fzya01a003; // 返回当前对象的 fzya01a003 属性值
    }


    public void setFzya01a003(String fzya01a003) {
        this.fzya01a003 = fzya01a003; // 将参数赋值给当前对象的 fzya01a003 属性
    }


    public String getFzya01a004() {
        return fzya01a004; // 返回当前对象的 fzya01a004 属性值
    }


    public void setFzya01a004(String fzya01a004) {
        this.fzya01a004 = fzya01a004; // 将参数赋值给当前对象的 fzya01a004 属性
    }

    public BigDecimal getArea() {
        return area; // 返回当前对象的 area 属性值
    }


    public void setArea(BigDecimal area) {
        this.area = area; // 将参数赋值给当前对象的 area 属性
    }
}