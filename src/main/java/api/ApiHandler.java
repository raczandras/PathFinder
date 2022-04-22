package api;

import model.Location;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import model.Path;
import org.json.*;

public class ApiHandler{

    public static final String apiKey = "";

    public ArrayList<Path> queryPathProperties(ArrayList<Location> locations) throws IOException, InterruptedException {
        createCoordinates(locations);
        ArrayList<Path> paths = createPairs(locations);
        for(int i = 0; i < paths.size(); i++){
            getRouteProperties(paths.get(i));
        }
        return paths;
    }

    private void getRouteProperties(Path path) throws IOException, InterruptedException {
        String link = "https://router.hereapi.com/v8/routes?transportMode=car&origin="
                + path.getStart().getLattitude()
                + ","
                + path.getStart().getLongitude()
                + "&destination="
                + path.getEnd().getLattitude()
                + ","
                + path.getEnd().getLongitude()
                +"&lang=hu-HU&return=summary,polyline,actions,instructions&apiKey="
                + apiKey;

        URL url = new URL(link);
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(link))
                .header("accept", "application/json")
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String answer = new String(response.body(), StandardCharsets.UTF_8);

        JSONObject jsonObj = new JSONObject(answer);

        JSONObject summary = jsonObj.getJSONArray("routes").getJSONObject(0).getJSONArray("sections").getJSONObject(0).getJSONObject("summary");
        JSONArray actions = jsonObj.getJSONArray("routes").getJSONObject(0).getJSONArray("sections").getJSONObject(0).getJSONArray("actions");

        path.setDuration(summary.getDouble("duration"));
        path.setLength(summary.getDouble("length"));
        path.setProperties();
        ArrayList<String> instructions = new ArrayList<String>();

        for(int i = 0; i < actions.length(); i++){
            instructions.add(actions.getJSONObject(i).getString("instruction"));
        }
        path.setInstructions(instructions);
    }

    private void createCoordinates(ArrayList<Location> locations) throws IOException, InterruptedException {
        for( int i = 0; i < locations.size(); i++){
            setCoordinate(locations.get(i));
        }
    }

    private void setCoordinate(Location location) throws IOException, InterruptedException {
        String link = "https://geocode.search.hereapi.com/v1/geocode?apiKey="
                + apiKey
                + "&q="
                + location.getHouse() + "+"
                + URLEncoder.encode(location.getStreet(), StandardCharsets.UTF_8.name())
                + "%2C" + "+" + location.getZipcode()
                + "+" + URLEncoder.encode(location.getCity(), StandardCharsets.UTF_8.name())
                + "%2C" + "+Hungary";

        URL url = new URL(link);
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(link))
                .header("accept", "application/json")
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String answer = new String(response.body(), StandardCharsets.UTF_8);

        JSONObject jsonObj = new JSONObject(answer);
        JSONObject positions = jsonObj.getJSONArray("items").getJSONObject(0).getJSONObject("position");

        location.setLongitude(positions.getDouble("lng"));
        location.setLattitude(positions.getDouble("lat"));
    }

    public ArrayList<Path> createPairs(ArrayList<Location> locations) throws IOException {
        ArrayList<Path> paths = new ArrayList<Path>();

        for( int i = 0; i < locations.size(); i++){
            for( int j = 0; j < locations.size(); j++){
                if( i != j){
                    Path path = new Path(locations.get(i), locations.get(j));
                    paths.add(path);
                }
            }
        }
        return paths;
    }
}
