package utils;

import enums.ProjectsEnum;
import model.Ticket;
import retrievers.TicketRetriever;

import java.util.ArrayList;
import java.util.List;

public class ColdStart {

    private ColdStart() {}

    public static List<Ticket> coldStart(){
        List<Ticket> consistentTicket = new ArrayList<>();
        for(ProjectsEnum projectsEnum: ProjectsEnum.values()){
            TicketRetriever retriever = new TicketRetriever(projectsEnum.name(), true);
            consistentTicket.addAll(retriever.getTickets());
        }

        return consistentTicket;
    }

}
