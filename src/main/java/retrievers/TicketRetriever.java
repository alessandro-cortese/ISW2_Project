package retrievers;

import model.Ticket;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import model.Version;
import utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Proportion;
import utils.VersionUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static utils.ColdStart.coldStart;

public class TicketRetriever {

    private static final String FIELDS = "fields";
    private VersionRetriever versionRetriever;
    private List<Ticket> tickets;
    private boolean coldStart = false;
    private CommitRetriever commitRetriever;

    public TicketRetriever(String projectName) {
        init(projectName);
    }

    public TicketRetriever(String projectName, boolean coldStart) {
        this.coldStart = coldStart;
        init(projectName);
    }
    
    private void init(String projectName) {

        String issueType = "Bug";
        String state = "closed";
        String resolution = "fixed";

        try {
            versionRetriever = new VersionRetriever(projectName);
            tickets = retrieveBugTickets(projectName, issueType, state, resolution);
            System.out.println("Ticket extract from " + projectName + "; " + tickets.size());
            int count = 0;
            for(Ticket ticket: tickets){
                count += ticket.getAssociatedCommits().size();
            }
            System.out.println("Commits associated to ticket extract from: " + projectName + ": " + count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setReleaseInTicket(@NotNull Ticket ticket){

        Version openingRelease = VersionUtil.retrieveNextRelease(versionRetriever, ticket.getTicketCreationDate());
        Version fixRelease = VersionUtil.retrieveNextRelease(versionRetriever , ticket.getTicketResolutionDate());

        ticket.setOpeningRelease(openingRelease);
        ticket.setFixedRelease(fixRelease);

    }

    private @NotNull List<Ticket> retrieveBugTickets(String projectName, String issueType, String state, String resolution) throws IOException, JSONException{

        int i = 0;
        int j;
        int total;
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
                String resolutionDate = issues.getJSONObject(i%1000).getJSONObject(FIELDS).get("resolutiondate").toString();
                String creationDate = issues.getJSONObject(i%1000).getJSONObject(FIELDS).get("created").toString();
                List<Version> releases = versionRetriever.getAffectedVersion(issues.getJSONObject(i%1000).getJSONObject(FIELDS).getJSONArray("versions"));
                Ticket ticket = new Ticket(creationDate, resolutionDate, key, releases, versionRetriever);
                setReleaseInTicket(ticket);
                //Discard incorrect ticket or that are after the last release
                if(ticket.getOpeningRelease() == null ||
                        (ticket.getInjectedRelease() != null &&
                                (ticket.getInjectedRelease().getIndex() > ticket.getOpeningRelease().getIndex())))
                    continue;
                //If the ticket doesn't have the fixed release, the ticket will be discarded
                if(ticket.getFixedRelease() != null){
                    System.out.println("Ticket key:" + ticket.getKey() + "; fixed release:  " + ticket.getFixedRelease().getIndex());
                    addTicket(ticket, consistentTickets, inconsistentTickets);
                }

            }
        } while (i < total);

        if(!this.coldStart)adjustInconsistentTickets(inconsistentTickets, consistentTickets);

        discardInvalidTicket(consistentTickets);

        commitRetriever = new CommitRetriever("/home/alessandro/Documenti/GitRepositories/" + projectName.toLowerCase(), versionRetriever);
        return commitRetriever.associateTicketAndCommit(consistentTickets);
    }

    /**Discard tickets that have OV > FV or that have IV=OV*/
    private void discardInvalidTicket(ArrayList<Ticket> tickets) {
        tickets.removeIf(ticket -> ticket.getOpeningRelease().getIndex() > ticket.getFixedRelease().getIndex() ||   //Discard if OV > FV
                ticket.getInjectedRelease().getIndex() >= ticket.getOpeningRelease().getIndex() ||
                (ticket.getOpeningRelease() == null || ticket.getFixedRelease() == null)); //Discard if IV >= OV
    }

    private void adjustInconsistentTickets(@NotNull List<Ticket> inconsistentTickets, @NotNull ArrayList<Ticket> consistentTickets) {

        double proportionValue;

        if(consistentTickets.size() >= 5){
            proportionValue = Proportion.computeProportionValue(consistentTickets);
        }else{
            proportionValue = Proportion.computeProportionValue(coldStart());
        }

        System.out.println("Proportion value: " + proportionValue);
        for(Ticket ticket: inconsistentTickets){
            fixTicket(ticket, proportionValue);
            if(!isNotConsistent(ticket)){
                throw new RuntimeException();
            }
            consistentTickets.add(ticket);
        }

    }


    private void fixTicket(Ticket ticket, double proportionValue){

        Version ov = ticket.getOpeningRelease();
        Version fv = ticket.getFixedRelease();
        int newIndex;

        if(Objects.equals(fv.getIndex(), ov.getIndex())){
            newIndex = (int) Math.floor(fv.getIndex() - proportionValue);
        }else{
            newIndex = (int) Math.floor(fv.getIndex() - (fv.getIndex() - ov.getIndex()) * proportionValue);
        }

        if(newIndex < 0){
            ticket.setInjectedRelease(versionRetriever.getProjectVersions().get(0));
            return;
        }
        ticket.setInjectedRelease(versionRetriever.getProjectVersions().get(newIndex));

    }

    //Check that tickets is consistent; if it isn't, the ticket will add to inconsistent ticket.
    private static void addTicket(Ticket ticket, ArrayList<Ticket> consistentTicket, ArrayList<Ticket> inconsistentTicket){

        // IV <= OV <= FV, IV = AV[0]
        // If condition is false, we have an inconsistency ticket

        if(!isNotConsistent(ticket))
            inconsistentTicket.add(ticket);
        else
            consistentTicket.add(ticket);

    }

    private static boolean isNotConsistent(Ticket ticket){

        Version iv = ticket.getInjectedRelease();
        Version ov = ticket.getOpeningRelease();
        Version fv = ticket.getFixedRelease();

        return iv != null && iv.getIndex() <= ov.getIndex() && ov.getIndex() <= fv.getIndex();

    }

    public VersionRetriever getVersionRetriever() {
        return versionRetriever;
    }

    public void setVersionRetriever(VersionRetriever versionRetriever) {
        this.versionRetriever = versionRetriever;
    }


    public List<Ticket> getTickets(){
        return this.tickets;
    }

    public void setTickets(List<Ticket> tickets){
        this.tickets = tickets;
    }

    public void setCommitRetriever(CommitRetriever commitRetriever){
        this.commitRetriever = commitRetriever;
    }

    public CommitRetriever getCommitRetriever(){
        return this.commitRetriever;
    }

}
