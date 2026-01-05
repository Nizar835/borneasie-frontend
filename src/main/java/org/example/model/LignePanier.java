package org.example.model;

public class LignePanier {
    public Plat plat;
    public int quantite;
    public String options;

    public LignePanier(Plat p, int q, String o) {
        this.plat = p;
        this.quantite = q;
        this.options = o;
    }

    public double getTotal() {
        return plat.prix * quantite;
    }
}