package model;

import java.util.List;

public class Node implements Comparable<Node> {

    public Location location;
    public Node parent = null;
    public List<Edge> neighbours;
    public double f = 0;
    public double g = 0;
    public double h = 0;

    public Node(Location location){
        this.location = location;
    }

    @Override
    public int compareTo(Node n){
        return Double.compare(this.f, n.f);
    }

    public static class Edge {
        public Edge(double weight, Node node){
            this.weight = weight;
            this.node = node;
        }

        public double weight;
        public Node node;
    }
}
