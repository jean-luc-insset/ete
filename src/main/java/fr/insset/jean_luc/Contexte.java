/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.insset.jean_luc;

import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Document;

/**
 * Pour &eacute;viter qu'un m&ecirc;me module ne s'ex&eacute;cute plusieurs
 * fois, le contexte m&eacute;morise les modules d&eacute;j&eagrave;
 * ex&eacute;cut&eacute;s.
 *
 * @author jldeleage
 */
public class Contexte {

    public Contexte(Document modele) {
        this.modele = modele;
    }

    public Document getModele() {
        return modele;
    }


    public boolean jeVoudraisMExecuter(String inNom) {
        return dejaExecutes.add(inNom);
    }


    private Set<String>     dejaExecutes = new HashSet<String>();
    private Document        modele;

}
