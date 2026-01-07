package fr.isen.project.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("13775cd8-8e22-4fac-85a1-980ccfbe0bf7")
public class Commande {
    @objid ("b9203382-e8ef-4abb-a44e-fd190c525610")
    public int id;

    @objid ("2b3898e3-ae09-4962-93f2-19a8eeac8245")
    public Date date;

    @objid ("b1a9485b-bdd8-41b5-9830-d10b5d219268")
    public String statut;

    @objid ("3bf8534a-17c4-406a-bc3c-fcd32705dbca")
    public String nomClient;

    @objid ("4b8c6f84-7645-4dda-9700-16985549653a")
    public List<LigneCommande> ligneCommande = new ArrayList<LigneCommande> ();

}
