package com.example.mostri_in_tasca;

public class ProfileClass {
    private String username;
    private String xp;
    private String lp;
    private String img;

    public ProfileClass(String username, String xp, String lp, String img) {
        this.username = username;
        this.xp = xp;
        this.lp = lp;
        this.img = img;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getXp() {
        return xp;
    }

    public void setXp(String xp) {
        this.xp = xp;
    }

    public String getLp() {
        return lp;
    }

    public void setLp(String lp) {
        this.lp = lp;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
