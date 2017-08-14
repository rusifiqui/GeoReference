package com.applus.georeference.entities;

import java.io.Serializable;
import java.util.Vector;

/**
 * Entidad ObjectTypes
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 */
public class ObjectTypes implements Serializable {
    Vector<ObjectType> objectTypes;

    public Vector<ObjectType> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(Vector<ObjectType> objectTypes) {
        this.objectTypes = objectTypes;
    }
}
