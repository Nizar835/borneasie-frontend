package org.example.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandeDTO {
    public List<Map<String, Object>> items = new ArrayList<>();
    public double total;
    public String clientNom;
}