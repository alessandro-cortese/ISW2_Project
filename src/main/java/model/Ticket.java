package model;

import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import retrievers.VersionRetriever;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

    private String key;
    private LocalDate creationDate;
    private LocalDate resolutionDate;
    private List<Version> affectedReleases;
    private Version openingRelease;
    private Version fixedRelease;
    private Version injectedRelease;
    private VersionRetriever versionRetriever;
    private List<RevCommit> associatedCommits;
    private RevCommit lastCommit;

    public Ticket(@NotNull String creationDate, @NotNull String resolutionDate, String key, List<Version> affectedReleases, @NotNull VersionRetriever versionRetriever) {
        this.creationDate = LocalDate.parse(creationDate.substring(0, 10));
        this.resolutionDate = LocalDate.parse(resolutionDate.substring(0, 10));
        this.key = key;
        setVersionRetriever(versionRetriever);
        setInjectedRelease(affectedReleases);
    }

    public void setVersionRetriever(VersionRetriever versionRetriever) {
        this.versionRetriever = versionRetriever;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setAssociatedCommits(@NotNull List<RevCommit> associatedCommits) {
        this.associatedCommits = associatedCommits;

        if(associatedCommits.isEmpty()) return;

        RevCommit com = associatedCommits.get(0);
        for(RevCommit commit: associatedCommits){
            if(commit.getCommitterIdent().getWhen().after(com.getCommitterIdent().getWhen())) com = commit;
        }

        this.lastCommit = com;
    }

    public LocalDate getResolutionDate() {
        return resolutionDate;
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

    public List<RevCommit> getAssociatedCommits() {
        return associatedCommits;
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

    private void setInjectedRelease(@NotNull List<Version> affectedReleases) {
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
        for (Version version : versionRetriever.getProjVersions()) {
            if ((version.getIndex() >= this.injectedRelease.getIndex()) && (version.getIndex() < this.fixedRelease.getIndex())) {
                releases.add(version);
            }
        }

        this.affectedReleases = releases;
    }

    public RevCommit getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(RevCommit lastCommit) {
        this.lastCommit = lastCommit;
    }
}
