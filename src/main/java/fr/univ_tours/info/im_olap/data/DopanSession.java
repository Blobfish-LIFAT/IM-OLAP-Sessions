package fr.univ_tours.info.im_olap.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

class DopanSession {
    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("cubeFile")
    @Expose
    private String cubeFile;
    @SerializedName("len")
    @Expose
    private Integer len;
    @SerializedName("queries")
    @Expose
    private List<DopanQuery> queries = null;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCubeFile() {
        return cubeFile;
    }

    public void setCubeFile(String cubeFile) {
        this.cubeFile = cubeFile;
    }

    public Integer getLen() {
        return len;
    }

    public void setLen(Integer len) {
        this.len = len;
    }

    public List<DopanQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<DopanQuery> queries) {
        this.queries = queries;
    }


}
