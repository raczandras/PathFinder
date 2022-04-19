package model;

public class Location {

    private int id;
    private String city;
    private String street;
    private int zipcode;
    private int house;
    private double lattitude;
    private double longitude;
    private String before;
    private String beforeTime;

    public Location(int id, String city, String street, int zipcode, int house, String before, String beforeTime) {
        this.id = id;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.house = house;
        this.before = before;
        this.beforeTime = beforeTime;
    }

    public double getLattitude() {
        return lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public int getZipcode() {
        return zipcode;
    }

    public int getHouse() {
        return house;
    }

    public String getBefore() {
        return before;
    }

    public String getBeforeTime() {
        return beforeTime;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return getZipcode() == location.getZipcode() && getHouse() == location.getHouse() && getCity().equals(location.getCity()) && getStreet().equals(location.getStreet());
    }

}
