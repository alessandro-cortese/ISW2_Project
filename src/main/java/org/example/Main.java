package org.example;

import model.Ticket;
import retrievers.CommitRetriever;
import retrievers.TicketRetriever;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {

        String issueType = "Bug";
        String state = "closed";
        String resolution = "fixed";
        String bookkeeper = "BOOKKEEPER";
        String bookkeeperPath = "/home/ales/Documents/GitHub/bookkeeper";
        String zookeeper = "ZOOKEEPER";
        String zookeeperPath = "/home/ales/Documents/GitHub/zookeeper";


        CommitRetriever bookkeeperCommitRetriever = new CommitRetriever(bookkeeperPath);
        CommitRetriever zookeeperCommitRetriever = new CommitRetriever(zookeeperPath);

        ArrayList<Ticket> bookkeeperTickets = new ArrayList<>();

        TicketRetriever bookkeeperRetriever = new TicketRetriever(bookkeeper, issueType, state, resolution);
        TicketRetriever zookeeperRetriever = new TicketRetriever(zookeeper, issueType, state, resolution);

    }

//    private static void associatedTicketAndCommit(CommitRetriever commitRetriever, ArrayList<Ticket> projectTickets){
//
//        try{
//            for(Ticket ticket: projectTickets){
//                ArrayList<RevCommit> commits = commitRetriever.retrieveCommit(ticket);
//                ticket.setAssociatedCommit(commits);
//            }
//        }catch (GitAPIException e){
//            throw new RuntimeException(e);
//        }
//
//    }

}