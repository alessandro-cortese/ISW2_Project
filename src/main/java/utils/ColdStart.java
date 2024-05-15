package utils;

import enums.ProjectsEnum;
import model.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;
import retrievers.TicketRetriever;

import java.util.ArrayList;
import java.util.List;

public class ColdStart {

    private ColdStart() {}

    public static List<Ticket> getTicketForColdStart(ProjectsEnum project) throws GitAPIException {
        TicketRetriever retriever = new TicketRetriever(project.toString(), true);
        return new ArrayList<>(retriever.getTickets());
    }

}
