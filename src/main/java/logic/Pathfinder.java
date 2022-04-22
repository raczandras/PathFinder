package logic;

import model.Location;
import model.Node;
import model.Path;

import java.time.LocalTime;
import java.util.*;

public class Pathfinder {

    private final ArrayList<Location> locations;
    private final ArrayList<Path> paths;
    private final String startTime;

    public Pathfinder(ArrayList<Location> locations, ArrayList<Path> paths, String startTime) {
        this.locations = locations;
        this.paths = paths;
        this.startTime = startTime;
    }

    public ArrayList<Location> calculateRoute(){
        setBefores();

        return calculateBestPath();
    }

    private void setBefores() {
        for (Location location : locations) {
            ArrayList<Location> beforeLocations = new ArrayList<>();
            for(int i = 0; i < locations.size(); i++){
                String locationId = Integer.toString(locations.get(i).getId());
                if(location.getBefore().contains(locationId)){
                    beforeLocations.add(locations.get(i));
                }
            }
            location.setBeforeLocations(beforeLocations);
        }
    }

    private ArrayList<Location> calculateBestPath() {
        ArrayList<Node> nodes =  new ArrayList<>();

        for( int i = 0; i < locations.size(); i++){
            Node node = new Node ( locations.get(i));
            nodes.add(node);
        }

        for (Node node : nodes) {
            node.neighbours = createNeighbours(node, nodes);
        }

        Node head = createHeadNode(nodes);
        Node solution = aStar(head);

        if( solution == null){
            return new ArrayList<>();
        }

        ArrayList<Location> bestPath = new ArrayList<>();
        bestPath.add(solution.location);

        Node iterator = solution;

        while( iterator.parent != null){
            iterator = iterator.parent;
            bestPath.add(iterator.location);
        }
        bestPath.remove(null);
        Collections.reverse(bestPath);

        return bestPath;
    }

    private Node createHeadNode(ArrayList<Node> nodes) {
        Node head = new Node(null);
        ArrayList<Node.Edge> neighbours = new ArrayList<>();

        for (Node node : nodes) {
            Node.Edge edge = new Node.Edge(0,node);
            neighbours.add(edge);
        }
        head.neighbours = neighbours;

        return head;
    }

    private List<Node.Edge> createNeighbours(Node node, ArrayList<Node> nodes) {
        ArrayList<Node.Edge> neighbours = new ArrayList<>();
        for (Node neighbour : nodes) {
            if (node != neighbour) {
                double weight = calculateWeight(node.location, neighbour.location);
                Node.Edge edge = new Node.Edge(weight, neighbour);
                neighbours.add(edge);
            }
        }
        return neighbours;
    }

    private double calculateWeight(Location from, Location to) {
        double cost = 0;
        for ( Path path : paths){
            if( from.equals(path.getStart()) && to.equals(path.getEnd())){
                cost += path.getLength()/1000 + path.getDuration()/60;
                return cost;
            }
        }
        return cost;
    }

    public Node aStar(Node start){
        PriorityQueue<Node> closedList = new PriorityQueue<>();
        PriorityQueue<Node> openList = new PriorityQueue<>();

        openList.add(start);

        while(!openList.isEmpty()){
            Node node = openList.peek();
            if( isSolution(node)){
                return node;
            }
            for( Node.Edge edge : node.neighbours){
                Node neighbour = edge.node;
                if( isApplicable(node, neighbour.location) && willArriveInTime(node, neighbour.location)){
                    double totalWeight = node.g + edge.weight;
                    if(!openList.contains(neighbour) && !closedList.contains(neighbour)){
                        neighbour.parent = node;
                        neighbour.g = totalWeight;
                        neighbour.h = calculateHeuristicCost(neighbour);
                        neighbour.f = neighbour.g + neighbour.h;
                        openList.add(neighbour);
                        break;
                    }
                    else{
                        if( totalWeight < neighbour.g){
                            neighbour.parent = node;
                            neighbour.g = totalWeight;
                            neighbour.h = calculateHeuristicCost(neighbour);
                            neighbour.f = neighbour.g + neighbour.h;
                            if( closedList.contains(neighbour)){
                                closedList.remove(neighbour);
                                openList.add(neighbour);
                                break;
                            }
                        }
                    }
                }
            }
            openList.remove(node);
            closedList.add(node);
        }
        return null;
    }

    private boolean willArriveInTime(Node node, Location locationToTest) {
        if( startTime.isEmpty()){
            return true;
        }

        ArrayList<Location> possibleRoute = new ArrayList<>();
        Node iterator = node;
        possibleRoute.add(node.location);

        while( iterator.parent != null){
            iterator = iterator.parent;
            possibleRoute.add(iterator.location);
        }
        possibleRoute.remove(null);
        Collections.reverse(possibleRoute);
        possibleRoute.add(locationToTest);

        for (Location location: locations) {
            if(!possibleRoute.contains(location)){
                possibleRoute.add(location);
                if(!isGoodTime(possibleRoute)){
                    return false;
                }
                possibleRoute.remove(location);
            }
        }

        return true;
    }

    private boolean isGoodTime(ArrayList<Location> possibleRoute) {
        LocalTime arrivalTime = LocalTime.parse(startTime);
        for( int i = 0; i < possibleRoute.size()-1; i++){
            for ( Path path : paths){
                if( possibleRoute.get(i).equals(path.getStart()) && possibleRoute.get(i+1).equals(path.getEnd())){
                    arrivalTime = arrivalTime.plusMinutes((long)path.getDuration() / 60);
                    if(!arriveInTime(arrivalTime, possibleRoute.get(i+1))){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean arriveInTime(LocalTime arrivalTime, Location location){
        if(!location.getBeforeTime().isEmpty()){
            LocalTime arriveBy = LocalTime.parse(location.getBeforeTime());
            if(arrivalTime.isAfter(arriveBy)){
                return false;
            }
        }
        return true;
    }

    private boolean isApplicable(Node node, Location locationToTest) {
        ArrayList<Location> existingLocations = new ArrayList<>();
        String locationID = Integer.toString(locationToTest.getId());

        Node iterator = node;
        while(iterator.parent != null){
            existingLocations.add(iterator.location);
            iterator = iterator.parent;
        }
        existingLocations.add(locationToTest);

        for (Location location: locations) {
            if(!existingLocations.contains(location)){
                if(location.getBefore().contains(locationID)){
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isSolution(Node node) {
        Node iterator = node;
        int nodes = 0;

        while( iterator.parent != null){
            iterator = iterator.parent;
            nodes++;
        }

        if( nodes == locations.size()){
            return true;
        }

        return false;
    }

    private double calculateHeuristicCost(Node node) {
        double totalCost = node.g;
        Node iterator = node;
        int nodes = 0;

        while( iterator.parent != null){
            totalCost += iterator.parent.g;
            iterator = iterator.parent;
            nodes++;
        }

        totalCost = totalCost / nodes;
        int nodesLeft = locations.size() - nodes;

        return totalCost * nodesLeft;
    }


    public ArrayList<Path> getBestRoutePairs(ArrayList<Location> bestRoute) {
        ArrayList<Path> bestPaths = new ArrayList<>();
        for( int i = 0; i < bestRoute.size()-1; i++){
            for ( Path path : paths) {
                if( bestRoute.get(i).equals(path.getStart()) && bestRoute.get(i+1).equals(path.getEnd())){
                    bestPaths.add(path);
                }
            }
        }
        return bestPaths;
    }
}
