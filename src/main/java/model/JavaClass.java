package model;

public class JavaClass {

    private String name;
    private String content;
    private Version release;

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
}
