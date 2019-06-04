package fr.univ_tours.info.im_olap.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

class DopanQuery {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("cubeName")
    @Expose
    private String cubeName;

    @SerializedName("mdx")
    @Expose
    private String mdx;

    @SerializedName("original_id")
    @Expose
    private Integer OriginId;

    @SerializedName("measures")
    @Expose
    private List<String> measures = null;
    @SerializedName("groupBySet")
    @Expose
    private List<String> groupBySet = null;
    @SerializedName("selection")
    @Expose
    private List<DopanPredicate> selection = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCubeName() {
        return cubeName;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public List<String> getMeasures() {
        return measures;
    }

    public void setMeasures(List<String> measures) {
        this.measures = measures;
    }

    public List<String> getGroupBySet() {
        return groupBySet;
    }

    public void setGroupBySet(List<String> groupBySet) {
        this.groupBySet = groupBySet;
    }

    public List<DopanPredicate> getSelection() {
        return selection;
    }

    public void setSelection(List<DopanPredicate> selection) {
        this.selection = selection;
    }

    public String getMdx() {
        return mdx;
    }

    public void setMdx(String mdx) {
        this.mdx = mdx;
    }

    public Integer getOriginId() {
        return OriginId;
    }

    public void setOriginId(Integer originId) {
        OriginId = originId;
    }
}
