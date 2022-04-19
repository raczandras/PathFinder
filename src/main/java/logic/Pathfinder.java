package logic;

import model.Location;
import model.Node;
import model.Path;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Pathfinder {

    public ArrayList<Location> calculateBestRoute(ArrayList<Path> paths, ArrayList<Location> locations, String startTime){
        var routes = listPermutations(locations);
        deleteWrongRoutes(routes);

        if(!startTime.isEmpty()){
            deleteWrongTimes(routes, paths, startTime);
        }

        if( routes.isEmpty()){
            return new ArrayList<>();
        }

        int minCostIndex = createNodes(routes, paths);

        return routes.get(minCostIndex);
    }


    public Node aStar(Node start, Node target){
        PriorityQueue<Node> closedList = new PriorityQueue<>();
        PriorityQueue<Node> openList = new PriorityQueue<>();

        openList.add(start);

        while(!openList.isEmpty()){
            Node n = openList.peek();
            if( n == target){
                return n;
            }
            for( Node.Edge edge : n.neighbours){
                Node m = edge.node;
                double totalWeight = n.g + edge.weight;
                if(!openList.contains(m) && !closedList.contains(m)){
                    m.parent = n;
                    m.g = totalWeight;
                    m.f = m.g + m.h;
                    openList.add(m);
                }
                else{
                    if( totalWeight < m.g){
                        m.parent = n;
                        m.g = totalWeight;
                        m.f = m.g + m.h;
                        if( closedList.contains(m)){
                            closedList.remove(m);
                            openList.add(m);
                        }
                    }
                }
            }
            openList.remove(n);
            closedList.add(n);
        }
        return null;
    }

    private double calculateHeuristic(Location from, Location to, ArrayList<Path> paths) {
        double cost = 0;
        for ( Path path : paths){
            if( from.equals(path.getStart()) && to.equals(path.getEnd())){
                cost += path.getLength()/1000 + path.getDuration()/60;
                return cost;
            }
        }
        return cost;
    }

    private int createNodes(ArrayList<ArrayList<Location>> routes, ArrayList<Path> paths) {
        int minCostIndex = 0;
        double minCost = 0;

        for( int i = 0; i < routes.size(); i++){
            ArrayList<Node> nodes =  new ArrayList<>();
            Node head = new Node ( routes.get(i).get(0));
            nodes.add(head);

            for( int j = 1; j < routes.get(i).size(); j++){
                Node node = new Node(routes.get(i).get(j));
                double cost = calculateHeuristic(routes.get(i).get(j-1), routes.get(i).get(j), paths);
                Node parent = nodes.get(nodes.size()-1);
                ArrayList<Node.Edge> parentNeighbours = new ArrayList<>();
                parentNeighbours.add(new Node.Edge(cost, node));
                parent.neighbours = parentNeighbours;
                nodes.add(node);
            }
            Node solution = aStar(head, nodes.get(nodes.size()-1));
            if (solution.g < minCost){
                minCost = solution.g;
                minCostIndex = i;
            }
        }

        return minCostIndex;
    }

    public static ArrayList<ArrayList<Location>> listPermutations(ArrayList<Location> list) {
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
