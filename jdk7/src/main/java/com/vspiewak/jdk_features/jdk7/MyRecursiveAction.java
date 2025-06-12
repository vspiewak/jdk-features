package com.vspiewak.jdk_features.jdk7;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class MyRecursiveAction extends RecursiveAction {

    private final List<String> workload;

    public static int count = 0;
    public static Set<String> threads = new HashSet<>();

    public MyRecursiveAction(List<String> workload) {
        this.workload = workload;
    }

    @Override
    protected void compute() {
        if (workload.size() == 1) {
            count++;
            threads.add(Thread.currentThread().getName());
            System.out.println("Processing: " + workload.getFirst() + " on Thread: " + Thread.currentThread().getName());
        } else {

            int half = workload.size() / 2;
            List<String> left = new ArrayList<>(workload.subList(0, half));
            List<String> right = new ArrayList<>(workload.subList(half, workload.size()));

            MyRecursiveAction taskLeft = new MyRecursiveAction(left);
            MyRecursiveAction taskRight = new MyRecursiveAction(right);

            // Fork both
            taskLeft.fork();
            taskRight.fork();

            // Wait for both to finish
            taskLeft.join();
            taskRight.join();
        }
    }
}
