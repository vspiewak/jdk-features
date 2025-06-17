package com.vspiewak.jdk_features.jdk17;

import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class Jdk17Tests {

    // Java 12: Compact Number Formatting
    @Test
    void canUseCompactNumberFormatting() {
        NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        assertThat(fmt.format(1_200_000)).isEqualTo("1M");
    }

    // Java 12: Unicode 11.0 support (e.g., checking a supplementary character)
    @Test
    void canUseUnicode11() {
        String reiwa = "\uD83C\uDFF4"; // Just testing that supplementary surrogate handling works
        assertThat(reiwa.codePointCount(0, reiwa.length())).isEqualTo(1);
    }

    // Java 14: Switch Expressions
    @Test
    void canUseSwitchExpression() {
        int month = 4;
        String quarter = switch (month) {
            case 1, 2, 3 -> "Q1";
            case 4, 5, 6 -> {
                yield "Q2";
            }
            case 7, 8, 9 -> "Q3";
            case 10, 11, 12 -> "Q4";
            default -> throw new IllegalArgumentException("Invalid month: " + month);
        };
        assertThat(quarter).isEqualTo("Q2");
    }

    // Java 15: Text Blocks
    @Test
    void canUseTextBlocks() {
        String json = """
                {
                  "name": "Alice",
                  "age": 30
                }
                """;
        assertThat(json).contains("\"name\": \"Alice\"");
    }

    // Java 16: Pattern Matching for instanceof
    @Test
    void canUsePatternMatchingInstanceof() {
        Object obj = "Hello World";
        if (obj instanceof String s) {
            assertThat(s).startsWith("Hello");
        } else {
            fail("Object was not a String");
        }
    }

    // Java 16: Records
    @Test
    void canUseRecords() {
        record Person(String name, int age) {
        }

        Person p1 = new Person("Bob", 25);
        Person p2 = new Person("Bob", 25);

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.name()).isEqualTo("Bob");
        assertThat(p1.age()).isEqualTo(25);
    }

    // Java 17: Sealed Classes
    @Test
    void canUseSealedClasses() {
        Vehicle car = new ElectricCar("Tesla");
        assertThat(car).isInstanceOf(ElectricCar.class);
        assertThat(car.name()).isEqualTo("Tesla");
    }

    // Java 17: Enhanced Pseudo-Random Number Generators
    @Test
    void canUseEnhancedPRNG() {
        RandomGenerator rng = RandomGeneratorFactory.of("L128X1024MixRandom").create();
        int val = rng.nextInt(100);
        assertThat(val).isBetween(0, 99);
    }

    sealed interface Vehicle permits ElectricCar {
        default String name() {
            return "Vehicle";
        }
    }

    final class ElectricCar implements Vehicle {
        private final String name;

        ElectricCar(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }


}