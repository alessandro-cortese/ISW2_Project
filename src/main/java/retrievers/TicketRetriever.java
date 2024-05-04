package retrievers;

import model.Ticket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import model.VersionInfo;
import utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Proportion;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

public class TicketRetriever {

    private VersionRetriever versionRetriever;
    private ArrayList<Ticket> tickets;


    public TicketRetriever(String projectName, String issueType, String state, String resolution) {

        try {

            versionRetriever = new VersionRetriever(projectName);
            tickets = retrieveBugTickets(projectName, issueType, state, resolution);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean setReleaseInfoInTicket(@NotNull Ticket ticket){

        VersionInfo openingRelease = retrieveRelease(ticket.getTicketCreationDate());
        VersionInfo fixRelease = retrieveRelease(ticket.getTicketResolutionDate());

        if(openingRelease == null || fixRelease == null)
            return false;

        ticket.setOpeningRelease(openingRelease);
        ticket.setFixedRelease(fixRelease);

        return true;

    }

    private @Nullable VersionInfo retrieveRelease(LocalDate date){

        for(VersionInfo versionInfo: versionRetriever.getProjectVersions()){
            LocalDate releaseDate = versionInfo.getDate();
            if(!releaseDate.isBefore(date)){
                return versionInfo;
            }
        }
        return null;

    }

    private @NotNull ArrayList<Ticket> retrieveBugTickets(String projectName, String issueType, String state, String resolution) throws IOException, JSONException{

        int i = 0, j, total;
        ArrayList<Ticket> consistentTickets = new ArrayList<>();
        ArrayList<Ticket> inconsistentTickets = new ArrayList<>();

        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projectName + "%22AND%22issueType%22=%22" + issueType + "%22AND(%22status%22=%22" + state + "%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22" + resolution + "%22&fields=key,resolutiondate,versions,created&startAt="
                    + i + "&maxResults=" + j;
            JSONObject json = JSONUtils.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug
                String key = issues.getJSONObject(i%1000).get("key").toString();
                String resolutionDate = issues.getJSONObject(i%1000).getJSONObject("fields").get("resolutiondate").toString();
                String creationDate = issues.getJSONObject(i%1000).getJSONObject("fields").get("created").toString();
                ArrayList<VersionInfo> releases = versionRetriever.getAffectedVersion(issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("versions"));
                Ticket ticket = new Ticket(creationDate, resolutionDate, key, releases, versionRetriever);
                if(!setReleaseInfoInTicket(ticket))
                    continue;
                addTicket(ticket, consistentTickets, inconsistentTickets);
            }
        } while (i < total);

        adjustInconsistentTickets(inconsistentTickets, consistentTickets);

        return consistentTickets;

    }

    private void adjustInconsistentTickets(@NotNull ArrayList<Ticket> inconsistentTickets, ArrayList<Ticket> consistentTickets) {

        double proportionValue = Proportion.computeProportionValue(consistentTickets);
        System.out.println("Proportion value: " + proportionValue);
        for(Ticket ticket: inconsistentTickets){
            fixTicket(ticket, proportionValue);
            if(!consistencyCheck(ticket)){
                throw new RuntimeException();
            }
            consistentTickets.add(ticket);
        }

    }

//    private void adjustTicket(Ticket ticket, double proportionValue) {
//        //assign the new injected version for the inconsistent ticket as max(0, FV-(FV-OV)*P)
//        VersionInfo OV = ticket.getOpeningRelease();
//        VersionInfo FV = ticket.getFixedRelease();
//        int newIndex = (int) (FV.getIndex() - (FV.getIndex() - OV.getIndex())*proportionValue);
//        if(newIndex < 0) {
//            ticket.setInjectedRelease(versionRetriever.projVersions.get(0));
//            return;
//        }
//        ticket.setInjectedRelease(versionRetriever.projVersions.get(newIndex));
//    }

    private void fixTicket(Ticket ticket, double proportionValue){

        VersionInfo OV = ticket.getOpeningRelease();
        VersionInfo FV = ticket.getFixedRelease();
        int newIndex = (int) (FV.getIndex() - (FV.getIndex() - OV.getIndex())*proportionValue);
        if(newIndex < 0){
            ticket.setInjectedRelease(versionRetriever.getProjectVersions().get(0));
            return;
        }
        ticket.setInjectedRelease(versionRetriever.getProjectVersions().get(newIndex));

    }

    private static void addTicket(Ticket ticket, ArrayList<Ticket> consistentTicket, ArrayList<Ticket> inconsistentTicket){

        // IV <= OV <= FV, IV = AV[0]
        // If condition is false, we have an inconsistency ticket

        if(!consistencyCheck(ticket))
            inconsistentTicket.add(ticket);
        else
            consistentTicket.add(ticket);

    }

    private static boolean  consistencyCheck(Ticket ticket){

        VersionInfo IV = ticket.getInjectedRelease();
        VersionInfo OV = ticket.getOpeningRelease();
        VersionInfo FV = ticket.getFixedRelease();

        return IV != null && IV.getIndex() <= OV.getIndex() && OV.getIndex() <= FV.getIndex();

    }

    public VersionRetriever getVersionRetriever() {
        return versionRetriever;
    }

    public void setVersionRetriever(VersionRetriever versionRetriever) {
        this.versionRetriever = versionRetriever;
    }


    public ArrayList<Ticket> getTickets(){
        return this.tickets;
    }

    public void setTickets(ArrayList<Ticket> tickets){
        this.tickets = tickets;
    }

}
