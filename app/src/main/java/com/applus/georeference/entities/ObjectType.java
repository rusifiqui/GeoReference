package com.applus.georeference.entities;

import java.io.Serializable;

/**
 * Entidad objeto
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 *
 */
public class ObjectType implements Serializable{
    private long objectId;
    private String objectName;

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
}
