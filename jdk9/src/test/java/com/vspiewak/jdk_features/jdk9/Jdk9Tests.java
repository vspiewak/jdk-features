package com.vspiewak.jdk_features.jdk9;

import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class Jdk9Tests {

    @Test
    void canUseModuleLayer() {
        ModuleLayer boot = ModuleLayer.boot();
        assertThat(boot.findModule("java.base")).isPresent();
    }

    @Test
    void canUseJShell() {
        try (JShell jshell = JShell.create()) {
            List<SnippetEvent> events = jshell.eval("1 + 2");
            assertThat(events.get(0).value()).isEqualTo("3");
        }
    }

    @Test
    void tryWithResourcesEffectivelyFinal() {
        class MyRes implements AutoCloseable {
            boolean closed = false;

            @Override
            public void close() {
                closed = true;
            }
        }
        MyRes res = new MyRes();           // not explicitly final
        try (res) { /* nothing */ }
        assertThat(res.closed).isTrue();
    }

    interface WithPriv {
        private String secret() {
            return "hidden";
        }

        default String expose() {
            return secret();
        }
    }

    @Test
    void privateInterfaceMethod() {

        WithPriv wp = new WithPriv() {
        };
        assertThat(wp.expose()).isEqualTo("hidden");
    }

    @Test
    void collectionFactories() {
        List<String> list = List.of("a", "b");
        Set<Integer> set = Set.of(1, 2);
        Map<String, Integer> m1 = Map.of("x", 1, "y", 2);

        assertThat(list).hasSize(2);
        assertThat(set).hasSize(2);
        assertThat(m1).hasSize(2);

    }

    @Test
    void processHandle() {
        ProcessHandle self = ProcessHandle.current();
        assertThat(self.isAlive()).isTrue();
        assertThat(self.pid()).isGreaterThan(0);
    }

    @Test
    void flowSubmissionPublisher() throws InterruptedException {
        SubmissionPublisher<String> pub = new SubmissionPublisher<>();
        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        Flow.Subscriber<String> sub = new Flow.Subscriber<>() {
            private Flow.Subscription s;

            @Override
            public void onSubscribe(Flow.Subscription s) {
                this.s = s;
                s.request(3);
            }

            @Override
            public void onNext(String item) {
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };

        pub.subscribe(sub);
        pub.submit("one");
        pub.submit("two");
        pub.submit("three");
        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(received).hasSize(3);
        pub.close();
    }

    @Test
    void varHandleArray() {
        VarHandle vh = MethodHandles.arrayElementVarHandle(int[].class);
        int[] arr = {10, 20, 30};
        vh.set(arr, 1, 99);

        assertThat(arr[1]).isEqualTo(99);
        assertThat(vh.get(arr, 0)).isEqualTo(10);
    }

    @Test
    void systemLogger() {
        System.Logger log = System.getLogger("Tst");
        log.log(System.Logger.Level.INFO, "hello from JDK9 logger");

        assertThat(log).isNotNull();
    }

    /*
    @Test
    void httpClientIncubator() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(10))
                .build();
        assertThat(client).isNotNull();
    }

    @Test
    void xmlCatalogApi() {
        CatalogManager mgr = CatalogManager.catalogManager();
        assertThat(mgr.getCatalogResolver()).isNotNull();
    }
     */

}
