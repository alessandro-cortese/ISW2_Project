package retrievers;

import model.Ticket;
import model.VersionInfo;
import model.ReleaseCommits;
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
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
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

    /*public void retrieveReleaseInfoForTickets(ArrayList<Ticket> tickets) {
        try {
            Iterable<RevCommit> commitIterable = git.log().call();
            ArrayList<RevCommit> commits = new ArrayList<>();
            for(RevCommit commit: commitIterable) {
                commits.add(commit);
            }
            for(Ticket ticket: tickets) {
                //TODO take the right commit? Date OpeningVersion > Date FixVersion
                RevCommit commit = retrieveCommit(commits, ticket);
                if(commit != null) setReleaseInfoInTicket(commit, ticket);
            }
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }*/


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

    public List<RevCommit> retrieveCommit(VersionRetriever versionRetriever) throws GitAPIException {
        Iterable<RevCommit> commitIterable = git.log().call();

        List<RevCommit> commits = new ArrayList<>();
        List<VersionInfo> projectVersion = versionRetriever.getProjectVersions();
        for(RevCommit commit: commitIterable) {
            if(!GitUtils.castToLocalDate(commit.getCommitterIdent().getWhen()).isAfter(projectVersion.get(projectVersion.size()-1).getDate())) {
                commits.add(commit);
            }
        }
        this.commitList = commits;

        return commits;
    }

    public List<Ticket> associateTicketAndCommit(VersionRetriever versionRetriever, CommitRetriever commitRetriever, List<Ticket> tickets) {
        try {
            List<RevCommit> commits = commitRetriever.retrieveCommit(versionRetriever);
            for (Ticket ticket : tickets) {
                List<RevCommit> associatedCommits = commitRetriever.retrieveAssociatedCommits(commits, ticket);
                ticket.setAssociatedCommits(associatedCommits);
                //GitUtils.printCommit(associatedCommits);
            }
            tickets.removeIf(ticket -> ticket.getAssociatedCommits().isEmpty());
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        return tickets;
    }

    public void retrieveChangesFromTickets(List<Ticket> tickets) {
        for(Ticket ticket: tickets) {
            retrieveChanges(ticket.getAssociatedCommits());
        }
    }

    private void retrieveChanges(List<RevCommit> commits) {
        for(RevCommit commit: commits) {
            retrieveChanges(commit);
        }
    }

    private void retrieveChanges(RevCommit commit) {
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
                System.out.println(entry.getNewPath() + " " + entry.getChangeType());

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public List<ReleaseCommits> getReleaseCommits(VersionRetriever versionRetriever, List<RevCommit> commits) throws GitAPIException, IOException {
        List<ReleaseCommits> releaseCommits = new ArrayList<>();
        LocalDate date = LocalDate.of(1900, 1, 1);
        for(VersionInfo versionInfo: versionRetriever.getProjectVersions()) {
            ReleaseCommits releaseCommit = GitUtils.getCommitsOfRelease(commits, versionInfo, date);
            if(releaseCommit != null) {
                Map<String, String> javaClasses = getClasses(releaseCommit.getLastCommit());
                releaseCommit.setJavaClasses(javaClasses);
                releaseCommits.add(releaseCommit);
            }
            date = versionInfo.getDate();
        }

        return releaseCommits;
    }

    private Map<String, String> getClasses(RevCommit commit) throws IOException {

        Map<String, String> javaClasses = new HashMap<>();

        RevTree tree = commit.getTree();	//We get the tree of the files and the directories that were belong to the repository when commit was pushed
        TreeWalk treeWalk = new TreeWalk(this.repository);	//We use a TreeWalk to iterate over all files in the Tree recursively
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while(treeWalk.next()) {
            //We are keeping only Java classes that are not involved in tests
            if(treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) {
                //We are retrieving (name class, content class) couples
                javaClasses.put(treeWalk.getPathString(), new String(this.repository.open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8));
            }
        }
        treeWalk.close();

        return javaClasses;

    }

}