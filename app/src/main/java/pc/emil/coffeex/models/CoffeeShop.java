package pc.emil.coffeex.models;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class CoffeeShop implements Serializable {

    private int id;
    private String name;
    private String phone;
    private String address;
    private String site;
    private String vk;
    private String fb;
    private String instagram;
    private ArrayList<Coffee> coffees;
    private float rating;

    public CoffeeShop(int id, String name, String phone,
                      String site, String fb, String instagram, String vk,
                      String country, String city, String street, String building, ArrayList<Coffee> arr, float rating) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.site = site.isEmpty() ? "-" : site;
        this.vk = vk.isEmpty()  ? "-" : vk;
        this.fb = fb.isEmpty() ? "-": fb;
        this.instagram = instagram.isEmpty() ? "-" : instagram;
        this.address = country + ", " + city + ", " + street + ", " + building;
        this.coffees = arr;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getSite() {
        return site;
    }

    public String getVk() {
        return vk;
    }

    public String getFb() {
        return fb;
    }

    public String getInstagram() {
        return instagram;
    }

    public ArrayList<Coffee> getCoffees() {
        return coffees;
    }

    public float getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return name + " \n" + address + "\n" + phone;
    }
}
