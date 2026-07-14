package com.jjang051.petcity.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "!1qaz2wsx";

        String hash = encoder.encode(password);

        System.out.println(hash);

        System.out.println(
                encoder.matches(password, hash)
        );

    }

}