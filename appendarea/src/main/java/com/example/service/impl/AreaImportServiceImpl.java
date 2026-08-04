package com.example.service.impl;

import com.example.entity.Fzya01a;
import com.example.mapper.Fzya01aMapper;
import com.example.vo.ImportSummaryVO;
import org.springframework.transaction.annotation.Transactional;
import com.example.service.AreaImportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 业务服务实现类：从shp生成的Excel中读取各sheet的防控编号和面积，
 * 然后根据编号匹配到目标台账Excel中，回填匹配面积，并同步更新数据库。
 */
@Service
public class AreaImportServiceImpl implements AreaImportService {
    // 日志对象，用于输出运行信息
    private static final Logger log = LoggerFactory.getLogger(AreaImportServiceImpl.class);
    // 预编译正则：清洗空白、全角空格、零宽空格、换行、制表、下划线、全角破折号等特殊干扰字符
    private static final Pattern CLEAN_PATTERN = Pattern.compile("[\\s\\u3000\\u200b\\r\\n\\t\\v＿－]+");

    // Spring自动注入Mapper，无需手动new对象，用于执行数据库面积更新操作
    @Autowired
    private Fzya01aMapper fzya01aMapper;

    /**
     * 实现接口方法：将shp文件（实为Excel）中的面积数据合并到目标台账文件
     * @param shpFilePath   源Excel文件路径（shp矢量文件导出的分区县Excel）
     * @param targetFilePath 目标台账Excel文件路径（业务原始台账表）
     * @return 输出文件路径，文件处理失败返回null
     * @throws IOException 文件读写IO异常向上抛出
     */
    @Transactional(rollbackFor = Exception.class) // 任意异常全部触发事务回滚，保证Excel写入与数据库更新原子一致性
    @Override
    public String mergeAreaToTarget(String shpFilePath, String targetFilePath) throws IOException {
        // 读取全部区县sheet，构建【防控编号->面积】键值映射缓存
        Map<String, BigDecimal> codeAreaMap = readAllSheetSkipChangYang(shpFilePath);
        // 校验读取结果，无有效数据直接终止流程
        if (codeAreaMap.isEmpty()) {
            log.error("shp文件未读取到任何有效防控编号数据");
            return null;
        }
        // 打印源文件有效数据总量，用于业务对账
        log.info("shp读取完成，有效防控编号共：{} 条", codeAreaMap.size());

        // 执行台账匹配、面积回填、数据库更新、生成新台账文件
        String resultFile = writeTargetByCodeMatch(targetFilePath, codeAreaMap);
        log.info("匹配完成，输出文件：{}", resultFile);
        return resultFile;
    }

    /**
     * 按sheet名称读取对应固定列防控编号，【不再跳过长阳，长阳同步读取】
     * @param filePath Excel源文件路径
     * @return 防控编号-面积映射集合
     * @throws IOException 文件读写异常
     */
    private Map<String, BigDecimal> readAllSheetSkipChangYang(String filePath) throws IOException {
        Map<String, BigDecimal> codeAreaMap = new HashMap<>();
        // try-with-resources语法：自动关闭文件输入流与Workbook对象，避免文件句柄泄露
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // 循环遍历Excel内所有工作表（按区县划分sheet）
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName().trim();

                log.info("开始读取工作表【{}】", sheetName);

                // 获取当前sheet第一行作为表头行
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    log.warn("{} 无表头，跳过", sheetName);
                    continue;
                }

                // 根据区县sheet名称，硬编码防控编号所在列下标（Excel列从0开始计数：A=0、B=1、D=3、F=5、I=8）
                int codeCol = switch (sheetName) {
                    case "兴山" -> 3;    // D列
                    case "夷陵" -> 5;    // F列
                    case "秭归" -> 8;    // I列
                    case "五峰" -> 1;    // B列
                    case "长阳" -> 3;    // 新增长阳区县，防控编号位于D列
                    default -> -1;       // 非业务区县sheet直接跳过
                };
                if (codeCol == -1) {
                    log.warn("未知工作表{}，无对应防控编号列配置，跳过", sheetName);
                    continue;
                }

                // 自动识别面积字段列，兼容多种表头命名格式
                int areaCol = findAreaColumn(headerRow);
                if (areaCol == -1) {
                    log.warn("{} 未找到面积列，跳过该表", sheetName);
                    continue;
                }
                log.info("{} 列定位：防控编号第{}列，面积第{}列", sheetName, codeCol, areaCol);

