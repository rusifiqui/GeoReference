package com.applus.georeference.entities;

import java.io.Serializable;

/**
 * Entidad ObjectPropertie
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 */
public class ObjectPropertie implements Serializable{

    private long idObjectPropertie;
    private long idObject;
    private String propertieName;

    // Tipo de dato de la propiedad
    // 0 -> Texto
    // 1 -> Número
    // 2 -> Fecha
    private int propertieType;

    public long getIdObjectPropertie() {
        return idObjectPropertie;
    }

    public void setIdObjectPropertie(long idObjectPropertie) {
        this.idObjectPropertie = idObjectPropertie;
    }

    public long getIdObject() {
        return idObject;
    }

    public void setIdObject(long idObject) {
        this.idObject = idObject;
    }

    public String getPropertieName() {
        return propertieName;
    }

    public void setPropertieName(String propertieName) {
        this.propertieName = propertieName;
    }

    public int getPropertieType() {
        return propertieType;
    }

    public void setPropertieType(int propertieType) {
        this.propertieType = propertieType;
    }
}
