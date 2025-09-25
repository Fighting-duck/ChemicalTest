package com.lsy.chemicaltest_new.domain;

import static com.blankj.utilcode.util.StringUtils.getString;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lsy.chemicaltest_new.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "history_table",
        foreignKeys = {
                @ForeignKey(
                        entity = Experimenter.class,
                        parentColumns = "id",
                        childColumns = "experimenter_id",
                        onDelete = ForeignKey.NO_ACTION,
                        onUpdate = ForeignKey.NO_ACTION
                ),
                @ForeignKey(
                        entity = ElecTestResult.class,
                        parentColumns = "id",
                        childColumns = "elec_id",
                        onDelete = ForeignKey.NO_ACTION,
                        onUpdate = ForeignKey.NO_ACTION
                ),
                @ForeignKey(
                        entity = ColoTestResult.class,
                        parentColumns = "id",
                        childColumns = "colo_id",
                        onDelete = ForeignKey.NO_ACTION,
                        onUpdate = ForeignKey.NO_ACTION
                ),
                @ForeignKey(
                        entity = ThermalTestResult.class,
                        parentColumns = "id",
                        childColumns = "thermal_id",
                        onDelete = ForeignKey.NO_ACTION,
                        onUpdate = ForeignKey.NO_ACTION
                ),
        },
        indices = {
                @Index(value = "experimenter_id"),
                @Index(value = "elec_id"),
                @Index(value = "colo_id"),
                @Index(value = "thermal_id"),
        })
