package com.rebelrobotics.scoutingapp;

public class Team {
    private String name;
    private int number;
    private String school;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    private String notes;

    // Default constructor (required by Firestore and other frameworks)
    public Team() {
    }

    // Constructor with parameters
    public Team(String name, int number, String school, String notes) {
        this.name = name;
        this.number = number;
        this.school = school;
        this.notes = notes;
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    // Optional: Override toString method for easier printing/debugging
    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                ", number=" + number +
                ", school='" + school + '\'' +
                '}';
    }
}
