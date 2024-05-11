package retrievers;

import model.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.JSONUtils;
import utils.VersionUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;

public class VersionRetriever {

    private List<Version> projectVersions;

    private static final String URL = "https://issues.apache.org/jira/rest/api/2/project/";
    private static final String VERSIONS = "versions";
    private static final String RELEASE_DATE = "releaseDate";
    private static final String ID = "id";
    private static final String NAME = "name";

    public VersionRetriever(String projectName){
        try{
            getVersions(projectName);
            VersionUtil.printVersion(projectVersions);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void getVersions(String projectName) throws IOException{

        String projectUrl = URL + projectName;
        JSONObject jsonObject = JSONUtils.readJsonFromUrl(projectUrl);
        JSONArray jsonArrayVersion = jsonObject.getJSONArray(VERSIONS);

        this.projectVersions = createVersionArray(jsonArrayVersion);

        this.projectVersions.sort(Comparator.comparing(Version::getDate));

        setIndex(this.projectVersions);

    }

    private void setIndex(List<Version> projectVersions) {

        int index = 0;
        for(Version versionInfo: projectVersions){
            versionInfo.setIndex(index);
            index++;
        }

    }

    public List<Version> getAffectedVersion(@NotNull JSONArray versions){

        String id;
        List<Version> affectedVersions = new ArrayList<>();

        for(int i = 0; i < versions.length(); i++){
            if(versions.getJSONObject(i).has(RELEASE_DATE) && versions.getJSONObject(i).has("id")){
                id = versions.getJSONObject(i).get(ID).toString();
                Version version = searchVersion(id);
                if(version == null)
                    throw new RuntimeException();
                affectedVersions.add(version);
            }
        }

        return affectedVersions;

    }

    private @Nullable Version searchVersion(String id) {

        for(Version versionInfo: this.projectVersions){
            if(Objects.equals(versionInfo.getId(), id)){
                return versionInfo;
            }
        }
        return null;
    }

    private @NotNull List<Version> createVersionArray(JSONArray jsonArrayVersion) {

        List<Version> versionInfoArrayList = new ArrayList<>();
        for(int i = 0; i < jsonArrayVersion.length(); i++){
            String name = "";
            String id = "";
            if(jsonArrayVersion.getJSONObject(i).has(RELEASE_DATE)){

                if(jsonArrayVersion.getJSONObject(i).has(NAME))
                    name = jsonArrayVersion.getJSONObject(i).get(NAME).toString();
                if(jsonArrayVersion.getJSONObject(i).has(ID))
                    id = jsonArrayVersion.getJSONObject(i).get(ID).toString();
                addRelease(jsonArrayVersion.getJSONObject(i).get(RELEASE_DATE).toString(), name, id, versionInfoArrayList);

            }
        }

        return versionInfoArrayList;
    }

    private void addRelease(String date, String name, String id, List<Version> versionInfoArrayList) {
        LocalDate localDate = LocalDate.parse(date);
        Version newReleaseInfo = new Version(id, name, localDate);
        versionInfoArrayList.add(newReleaseInfo);
    }

    public List<Version> getProjectVersions(){
        return this.projectVersions;
    }

    public void setProjectVersions(List<Version> projectVersions){
        this.projectVersions = projectVersions;
    }
}