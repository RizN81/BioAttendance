package com.bio.attendance.utils;

/**
 * Created by Riz on 2/16/2017.
 */

public class Student {
    Long			id;
    private String	name;
    private String	rollno;
    private String	year;
    private Long	subjectID;
    private String	fingerprint;

    public Student(Long id, String name, String rollno, String year, Long subjectID, String fingerprint) {
        this.id = id;
        this.name = name;
        this.rollno = rollno;
        this.year = year;
        this.subjectID = subjectID;
        this.fingerprint = fingerprint;
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

    public String getRollno() {
        return rollno;
    }

    public void setRollno(String rollno) {
        this.rollno = rollno;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Long getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(Long subjectID) {
        this.subjectID = subjectID;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
