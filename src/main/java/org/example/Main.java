package org.example;

import model.Ticket;
import retrievers.CommitRetriever;
import retrievers.TicketRetriever;
import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import utils.GitUtils;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {

        String bookkeeper = "BOOKKEEPER";
        String bookkeeperPath = "/home/ales/Documents/GitHub/bookkeeper";
        String zookeeper = "ZOOKEEPER";
        String zookeeperPath = "/home/ales/Documents/GitHub/zookeeper";


        //CommitRetriever bookkeeperCommitRetriever = new CommitRetriever(bookkeeperPath);
        //CommitRetriever zookeeperCommitRetriever = new CommitRetriever(zookeeperPath);

        TicketRetriever bookkeeperRetriever = new TicketRetriever(bookkeeper);
        TicketRetriever zookeeperRetriever = new TicketRetriever(zookeeper);

        ArrayList<Ticket> bookkeeperTickets = bookkeeperRetriever.getTickets();
        ArrayList<Ticket> zookeeperTickets = zookeeperRetriever.getTickets();

        //associatedTicketAndCommit(bookkeeperCommitRetriever, bookkeeperTickets);
        //associatedTicketAndCommit(zookeeperCommitRetriever, zookeeperTickets);

    }

    private static void associatedTicketAndCommit(CommitRetriever commitRetriever, ArrayList<Ticket> projectTickets){

        try{
            ArrayList<RevCommit> commits = commitRetriever.retrieveCommit();
            for(Ticket ticket: projectTickets){
                ArrayList<RevCommit> associatedCommits = commitRetriever.retrieveCommit(ticket);
                ticket.setAssociatedCommits(associatedCommits);
            }
        }catch (GitAPIException e){
            throw new RuntimeException(e);
        }

    }

}