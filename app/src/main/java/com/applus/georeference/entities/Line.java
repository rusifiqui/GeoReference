package com.applus.georeference.entities;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;
import java.util.Vector;

/**
 * Entidad Linea
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 */
public class Line implements Serializable{
    private long idLine;
    private long idProject;
    private Vector<UserObject> points;
    private float length;
    private String name;
    private String desc;

    public long getIdLine() {
        return idLine;
    }

    public void setIdLine(long idLine) {
        this.idLine = idLine;
    }

    public long getIdProject() {
        return idProject;
    }

    public void setIdProject(long idProject) {
        this.idProject = idProject;
    }

    public Vector<UserObject> getPoints() {
        return points;
    }

    public void setPoints(Vector<UserObject> points) {
        this.points = points;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
