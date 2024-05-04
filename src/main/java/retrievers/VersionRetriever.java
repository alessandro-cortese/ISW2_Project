package retrievers;

import model.VersionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.JSONUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class VersionRetriever {

    private ArrayList<VersionInfo> projectVersions;

    private static final String URL = "https://issues.apache.org/jira/rest/api/2/project/";
    private static final String VERSIONS = "versions";
    private static final String RELEASE_DATE = "releaseDate";
    private static final String ID = "id";
    private static final String NAME = "name";
    public VersionRetriever(String projectName){
        try{
            getVersions(projectName);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void getVersions(String projectName) throws IOException{

        String projectUrl = URL + projectName;
        JSONObject jsonObject = JSONUtils.readJsonFromUrl(projectUrl);
        JSONArray jsonArrayVersion = jsonObject.getJSONArray(VERSIONS);

        this.projectVersions = createVersionArray(jsonArrayVersion);

        this.projectVersions.sort(Comparator.comparing(VersionInfo::getDate));

        setIndex(this.projectVersions);

    }

    private @NotNull ArrayList<VersionInfo> createVersionArray(JSONArray jsonArrayVersion) {

        ArrayList<VersionInfo> versionInfoArrayList = new ArrayList<>();
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

    private void addRelease(String date, String name, String id, ArrayList<VersionInfo> versionInfoArrayList) {
        LocalDate localDate = LocalDate.parse(date);
        VersionInfo newReleaseInfo = new VersionInfo(id, name, localDate);
        versionInfoArrayList.add(newReleaseInfo);

    }
    private void setIndex(ArrayList<VersionInfo> projectVersions) {

        int index = 0;
        for(VersionInfo versionInfo: projectVersions){
            versionInfo.setIndex(index);
            index++;
        }

    }

    public ArrayList<VersionInfo> getAffectedVersion(@NotNull JSONArray versions){

        String id;
        ArrayList<VersionInfo> affectedVersions = new ArrayList<>();

        for(int i = 0; i < versions.length(); i++){
            if(versions.getJSONObject(i).has(RELEASE_DATE)){
                if(versions.getJSONObject(i).has(ID)){
                    id = versions.getJSONObject(i).get(ID).toString();
                    VersionInfo version = searchVersion(id);
                    if(version == null) throw new RuntimeException();
                    affectedVersions.add(version);
                }

            }
        }

        return affectedVersions;

    }

    private @Nullable VersionInfo searchVersion(String id) {

        for(VersionInfo versionInfo: this.projectVersions){
            if(Objects.equals(versionInfo.getId(), id)){
                return versionInfo;
            }
        }
        return null;
    }

    public ArrayList<VersionInfo> getProjectVersions(){
        return this.projectVersions;
    }

    public void setProjectVersions(ArrayList<VersionInfo> projectVersions){
        this.projectVersions = projectVersions;
    }
}