package main;

import enums.CsvNamesEnum;
import model.ReleaseCommits;
import model.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.IO;
import retrievers.CommitRetriever;
import retrievers.MetricsRetriever;
import retrievers.TicketRetriever;
import view.FileCreator;

import java.io.IOException;
import java.util.List;

public class RunningExecution {

    public RunningExecution(String projectName) {
        TicketRetriever ticketRetriever = new TicketRetriever(projectName);
        List<Ticket> tickets = ticketRetriever.getTickets();
        CommitRetriever commitRetriever = ticketRetriever.getCommitRetriever();

        try{
            List<ReleaseCommits> releaseCommitsList = commitRetriever.getReleaseCommits(ticketRetriever.getVersionRetriever(), commitRetriever.retrieveCommit());
            printReleaseCommit(projectName, releaseCommitsList);
            MetricsRetriever.addBuggynessLabel(releaseCommitsList, tickets, commitRetriever, ticketRetriever.getVersionRetriever());
            MetricsRetriever.computeMetrics(releaseCommitsList);
            FileCreator.writeOnCsv(projectName, releaseCommitsList, CsvNamesEnum.BUGGY, 0);
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException();

        }
    }
;
    private static void printReleaseCommit(String projectName, List<ReleaseCommits> releaseCommitsList) {
        for(ReleaseCommits rc: releaseCommitsList) {
            System.out.println(projectName + " version: " + rc.getRelease().getName() + "; Commits: " + rc.getCommits().size() + "; Java classes: " + rc.getJavaClasses().size());
        }
    }

}
