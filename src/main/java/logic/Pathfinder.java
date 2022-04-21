package logic;

import model.Location;
import model.Node;
import model.Path;

import java.time.LocalTime;
import java.util.*;

public class Pathfinder {

    public ArrayList<Location> calculateRoute(ArrayList<Path> paths, ArrayList<Location> locations, String startTime){
        var routes = listPermutations(locations);
        deleteWrongRoutes(routes);

        if(!startTime.isEmpty()){
            deleteWrongTimes(routes, paths, startTime);
        }

        if( routes.isEmpty()){
            return new ArrayList<>();
        }

        return calculateBestPath(locations, paths);
    }

    private ArrayList<Location> calculateBestPath(ArrayList<Location> locations, ArrayList<Path> paths) {
        ArrayList<Node> nodes =  new ArrayList<>();

        for( int i = 0; i < locations.size(); i++){
            Node node = new Node ( locations.get(i));
            nodes.add(node);
        }

        for (Node node : nodes) {
            node.neighbours = createNeighbours(node, nodes, paths);
        }

        Node solution = aStar(nodes.get(0), locations);

        ArrayList<Location> bestPath = new ArrayList<>();
        bestPath.add(solution.location);

        Node iterator = solution;

        while( iterator.parent != null){
            iterator = iterator.parent;
            bestPath.add(iterator.location);
        }

        Collections.reverse(bestPath);

        return bestPath;
    }

    private List<Node.Edge> createNeighbours(Node node, ArrayList<Node> nodes, ArrayList<Path> paths) {
        ArrayList<Node.Edge> neighbours = new ArrayList<>();
        for (Node neighbour : nodes) {
            if (node != neighbour) {
                double weight = calculateWeight(node.location, neighbour.location, paths);
                Node.Edge edge = new Node.Edge(weight, neighbour);
                neighbours.add(edge);
            }
        }
        return neighbours;
    }

    private double calculateWeight(Location from, Location to, ArrayList<Path> paths) {
        double cost = 0;
        for ( Path path : paths){
            if( from.equals(path.getStart()) && to.equals(path.getEnd())){
                cost += path.getLength()/1000 + path.getDuration()/60;
                return cost;
            }
        }
        return cost;
    }


    public Node aStar(Node start, ArrayList<Location> locations){
        PriorityQueue<Node> closedList = new PriorityQueue<>();
        PriorityQueue<Node> openList = new PriorityQueue<>();

        openList.add(start);

        while(!openList.isEmpty()){
            Node node = openList.peek();
            if( isSolution(node, locations)){
                return node;
            }
            for( Node.Edge edge : node.neighbours){
                Node neighbour = edge.node;
                double totalWeight = node.g + edge.weight;
                if(!openList.contains(neighbour) && !closedList.contains(neighbour)){
                    neighbour.parent = node;
                    neighbour.g = totalWeight;
                    neighbour.h = calculateHeuristicCost(neighbour, locations );
                    neighbour.f = neighbour.g + neighbour.h;
                    openList.add(neighbour);
                    break;
                }
                else{
                    if( totalWeight < neighbour.g){
                        neighbour.parent = node;
                        neighbour.g = totalWeight;
                        neighbour.h = calculateHeuristicCost(neighbour, locations);
                        neighbour.f = neighbour.g + neighbour.h;
                        if( closedList.contains(neighbour)){
                            closedList.remove(neighbour);
                            openList.add(neighbour);
                            break;
                        }
                    }
                }
            }
            openList.remove(node);
            closedList.add(node);
        }
        return null;
    }

    private boolean isSolution(Node node, ArrayList<Location> locations) {
        Node iterator = node;
        int nodes = 1;

        while( iterator.parent != null){
            iterator = iterator.parent;
            nodes++;
        }

        if( nodes == locations.size()){
            return true;
        }

        return false;
    }

    private double calculateHeuristicCost(Node node, ArrayList<Location> locations) {
        double totalCost = node.g;
        Node iterator = node;
        int nodes = 1;

        while( iterator.parent != null){
            totalCost += iterator.parent.g;
            iterator = iterator.parent;
            nodes++;
        }

        totalCost = totalCost / nodes;
        int nodesLeft = locations.size() - nodes;

        return totalCost * nodesLeft;
    }



    public static ArrayList<ArrayList<Location>> listPermutations(ArrayList<Location> locations) {
        ArrayList<Location> list = new ArrayList<>(locations);

        if (list.size() == 0) {
            ArrayList<ArrayList<Location>> result = new ArrayList<ArrayList<Location>>();
            result.add(new ArrayList<Location>());
            return result;
        }
        ArrayList<ArrayList<Location>> returnList = new ArrayList<ArrayList<Location>>();
        Location firstElement = list.remove(0);

        ArrayList<ArrayList<Location>> recursiveReturn = listPermutations(list);
        for (ArrayList<Location> li : recursiveReturn) {
            for (int index = 0; index <= li.size(); index++) {
                ArrayList<Location> temp = new ArrayList<Location>(li);
                temp.add(index, firstElement);
                returnList.add(temp);
            }
        }
        return returnList;
    }

    private void deleteWrongTimes(ArrayList<ArrayList<Location>> routes, ArrayList<Path> paths, String startTime) {
        for( int i = 0; i < routes.size(); i++){
            if(isWrongTime(routes.get(i), paths, startTime)){
                routes.remove(i);
                i--;
            }
        }
    }

    private boolean isWrongTime(ArrayList<Location> locations, ArrayList<Path> paths, String startTime) {
        LocalTime arrivalTime = LocalTime.parse(startTime);
        for( int i = 0; i < locations.size()-1; i++){
            for ( Path path : paths){
                if( locations.get(i).equals(path.getStart()) && locations.get(i+1).equals(path.getEnd())){
                    if(arriveInTime(arrivalTime, path, locations.get(i+1))){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean arriveInTime(LocalTime arrivalTime, Path path, Location location){
        arrivalTime = arrivalTime.plusMinutes((long)path.getDuration() / 60);
        if(!location.getBeforeTime().isEmpty()){
            LocalTime arriveBy = LocalTime.parse(location.getBeforeTime());
            if(arrivalTime.isAfter(arriveBy)){
                return true;
            }
        }
        return false;
    }

    private void deleteWrongRoutes(ArrayList<ArrayList<Location>> allRoutes) {
        for( int i = 0; i < allRoutes.size(); i++){
            if(isBadPath(allRoutes.get(i))){
                allRoutes.remove(i);
                i--;
            }
        }
    }

    private Boolean isBadPath(ArrayList<Location> locations) {
        for( int i = 0; i < locations.size(); i++){
            for(String beforeID : locations.get(i).getBefore().split(",")) {
                if(!beforeID.isEmpty() && isBefore(locations, i, beforeID)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBefore(ArrayList<Location> locations, int index, String beforeID) {
        for( int i = 0; i< locations.size(); i++){
            if( locations.get(i).getId() == Integer.parseInt(beforeID) ){
                return index > i;
            }
        }
        return false;
    }


    public ArrayList<Path> getBestRoutePairs(ArrayList<Location> bestRoute, ArrayList<Path> paths) {
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
