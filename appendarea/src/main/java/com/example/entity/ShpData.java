package com.example.entity; // 定义包名，该类属于实体层（entity），对应数据对象

import java.math.BigDecimal; // 导入高精度小数类，用于精确表示面积

/**
 * shp导出Excel对应实体
 * 该类用于映射从 shp 地理信息数据导出的 Excel 文件中的行数据，
 * 包含防控编号、面积以及所属乡镇、村等信息。
 */
public class ShpData { // 类名 ShpData，表示 shp 数据实体

    /**
     * 防控编号（fkbh，即“防控编号”的拼音缩写）
     * 对应 shp Excel 中的“防控编号”列，作为唯一标识或匹配键
     */
    private String fkbh; // 私有字符串类型，存储防控编号

    /**
     * 面积（area）
     * 对应 shp Excel 中的“面积”列，使用 BigDecimal 保证精度
     */
    private BigDecimal area; // 私有高精度小数，存储面积值

    /**
     * 所属乡镇（town）
     * 对应 shp Excel 中的“乡镇”或“镇”列，记录该地块所在的行政乡镇
     */
    private String town; // 私有字符串，存储乡镇名称

    /**
     * 所属村（village）
     * 对应 shp Excel 中的“村”或“村庄”列，记录该地块所在的行政村
     */
    private String village; // 私有字符串，存储村名称

    // ========== 以下是所有字段的 getter 和 setter 方法 ==========

    /**
     * 获取防控编号
     * @return 防控编号字符串
     */
    public String getFkbh() {
        return fkbh; // 返回当前对象的 fkbh 属性值
    }

    /**
     * 设置防控编号
     * @param fkbh 要设置的防控编号
     */
    public void setFkbh(String fkbh) {
        this.fkbh = fkbh; // 将参数赋值给当前对象的 fkbh 属性
    }

    /**
     * 获取面积
     * @return 面积值（BigDecimal 类型）
     */
    public BigDecimal getArea() {
        return area; // 返回当前对象的 area 属性值
    }

    /**
     * 设置面积
     * @param area 要设置的面积
     */
    public void setArea(BigDecimal area) {
        this.area = area; // 将参数赋值给当前对象的 area 属性
    }

    /**
     * 获取所属乡镇
     * @return 乡镇名称
     */
    public String getTown() {
        return town; // 返回当前对象的 town 属性值
    }

    /**
     * 设置所属乡镇
     * @param town 要设置的乡镇名称
     */
    public void setTown(String town) {
        this.town = town; // 将参数赋值给当前对象的 town 属性
    }

    /**
     * 获取所属村
     * @return 村名称
     */
    public String getVillage() {
        return village; // 返回当前对象的 village 属性值
    }

    /**
     * 设置所属村
     * @param village 要设置的村名称
     */
    public void setVillage(String village) {
        this.village = village; // 将参数赋值给当前对象的 village 属性
    }
}