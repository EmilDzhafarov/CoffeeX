package pc.emil.coffeex.models;

import java.io.Serializable;

public class CoffeeShop implements Serializable{

    private int id;
    private String name;
    private String phone;
    private String address;
    private String site;
    private String vk;
    private String fb;
    private String instagram;


    public CoffeeShop(int id, String name, String phone,
                      String site,String fb,  String instagram,String vk,
                      String country, String city, String street, String building) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.site = site.isEmpty() ? "-" : site;
        this.vk = vk.isEmpty()  ? "-" : vk;
        this.fb = fb.isEmpty() ? "-": fb;
        this.instagram = instagram.isEmpty() ? "-" : instagram;
        this.address = country + ", " + city + ", " + street + ", " + building;
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

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getVk() {
        return vk;
    }

    public void setVk(String vk) {
        this.vk = vk;
    }

    public String getFb() {
        return fb;
    }

    public void setFb(String fb) {
        this.fb = fb;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    @Override
    public String toString() {
        return name + " \n" + address + "\n" + phone;
    }
}
