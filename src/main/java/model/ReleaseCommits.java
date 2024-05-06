package model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.Map;

public class ReleaseCommits {

    private VersionInfo release;
    private List<RevCommit> commits;
    private RevCommit lastCommit;

    //Classes that were present when the release was deployed;
    //they are represented by their name and their content.
    private Map<String, String> javaClasses;

    public ReleaseCommits(VersionInfo versionInfo, List<RevCommit> commits, RevCommit lastCommit){

        this.release = versionInfo;
        this.commits = commits;
        this.lastCommit = lastCommit;

    }


    public VersionInfo getRelease() {
        return release;
    }

    public void setRelease(VersionInfo release) {
        this.release = release;
    }

    public List<RevCommit> getCommits() {
        return commits;
    }

    public void setCommits(List<RevCommit> commits) {
        this.commits = commits;
    }

    public RevCommit getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(RevCommit lastCommit) {
        this.lastCommit = lastCommit;
    }

    public Map<String, String> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(Map<String, String> javaClasses) {
        this.javaClasses = javaClasses;
    }
}
