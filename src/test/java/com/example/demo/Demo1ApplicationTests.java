package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class Demo1ApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testAddition() {
        int a = 2;
        int b = 3;
        int expectedSum = 5;
        int actualSum = a + b;
        assertEquals(expectedSum, actualSum);
    }
}
