package main;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        new RunningExecution("BOOKKEEPER");
        new RunningExecution("ZOOKEEPER");
    }
}