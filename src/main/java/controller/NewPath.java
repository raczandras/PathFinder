package controller;

import api.ApiHandler;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import logic.Pathfinder;
import model.Location;
import model.Path;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class NewPath implements Initializable {
    @FXML
    public TextField zipcode;
    @FXML
    public TextField city;
    @FXML
    public TextField street;
    @FXML
    public TextField house;
    @FXML
    public TextField before;
    @FXML
    public TextField beforeTime;
    @FXML
    public TextField startTime;
    @FXML
    public VBox infoBox;
    @FXML
    public TableView<String> pathsTable;
    @FXML
    public TableColumn<String, String> tableInstructions;
    @FXML
    private TableView<Location> locationsTable;
    @FXML
    public TableColumn<Location, Integer> tableID;
    @FXML
    private TableColumn<Location, Integer> tableZipcode;
    @FXML
    private TableColumn<Location, String> tableCity;
    @FXML
    private TableColumn<Location, String> tableStreet;
    @FXML
    private TableColumn<Location, Integer> tableHouse;
    @FXML
    public TableColumn<Location, String> tableBefore;
    @FXML
    public TableColumn<Location, String> tableBeforeTime;
    @FXML
    public TableView<Path> pairsTable;
    @FXML
    public TableColumn<Path, String> tableFrom;
    @FXML
    public TableColumn<Path, String> tableTo;
    @FXML
    public TableColumn<Path, String> tableDistance;
    @FXML
    public TableColumn<Path, String> tableTime;

    private ArrayList<Location> locations = new ArrayList<>();
    private ApiHandler apiHandler = new ApiHandler();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void addLocation(ActionEvent actionEvent) {
        Location location = new Location(locations.size()+1, city.getText(), street.getText(), Integer.parseInt(zipcode.getText()), Integer.parseInt(house.getText()), before.getText(), beforeTime.getText());
        locations.add(location);
        printLocations();
        city.setText("");
        street.setText("");
        zipcode.setText("");
        house.setText("");
        beforeTime.setText("");
        before.setText("");
    }

    public void calculatePath(ActionEvent actionEvent) throws IOException, InterruptedException {
        ArrayList<Path> paths = apiHandler.queryPathProperties(locations);
        Pathfinder pathfinder = new Pathfinder();
        ArrayList<Location> bestRoute = pathfinder.calculateRoute(paths, locations, startTime.getText());

        if(bestRoute.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hiba");
            alert.setHeaderText(null);
            alert.setContentText("Nem létezik olyan út, amely megfelel ezeknek a feltételeknek!");
            alert.showAndWait();

            return;
        }

        ArrayList<Path> bestRoutePaths = pathfinder.getBestRoutePairs(bestRoute, paths);
        ArrayList<String> instructions = new ArrayList<>();

        for (Path path : bestRoutePaths){
            instructions.addAll(path.getInstructions());
            instructions.add("");
        }
        instructions.remove(instructions.size()-1);

        tableInstructions.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()));
        ObservableList<String> observableResult = FXCollections.observableArrayList();
        observableResult.addAll(instructions);
        pathsTable.setItems(observableResult);

        tableFrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        tableTo.setCellValueFactory(new PropertyValueFactory<>("to"));
        tableDistance.setCellValueFactory(new PropertyValueFactory<>("distance"));
        tableTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        ObservableList<Path> observablePaths = FXCollections.observableArrayList();
        observablePaths.addAll(bestRoutePaths);

        pairsTable.setItems(observablePaths);

    }

    public void printLocations(){
        tableZipcode.setCellValueFactory(new PropertyValueFactory<>("zipcode"));
        tableCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        tableStreet.setCellValueFactory(new PropertyValueFactory<>("street"));
        tableHouse.setCellValueFactory(new PropertyValueFactory<>("house"));
        tableID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableBefore.setCellValueFactory(new PropertyValueFactory<>("before"));
        tableBeforeTime.setCellValueFactory(new PropertyValueFactory<>("beforeTime"));

        ObservableList<Location> observableResult = FXCollections.observableArrayList();
        observableResult.addAll(locations);

        locationsTable.setItems(observableResult);
    }

}
