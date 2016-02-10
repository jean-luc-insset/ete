/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.insset.jean_luc;

import com.jldeleage.mda.transfo.ExceptionTransformation;
import com.jldeleage.mda.transfo.Transformation;
import org.w3c.dom.Element;

/**
 *
 * @author jldeleage
 */
public class FabriqueWrapper implements FabriqueTransformation {

    @Override
    public Transformation newTransformation(Object data) throws ExceptionTransformation {
        Element elt = (Element) data;
        return null;
    }
    
}
