package com.eventjoin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventJoinApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventJoinApplication.class, args);
    }
}
