package model;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public class VersionInfo {

    private String Id;
    private Integer index;
    private String name;
    private LocalDate date;

    public VersionInfo(String Id, String name,Integer index, @NotNull LocalDate date){

        this.Id = Id;
        this.name = name;
        this.index = index;
        this.date = date;

    }

    public VersionInfo(String Id, String name, @NotNull LocalDate date){

        this.Id = Id;
        this.name = name;
        this.date = date;

    }

    public String getId() {
        return this.Id;
    }

    public void setId(String id) {
        this.Id = id;
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
