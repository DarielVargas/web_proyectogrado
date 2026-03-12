package com.dv.agro_web.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String dariel = "Dariel1234";
        String carlos = "Carlos1234";

        String hashDariel = encoder.encode(dariel);
        String hashCarlos = encoder.encode(carlos);

        System.out.println("Dariel1234 => " + hashDariel);
        System.out.println("Carlos1234 => " + hashCarlos);
    }
}
