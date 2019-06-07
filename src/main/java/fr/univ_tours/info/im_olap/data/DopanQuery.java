package fr.univ_tours.info.im_olap.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
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

}
