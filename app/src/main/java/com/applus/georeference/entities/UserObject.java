package com.applus.georeference.entities;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Entidad UserObject
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 */

public class UserObject implements Serializable{
    private long idUsertObject;
    private String createDate;
    private long idProject;
    private Double lat;
    private Double lon;
    private int type;
    private String description;

    public long getIdUsertObject() {
        return idUsertObject;
    }

    public void setIdUsertObject(long idUsertObject) {
        this.idUsertObject = idUsertObject;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public long getIdProject() {
        return idProject;
    }

    public void setIdProject(long idProject) {
        this.idProject = idProject;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLng getPosition(){
        return new LatLng(lat, lon);
    }
}
