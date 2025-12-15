package com.mzc.lp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LpApplication {

    public static void main(String[] args) {
        SpringApplication.run(LpApplication.class, args);
    }
}
