package com.vspiewak.jdk_features.jdk11;

import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.net.ssl.SSLContext;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

class Jdk11Tests {

    // JEP 320: Remove Java EE and CORBA modules
    @Test
    void javaEEModulesAreRemoved() {
        assertThat(ModuleLayer.boot().findModule("java.corba")).isEmpty();
        assertThat(ModuleLayer.boot().findModule("java.xml.ws")).isEmpty();
    }

    // JEP 181: Nest-Based Access Control
    @Test
    void canUseNestBasedAccessControl() {
        class Outer {
            private final String secret = "s";

            class Inner {
                String reveal() {
                    return secret;
                }
            }
        }
        assertThat(new Outer().new Inner().reveal()).isEqualTo("s");
    }

    // JEP 323: Local-Variable Syntax for Lambda Parameters
    @Test
    void canUseLocalVarSyntaxInLambda() {
        BiFunction<String, String, String> concat = (var a, var b) -> a + b;
        assertThat(concat.apply("foo", "bar")).isEqualTo("foobar");
    }

    // JEP 321: HTTP Client (Standard)
    @Test
    void canUseHttpClientStandardApi() {
        HttpClient client = HttpClient.newHttpClient();
        assertThat(client).isNotNull();
    }

    // JEP 327: Unicode 10
    @Test
    void canUseUnicode10() {
        int faceWithMonocle = 0x1F9D0; // U+1F9D0 “Face with Monocle” added in Unicode 10
        assertThat(Character.isValidCodePoint(faceWithMonocle)).isTrue();
    }

    // JEP 328: Flight Recorder
    @Test
    void canUseFlightRecorderApi() throws Exception {
        Recording r = new Recording();
        r.start();
        r.stop();
        r.close();
        assertThat(r.getState()).isEqualTo(RecordingState.CLOSED);
    }

    // JEP 329: ChaCha20 and Poly1305 Cryptographic Algorithms
    @Test
    void canUseChaCha20Poly1305Cipher() throws Exception {
        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
        assertThat(cipher.getAlgorithm()).isEqualTo("ChaCha20-Poly1305");
    }

    // JEP 332: TLS 1.3
    @Test
    void canUseTls13ByDefault() throws Exception {
        SSLContext ctx = SSLContext.getDefault();
        String[] protocols = ctx.getSupportedSSLParameters().getProtocols();
        assertThat(Arrays.asList(protocols)).contains("TLSv1.3");
    }

}