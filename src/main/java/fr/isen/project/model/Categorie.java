package fr.isen.project.model;

import java.util.ArrayList;
import java.util.List;
import com.modeliosoft.modelio.javadesigner.annotations.objid;

@objid ("65920de3-712c-43a7-897a-c8b2f149588f")
public class Categorie {
    @objid ("003dbe7f-a700-4208-836f-1abab3a3e6d7")
    public int id ;

    @objid ("2d714e09-455b-46ae-a2f4-4b6dcc0700c5")
    public String nom;

    @objid ("3535a6d0-5ce0-42f7-ac6a-afa62806333f")
    public String visuel;

    @objid ("4defafc2-037e-4f42-93ff-2012d964553c")
    public List<Plat> plats = new ArrayList<Plat> ();

}
