package utils;

import model.Version;
import org.jetbrains.annotations.Nullable;
import retrievers.VersionRetriever;

import java.time.LocalDate;
import java.util.List;
public class VersionUtil {

    public static void printVersion(List<Version> versionList) {
        for(Version version: versionList) {
            System.out.println("Version: "
                    + version.getId() + ", "
                    + version.getName() + ", "
                    + version.getDate());
        }
    }

    public static @Nullable Version retrieveNextRelease(VersionRetriever versionRetriever, LocalDate localDate) {
        for(Version version : versionRetriever.getProjectVersions()) {
            LocalDate releaseDate = version.getDate();
            if(!releaseDate.isBefore(localDate)) {
                return version;
            }
        }
        return null;
    }
}