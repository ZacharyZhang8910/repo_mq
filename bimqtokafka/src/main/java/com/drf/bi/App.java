package com.drf.bi;

import com.drf.bi.config.BusinessEnum;
import com.drf.bi.util.SpringContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@SpringBootApplication(exclude = RabbitAutoConfiguration.class)
public class App {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Business parameters cannot be empty.");
            System.out.println("Usage:bimqtokafka <" + BusinessEnum.getSupportTypeName() + "> <topic>");
            System.exit(1);
        }
        ConfigurableApplicationContext cxt = SpringApplication.run(App.class, args);
        cxt.registerShutdownHook();
    }

    @Bean
    @Lazy(false)
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

}
