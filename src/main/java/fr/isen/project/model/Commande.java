package fr.isen.project.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//begin of modifiable zone(Javadoc).......C/13775cd8-8e22-4fac-85a1-980ccfbe0bf7

//end of modifiable zone(Javadoc).........E/13775cd8-8e22-4fac-85a1-980ccfbe0bf7
public class Commande {
//begin of modifiable zone(Javadoc).......C/b9203382-e8ef-4abb-a44e-fd190c525610

    //end of modifiable zone(Javadoc).........E/b9203382-e8ef-4abb-a44e-fd190c525610
    public int id;

//begin of modifiable zone(Javadoc).......C/2b3898e3-ae09-4962-93f2-19a8eeac8245

    //end of modifiable zone(Javadoc).........E/2b3898e3-ae09-4962-93f2-19a8eeac8245
    public Date date;

//begin of modifiable zone(Javadoc).......C/b1a9485b-bdd8-41b5-9830-d10b5d219268

    //end of modifiable zone(Javadoc).........E/b1a9485b-bdd8-41b5-9830-d10b5d219268
    public String statut;

//begin of modifiable zone(Javadoc).......C/3bf8534a-17c4-406a-bc3c-fcd32705dbca

    //end of modifiable zone(Javadoc).........E/3bf8534a-17c4-406a-bc3c-fcd32705dbca
    public String nomClient;

//begin of modifiable zone(Javadoc).......C/4b8c6f84-7645-4dda-9700-16985549653a

    //end of modifiable zone(Javadoc).........E/4b8c6f84-7645-4dda-9700-16985549653a
    public List<LigneCommande> ligneCommande = new ArrayList<LigneCommande> ();

}

