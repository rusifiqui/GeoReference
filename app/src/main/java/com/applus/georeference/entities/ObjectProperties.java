package com.applus.georeference.entities;

import java.io.Serializable;
import java.util.Vector;

/**
 * Entidad ObjectProperties
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 */
public class ObjectProperties implements Serializable{
    private Vector<Vector<ObjectPropertie>> properties;

    public Vector<Vector<ObjectPropertie>> getProperties() {
        return properties;
    }

    public void setProperties(Vector<Vector<ObjectPropertie>> properties) {
        this.properties = properties;
    }
}
