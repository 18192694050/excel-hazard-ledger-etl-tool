//声明当前类所在包，这个接口放在com.cxample.service包下
package com.example.service;

//导入返回结果封装类，ImportSummaryVO是导入之后的汇总结果对象，存成功数，失败数，错误信息
import com.example.vo.ImportSummaryVO;
//Spring框架提供的事务注解，后面也会用到Transactional
import org.springframework.transaction.annotation.Transactional;
//IO异常，文件读写的时候抛出异常，文件找不到，读取出错都会报这个异常
import java.io.IOException;

/**
*面积/监测点导入业务接口
*只定义方法签名，**不写具体业务代码**，真正逻辑写在impl实现类
*/
public interface AreaImportService {
    /**
    注意：普通//单行注释不会生成文档提示，只有/***/的javadoc才生效
    
    @param，@return,@throws都是java文档注释
    //这三个一定要按照顺序写，先进来后出去最后报错
    @param: 讲传入进来什么
    @return: 讲返回出去什么
    @throws：讲会抛出什么异常

    
    @Transactional 事务注解
    @param shpFilepath shp文件路径
    @param targetFilePath 目标输出文件路径
    @return String 返回处理结果消息
    @throws IO Exception 文件读写异常，调用方必须处理这个异常
    */

    //rollbackFor = Exception.class 代表只要抛出任何Exception（包含检查异常）全部回滚
    //避免导入一半出错，数据库残留一半数据
    @Transactional(rollbackFor = Exception.class)
    String mergeAreaToTarget(String shpFilePath, String targetFilePath) throws IOException;

    // 原有面积入库接口
    //ImportSummaryVO:VO是视图对象，专门用来返回给上层controller，存放导入之后的统计信息（成功多少条，失败多少条，错误原因）
    ImportSummaryVO mergeAreaToDatabase(String shpFilePath, String targetFilePath) throws IOException;

    // 新增：云申监测点 → 三维模型Excel转换接口
    String convertMonitorTo3dExcel(String sourceExcelPath, String template3dPath) throws IOException;
}
