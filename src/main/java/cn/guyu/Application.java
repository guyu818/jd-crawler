package cn.guyu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Des 启动类
 * @Author guyu
 * @Date 2020/7/9 23:50
 * @Param 
 * @Return 
 */
@SpringBootApplication
@EnableScheduling  //开启定时任务的注解
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
