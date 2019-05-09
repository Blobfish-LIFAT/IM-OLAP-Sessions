package fr.univ_tours.info.im_olap.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DopanPredicate {
    @SerializedName("level")
    @Expose
    private String level;
    @SerializedName("value")
    @Expose
    private String value;

    public DopanPredicate() {
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" +
                "level=\"" + level + '\"' +
                ", value=\"" + value + '\"' +
                '}';
    }
}
