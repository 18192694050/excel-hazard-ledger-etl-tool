package com.example.service;

import com.example.vo.ImportSummaryVO;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

public interface AreaImportService {
    @Transactional(rollbackFor = Exception.class)
    String mergeAreaToTarget(String shpFilePath, String targetFilePath) throws IOException;

    // 原有面积入库接口
    ImportSummaryVO mergeAreaToDatabase(String shpFilePath, String targetFilePath) throws IOException;

    // 新增：云申监测点 → 三维模型Excel转换接口
    String convertMonitorTo3dExcel(String sourceExcelPath, String template3dPath) throws IOException;
}