                // 处理Excel合并单元格，将合并区域空白单元格填充有效值，避免读取为空造成匹配失败
                fillMergedCells(sheet);

                int validCount = 0;
                // 从第二行开始遍历业务数据（跳过表头）
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue; // 空行直接跳过

                    // 读取原始防控编号，执行字符清洗+大写统一，消除格式差异
                    String rawCode = getCellText(row.getCell(codeCol));
                    String cleanCode = CLEAN_PATTERN.matcher(rawCode).replaceAll("").toUpperCase();
                    //正则 CLEAN_PATTERN 会删除：
                    //普通空格 、全角空格 　、零宽不可见空格 \u200b（复制粘贴自带隐形字符）
                    //换行 \r\n、制表符 \t、垂直制表符
                    //全角下划线、全角破折号等异形符号
                    //toUpperCase() 统一字母大小写，避免大小写不一致匹配失效
                    if (cleanCode.isBlank()) continue;//清洗后编号为空，直接跳过该行

                    // 读取原始面积文本，只保留数字与小数点，剔除单位、备注文字
                    String rawArea = getCellText(row.getCell(areaCol));
                    String numStr = CLEAN_PATTERN.matcher(rawArea).replaceAll("").replaceAll("[^0-9.]", "");
                    BigDecimal areaVal = BigDecimal.ZERO; // 默认面积0
                    try {
                        if (!numStr.isBlank()) {
                            areaVal = new BigDecimal(numStr); // BigDecimal存储避免浮点精度丢失
                        }
                    } catch (Exception e) {
                        log.warn("{} 第{}行 面积解析失败", sheetName, i); // 脏数据解析异常只告警，不中断整体任务
                    }

