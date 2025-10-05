package com.lsy.chemicaltest_new.domain;


import static com.blankj.utilcode.util.StringUtils.getString;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 标准曲线
 */
@Entity(tableName = "standard_curve_table",
        foreignKeys = {
            @ForeignKey(
                    entity = Sample.class,
                    parentColumns = "id",
                    childColumns = "sample_id",
                    onDelete = ForeignKey.NO_ACTION,
                    onUpdate = ForeignKey.CASCADE
            ),
        },
        indices = {
            @Index(value = "sample_id"),
            @Index(value = "point_set")
        }
)
public class StandardCurve implements Cloneable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id; // 标准曲线ID
    @ColumnInfo(name = "name")
    private String name; // 标准曲线名称
    @ColumnInfo(name = "sample_id")
    private Integer sample_id;//关联样品
    @ColumnInfo(name = "type")
    private Integer type; //曲线类型 1：电信号检测 2：比色信号检测 3：光热图像检测
    @ColumnInfo(name = "point_set")
    private String point_set;//关联点集表
    @ColumnInfo(name = "CORR")
    private Float CORR; // data的线性拟合系数----相关系数
    @ColumnInfo(name = "expression")
    private String expression;//函数表达式 k,b
    @ColumnInfo(name = "x_axis_unit")
    private String x_axis_unit; // 浓度单位---x轴单位
    @ColumnInfo(name = "y_axis_unit")
    private String y_axis_unit; // 单位---y轴单位
    @ColumnInfo(name = "min_CO")
    private Float min_CO; // 浓度最小值
    @ColumnInfo(name = "max_CO")
    private Float max_CO; // 浓度最大值
    @ColumnInfo(name = "minCorr")
    private Float minCorr;//最小相关系数，小于此数提示异常，需要校准
    @ColumnInfo(name = "description")
    private String description; // 标准曲线描述
    @ColumnInfo(name = "validity")
    private int validity = 1; // 0：无效 1：有效
    @Ignore
    private Sample sample;
    @Ignore
    private List<Point> pointList = new ArrayList<>(); // 标准曲线数据点集合<x,y>
    @Ignore
    private Expression formula = new Expression();
    @Ignore
    public static final HashMap<Integer,String> TYPE = new HashMap<>();
    @Ignore
    private static final Map<Integer, Integer> colorMap = new HashMap<>();
    static {

        TYPE.put(1,getString(R.string.elec));
        TYPE.put(2,getString(R.string.colo));
        TYPE.put(3,getString(R.string.thermal));

        colorMap.put(1, R.color.line_electric);
        colorMap.put(2, R.color.line_colorimetric);
        colorMap.put(3, R.color.line_thermal);
    }

    /***
     * 返回颜色资源 ID
     * @return
     */
    public Integer getCurveColor() {
        return colorMap.get(type); // 直接返回 null 如果键不存在
    }
    /***
     * 返回颜色资源 ID
     * @return
     */
    public static Integer getCurveColor(int type) {
        return colorMap.get(type); // 直接返回 null 如果键不存在
    }

    public String getCurveType() {
        return TYPE.get(type);
    }

    /***
     * 由值找键，即通过类型字符串找类型id
     * @param typeName
     * @return
     */
    public static Integer getTypeId(String typeName) {
        for (Map.Entry<Integer, String> entry : TYPE.entrySet()) {
            if (typeName.equals(entry.getValue())) {
                return entry.getKey(); // 返回匹配的键
            }
        }
        return null; // 如果没有找到匹配的值，返回 null
    }

    /**
     * 通过 x 值计算 y曲线 值
     * @param x x 值
     * @return y 值
     */
    public Float calculate(Float x) {
        if (formula != null) {
            return formula.calculate(x);
        } else if (expression != null && !expression.isEmpty()) {
            try {
                String[] ab = expression.split(",");
                if (ab.length != 2) {
                    throw new IllegalArgumentException("表达式格式错误：必须包含两个由逗号分隔的值。");
                }
                float a = Float.parseFloat(ab[0]);
                float b = Float.parseFloat(ab[1]);
                return a * x + b;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("表达式格式错误：请输入两个有效的浮点数，中间用逗号分隔。", e);
            }
        } else {
            throw new IllegalStateException("未提供有效的公式或表达式。");
        }
    }

    /**
     * 通过 y 值计算 x 值
     * @param y y 值 (电流、blue、温度)
     * @return x 值 (浓度)
     */
    public Float calculateX_toY(Float y) {
        if (formula != null) {
            return formula.calculateX_toY(y);
        }
        else if (expression != null && !expression.isEmpty()) {
            try {
                String[] kb = expression.split(",");
                if (kb.length != 2) {
                    throw new IllegalArgumentException("表达式格式错误：必须包含两个由逗号分隔的值。");
                }
                float k = Float.parseFloat(kb[0]);
                float b = Float.parseFloat(kb[1]);
                return new Expression(k, b).calculateX_toY(y);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("表达式格式错误：请输入两个有效的浮点数，中间用逗号分隔。", e);
            }
        } else {
            throw new IllegalStateException("未提供有效的公式或表达式。");
        }
    }

    /***
     * 判断此类型是否是曲线类型之一
     * @param type
     * @return
     */
    public static Boolean isCurveType(int type){
       return TYPE.containsKey(type);
    }
    public static Boolean isCurveType(String name){
        return TYPE.containsValue(name);
    }

    /***
     * 获取 curve 类型名称
     * @param type 类型id
     * @return 类型名称
     */
    public static String getCurveType(int type){
        return TYPE.get(type);
    }

    public static List<Point> getPointList(String point_list) {
        //1.字符串转化为List
        // 去除首尾的方括号
        String trimmedInput = point_list.substring(1, point_list.length() - 1);
        // 按逗号分隔字符串
        String[] items = trimmedInput.split(",");
        // 将每个分隔后的字符串转换为整数，并收集到列表中
        List<Integer> points_ids = new ArrayList<>();
        for (String item : items) {
            // 去除每个元素周围的空白字符
            String trimmedItem = item.trim();
            // 将字符串转换为整数
            Integer number = Integer.parseInt(trimmedItem);
            // 添加到结果列表中
            points_ids.add(number);
        }
        //2.循环读取数据库得到point
        List<Point> points = new ArrayList<>();
        for (Integer point_id : points_ids) {
            // 查询数据库
            Point point = MyApplication.DATABASE_INSTANCE.getPointDao().findById(point_id);
            points.add(point);
        }
        //3.返回pointList
        return points;
    }

    public StandardCurve(){}
    // 私有构造函数，防止外部直接创建对象
    private StandardCurve(StandardCurveBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.sample_id = builder.sample_id;
        this.type = builder.type;
        this.point_set = builder.point_set;
        this.CORR = builder.CORR;
        this.expression = builder.expression;
        this.x_axis_unit = builder.x_axis_unit;
        this.y_axis_unit = builder.y_axis_unit;
        this.min_CO = builder.min_CO;
        this.max_CO = builder.max_CO;
        this.minCorr = builder.minCorr;
        this.description = builder.description;
        this.validity = builder.validity;
    }

    // 静态内部类Builder
    public static class StandardCurveBuilder {
        private int id; // 标准曲线ID
        private String name; // 标准曲线名称
        private Integer sample_id;//关联样品
        private Integer type; //曲线类型 1：电信号检测 2：比色信号检测 3：光热图像检测
        private String point_set;//关联点集表
        private Float CORR; // data的线性拟合系数----相关系数
        private String expression;//函数表达式
        private String x_axis_unit; // 浓度单位---x轴单位
        private String y_axis_unit; // 单位---y轴单位
        private Float min_CO; // 浓度最小值
        private Float max_CO; // 浓度最大值
        private Float minCorr;//最小相关系数，小于此数提示异常，需要校准
        private String description; // 标准曲线描述
        private int validity = 1; // 0：无效 1：有效

        public StandardCurveBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public StandardCurveBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public StandardCurveBuilder setSample_id(Integer sample_id) {
            this.sample_id = sample_id;
            return this;
        }

        public StandardCurveBuilder setType(Integer type) {
            this.type = type;
            return this;
        }

        public StandardCurveBuilder setpoint_set(String point_set) {
            this.point_set = point_set;
            return this;
        }

        public StandardCurveBuilder setCORR(Float CORR) {
            this.CORR = CORR;
            return this;
        }

        public StandardCurveBuilder setExpression(String expression) {
            this.expression = expression;
            return this;
        }

        public StandardCurveBuilder setX_axis_unit(String x_axis_unit) {
            this.x_axis_unit = x_axis_unit;
            return this;
        }

        public StandardCurveBuilder setY_axis_unit(String y_axis_unit) {
            this.y_axis_unit = y_axis_unit;
            return this;
        }

        public StandardCurveBuilder setMin_CO(Float min_CO) {
            this.min_CO = min_CO;
            return this;
        }

        public StandardCurveBuilder setMax_CO(Float max_CO) {
            this.max_CO = max_CO;
            return this;
        }

        public StandardCurveBuilder setMinCorr(Float minCorr) {
            this.minCorr = minCorr;
            return this;
        }

        public StandardCurveBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public StandardCurveBuilder setValidity(int validity) {
            this.validity = validity;
            return this;
        }

        // 构建方法
        public StandardCurve build() {
            return new StandardCurve(this);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSample_id() {
        return sample_id;
    }

    public void setSample_id(Integer sample_id) {
        this.sample_id = sample_id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPoint_set() {
        return point_set;
    }

    public void setPoint_set(String point_set) {
        this.point_set = point_set;
    }

    public Float getCORR() {
        return CORR;
    }

    public void setCORR(Float CORR) {
        this.CORR = CORR;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getX_axis_unit() {
        return x_axis_unit;
    }

    public void setX_axis_unit(String x_axis_unit) {
        this.x_axis_unit = x_axis_unit;
    }

    public String getY_axis_unit() {
        return y_axis_unit;
    }

    public void setY_axis_unit(String y_axis_unit) {
        this.y_axis_unit = y_axis_unit;
    }

    public Float getMin_CO() {
        return min_CO;
    }

    public void setMin_CO(Float min_CO) {
        this.min_CO = min_CO;
    }

    public Float getMax_CO() {
        return max_CO;
    }

    public void setMax_CO(Float max_CO) {
        this.max_CO = max_CO;
    }

    public Float getMinCorr() {
        return minCorr;
    }

    public void setMinCorr(Float minCorr) {
        this.minCorr = minCorr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    public List<Point> getPointList() {
        return pointList;
    }

    public void setPointList(List<Point> pointList) {
        this.pointList = pointList;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public void addPoint(Point point) {
        pointList.add(point);
    }

    public Expression getFormula() {
        return formula;
    }

    public void setFormula(Expression formula) {
        this.formula = formula;
    }

    @Override
    public String toString() {
        return "StandardCurve{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sample_id=" + sample_id +
                ", type=" + type +
                ", point_set=" + point_set +
                ", CORR=" + CORR +
                ", expression='" + expression + '\'' +
                ", x_axis_unit='" + x_axis_unit + '\'' +
                ", y_axis_unit='" + y_axis_unit + '\'' +
                ", min_CO=" + min_CO +
                ", max_CO=" + max_CO +
                ", minCorr=" + minCorr +
                ", description='" + description + '\'' +
                ", validity=" + validity +
                ", data=" + pointList +
                ", formula=" + formula +
                '}';
    }
    //手动实现深拷贝
    @Override
    public StandardCurve clone() throws CloneNotSupportedException {
        StandardCurve clone = (StandardCurve) super.clone();
        clone.pointList = new ArrayList<>(this.pointList); // 创建引用类型字段的副本
        clone.formula = new Expression(clone.formula.getK(),clone.formula.getB());
        return clone;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;

        if (obj.getClass() != this.getClass()) return false;
        else {
            StandardCurve standardCurve = (StandardCurve) obj;
            return this.id == standardCurve.getId();
        }
    }
    public StandardCurve(StandardCurve other){
        // 简单类型
        this.id = other.id;
        this.name = other.name;
        this.sample_id = other.sample_id;
        this.type = other.type;
        this.point_set = other.point_set;
        this.CORR = other.CORR;
        this.expression = other.expression;
        this.x_axis_unit = other.x_axis_unit;
        this.y_axis_unit = other.y_axis_unit;
        this.min_CO = other.min_CO;
        this.max_CO = other.max_CO;
        this.minCorr = other.minCorr;
        this.description = other.description;
        this.validity = other.validity;
        this.sample = other.sample;
        // 列表
        this.pointList = new ArrayList<>(other.pointList);
        // 关联对象
        this.formula = other.formula != null ? new Expression(other.formula) : null;
    }

    // 数据保存与恢复
    // Parcelable 构造函数
    protected StandardCurve(Parcel in) {
        // 使用 readValue 保持与 CREATOR 一致
        id = (Integer) in.readValue(Integer.class.getClassLoader());
        name = in.readString();
        sample_id = (Integer) in.readValue(Integer.class.getClassLoader());
        type = (Integer) in.readValue(Integer.class.getClassLoader());
        point_set = (String) in.readValue(String.class.getClassLoader());
        CORR = (Float) in.readValue(Float.class.getClassLoader());
        expression = in.readString();
        x_axis_unit = in.readString();
        y_axis_unit = in.readString();
        min_CO = (Float) in.readValue(Float.class.getClassLoader());
        max_CO = (Float) in.readValue(Float.class.getClassLoader());
        minCorr = (Float) in.readValue(Float.class.getClassLoader());
        description = in.readString();
        validity = in.readInt();
        // 处理 List<Point> 类型的数据
        in.readTypedList(pointList, Point.CREATOR);
        // 由于 Sample 被 @Ignore 注解标记，这里不处理其反序列化
        // 处理 Expression 类型的数据，假设 Expression 类也实现了 Parcelable 接口
        formula = in.readParcelable(Expression.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeValue(id);
        parcel.writeString(name);
        parcel.writeValue(sample_id);
        parcel.writeValue(type);
        parcel.writeValue(point_set);
        parcel.writeValue(CORR);
        parcel.writeString(expression);
        parcel.writeString(x_axis_unit);
        parcel.writeString(y_axis_unit);
        parcel.writeValue(min_CO);
        parcel.writeValue(max_CO);
        parcel.writeValue(minCorr);
        parcel.writeString(description);
        parcel.writeInt(validity);
        // 处理 List<Point> 类型的数据
        parcel.writeTypedList(pointList);
        parcel.writeParcelable(sample,i);
        parcel.writeParcelable(formula, i);
    }

    public static final Parcelable.Creator<StandardCurve> CREATOR = new Parcelable.Creator<StandardCurve>() {
        @Override
        public StandardCurve createFromParcel(Parcel in) {
            return new StandardCurve(in);
        }

        @Override
        public StandardCurve[] newArray(int size) {
            return new StandardCurve[size];
        }
    };
}
