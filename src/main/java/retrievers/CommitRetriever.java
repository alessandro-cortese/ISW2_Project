package retrievers;

import model.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.GitUtils;
import utils.RegularExpression;
import utils.VersionUtil;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.*;
import java.io.IOException;

public class CommitRetriever {

    private Git git;
    private Repository repository;
    private VersionRetriever versionRetriever;
    private List<RevCommit> commitList;

    public CommitRetriever(String repositoryPath, VersionRetriever versionRetriever) {
        this.repository = GitUtils.getRepository(repositoryPath);
        this.git = new Git(repository);
        this.versionRetriever = versionRetriever;
    }


    public @Nullable List<RevCommit> retrieveAssociatedCommits(@NotNull List<RevCommit> commits, Ticket ticket) throws GitAPIException{

        Iterable<RevCommit> commitIterable = git.log().call();
        for(RevCommit commit: commitIterable)
            commits.add(commit);

        List<RevCommit> associatedCommit = new ArrayList<>();
        for(RevCommit commit: commits){
            if(RegularExpression.matchRegex(commit.getFullMessage(), ticket.getKey())){
                associatedCommit.add(commit);
            }
        }
        return associatedCommit;
    }

    public List<RevCommit> retrieveCommit() throws GitAPIException {
        if(commitList != null) return commitList;
        Iterable<RevCommit> commitIterable = git.log().call();

        List<RevCommit> commits = new ArrayList<>();
        List<Version> projectVersion = versionRetriever.getProjectVersions();
        Version lastVersion = projectVersion.get(projectVersion.size() - 1);
        for(RevCommit commit: commitIterable) {
            if(!GitUtils.castToLocalDate(commit.getCommitterIdent().getWhen()).isAfter(lastVersion.getDate())) {
                commits.add(commit);
            }
        }

        commits.sort(Comparator.comparing(o -> o.getCommitterIdent().getWhen()));

        this.commitList = commits;

        return commits;
    }



    public List<Ticket> associateTicketAndCommit(List<Ticket> tickets) {
        try {
            List<RevCommit> commits = this.retrieveCommit();
            for (Ticket ticket : tickets) {
                List<RevCommit> associatedCommits = this.retrieveAssociatedCommits(commits, ticket);
                ticket.setAssociatedCommits(associatedCommits);
                //GitUtils.printCommit(associatedCommits);
            }
            tickets.removeIf(ticket -> ticket.getAssociatedCommits().isEmpty());
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        return tickets;
    }

    private void retrieveChanges(List<RevCommit> commits) {
        for(RevCommit commit: commits) {
            List<JavaClass> classes = retrieveChanges(commit);
        }
    }

    private List<JavaClass> retrieveChanges(RevCommit commit) {

        List<JavaClass> javaClassList = new ArrayList<>();
        try {
            ObjectReader reader = git.getRepository().newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = git.getRepository().resolve(commit.getName() + "~1^{tree}");
            oldTreeIter.reset(reader, oldTree);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = git.getRepository().resolve(commit.getName() + "^{tree}");
            newTreeIter.reset(reader, newTree);

            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(git.getRepository());
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            for (DiffEntry entry : entries) {
                JavaClass javaClass = new JavaClass(
                        entry.getNewPath(),
                        new String(this.repository.open(entry.getNewId().toObjectId()).getBytes(), StandardCharsets.UTF_8),
                        VersionUtil.retrieveNextRelease(
                                versionRetriever,
                                GitUtils.castToLocalDate(commit.getCommitterIdent().getWhen()))
                );
                JavaClassChange newJavaClassChange = new JavaClassChange(javaClass, entry.getChangeType());
                javaClassList.add(newJavaClassChange.getJavaClass());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return javaClassList;
    }

    public List<ReleaseCommits> getReleaseCommits(VersionRetriever versionRetriever, List<RevCommit> commits) throws GitAPIException, IOException {
        List<ReleaseCommits> releaseCommits = new ArrayList<>();
        LocalDate lowerBound = LocalDate.of(1900, 1, 1);
        for(Version versionInfo: versionRetriever.getProjectVersions()) {
            ReleaseCommits releaseCommit = GitUtils.getCommitsOfRelease(commits, versionInfo, lowerBound);
            if(releaseCommit != null) {
                List<JavaClass> javaClasses = getClasses(releaseCommit.getLastCommit());
                releaseCommit.setJavaClasses(javaClasses);
                releaseCommits.add(releaseCommit);
            }
            lowerBound = versionInfo.getDate();
        }

        return releaseCommits;
    }

    private List<JavaClass> getClasses(RevCommit commit) throws IOException {

        List<JavaClass> javaClasses = new ArrayList<>();

        RevTree tree = commit.getTree();	                    //We get the tree of the files and the directories that were belong to the repository when commit was pushed
        TreeWalk treeWalk = new TreeWalk(this.repository);      //We use a TreeWalk to iterate over all files in the Tree recursively
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while(treeWalk.next()) {
            //We are keeping only Java classes that are not involved in tests
            if(treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) {
                //We are retrieving (name class, content class) couples
                Version release = VersionUtil.retrieveNextRelease(versionRetriever, GitUtils.castToLocalDate(commit.getCommitterIdent().getWhen()));

                if(release == null) throw new RuntimeException();

                javaClasses.add(new JavaClass(
                        treeWalk.getPathString(),
                        new String(this.repository.open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8),
                        release));
            }
        }
        treeWalk.close();

        return javaClasses;

    }

    public Git getGit() {
        return this.git;
    }

    public void setGit(Git git) {
        this.git = git;
    }

    public Repository getRepository() {
        return this.repository;
    }

    public void setRepository(Repository repository){
        this.repository = repository;
    }

}