                    // 编号与面积存入Map缓存
                    codeAreaMap.put(cleanCode, areaVal);
                    validCount++;
                }
                log.info("{} 读取完成，有效数据 {} 条", sheetName, validCount);
            }
        }
        return codeAreaMap;
    }

    /**
     * 模糊查找面积列：兼容 面积 / MJ / 面积km2 三种表头名称
     * @param headerRow 表头行对象
     * @return 面积列下标，未找到返回-1
     */
    private int findAreaColumn(Row headerRow) {
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            String val = getCellText(headerRow.getCell(c)).trim();
            if (val.equals("面积") || val.equalsIgnoreCase("MJ") || val.contains("面积km")) {
                return c;
            }
        }
        return -1;
    }

    /**
     * 填充所有合并单元格：把合并区域左上角单元格的值，填充到合并区域内所有空白单元格
     * 解决POI读取合并单元格时，非左上角格子为空值的问题
     * @param sheet 当前工作表
     */
    private void fillMergedCells(Sheet sheet) {
        // 遍历当前sheet全部合并单元格区域
        for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
            int firstRow = mergedRegion.getFirstRow();
            int lastRow = mergedRegion.getLastRow();
            int firstCol = mergedRegion.getFirstColumn();
            int lastCol = mergedRegion.getLastColumn();

            Row firstRowObj = sheet.getRow(firstRow);
            if (firstRowObj == null) continue;
            // 获取合并区域左上角单元格的真实值
            String mergedValue = getCellText(firstRowObj.getCell(firstCol));

            // 双层循环填充整个合并区域
            for (int r = firstRow; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) row = sheet.createRow(r); // 空行直接创建行对象
                for (int col = firstCol; col <= lastCol; col++) {
                    Cell cell = row.getCell(col);
                    if (cell == null) cell = row.createCell(col); // 空单元格创建单元格
                    // 空白单元格赋值为合并主值
                    if (getCellText(cell).isBlank()) {
                        cell.setCellValue(mergedValue);
                    }
                }
            }
        }
    }

    /**
     * 读取目标台账，根据防控编号匹配面积回填Excel，生成新台账文件，同步更新数据库面积字段
     * @param targetPath 原始台账文件路径
     * @param codeMap 源文件编号-面积缓存Map
     * @return 生成的新台账文件绝对路径
     * @throws IOException 文件读写异常
     */
    private String writeTargetByCodeMatch(String targetPath, Map<String, BigDecimal> codeMap) throws IOException {
        // 使用时间戳命名输出文件，防止旧文件被覆盖
        String outFileName = "精准防控编号匹配结果_" + System.currentTimeMillis() + ".xlsx";
        // 固定输出路径：桌面指定文件夹（部署服务器需改为配置文件路径）
        String outPath = "C:\\Users\\89435\\Desktop\\666\\" + outFileName;

        // 自动关闭台账输入流、工作簿、文件输出流
        try (FileInputStream fis = new FileInputStream(targetPath);
             Workbook wb = WorkbookFactory.create(fis);
             FileOutputStream fos = new FileOutputStream(outPath)) {

            Sheet sheet = wb.getSheetAt(0); // 台账数据在第一个工作表
            Row header = sheet.getRow(0);
            // 定位台账主键列：FZYA01A001（防控编号数据库字段名）
            int codeCol = findColumn(header, "FZYA01A001");
            if (codeCol == -1) {
                log.error("目标台账未找到 FZYA01A001 编号列");
                return null;
            }

            // 在表头最右侧新增【匹配面积】列，用于存放匹配后的面积数据
            int newAreaCol = header.getLastCellNum();
            header.createCell(newAreaCol).setCellValue("匹配面积");

            int totalRow = sheet.getLastRowNum(); // 台账总行数
            int matchCount = 0; // 成功匹配计数

            // 逐行遍历台账业务数据
            for (int i = 1; i <= totalRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) row = sheet.createRow(i);
                Cell areaCell = row.createCell(newAreaCol); // 创建面积写入单元格

                // 台账编号执行和源文件一致的清洗逻辑，保证编号格式统一
                String rawCode = getCellText(row.getCell(codeCol));
                String cleanCode = CLEAN_PATTERN.matcher(rawCode).replaceAll("").toUpperCase();

                // 空编号直接填充面积0
                if (cleanCode.isBlank()) {
                    areaCell.setCellValue(0D);
                    continue;
                }

                // 根据清洗后的编号查询对应面积
                BigDecimal matchArea = codeMap.get(cleanCode);
                if (matchArea != null) {
                    areaCell.setCellValue(matchArea.doubleValue()); // Excel写入面积
                    matchCount++;
                    // ============数据库更新核心逻辑：按防控编号更新台账面积字段============
                    Fzya01a updateEntity = new Fzya01a();
                    updateEntity.setFzya01a001(cleanCode); // 设置主键防控编号
                    updateEntity.setArea(matchArea); // 设置更新面积
                    // 调用MyBatis Mapper执行单条更新SQL
                    int updateRow = fzya01aMapper.updateAreaByFkbh(updateEntity);
                    log.info("编号{} 数据库更新成功，影响行数：{}", cleanCode, updateRow);
                } else {
                    // 无匹配数据，面积填充0
                    areaCell.setCellValue(0D);
                }
            }

            // 打印整体匹配统计结果
            log.info("最终匹配统计：总行{}，成功{}条，匹配率{}%",
                    totalRow, matchCount, String.format("%.2f", matchCount * 100.0 / totalRow));
            // 将修改完成的工作簿写入到输出文件
            wb.write(fos);
        }
        return outPath;
    }

    /**
     * 精确匹配表头列名（忽略大小写），用于定位主键列
     * @param headerRow 表头行
     * @param targetName 目标列名称
     * @return 匹配列下标，无匹配返回-1
     */
    private int findColumn(Row headerRow, String targetName) {
        if (headerRow == null) return -1;
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            String val = getCellText(cell).trim();
            // 忽略大小写匹配列名
            if (val.equalsIgnoreCase(targetName)) {
                return c;
            }
        }
        return -1;
    }

    /**
     * 通用单元格读取工具，兼容文本、数字、公式类型单元格，统一返回字符串
     * 解决POI不同单元格类型取值方式不一致、公式单元格直接取值拿到公式文本的问题
     * @param cell Excel单元格对象
     * @return 单元格文本值
     */
    private String getCellText(Cell cell) {
        if (cell == null) return "";
        CellType cellType = cell.getCellType();
        // 如果是公式单元格，获取公式计算后的实际结果类型
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }
        // 根据单元格类型取值并转为字符串
        return switch (cellType) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> ""; // 空白、布尔、错误类型统一返回空字符串
        };
    }

    /**
     * 接口预留实现：结构化返回导入汇总VO对象，当前未实现
     */
    @Override
    public ImportSummaryVO mergeAreaToDatabase(String shpFilePath, String targetFilePath) throws IOException {
        return null;
    }

    /**
     * 接口预留实现：监测点数据转为三维台账Excel，预留扩展，当前空实现
     */
    @Override
    public String convertMonitorTo3dExcel(String sourceExcelPath, String template3dPath) throws IOException {
        return "";
    }
}
