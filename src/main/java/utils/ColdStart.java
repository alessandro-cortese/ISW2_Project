package utils;

import enums.ProjectsEnum;
import model.Ticket;
import retrievers.TicketRetriever;

import java.util.ArrayList;
import java.util.List;

public class ColdStart {

    private ColdStart() {}

    public static List<Ticket> getTicketForColdStart(ProjectsEnum project){
        TicketRetriever retriever = new TicketRetriever(project.toString(), true);
        return new ArrayList<>(retriever.getTickets());
    }

}
