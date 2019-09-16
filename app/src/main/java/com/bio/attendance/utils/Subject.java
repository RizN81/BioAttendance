package com.bio.attendance.utils;

/**
 * Created by Riz on 2/16/2017.
 */

public class Subject {
    Long id;
    String name;

    public Subject(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
