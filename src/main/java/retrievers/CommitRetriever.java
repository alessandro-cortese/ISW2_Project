package retrievers;

import model.Ticket;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import utils.GitUtils;

import java.util.ArrayList;

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

    private @Nullable RevCommit retrieveCommit(@NotNull ArrayList<RevCommit> commits, Ticket ticket) {
        for (RevCommit commit : commits) {
            if (commit.getFullMessage().contains(ticket.getKey())) {
                return commit;
            }
        }
        return null;
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