package retrievers;

import model.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.jetbrains.annotations.NotNull;
import utils.GitUtils;
import utils.JavaClassUtil;
import utils.RegularExpression;
import utils.VersionUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommitRetriever {

    private final Git git;
    private final Repository repo;
    private final VersionRetriever versionRetriever;
    private List<RevCommit> commitList;

    public CommitRetriever(String repositoryPath, VersionRetriever versionRetriever) throws IOException {
        this.repo = GitUtils.getRepository(repositoryPath);
        this.git = new Git(repo);
        this.versionRetriever = versionRetriever;
    }
}
