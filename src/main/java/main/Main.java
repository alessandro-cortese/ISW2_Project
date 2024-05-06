package main;

import model.Ticket;
import model.ReleaseCommits;
import retrievers.CommitRetriever;
import retrievers.TicketRetriever;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.jgit.api.errors.GitAPIException;
import utils.TicketUtils;

public class Main {
    public static void main(String[] args) throws IOException {

        String bookkeeper = "BOOKKEEPER";
        String bookkeeperPath = "/home/ales/Documents/GitHub/bookkeeper";
        String zookeeper = "ZOOKEEPER";
        String zookeeperPath = "/home/ales/Documents/GitHub/zookeeper";

        TicketRetriever bookkeeperRetriever = new TicketRetriever(bookkeeper);
        CommitRetriever bookCommitRetriever = bookkeeperRetriever.getCommitRetriever();
        //TicketRetriever zookeeperRetriever = new TicketRetriever(zookeeper);
        List<Ticket> bookkeeperTickets = bookkeeperRetriever.getTickets();
        //ArrayList<Ticket> zookeeperTickets = zookeeperRetriever.getTickets();

        try{
            List<ReleaseCommits> releaseCommits = bookCommitRetriever.getReleaseCommits(bookkeeperRetriever.getVersionRetriever(), TicketUtils.getAssociatedCommit(bookkeeperTickets));
            printReleaseCommit(releaseCommits);
        }catch (GitAPIException | IOException e){
            throw new RemoteException();
        }


    }

    private static void printReleaseCommit(List<ReleaseCommits> releaseCommits) {
        for(ReleaseCommits rc: releaseCommits) {
            System.out.println(rc.getJavaClasses().keySet());
        }
    }

}