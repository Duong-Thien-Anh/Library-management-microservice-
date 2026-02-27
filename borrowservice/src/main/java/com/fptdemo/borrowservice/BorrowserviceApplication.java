package com.fptdemo.borrowservice;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BorrowserviceApplication {

    public static void main(String[] args) {

        SpringApplication.run(BorrowserviceApplication.class, args);

    }
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
