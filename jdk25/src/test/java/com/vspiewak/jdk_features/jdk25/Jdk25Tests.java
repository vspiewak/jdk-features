package com.vspiewak.jdk_features.jdk25;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class Jdk25Tests {

    // JEP 456: Anonymous variables & patterns
    @Test
    void canUseAnonymousVariablesAndPatterns() {

        var count = 0;
        var elts = List.of("a", "b", "c");

        for (var _ : elts) {
            count++;
        }

        assertThat(count).isEqualTo(elts.size());
    }

    // JEP 471: Deprecate sun.misc.Unsafe Memory-Access Methods
    @Test
    void canUseDeprecateUnsafeMemoryAccessMethods() throws Exception {
        Unsafe unsafe = getUnsafeInstance();
        Method m = Unsafe.class.getDeclaredMethod("getInt", Object.class, long.class);
        Deprecated dep = m.getAnnotation(Deprecated.class);
        assertThat(dep).isNotNull();
        assertThat(dep.forRemoval()).isTrue();
    }

    private Unsafe getUnsafeInstance() throws Exception {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    }

    // JEP 485: Stream Gatherers
    @Test
    void canUseStreamGatherers() {

        // fixed window
        var s1 = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowFixed(3))
                .toList();

        assertThat(s1.get(0)).isEqualTo(List.of(1, 2, 3));
        assertThat(s1.get(1)).isEqualTo(List.of(4, 5));

        // window sliding
        var s2 = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowSliding(3))
                .toList();

        assertThat(s2.get(0)).isEqualTo(List.of(1, 2, 3));
        assertThat(s2.get(1)).isEqualTo(List.of(2, 3, 4));
        assertThat(s2.get(2)).isEqualTo(List.of(3, 4, 5));

        // fold
        var s3 = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.fold(
                        () -> 0,
                        (acc, n) -> acc + n
                )).toList();

        assertThat(s3).isEqualTo(List.of(15));

    }


    // JEP 486: Permanently Disable the Security Manager
    @Test
    void canUsePermanentlyDisableSecurityManager() {
        assertThat(System.getSecurityManager()).isNull();
    }

}