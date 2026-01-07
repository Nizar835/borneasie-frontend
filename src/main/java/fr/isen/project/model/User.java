package fr.isen.project.model;

public class User {
    private int id;
    private String name;
    private String email;

    // Constructeur vide (Obligatoire pour le code du prof)
    public User() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}