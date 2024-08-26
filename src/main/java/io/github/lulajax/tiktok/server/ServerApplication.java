package io.github.lulajax.tiktok.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class ServerApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(ServerApplication.class, args);

        ConfigurableEnvironment env = context.getEnvironment();
        String url = env.getProperty("index.url");
//        openBrowser(url);
    }

    private static void openBrowser(String url) {
        log.info("--------------------------------------------------------------------------------");
        log.info("- PLEASE OPEN YOUR BROWSER TO ACCESS：" + url +"               -");
        log.info("- PLEASE OPEN YOUR BROWSER TO ACCESS：" + url +"               -");
        log.info("- PLEASE OPEN YOUR BROWSER TO ACCESS：" + url +"               -");
        log.info("--------------------------------------------------------------------------------");
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else {
                System.out.println("无法识别的操作系统，无法自动打开浏览器。");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
