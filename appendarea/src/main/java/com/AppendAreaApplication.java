package com;  // 定义包名为 com，这是当前类的包路径

// 导入 MyBatis-Plus 或 MyBatis 的 Spring 注解，用于指定 Mapper 接口的扫描包路径
import org.mybatis.spring.annotation.MapperScan;
// 导入 Spring Boot 的核心启动类，包含运行 Spring 应用的方法
import org.springframework.boot.SpringApplication;
// 导入 Spring Boot 自动配置注解，组合了 @Configuration、@EnableAutoConfiguration、@ComponentScan
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用程序主启动类
 * 该类是整个应用的入口，负责启动内嵌的 Web 容器并初始化 Spring 上下文
 */
@SpringBootApplication  // 标注为 Spring Boot 应用，启用自动配置和组件扫描（默认扫描当前包及子包）
@MapperScan("com.example.mapper")  // 指定 MyBatis Mapper 接口所在的包路径，让 Spring 自动生成代理实现并注入
public class AppendAreaApplication {  // 定义公共类 AppendAreaApplication

    /**
     * 程序主方法，JVM 调用的入口
     * @param args 命令行传入的参数
     */
    public static void main(String[] args) {
        // 调用 SpringApplication 的静态 run 方法，启动 Spring Boot 应用
        // 参数：当前类的 Class 对象（用于加载配置和组件），以及命令行参数
        SpringApplication.run(AppendAreaApplication.class, args);
    }
}