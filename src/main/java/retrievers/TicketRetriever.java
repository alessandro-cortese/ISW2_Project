package retrievers;

import model.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TicketRetriever {

    private static final String FIELDS = "fields";
    private VersionRetriever versionRetriever;
    private CommitRetriever commitRetriever;
    private List<Ticket> tickets;
    private boolean coldStart = false;

    /**
     * This is the constructor that you have to use for retrieve tickets without applying cold start.
     * @param projectName The project name from which retrieve tickets.
     */
    public TicketRetriever(String projectName) {
        init(projectName);
        try {
            commitRetriever = new CommitRetriever("/home/alessandro/Documenti/GitRepositories/" + projectName.toLowerCase(), versionRetriever);
            commitRetriever.associateCommitAndVersion(versionRetriever.getProjectVersions()); //Association of commits and versions and deletion of the version without commits
            VersionUtil.printVersion(versionRetriever.getProjectVersions());
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is the constructor that you have to use for retrieve tickets applying cold start.
     * @param projectName The project name from which retrieve tickets.
     * @param coldStart The value used to specifying that you are using cold start. Must be true.
     */
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
                                (ticket.getInjectedRelease().getIndex() > ticket.getOpeningRelease().getIndex())) ||
                        ticket.getFixedRelease() == null)
                    continue;
                //If the ticket doesn't have the fixed release, the ticket will be discarded
                addTicket(ticket, consistentTickets, inconsistentTickets);

            }
        } while (i < total);

        if(!coldStart) {
            consistentTickets.sort(Comparator.comparing(Ticket::getTicketResolutionDate));
            adjustInconsistentTickets(inconsistentTickets, consistentTickets); //Adjust the inconsistency tickets using proportion for missing IV, when you are not using cold start
            consistentTickets.sort(Comparator.comparing(Ticket::getTicketResolutionDate));
            commitRetriever = new CommitRetriever("/home/alessandro/Documenti/GitRepositories/" + projectName.toLowerCase(), versionRetriever);
            commitRetriever.associateTicketAndCommit(consistentTickets);
        }

        discardInvalidTicket(consistentTickets);

        return consistentTickets;
    }

    /**Discard tickets that have OV > FV or that have IV=OV*/
    private void discardInvalidTicket(ArrayList<Ticket> tickets) {
        tickets.removeIf(ticket -> ticket.getOpeningRelease().getIndex() > ticket.getFixedRelease().getIndex() ||   //Discard if OV > FV
                ticket.getInjectedRelease().getIndex() >= ticket.getOpeningRelease().getIndex() ||
                (ticket.getOpeningRelease() == null || ticket.getFixedRelease() == null)); //Discard if IV >= OV
    }

    // Make consistency the inconsistency ticket
    private void adjustInconsistentTickets(@NotNull List<Ticket> inconsistentTickets, @NotNull ArrayList<Ticket> consistentTickets) {

        List<Ticket> ticketForProportion = new ArrayList<>();
        double oldValue = 0;
        for(Ticket ticket: inconsistentTickets){
            double proportionValue = incrementalProportion(ticketForProportion);
            if(oldValue != proportionValue){
                oldValue = proportionValue;
            }
            fixTicket(ticket, proportionValue);
            if(!isNotConsistent(ticket)){
                throw new RuntimeException();
            }
            consistentTickets.add(ticket);
            if(Proportion.isAValidTicketForProportion(ticket)) ticketForProportion.add(ticket);
        }
    }

    private static double incrementalProportion(@NotNull List<Ticket> consistentTickets) {
        double proportionValue;

        if(consistentTickets.size() >= 5) {
            proportionValue = Proportion.computeProportionValue(consistentTickets);
        } else {
            proportionValue = Proportion.computeColdStartProportionValue();
        }
        return proportionValue;
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
