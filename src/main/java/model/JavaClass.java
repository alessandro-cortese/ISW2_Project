package model;

import org.eclipse.jgit.revwalk.RevCommit;
import java.util.ArrayList;
import java.util.List;

public class JavaClass {

    private String name;
    private String content;
    private Version release;
    private List<RevCommit> commits = new ArrayList<>();
    private final Metrics metrics  = new Metrics();

    public JavaClass(String name, String content, Version release) {

        this.name = name;
        this.content = content;
        this.release = release;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public void addCommit(RevCommit commit) {
        this.commits.add(commit);
    }

    public Metrics getMetrics() {
        return metrics;
    }

}
