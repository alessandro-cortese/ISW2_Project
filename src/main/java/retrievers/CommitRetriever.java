package retrievers;

import model.Ticket;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public @Nullable ArrayList<RevCommit> retrieveAssociatedCommit(@NotNull ArrayList<RevCommit> commits, Ticket ticket) throws GitAPIException{

        Iterable<RevCommit> commitIterable = git.log().call();
        for(RevCommit commit: commitIterable)
            commits.add(commit);

        ArrayList<RevCommit> associatedCommit = new ArrayList<>();
        for(RevCommit commit: commits){
            if(commit.getFullMessage().contains(ticket.getKey())){
                associatedCommit.add(commit);
            }
        }
        return associatedCommit;
    }

    public ArrayList<RevCommit> retrieveCommit() throws GitAPIException {
        Iterable<RevCommit> commitIterable = git.log().call();

        ArrayList<RevCommit> commits = new ArrayList<>();
        for(RevCommit commit: commitIterable) {
            commits.add(commit);
        }

        return commits;
    }

    public ArrayList<Ticket> associateTicketAndCommit(CommitRetriever commitRetriever, ArrayList<Ticket> tickets) {
        try {
            ArrayList<RevCommit> commits = commitRetriever.retrieveCommit();
            for (Ticket ticket : tickets) {
                ArrayList<RevCommit> associatedCommits = commitRetriever.retrieveAssociatedCommit(commits, ticket);
                ticket.setAssociatedCommits(associatedCommits);
                //GitUtils.printCommit(associatedCommits);
            }
            tickets.removeIf(ticket -> ticket.getAssociatedCommits().isEmpty());
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        return tickets;
    }

//    private @Nullable RevCommit retrieveCommit(@NotNull ArrayList<RevCommit> commits, Ticket ticket) {
//        for(RevCommit commit: commits) {
//            if(commit.getFullMessage().contains(ticket.getKey())) {
//                return commit;
//            }
//        }
//        return null;
//    }
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