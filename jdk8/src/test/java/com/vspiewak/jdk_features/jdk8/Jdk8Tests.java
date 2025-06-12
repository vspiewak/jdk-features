package com.vspiewak.jdk_features.jdk8;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.StampedLock;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Jdk8Tests {

    interface MyInterface {
        default String log(String message) {
            return message;
        }

        static String hello() {
            return "hello";
        }
    }

    @Test
    void canUseDefaultAndStaticMethodsInInterfaces() {
        class MyClass implements MyInterface {
        }
        MyClass myClass = new MyClass();
        String log = myClass.log("hello");
        String hello = MyInterface.hello();
        assertThat(log).isEqualTo("hello");
        assertThat(hello).isEqualTo("hello");
    }

    @Test
    void canUseOptional() {

        assertThat(Optional.empty()).isEmpty();
        assertThat(Optional.ofNullable(null)).isEmpty();
        assertThat(Optional.of("foo")).isPresent().get().isEqualTo("foo");

    }

    @Test
    void canAvoidNpeWithOptional() {

        class Address {
            String street;
        }

        class User {
            Address address;
        }

        assertThat(
                Optional
                        .of(new User())
                        .map(u -> u.address)
                        .map(a -> a.street)
                        .orElse("default")
        ).isEqualTo("default");

    }

    @Test
    void canUseFunction() {

        Function<Integer, Integer> timeTwo = i -> i * 2;
        Function<Integer, Integer> plusThree = i -> i + 3;

        assertThat(timeTwo.apply(2)).isEqualTo(4);
        assertThat(timeTwo.andThen(plusThree).apply(3)).isEqualTo(9);
        assertThat(plusThree.andThen(timeTwo).apply(3)).isEqualTo(12);
        assertThat(timeTwo.compose(plusThree).apply(3)).isEqualTo(12);

    }

    @Test
    void canUseUnaryOperator() {

        UnaryOperator<Integer> timeTwo = i -> i * 2;
        UnaryOperator<Integer> plusThree = i -> i + 3;

        assertThat(timeTwo.apply(2)).isEqualTo(4);
        assertThat(timeTwo.andThen(plusThree).apply(3)).isEqualTo(9);
        assertThat(plusThree.andThen(timeTwo).apply(3)).isEqualTo(12);
        assertThat(timeTwo.compose(plusThree).apply(3)).isEqualTo(12);

    }

    @Test
    void canUseSupplier() {
        Supplier<double[]> randInts = () -> new Random().doubles(2).toArray();

        double[] first = randInts.get();
        double[] second = randInts.get();
        assertThat(first.length).isEqualTo(second.length).isEqualTo(2);
        assertThat(first).doesNotContain(second);
    }

    @Test
    void canUsePredicateInFilter() {
        Predicate<String> notEmpty = s -> s != null && !s.isEmpty();
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<String> startsWithAOrEmpty = startsWithA.or(notEmpty.negate());

        assertThat(Stream.of("Alice", "", "Bob").filter(startsWithAOrEmpty)).contains("Alice", "");
    }

    @Test
    void canUseConsumerInForEach() {
        Consumer<String> log = s -> System.out.println("log: " + s);
        Consumer<String> save = s -> System.out.println("save: " + s);
        Stream.of("a", "b").forEach(log.andThen(save));
    }

    @Test
    void canUseABiFunction() {
        BiFunction<Integer, Integer, Integer> multiply = (i, j) -> i * j;
        assertThat(multiply.apply(2, 3)).isEqualTo(6);
    }

    @Test
    void canUseBiPredicateInFilter() {

        BiPredicate<String, Integer> lowScore = (name, score) -> name.startsWith("A") || score < 70;

        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 90);
        scores.put("Bob", 10);

        scores.entrySet().removeIf(entry -> lowScore.test(entry.getKey(), entry.getValue()));

        assertThat(scores).isEmpty();
    }

    @Test
    void canUseBiConsumerInForEach() {
        BiConsumer<String, Integer> log = (name, score) -> System.out.println(name + " scored " + score);
        log.accept("Alice", 90);
    }

    @Test
    void canUseStreamAndLambda() {
        List<String> actual = Stream
                .of("Vincent", "Jack", "Bob", "Alice")
                .filter(s -> s.startsWith("V"))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        assertThat(actual).containsExactly("vincent");
    }

    @Test
    void canUseStreamGenerate() {
        assertThat(Stream.generate(UUID::randomUUID).limit(2)).hasSize(2);
    }

    @Test
    void canUseStreamIterate() {
        assertThat(Stream.iterate(0, i -> i + 1).limit(3)).containsAll(Arrays.asList(0, 1, 2));
    }

    @Test
    void canUseRangeOnIntStream() {
        assertThat(
                IntStream
                        .range(0, 10)
                        .asLongStream()
                        .asDoubleStream()
                        .reduce(Double::sum))
                .isPresent()
                .hasValue(45);
    }

    @Test
    void canUseCollectorsJoining() {
        String actual = Stream.of("a", "b", "c").collect(Collectors.joining("|"));
        assertThat(actual).isEqualTo("a|b|c");
    }

    @Test
    void canUseCollectorsGroupingBy() {
        Map<Integer, List<String>> byLen = Stream.of("a", "bc").collect(Collectors.groupingBy(String::length));
        assertThat(byLen.get(1)).containsExactly("a");
        assertThat(byLen.get(2)).containsExactly("bc");
    }

    @Test
    void canUseReduceWithAccumulatorInStream() {
        Optional<Integer> actual = Stream.of(1, 2, 3).reduce(Integer::max);
        assertThat(actual).isPresent().hasValue(3);

    }

    @Test
    void canUseReduceWithAccumulatorAndIdentityInStream() {
        Integer actual = Stream.of(1, 2, 3).reduce(0, Integer::sum);
        assertThat(actual).isEqualTo(6);
    }

    @Test
    void canUseReduceWithAccumulatorAndIdentityAndCombinerInStream() {
        Integer actual = Arrays
                .asList("a", "bc")
                .parallelStream()
                .reduce(0, (acc, s) -> acc + s.length(), Integer::sum);

        assertThat(actual).isEqualTo(3);
    }

    @Test
    void canUseMapEnhancements() {
        Map<String, Integer> map = new HashMap<>();
        map.computeIfAbsent("k", k -> 1);
        map.merge("k", 2, Integer::sum);
        assertThat(map.get("k")).isEqualTo(3);
    }

    @Test
    void canUseCompletableFuture() throws Exception {
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "done");
        assertThat(f.get(1, TimeUnit.SECONDS)).isEqualTo("done");
    }

    @Test
    void canUseStampedLock() {
        StampedLock lock = new StampedLock();
        long stamp = lock.tryOptimisticRead();
        assertThat(lock.validate(stamp)).isTrue();
        lock.writeLock();
        assertThat(lock.validate(stamp)).isFalse();
    }

    @Test
    void canUseUnsignedArithmetic() {
        int x = -1;
        long ux = Integer.toUnsignedLong(x);
        assertThat(ux).isNotNegative().isEqualTo(0xFFFFFFFFL);
    }

    @Test
    void canUseJavaDotTime() {
        LocalTime time = LocalTime.parse("14:30");
        LocalDate date = LocalDate.of(1984, 8, 2);
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(date, time, ZoneId.of("UTC"));

        assertThat(time.getHour()).isEqualTo(14);
        assertThat(date.getYear()).isEqualTo(1984);
        assertThat(dateTime.toLocalTime()).isEqualTo(time);
        assertThat(dateTime.toLocalDate()).isEqualTo(date);
        assertThat(zonedDateTime.getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void canUseBase64() {
        String original = "test";
        String encoded = Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        assertThat(original).isEqualTo(decoded);
    }

    @Test
    void canUseLongAdder() {

        LongAdder counter = new LongAdder();
        int numberOfThreads = 4;
        int numberOfIncrements = 100;

        try (ExecutorService executorService = Executors.newFixedThreadPool(8)) {

            Runnable incrementAction = () -> IntStream
                    .range(0, numberOfIncrements)
                    .forEach(i -> counter.increment());

            for (int i = 0; i < numberOfThreads; i++) {
                executorService.execute(incrementAction);
            }
        }

        assertThat(counter.sum()).isEqualTo(numberOfIncrements * numberOfThreads);

    }

    @Test
    void canUseLongAccumulator() {

        LongAccumulator accumulator = new LongAccumulator(Long::sum, 0);
        int numberOfThreads = 4;
        int numberOfIncrements = 100;

        try (ExecutorService executorService = Executors.newFixedThreadPool(8)) {

            Runnable incrementAction = () -> IntStream
                    .range(0, numberOfIncrements)
                    .forEach(accumulator::accumulate);

            for (int i = 0; i < numberOfThreads; i++) {
                executorService.execute(incrementAction);
            }
        }

        assertThat(accumulator.get()).isEqualTo(19_800);

    }

}
