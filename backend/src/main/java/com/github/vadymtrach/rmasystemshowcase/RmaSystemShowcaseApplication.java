package com.github.vadymtrach.rmasystemshowcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RmaSystemShowcaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RmaSystemShowcaseApplication.class, args);
    }

}
