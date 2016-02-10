/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.insset.jean_luc;

import com.jldeleage.mda.transfo.ExceptionTransformation;
import com.jldeleage.mda.transfo.Transformation;


/**
 *
 * @author jldeleage
 */
public interface FabriqueTransformation<T extends Transformation> {

    public T  newTransformation(Object data) throws ExceptionTransformation;
}
