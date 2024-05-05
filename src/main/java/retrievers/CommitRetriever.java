package retrievers;

import model.Ticket;
import model.VersionInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.GitUtils;
import utils.RegularExpression;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class CommitRetriever {

    private Git git;
    private Repository repository;

    public CommitRetriever(String repositoryPath) {
        this.repository = GitUtils.getRepository(repositoryPath);
        this.git = new Git(repository);
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

    public void retrieveChanges(RevCommit commit) {
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

}