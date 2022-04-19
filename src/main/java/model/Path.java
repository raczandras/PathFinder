package model;

import java.util.ArrayList;

public class Path {
    private Location start;
    private Location end;
    private double duration;
    private double length;
    private String distance;
    private String time;
    private ArrayList<String> instructions;
    public String from;
    public String to;

    public Path(Location start, Location end) {
        this.start = start;
        this.end = end;
        this.from = start.getZipcode() + " " + start.getCity() + ", " + start.getStreet() + " " + start.getHouse() + ".";
        this.to = end.getZipcode() + " " + end.getCity() + ", " + end.getStreet() + " " + end.getHouse() + ".";
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDistance() {
        return distance;
    }

    public String getTime() {
        return time;
    }

    public ArrayList<String> getInstructions() {
        return instructions;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    public double getDuration() {
        return duration;
    }

    public double getLength() {
        return length;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public void setInstructions(ArrayList<String> instructions) {
        this.instructions = instructions;
    }

    public void setProperties(){
        this.distance = String.format("%.1f", length/1000) + " km";
        this.time = ((int)duration/60) + " perc";
    }
}
