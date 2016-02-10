/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.insset.jean_luc;

import com.jldeleage.mda.transfo.ExceptionTransformation;
import com.jldeleage.mda.transfo.Transformation;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jldeleage
 */
public class FabriqueSimple<T extends Transformation> implements FabriqueTransformation<T> {


    public FabriqueSimple(Class<T> classe) {
        this.classe = classe;
    }


    public T newTransformation(Object data) throws ExceptionTransformation {
        try {
            return classe.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(FabriqueSimple.class.getName()).log(Level.SEVERE, null, ex);
            throw new ExceptionTransformation(ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FabriqueSimple.class.getName()).log(Level.SEVERE, null, ex);
            throw new ExceptionTransformation(ex);
        }
    }


    private Class<T>  classe;


}
