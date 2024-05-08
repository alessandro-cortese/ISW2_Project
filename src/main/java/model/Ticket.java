package model;

import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import retrievers.VersionRetriever;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

     private String key;
    private LocalDate ticketCreationDate;
    private LocalDate ticketResolutionDate;
    private List<Version> affectedReleases;
    private List<RevCommit> associatedCommits;
    private Version openingRelease;
    private Version fixedRelease;
    private Version injectedRelease;
    private VersionRetriever versionRetriever;

    public Ticket(@NotNull String creationDate, @NotNull String resolutionDate, String key, List<Version> affectedReleases, @NotNull VersionRetriever versionRetriever) {
        this.ticketCreationDate = LocalDate.parse(creationDate.substring(0, 10));
        this.ticketResolutionDate = LocalDate.parse(resolutionDate.substring(0, 10));
        this.key = key;
        setVersionRetriever(versionRetriever);
        setInjectedRelease(affectedReleases);
    }

    public VersionRetriever getVersionRetriever() {
        return versionRetriever;
    }

    public void setVersionRetriever(VersionRetriever versionRetriever) {
        if(versionRetriever == null) {
            throw new RuntimeException();
        }
        this.versionRetriever = versionRetriever;
    }

    public LocalDate getTicketCreationDate() {
        return ticketCreationDate;
    }

    public LocalDate getTicketResolutionDate() {
        return ticketResolutionDate;
    }

    public void setAssociatedCommits(List<RevCommit> associatedCommits){
        this.associatedCommits = associatedCommits;
    }

    public List<RevCommit> getAssociatedCommits() {
        return this.associatedCommits;
    }

    public String getKey() {
        return key;
    }

    public List<Version> getAffectedReleases() {
        return affectedReleases;
    }

    public Version getOpeningRelease() {
        return openingRelease;
    }

    public void setOpeningRelease(Version openingRelease) {
        this.openingRelease = openingRelease;
    }

    public Version getFixedRelease() {
        return fixedRelease;
    }

    public void setFixedRelease(Version fixedRelease) {
        this.fixedRelease = fixedRelease;
        computeAffectedRelease();
    }

    public Version getInjectedRelease() {
        return injectedRelease;
    }

    public void setInjectedRelease(Version release) {
        this.injectedRelease = release;
        computeAffectedRelease();
    }


    private void setInjectedRelease(List<Version> affectedReleases) {
        if(!affectedReleases.isEmpty()) {
            this.injectedRelease = affectedReleases.get(0);
            computeAffectedRelease();
        } else {
            this.injectedRelease = null;
        }
    }

    public void computeAffectedRelease() {
        // Execute the method only if the ticket has fixed and injected release
        if(this.injectedRelease == null || this.fixedRelease == null) return;

        List<Version> releases = new ArrayList<>();
        for (Version versionInfo : versionRetriever.getProjectVersions()) {
            if ((versionInfo.getIndex() >= this.injectedRelease.getIndex()) && (versionInfo.getIndex() < this.fixedRelease.getIndex())) {
                releases.add(versionInfo);
            }
        }

        this.affectedReleases = releases;
    }
}