package utils;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class GitUtils {

    public static Repository getRepository(String repoPath) throws IOException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        return repositoryBuilder.setGitDir(new File(repoPath + "/.git")).build();

    }

}