public class History_multiple implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String saveTime;
    private Integer experimenter_id;
    private String sample_ids;//各直线的样本id组合(elec_sampleId,colo_sampleId,thermal_sampleId)
    private String curve_ids;//各直线的曲线id组合(elec_curveId,colo_curveId,thermal_curveId)
    private Integer elec_id;
    private Integer colo_id;
    private Integer thermal_id;
    private Integer degree_id;
    private Float credibility;
    private String remarks;

    @Ignore
    private Experimenter experimenter;
    @Ignore
    private List<Sample> sampleList = new ArrayList<Sample>();//唯一样本id的列表
    @Ignore
    private ElecTestResult elecTestResult;
    @Ignore
    private ColoTestResult coloTestResult;
    @Ignore
    private ThermalTestResult thermalTestResult;
    @Ignore
    private Temperature_Elec temperature_elec;

    public History_multiple(){}

    public History_multiple(History_multiple other){
        //简单类型
        this.id = other.id;
        this.saveTime = other.saveTime;
        this.experimenter_id = other.experimenter_id;
        this.sample_ids = other.sample_ids;
        this.elec_id = other.elec_id;
        this.colo_id = other.colo_id;
        this.thermal_id = other.thermal_id;
        this.degree_id = other.degree_id;
        this.credibility = other.credibility;
        this.remarks = other.remarks;
        //列表
        this.sampleList = new ArrayList<>(other.sampleList); // 复制列表
        //复杂类型
        this.experimenter =  other.experimenter != null ? new Experimenter(other.experimenter) : null;
        this.elecTestResult = other.elecTestResult != null ? new ElecTestResult(other.elecTestResult) : null;
        this.coloTestResult = other.coloTestResult !=null ? new ColoTestResult(other.coloTestResult) : null;
        this.thermalTestResult = other.thermalTestResult != null ? new ThermalTestResult(other.thermalTestResult) : null;
        this.temperature_elec = other.temperature_elec != null ? new Temperature_Elec(other.temperature_elec) : null;
    }

    public String getHistoryName(){
        StringBuilder name = new StringBuilder();
        if (sampleList.isEmpty()){
            name.append(getString(R.string.default_no));
        }else {
            for (Sample sample : sampleList) {
                name.append(sample.getName()).append("+");
            }
            name.deleteCharAt(name.length()-1);//去掉末尾的+
            name.append("--");
        }
        name.append(getString(R.string.title_sampleTest));
        return name.toString();
    }

    public String getSampleName() {
        if (sampleList.isEmpty()) return "";
        StringBuilder name = new StringBuilder();
        for (Sample sample : sampleList) {
            name.append(sample.getName()).append("+");
        }
        name.deleteCharAt(name.length()-1);//去掉末尾的+
        return name.toString();
    }

    //Setter 和 Getter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSaveTime() {
        return saveTime;
    }
    public void setSaveTime(String saveTime) {
        this.saveTime = saveTime;
    }
    public Integer getExperimenter_id() {
        return experimenter_id;
    }

    public void setExperimenter_id(Integer experimenter_id) {
        this.experimenter_id = experimenter_id;
    }

    public String getSample_ids() {
        return sample_ids;
    }

    public void setSample_ids(String sample_ids) {
        this.sample_ids = sample_ids;
    }

    public List<Sample> getSampleList() {
        return sampleList;
    }

    public void setSampleList(List<Sample> sampleList) {
        this.sampleList = sampleList;
    }

    public Integer getElec_id() {
        return elec_id;
    }

    public void setElec_id(Integer elec_id) {
        this.elec_id = elec_id;
    }

    public Integer getColo_id() {
        return colo_id;
    }

    public void setColo_id(Integer colo_id) {
        this.colo_id = colo_id;
    }

    public Integer getThermal_id() {
        return thermal_id;
    }

    public void setThermal_id(Integer thermal_id) {
        this.thermal_id = thermal_id;
    }

    public Float getCredibility() {
        return credibility;
    }

    public void setCredibility(Float credibility) {
        this.credibility = credibility;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Experimenter getExperimenter() {
        return experimenter;
    }

    public void setExperimenter(Experimenter experimenter) {
        this.experimenter = experimenter;
    }

    public ElecTestResult getElecTestResult() {
        return elecTestResult;
    }

    public void setElecTestResult(ElecTestResult elecTestResult) {
        this.elecTestResult = elecTestResult;
    }

    public ColoTestResult getColoTestResult() {
        return coloTestResult;
    }

    public void setColoTestResult(ColoTestResult coloTestResult) {
        this.coloTestResult = coloTestResult;
    }

    public ThermalTestResult getThermalTestResult() {
        return thermalTestResult;
    }

    public void setThermalTestResult(ThermalTestResult thermalTestResult) {
        this.thermalTestResult = thermalTestResult;
    }

    public Integer getDegree_id() {
        return degree_id;
    }

    public void setDegree_id(Integer degree_id) {
        this.degree_id = degree_id;
    }

    public Temperature_Elec getTemperature_elec() {
        return temperature_elec;
    }

    public void setTemperature_elec(Temperature_Elec temperature_elec) {
        this.temperature_elec = temperature_elec;
    }

    public String getCurve_ids() {
        return curve_ids;
    }

    public void setCurve_ids(String curve_ids) {
        this.curve_ids = curve_ids;
    }

    @NonNull
    @Override
    public String toString() {
        String result =  "History_multiple{" +
                "id=" + id +
                ", saveTime='" + saveTime + '\'' +
                ", experimenter_id=" + experimenter_id +
                ", sample_ids=" + sample_ids +
                ", elec_id=" + elec_id +
                ", colo_id=" + colo_id +
                ", thermal_id=" + thermal_id +
                ", credibility=" + credibility +
                ", remarks='" + remarks + '\'' +
                ", experimenter=" + experimenter +
                ", sampleList=" + sampleList ;
        if (elecTestResult != null) {
            result += ", elecTestResult=" + elecTestResult;
        }
        else result += ", elecTestResult=null";
        if (coloTestResult != null) {
            result += ", coloTestResult=" + coloTestResult;
        }
        else result += ", coloTestResult=null";
        if (thermalTestResult != null) {
            result += ", thermalTestResult=" + thermalTestResult;
        }
        else result += ", thermalTestResult=null";
        if (temperature_elec != null) {
            result += ", temperature_elec=" + temperature_elec;
        }
        else result += ", temperature_elec=null";
        result += '}';
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;

        if (obj.getClass() != this.getClass()) return false;
        else {
            History_multiple history_multiple = (History_multiple) obj;
            return this.id == history_multiple.getId();
        }
    }
}
