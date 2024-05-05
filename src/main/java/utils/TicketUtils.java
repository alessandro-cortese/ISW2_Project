package utils;

import model.Ticket;
import model.VersionInfo;
import org.jetbrains.annotations.NotNull;

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

    public static void sortTickets(ArrayList<Ticket> tickets) {
        tickets.sort(Comparator.comparing(Ticket::getTicketCreationDate));
    }

}
