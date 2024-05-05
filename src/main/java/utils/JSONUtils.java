package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class JSONUtils {

    private JSONUtils() {}

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {

        try(InputStream inputStream = (new URI(url)).toURL().openStream()){

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String textJSON = readAllFIle(bufferedReader);
            return new JSONObject(textJSON);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readAllFIle(BufferedReader bufferedReader) throws IOException{

        StringBuilder stringBuilder = new StringBuilder();
        int cp;
        while((cp = bufferedReader.read()) != -1){
            stringBuilder.append((char) cp);
        }
        return stringBuilder.toString();

    }

}
