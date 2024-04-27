package model;

import org.jetbrains.annotations.NotNull;
import retrievers.VersionRetriever;

import java.time.LocalDate;
import java.util.ArrayList;

public class Ticket {

    private String key;
    private LocalDate ticketCreationDate;
    private LocalDate ticketResolutionDate;
    private ArrayList<VersionInfo> affectedRelease;
    private VersionInfo openingRelease;
    private VersionInfo injectedRelease;
    private VersionInfo fixedRelease;
    private VersionRetriever versionRetriever;

    public Ticket(@NotNull String creationDate, @NotNull String resolutionDate, String key, ArrayList<VersionInfo> affectedRelease, @NotNull VersionRetriever versionRetriever){

        this.ticketCreationDate = LocalDate.parse(creationDate.substring(0, 10));
        this.ticketResolutionDate = LocalDate.parse(resolutionDate.substring(0, 10));
        this.key = key;
        this.affectedRelease = affectedRelease;
        this.versionRetriever = versionRetriever;

    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDate getTicketCreationDate() {
        return this.ticketCreationDate;
    }

    public void setTicketCreationDate(LocalDate ticketCreationDate) {
        this.ticketCreationDate = ticketCreationDate;
    }

    public LocalDate getTicketResolutionDate() {
        return this.ticketResolutionDate;
    }

    public void setTicketResolutionDate(LocalDate ticketResolutionDate) {
        this.ticketResolutionDate = ticketResolutionDate;
    }

    public ArrayList<VersionInfo> getAffectedRelease() {
        return this.affectedRelease;
    }

    public void setAffectedRelease(ArrayList<VersionInfo> affectedRelease) {
        this.affectedRelease = affectedRelease;
    }

    public VersionInfo getOpeningRelease() {
        return this.openingRelease;
    }

    public void setOpeningRelease(VersionInfo openingRelease) {
        this.openingRelease = openingRelease;
    }

    public VersionInfo getInjectedRelease() {
        return this.injectedRelease;
    }

    public void setInjectedRelease(VersionInfo injectedRelease) {
        this.injectedRelease = injectedRelease;
    }

    public VersionInfo getFixedRelease() {
        return this.fixedRelease;
    }

    public void setFixedRelease(VersionInfo fixedRelease) {
        this.fixedRelease = fixedRelease;
    }

    public VersionRetriever getVersionRetriever() {
        return this.versionRetriever;
    }

    public void setVersionRetriever(VersionRetriever versionRetriever) {
        this.versionRetriever = versionRetriever;
    }

    private void computeAffectedRelease(){

        if(this.injectedRelease == null || this.fixedRelease == null)
            return;

        ArrayList<VersionInfo> affectedVersions = new ArrayList<>();
        for(VersionInfo versionInfo: versionRetriever.getProjectVersions()){
            if((versionInfo.getIndex() >= this.injectedRelease.getIndex()) && (versionInfo.getIndex() <= this.fixedRelease.getIndex())){
                affectedVersions.add(versionInfo);
            }
        }

        this.affectedRelease = affectedVersions;
    }

}
