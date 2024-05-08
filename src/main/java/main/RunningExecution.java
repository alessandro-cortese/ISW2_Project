package main;

import model.ReleaseCommits;
import model.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.IO;
import retrievers.CommitRetriever;
import retrievers.TicketRetriever;

import java.io.IOException;
import java.util.List;

public class RunningExecution {

    public RunningExecution(String projectName) {
        TicketRetriever ticketRetriever = new TicketRetriever(projectName);
        List<Ticket> tickets = ticketRetriever.getTickets();
        CommitRetriever commitRetriever = ticketRetriever.getCommitRetriever();

        try{
            List<ReleaseCommits> releaseCommitsList = commitRetriever.getReleaseCommits(ticketRetriever.getVersionRetriever(), commitRetriever.retrieveCommit());
            printReleaseCommit(releaseCommitsList);
            for(ReleaseCommits releaseCommits: releaseCommitsList) {
                System.out.println("Version: " + releaseCommits.getRelease().getName() + "Java classes: " + releaseCommits.getJavaClasses().size());
            }
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException();

        }
    }

    private static void printReleaseCommit(List<ReleaseCommits> releaseCommitsList) {
        for(ReleaseCommits rc: releaseCommitsList) {
            System.out.println(rc.getRelease().getName() + " -> " + rc.getCommits().size());
        }
    }

}
