package com.example.controller;  // 定义包名，表示该类属于控制器层（Controller）

// 导入自定义的面积导入服务接口
import com.example.service.AreaImportService;
// 导入日志相关类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 导入Spring框架的自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// 导入Web层相关注解：@PostMapping、@RequestMapping、@RequestParam、@RestController
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// 导入Spring文件上传支持类 MultipartFile
import org.springframework.web.multipart.MultipartFile;

// 导入Java IO相关的File和IOException
import java.io.File;
import java.io.IOException;

/**
 * 区域面积导入控制器
 * 提供HTTP接口，接收两个Excel文件（源数据文件和目标台账文件），
 * 调用服务层进行面积匹配，并返回处理结果
 */

//问题：为什么一般就是Controller，这里的RestController作用是什么，为什么用RestController
//答：首先弄清@RestController本质是什么是@Controller+@ResponseBody
       //这是一个组合注释同时包含两个功能：
       // @Controller:把当前类交给Spring管理，成为控制器，接受前端HTTP请求；
       //@ ResponseBody:方法返回值直接转为JSON字符串，返回给前端不跳转页面


//答：@RestController
       //适用：前后端分离、纯后端 API 接口（你的面积导入、查询接口都属于这类）
       //特点：所有方法返回值自动序列化 JSON，无页面跳转。
//@Controller
       //适用：传统 SSM/springmvc 项目，服务端渲染页面（返回 html/jsp）
       //特点：默认跳转视图，返回 JSON 需要额外加@ResponseBody

//答：你的项目是后端接口服务（shp/Excel 面积同步、提供 API 给前端调用），属于前后端分离项目：
//前端（网页 / 小程序）通过 http 接口请求后端；
//后端只需要返回 JSON 数据，不需要渲染 JSP/HTML 页面；
//这种场景必须用 @RestController。
//如果只用 @Controller 会出现什么问题？
//只写 @Controller 不加 @ResponseBody：
//方法 return 的对象不会转 JSON，Spring 会把返回值当成页面视图名称去跳转；
//前端拿到的不是数据，而是 404 找不到页面，接口直接报错

// RestController  // 标记为RESTful控制器，该类的所有方法返回JSON或纯文本，而非视图
@RequestMapping("/api")  // 定义该类所有接口的父路径为 /api
public class AreaImportController {

    // 日志对象，用于记录运行信息
    private static final Logger log = LoggerFactory.getLogger(AreaImportController.class);

    // 固定的基础目录，用于存储临时上传的文件（Windows路径）
    private static final String BASE_DIR = "C:\\Users\\89435\\Desktop\\666\\";

    // 自动注入服务层实现，用于执行核心业务逻辑
    @Autowired
    private AreaImportService areaImportService;

    /**
     * 处理 /api/merge 的POST请求，接收两个MultipartFile文件
     * @param shpFile   源数据Excel（包含各sheet的防控编号和面积）
     * @param targetFile 目标台账Excel（需要回填面积）
     * @return 处理结果信息（成功时返回输出文件路径，失败返回错误信息）
     */
    @PostMapping("/merge")  // 映射POST请求到 /api/merge 路径
    public String mergeExcel(
            @RequestParam("shpFile") MultipartFile shpFile,     // 接收名为 shpFile 的上传文件
            @RequestParam("targetFile") MultipartFile targetFile // 接收名为 targetFile 的上传文件
    ) {
        // 1. 自动创建存放临时文件的文件夹（如果不存在）
        File dir = new File(BASE_DIR);  // 根据路径字符串创建File对象
        if (!dir.exists()) {           // 若目录不存在
            dir.mkdirs();              // 创建多级目录（包括所有不存在的父目录）
        }

        // 2. 校验文件后缀，仅支持 .xlsx 格式（根据业务需求）
        String shpName = shpFile.getOriginalFilename();      // 获取原始文件名（包含扩展名）
        String targetName = targetFile.getOriginalFilename();
        // 检查文件名是否以 .xlsx 结尾（不区分大小写？这里没有忽略大小写，要求严格小写）
        if (!shpName.endsWith(".xlsx") || !targetName.endsWith(".xlsx")) {
            return "仅支持 .xlsx Excel 文件，请重新上传";   // 返回错误提示信息
        }

        // 创建两个临时文件对象，用于保存上传的文件内容
        File shpTemp = new File(BASE_DIR + "shp_temp.xlsx");       // 源文件临时存储
        File targetTemp = new File(BASE_DIR + "target_temp.xlsx"); // 目标文件临时存储
        try {
            // 将上传的MultipartFile内容写入临时文件（transferTo方法会处理流关闭）
            shpFile.transferTo(shpTemp);
            targetFile.transferTo(targetTemp);
            // 记录日志，显示临时文件绝对路径
            log.info("临时文件写入完成 shp:{}, target:{}", shpTemp.getAbsolutePath(), targetTemp.getAbsolutePath());

            // 调用服务层的核心方法，传入两个临时文件路径，获取输出结果文件路径
            String resultPath = areaImportService.mergeAreaToTarget(
                    shpTemp.getAbsolutePath(),   // 源文件绝对路径
                    targetTemp.getAbsolutePath() // 目标文件绝对路径
            );

            // 判断结果路径是否有效
            if (resultPath == null || resultPath.isBlank()) {
                return "匹配失败，请查看后端控制台日志排查问题";   // 失败返回提示
            }
            // 成功返回输出文件路径信息
            return "处理完成！文件路径：" + resultPath;
        } catch (IOException e) {
            // 捕获IO异常（文件读写、转移等），记录错误日志，并返回用户友好信息
            log.error("Excel处理IO异常", e);
            return "文件读写失败：" + e.getMessage();
        } finally {
            // 无论成功或异常，最终都要清理临时文件（释放磁盘空间）
            boolean delShp = shpTemp.delete();        // 删除源临时文件
            boolean delTarget = targetTemp.delete();  // 删除目标临时文件
            // 记录清理结果日志
            log.info("临时文件清理：shp={}, target={}", delShp, delTarget);
        }
    }
}