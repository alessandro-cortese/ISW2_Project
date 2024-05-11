package main;

import enums.CsvNamesEnum;
import model.ReleaseInfo;
import model.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import retrievers.CommitRetriever;
import retrievers.MetricsRetriever;
import retrievers.TicketRetriever;
import utils.TicketUtils;
import view.FileCreator;

import java.io.IOException;
import java.util.List;

public class RunningExecution {

    private RunningExecution () {}

    public  static void collectData(String projectName) {

        //Retrieve all project ticket that are valid ticket
        TicketRetriever ticketRetriever = new TicketRetriever(projectName);
        List<Ticket> tickets = ticketRetriever.getTickets();

        CommitRetriever commitRetriever = ticketRetriever.getCommitRetriever();

        try{

            System.out.println("\n" + projectName + " - NUMBER OF COMMIT: " + commitRetriever.retrieveCommit().size() + "\n");

            List<ReleaseInfo> allTheReleaseCommit = commitRetriever.getReleaseCommits(ticketRetriever.getVersionRetriever(), commitRetriever.retrieveCommit());
            MetricsRetriever.computeMetrics(allTheReleaseCommit, tickets, commitRetriever, ticketRetriever.getVersionRetriever());

            FileCreator.writeOnCsv(projectName, allTheReleaseCommit, CsvNamesEnum.BUGGY, 0);

            //----------- WALK FORWARD -----------
            List<ReleaseInfo> releaseInfoList = discardHalfReleases(allTheReleaseCommit);
            for(int i = 0; i < allTheReleaseCommit.size(); i++){
                TicketUtils.getTicketsUntilRelease(tickets, i);
            }

            printReleaseCommit(projectName, allTheReleaseCommit);

        } catch (GitAPIException | IOException e) {
            throw new RuntimeException();
        }
    }

    private static List<ReleaseInfo> discardHalfReleases(List<ReleaseInfo> releaseInfoList) {
        //TODO dobbiamo tenerci la metà delle release per il training e un'altra per il testing (quindi n/2 +1 set)
        // oppure dobbiamo tenerci n/2 set in totale, ovvero n/2-1 set per il training e uno per il testing?
        // Cosa fare se n è dispari?

        int n = releaseInfoList.size();

        return releaseInfoList.subList(0, n/2+1);
    }

    ;
    private static void printReleaseCommit(String projectName, @NotNull List<ReleaseInfo> releaseCommitsList) {
        for(ReleaseInfo rc: releaseCommitsList) {

            System.out.println(projectName + " version: " + rc.getRelease().getName() + ";" +
                    " Commits: " + rc.getCommits().size() + ";" +
                    " Java classes: " + rc.getJavaClasses().size() + ";" +
                    " Buggy classes: " + rc.getBuggyClasses());
        }
    }

}
