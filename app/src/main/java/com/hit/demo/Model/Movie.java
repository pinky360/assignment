package com.hit.demo.Model;

import java.io.Serializable;

public class Movie implements Serializable {

    String id;
    String title;
    String poster;
    String popularity;
    String rating;


    public Movie() {
    }

    public Movie(String id,String poster, String title, String popularity, String rating) {

        this.id=id;
        this.poster=poster;
        this.title=title;
        this.popularity=popularity;
        this.rating=rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPopularity() {
        return popularity;
    }

    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }


    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
