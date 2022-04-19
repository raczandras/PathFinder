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
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Path;

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

        JsonParser parser = new JsonParser();
        JsonObject jsonObj = (JsonObject) parser.parse(answer);
        Map<String, ArrayList<Map<String, ArrayList<Map<String, Map<String,Double>>>>>> responseMap = new Gson().fromJson(jsonObj.toString(),Map.class);
        Map<String, ArrayList<Map<String, ArrayList<Map<String,ArrayList<Map<String,String>>>>>>> actions = new Gson().fromJson(jsonObj.toString(),Map.class);

        var routes = responseMap.get("routes");
        var summary = routes.get(0).get("sections").get(0).get("summary");

        var paths = actions.get("routes").get(0).get("sections").get(0).get("actions");
        path.setDuration(summary.get("duration"));
        path.setLength(summary.get("length"));
        path.setProperties();
        ArrayList<String> instructions = new ArrayList<String>();
        for(int i = 0; i < paths.size(); i++){
            instructions.add(paths.get(i).get("instruction"));
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

        JsonParser parser = new JsonParser();
        JsonObject jsonObj = (JsonObject) parser.parse(answer);
        Map<String, ArrayList<Map<String, Map<String, Double>>>> responseMap = new Gson().fromJson(jsonObj.toString(),Map.class);
        var items = responseMap.get("items");
        var positions = items.get(0).get("position");

        location.setLongitude(positions.get("lng"));
        location.setLattitude(positions.get("lat"));
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
