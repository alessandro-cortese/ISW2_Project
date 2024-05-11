package model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

public class ReleaseInfo {

    private final Version release;
    private final List<RevCommit> commits;
    private final RevCommit lastCommit;
    private List<JavaClass> javaClasses;
    private int buggyClasses;

    //Classes that were present when the release was deployed;
    //they are represented by their name and their content.


    public ReleaseInfo(Version versionInfo, List<RevCommit> commits, RevCommit lastCommit){

        this.release = versionInfo;
        this.commits = commits;
        this.lastCommit = lastCommit;
        this.javaClasses = null;

    }

    public Version getRelease() {
        return release;
    }


    public List<RevCommit> getCommits() {
        return commits;
    }


    public RevCommit getLastCommit() {
        return lastCommit;
    }

    public List<JavaClass> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(List<JavaClass> javaClasses) {
        this.javaClasses = javaClasses;
    }

    public int getBuggyClasses() {
        return buggyClasses;
    }

    public void setBuggyClasses(int buggyClasses) {
        this.buggyClasses = buggyClasses;
    }
}