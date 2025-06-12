package com.vspiewak.jdk_features.jdk25;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

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
    //FIXME


    // JEP 486: Permanently Disable the Security Manager
    @Test
    void canUsePermanentlyDisableSecurityManager() {
        assertThat(System.getSecurityManager()).isNull();
    }

}