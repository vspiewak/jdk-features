package com.vspiewak.jdk_features.jdk7;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class MyRecursiveTask extends RecursiveTask<Integer> {

    public List<Integer> workload;

    public MyRecursiveTask(List<Integer> workload) {
        this.workload = workload;
    }

    @Override
    protected Integer compute() {
        if (workload.size() == 1) {
            return workload.getFirst();
        } else {
            int half = workload.size() / 2;
            List<Integer> left = new ArrayList<>(workload.subList(0, half));
            List<Integer> right = new ArrayList<>(workload.subList(half, workload.size()));

            MyRecursiveTask taskLeft = new MyRecursiveTask(left);
            MyRecursiveTask taskRight = new MyRecursiveTask(right);

            // fork both
            taskLeft.fork();
            taskRight.fork();

            // wait for both to finish
            return taskLeft.join() + taskRight.join();
        }
    }
}
