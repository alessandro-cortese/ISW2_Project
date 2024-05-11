package main;

import enums.CsvNamesEnum;
import model.ReleaseCommits;
import model.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import retrievers.CommitRetriever;
import retrievers.MetricsRetriever;
import retrievers.TicketRetriever;
import view.FileCreator;
import java.util.logging.Logger;

import java.io.IOException;
import java.util.List;

public class RunningExecution {

    private RunningExecution () {}

    public  static void collectData(String projectName) {

        TicketRetriever ticketRetriever = new TicketRetriever(projectName);
        List<Ticket> tickets = ticketRetriever.getTickets();
        CommitRetriever commitRetriever = ticketRetriever.getCommitRetriever();

        try{

            System.out.println("\n" + projectName + " - NUMBER OF COMMIT: " + commitRetriever.retrieveCommit().size() + "\n");

            List<ReleaseCommits> releaseCommitsList = commitRetriever.getReleaseCommits(ticketRetriever.getVersionRetriever(), commitRetriever.retrieveCommit());

            MetricsRetriever.computeBuggynessAndFixedDefects(releaseCommitsList, tickets, commitRetriever, ticketRetriever.getVersionRetriever());
            MetricsRetriever.computeMetrics(releaseCommitsList, tickets, commitRetriever, ticketRetriever.getVersionRetriever());

            FileCreator.writeOnCsv(projectName, releaseCommitsList, CsvNamesEnum.BUGGY, 0);

            for(ReleaseCommits commits : releaseCommitsList){
                //Leave this here for now
            }

            printReleaseCommit(projectName, releaseCommitsList);

        } catch (GitAPIException | IOException e) {
            throw new RuntimeException();
        }
    }
;
    private static void printReleaseCommit(String projectName, @NotNull List<ReleaseCommits> releaseCommitsList) {
        for(ReleaseCommits rc: releaseCommitsList) {

            System.out.println(projectName + " version: " + rc.getRelease().getName() + ";" +
                    " Commits: " + rc.getCommits().size() + ";" +
                    " Java classes: " + rc.getJavaClasses().size() + ";" +
                    " Buggy classes: " + rc.getBuggyClasses());
        }
    }

}
