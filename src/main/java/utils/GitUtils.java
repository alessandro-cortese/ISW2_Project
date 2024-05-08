package utils;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import model.ReleaseCommits;
import model.Version;
import java.time.ZoneId;
import java.util.ArrayList;

public class GitUtils {

    private GitUtils() {}

    public static Repository getRepository(String repoPath) {
        try{
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            return repositoryBuilder.setGitDir(new File(repoPath + "/.git")).build();
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    public static LocalDate castToLocalDate(Date date) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return LocalDate.parse(dateFormatter.format(date));
    }

    public static void printCommit(List<RevCommit> commits){
        for(RevCommit commit: commits){
            System.out.println("Commit: " + commit.getAuthorIdent().getName());
            System.out.println(commit.getFullMessage());
        }
    }

    public static ReleaseCommits getCommitsOfRelease(List<RevCommit> commitsList, Version release, LocalDate firstDate) {

        List<RevCommit> matchingCommits = new ArrayList<>();
        LocalDate lastDate = release.getDate();

        for(RevCommit commit : commitsList) {
            LocalDate commitDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            //if firstDate < commitDate <= lastDate then add the commit in matchingCommits list
            if(commitDate.isAfter(firstDate) && (!commitDate.isAfter(lastDate))) {
                matchingCommits.add(commit);
            }

        }

        if(matchingCommits.isEmpty()) return null;

        RevCommit lastCommit = getLastCommit(matchingCommits);

        return new ReleaseCommits(release, matchingCommits, lastCommit);

    }

    private static RevCommit getLastCommit(List<RevCommit> commitsList) {

        RevCommit lastCommit = commitsList.get(0);
        for(RevCommit commit : commitsList) {
            //if commitDate > lastCommitDate then refresh lastCommit
            if(commit.getCommitterIdent().getWhen().after(lastCommit.getCommitterIdent().getWhen())) {
                lastCommit = commit;
            }
        }
        return lastCommit;
    }

}
