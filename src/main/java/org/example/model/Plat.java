package org.example.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Plat {
    public String id;
    public String nom;
    public String description;
    public double prix;
    public String categorie;
    public boolean disponible;

    @Override
    public String toString() { return nom; }
}