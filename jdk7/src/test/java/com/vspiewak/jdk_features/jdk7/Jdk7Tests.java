package com.vspiewak.jdk_features.jdk7;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;

class Jdk7Tests {


    @Test
    void canUseDiamondOperator() {

        List<String> expected = List.of("a", "b", "c");

        ArrayList<String> actual = new ArrayList<>(expected);

        assertThat(actual)
                .hasSize(3)
                .containsExactlyElementsOf(expected);

    }

    @Test
    void canUseUnderscoreInNumericLiterals() {
        assertThat(1_000).isEqualTo(1000L);
    }

    @Test
    void canUseBinaryLiteralsWith0bPrefix() {
        assertThat(0b1010).isEqualTo(10L);
        assertThat(0B1010).isEqualTo(10L);
        assertThat(0b1100_0101).isEqualTo(197L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"one", "two"})
    void canUseStringsInSwitch(String input) {

        switch (input) {
            case "one": {
                assertThat(input).isEqualTo("one");
                break;
            }
            case "two": {
                assertThat(input).isEqualTo("two");
            }
        }
    }

    @Test
    void canUseMultipleExceptionsInACatchBlock() {
        try {

            long actualLong = Long.parseLong("1");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            Date actualDate = sdf.parse("02/08/1984");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(actualDate);

            assertThat(actualLong).isEqualTo(1L);
            assertThat(calendar.get(Calendar.YEAR)).isEqualTo(1984);

        } catch (NumberFormatException | ParseException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void canUseTryWithResources() {
        try (BufferedReader r = new BufferedReader(new FileReader("pom.xml"))) {
            assertThat(r.lines()).contains("</project>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void canUseTryWithResourcesIfImplementAutoCloseable() {

        MyResource after = null;

        try (MyResource r = new MyResource()) {
            after = r;
            assertThat(r.closed).isFalse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(after.closed).isTrue();

    }

    @Test
    void canUseForkJoinWithRecursiveAction() throws InterruptedException {
        MyRecursiveAction myRecursiveAction = new MyRecursiveAction(List.of("a", "b", "c", "d"));
        try (ForkJoinPool forkJoinPool = new ForkJoinPool(2)) {
            forkJoinPool.invoke(myRecursiveAction);
        }
        assertThat(MyRecursiveAction.threads).hasSize(2);
        assertThat(MyRecursiveAction.count).isEqualTo(4);
    }

    @Test
    void canUseForkJoinWithRecursiveTask() throws InterruptedException {

        long actual = 0;

        MyRecursiveTask task = new MyRecursiveTask(List.of(1, 2, 3, 4));
        try (ForkJoinPool forkJoinPool = new ForkJoinPool()) {
            actual = forkJoinPool.invoke(task);
        }
        assertThat(actual).isEqualTo(10);
    }

    @Test
    void canUseNIO2() throws IOException {

        // Path
        assertThat(Path.of("pom.xml").toAbsolutePath().toString()).contains("jdk-features");

        // Glob
        assertThat(
                FileSystems
                        .getDefault()
                        .getPathMatcher("glob:**/*.{java,class}")
                        .matches(Path.of("src", "main", "java", "Test.java"))
        ).isTrue();

        // FileSystems
        assertThat(FileSystems.getDefault().getSeparator()).isEqualTo(File.separator);

        // FileStore
        assertThat(FileSystems.getDefault().getFileStores().iterator().next().getUnallocatedSpace()).isGreaterThan(0);

        // Files
        Path dir = Files.createTempDirectory("my-prefix");
        Path file = dir.resolve(".temp.txt");

        // 1. write
        Files.writeString(file, "hello");

        // 2. read
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertThat(lines).contains("hello");

        // 3. existence & attributes
        assertThat(Files.exists(file)).isTrue();
        assertThat(Files.isHidden(file)).isTrue();
        assertThat(Files.isDirectory(dir)).isTrue();

        // 4. copy file
        Path copy = dir.resolve("copy.txt");
        Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
        assertThat(Files.exists(copy)).isTrue();

        // 5. move file
        Path moved = dir.resolve("moved.txt");
        Files.move(copy, moved);
        assertThat(Files.exists(copy)).isFalse();
        assertThat(Files.exists(moved)).isTrue();

        // 6. walking file tree
        final Set<Path> paths = new HashSet<>();
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                paths.add(file);
                return FileVisitResult.CONTINUE;
            }
        });
        assertThat(paths).contains(file, moved);

        // 7. delete files & dir
        Files.deleteIfExists(file);
        Files.deleteIfExists(moved);
        Files.deleteIfExists(dir);
        assertThat(Files.exists(file)).isFalse();

    }

}
