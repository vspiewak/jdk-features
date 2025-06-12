package com.vspiewak.jdk_features.jdk10;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Jdk10Tests {

    @Test
    void canUseVar() {
        var l = List.of(1, 2, 3);
        assertThat(l).containsExactly(1, 2, 3);
    }

    @Test
    void canUseOptionalOrElseThrow() {
        Optional<String> o = Optional.of("x");
        assertThat(o.orElseThrow()).isEqualTo("x");
    }

    @Test
    void canUseUnmodifiableList() {
        var mlist = new ArrayList<>(List.of("a", "b", "c"));
        var ul = List.copyOf(mlist);
        var us = Stream.of(mlist).collect(Collectors.toUnmodifiableList());
        assertThat(ul).isUnmodifiable();
        assertThat(us).isUnmodifiable();
    }

}
