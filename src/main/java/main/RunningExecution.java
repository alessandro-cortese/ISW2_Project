package main;

import enums.FilenamesEnum;
import model.ClassifierEvaluation;
import model.ReleaseInfo;
import model.Ticket;
import org.jetbrains.annotations.NotNull;
import retrievers.*;
import utils.TicketUtils;
import view.FileCreator;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class RunningExecution {

    private RunningExecution () {}

    public  static void collectData(String projectName) throws Exception {

        //Retrieve all project ticket that are valid ticket
        TicketRetriever ticketRetriever = new TicketRetriever(projectName);
        CommitRetriever commitRetriever = ticketRetriever.getCommitRetriever();
        VersionRetriever versionRetriever = ticketRetriever.getVersionRetriever();

        List<Ticket> tickets = ticketRetriever.getTickets();
        //Retrieve the release information about commits, classes and metrics that involve the release.
        List<ReleaseInfo> allTheReleaseInfo = commitRetriever.getReleaseCommits(versionRetriever, commitRetriever.retrieveCommit());
        MetricsRetriever.computeMetrics(allTheReleaseInfo, tickets, commitRetriever, versionRetriever);
        FileCreator.writeOnCsv(projectName, allTheReleaseInfo, FilenamesEnum.METRICS, 0);
        System.out.println("allReleaseInfoSize: " + allTheReleaseInfo.size());

        //----------------------------------------------------------- WALK FORWARD -----------------------------------------------------------

        List<ReleaseInfo> releaseInfoListHalved = discardHalfReleases(allTheReleaseInfo);

        //Iterate starting by 1 so that the walk forward starts from using at least one training set.

        for(int i = 1; i < releaseInfoListHalved.size(); i++) {

            //Selection of the tickets opened until the i-th release.
            List<Ticket> ticketsUntilRelease = TicketUtils.getTicketsUntilRelease(tickets, i);

            //Non viene aggiornata la buggyness del testing set.
            MetricsRetriever.computeBuggyness(releaseInfoListHalved.subList(0, i), ticketsUntilRelease, commitRetriever, versionRetriever);

            FileCreator.writeOnArff(projectName, releaseInfoListHalved.subList(0, i), FilenamesEnum.TRAINING, i);
            ArrayList<ReleaseInfo> testingRelease = new ArrayList<>();
            testingRelease.add(releaseInfoListHalved.get(i));
            FileCreator.writeOnArff(projectName, testingRelease, FilenamesEnum.TESTING, i);
            /*if(i == releaseInfoListHalved.size() - 1) {
                FileCreator.writeOnCsv(projectName, testingRelease, FilenamesEnum.TESTING, i);
            }*/
        }

        WekaInfoRetriever wekaInfoRetriever = new WekaInfoRetriever(projectName, allTheReleaseInfo.size()/2);
        List<ClassifierEvaluation> classifierEvaluationList = wekaInfoRetriever.retrieveClassifiersEvaluation(projectName);
        FileCreator.writeEvaluationDataOnCsv(projectName, classifierEvaluationList);
    }

    private static @NotNull List<ReleaseInfo> discardHalfReleases(@NotNull List<ReleaseInfo> releaseInfoList) {

        //TODO dobbiamo tenerci la metà delle release per il training e un'altra per il testing (quindi n/2 +1 set)
        // oppure dobbiamo tenerci n/2 set in totale, ovvero n/2-1 set per il training e uno per il testing?
        // Cosa fare se n è dispari?

        int n = releaseInfoList.size();

        releaseInfoList.sort((o1, o2) -> {
            Integer i1 = o1.getRelease().getIndex();
            Integer i2 = o2.getRelease().getIndex();
            return i1.compareTo(i2);
        });


        return releaseInfoList.subList(0, n/2+1);
    }

}
