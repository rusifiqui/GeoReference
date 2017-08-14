package com.applus.georeference.entities;

import java.util.Date;

/**
 * Entidad proyecto
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 */
public class Project {
    private long projectId;
    private String createDate;
    private String projectName;
    private String projectDesc;

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }
}
