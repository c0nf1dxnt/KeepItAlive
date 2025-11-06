package pro.keepitalive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SiteMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiteMonitorApplication.class, args);
    }
}