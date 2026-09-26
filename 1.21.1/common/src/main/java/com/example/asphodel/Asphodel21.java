package com.example.asphodel;

public class Asphodel21 {
    // asphodel::exclude
    private static final int EXAMPLE = 0;

    public static void setup21() {
        System.out.println("CUSTOM CODE FOR 1.21.1 RAN");
        System.out.println("Constant 'example' remains: " + EXAMPLE + " and was not merged into source, as a demonstration of asphodel::exclude");
    }
}