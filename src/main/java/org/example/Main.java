package org.example;

import retrievers.CommitRetriever;
import retrievers.TicketRetriever;
import retrievers.VersionRetriever;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        String bookkeeper = "BOOKKEEPER";
        String BOOKKEEPER_PATH = "/home/ales/Documents/GitHub/bookkeeper";
        String zookeeper = "ZOOKEEPER";
        String ZOOKEEPER_PATH = "/home/ales/Documents/GitHub/zookeper";
        String issueType = "Bug";
        String state = "closed";
        String resolution = "fixed";


        CommitRetriever bookkeeperCommitRetriever = new CommitRetriever(BOOKKEEPER_PATH);
        CommitRetriever zookeeperCommitRetriever = new CommitRetriever(ZOOKEEPER_PATH);
        TicketRetriever bookkeeperRetriever = new TicketRetriever(bookkeeper, issueType, state, resolution);
        TicketRetriever zookeeperRetriever = new TicketRetriever(zookeeper, issueType, state, resolution);

    }
}