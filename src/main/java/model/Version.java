package model;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public class Version {

    private String id;
    private Integer index;
    private String name;
    private LocalDate date;

    public Version(String id, String name, Integer index, @NotNull LocalDate date){

        this.id = id;
        this.name = name;
        this.index = index;
        this.date = date;

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
