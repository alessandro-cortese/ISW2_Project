package main;

import model.Ticket;
import retrievers.CommitRetriever;
import retrievers.TicketRetriever;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        String bookkeeper = "BOOKKEEPER";
        String bookkeeperPath = "/home/ales/Documents/GitHub/bookkeeper";
        String zookeeper = "ZOOKEEPER";
        String zookeeperPath = "/home/ales/Documents/GitHub/zookeeper";

        TicketRetriever bookkeeperRetriever = new TicketRetriever(bookkeeper);
        //TicketRetriever zookeeperRetriever = new TicketRetriever(zookeeper);
        CommitRetriever commitRetriever = new CommitRetriever("/home/ales/Documents/GitRepositories/bookkeeper");
        List<Ticket> bookkeeperTickets = bookkeeperRetriever.getTickets();
        //ArrayList<Ticket> zookeeperTickets = zookeeperRetriever.getTickets();

    }

}