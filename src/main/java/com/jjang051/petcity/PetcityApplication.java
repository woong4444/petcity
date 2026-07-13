package com.jjang051.petcity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetcityApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetcityApplication.class, args);
    }

}
