package utils;

import enums.ProjectsEnum;
import model.Ticket;
import retrievers.TicketRetriever;

import java.util.ArrayList;

public class ColdStart {

    public static ArrayList<Ticket> coldStart(){
        ArrayList<Ticket> consistentTicket = new ArrayList<>();
        for(ProjectsEnum projectsEnum: ProjectsEnum.values()){
            TicketRetriever retriever = new TicketRetriever(projectsEnum.name(), true);
            consistentTicket.addAll(retriever.getTickets());
        }

        return consistentTicket;
    }

}
