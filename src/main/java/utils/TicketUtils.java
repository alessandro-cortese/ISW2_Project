package utils;

import model.Ticket;
import model.VersionInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TicketUtils {

    public static void printTickets(@NotNull ArrayList<Ticket> tickets) {
        for(Ticket ticket: tickets) {
            if(ticket.getInjectedRelease() != null && ticket.getOpeningRelease() != null && ticket.getFixedRelease() != null) {
                System.out.println(ticket.getKey() + "," + ticket.getTicketCreationDate() + "," + ticket.getTicketResolutionDate() + "  ->  " +
                        "Injected Version:" + ticket.getInjectedRelease().getName() + " - " +
                        "Opening Version:" + ticket.getOpeningRelease().getName() + " - " +
                        "Fix Version:" + ticket.getFixedRelease().getName());
            }
        }
    }

    private void printAffectedRelease(ArrayList<VersionInfo> releases) {
        for(VersionInfo release: releases) {
            System.out.println("id: " + release.getId() + " name: " + release.getName() + " date: " + release.getDate());
        }
    }

}