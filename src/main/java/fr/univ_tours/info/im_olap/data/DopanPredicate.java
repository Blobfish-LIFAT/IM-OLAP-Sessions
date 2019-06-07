package fr.univ_tours.info.im_olap.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class DopanPredicate {
    @SerializedName("level")
    @Expose
    private String level;
    @SerializedName("value")
    @Expose
    private String value;
}
