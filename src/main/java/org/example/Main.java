package org.example;

import retrievers.TicketRetriever;
import retrievers.VersionRetriever;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("TICKET RETRIEVER");
        TicketRetriever.retrieveTicket("BOOKKEEPER");
        TicketRetriever.retrieveTicket("ZOOKEEPER");
        System.out.println("RELEASE INFO");
        VersionRetriever.getVersion("BOOKKEEPER");
        VersionRetriever.getVersion("ZOOKEEPER");
    }
}