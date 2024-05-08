package model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.Map;

public class ReleaseCommits {

    private Version release;
    private List<RevCommit> commits;
    private RevCommit lastCommit;
    private List<JavaClass> javaClasses;
    //Classes that were present when the release was deployed;
    //they are represented by their name and their content.


    public ReleaseCommits(Version versionInfo, List<RevCommit> commits, RevCommit lastCommit){

        this.release = versionInfo;
        this.commits = commits;
        this.lastCommit = lastCommit;

    }


    public Version getRelease() {
        return release;
    }

    public void setRelease(Version release) {
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

    public List<JavaClass> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(List<JavaClass> javaClasses) {
        this.javaClasses = javaClasses;
    }
}
