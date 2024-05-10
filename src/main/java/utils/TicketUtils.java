package utils;

import model.Ticket;
import org.jetbrains.annotations.NotNull;
import org.eclipse.jgit.revwalk.RevCommit;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class TicketUtils {

    private TicketUtils() {}

    public static void printTickets(@NotNull List<Ticket> tickets) {
        for(Ticket ticket: tickets) {
            if(ticket.getInjectedRelease() != null && ticket.getOpeningRelease() != null && ticket.getFixedRelease() != null) {
                System.out.println(ticket.getKey() + "," + ticket.getTicketCreationDate() + "," + ticket.getTicketResolutionDate() + "  ->  " +
                        "Injected Version:" + ticket.getInjectedRelease().getName() + " - " +
                        "Opening Version:" + ticket.getOpeningRelease().getName() + " - " +
                        "Fix Version:" + ticket.getFixedRelease().getName());
            }
        }
    }

    public static void sortTickets(@NotNull ArrayList<Ticket> tickets) {
        tickets.sort(Comparator.comparing(Ticket::getTicketCreationDate));
    }

    public static @NotNull List<RevCommit> getAssociatedCommit(@NotNull List<Ticket> tickets) {
        List<RevCommit> commits = new ArrayList<>();

        for(Ticket t: tickets) {
            commits.addAll(t.getAssociatedCommits());
        }

        return commits;
    }

}
