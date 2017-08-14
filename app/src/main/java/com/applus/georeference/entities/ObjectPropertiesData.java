package com.applus.georeference.entities;

import java.io.Serializable;
import java.util.Vector;

/**
 * Entidad que representa toda la información relativa a un objeto y a sus propiedades
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 *
 */
public class ObjectPropertiesData implements Serializable{
    long idObject;
    ObjectType ot;
    Vector<ObjectPropertie> p;
    Vector<String> values;

    public long getIdObject() {
        return idObject;
    }

    public void setIdObject(long idObject) {
        this.idObject = idObject;
    }

    public ObjectType getOt() {
        return ot;
    }

    public void setOt(ObjectType ot) {
        this.ot = ot;
    }

    public Vector<ObjectPropertie> getP() {
        return p;
    }

    public void setP(Vector<ObjectPropertie> p) {
        this.p = p;
    }

    public Vector<String> getValues() {
        return values;
    }

    public void setValues(Vector<String> values) {
        this.values = values;
    }
}
