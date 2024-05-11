package model;

import org.jetbrains.annotations.NotNull;
import org.eclipse.jgit.revwalk.RevCommit;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class Version {

    private String id;
    private Integer index;
    private String name;
    private LocalDate date;
    private List<RevCommit> commitList = new ArrayList<>();

    public Version(String id, String name, Integer index, @NotNull LocalDate date){

        this.id = id;
        this.name = name;
        this.index = index;
        this.date = date;

    }

    public boolean isCommitListEmpty() {
        return commitList.isEmpty();
    }

    public void addCommitToList(RevCommit commit) {
        this.commitList.add(commit);
    }

    public Version(String id, String name, @NotNull LocalDate date){

        this.id = id;
        this.name = name;
        this.date = date;

    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
