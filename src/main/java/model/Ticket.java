package model;

import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import retrievers.VersionRetriever;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

    String key;
    LocalDate ticketCreationDate;
    LocalDate ticketResolutionDate;
    List<VersionInfo> affectedReleases;

    List<RevCommit> associatedCommits;
    VersionInfo openingRelease;
    VersionInfo fixedRelease;
    VersionInfo injectedRelease;
    VersionRetriever versionRetriever;

    public Ticket(@NotNull String creationDate, @NotNull String resolutionDate, String key, List<VersionInfo> affectedReleases, @NotNull VersionRetriever versionRetriever) {
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

    public List<VersionInfo> getAffectedReleases() {
        return affectedReleases;
    }

    public VersionInfo getOpeningRelease() {
        return openingRelease;
    }

    public void setOpeningRelease(VersionInfo openingRelease) {
        this.openingRelease = openingRelease;
    }

    public VersionInfo getFixedRelease() {
        return fixedRelease;
    }

    public void setFixedRelease(VersionInfo fixedRelease) {
        this.fixedRelease = fixedRelease;
        computeAffectedRelease();
    }

    public VersionInfo getInjectedRelease() {
        return injectedRelease;
    }

    public void setInjectedRelease(VersionInfo release) {
        this.injectedRelease = release;
        computeAffectedRelease();
    }


    private void setInjectedRelease(List<VersionInfo> affectedReleases) {
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

        List<VersionInfo> releases = new ArrayList<>();
        for (VersionInfo versionInfo : versionRetriever.getProjectVersions()) {
            if ((versionInfo.getIndex() >= this.injectedRelease.getIndex()) && (versionInfo.getIndex() < this.fixedRelease.getIndex())) {
                releases.add(versionInfo);
            }
        }

        this.affectedReleases = releases;
    }
}