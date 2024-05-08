package model;

import org.eclipse.jgit.diff.DiffEntry;

public class JavaClassChange {

    private JavaClass javaClass;
    private DiffEntry.ChangeType changeType;

    public JavaClassChange(JavaClass javaClass, DiffEntry.ChangeType changeType) {
        this.javaClass = javaClass;
        this.changeType = changeType;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(JavaClass javaClass) {
        this.javaClass = javaClass;
    }

    public DiffEntry.ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(DiffEntry.ChangeType changeType) {
        this.changeType = changeType;
    }
}
