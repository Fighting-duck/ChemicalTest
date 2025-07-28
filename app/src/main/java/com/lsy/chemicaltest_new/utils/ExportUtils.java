package com.lsy.chemicaltest_new.utils;

import static com.blankj.utilcode.util.FileUtils.getFileExtension;
import static com.blankj.utilcode.util.StringUtils.getString;
import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.Experimenter;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/***
 * 导出工具类
 */
public class ExportUtils {
    private static final String TAG = "ExportUtils";

    /***
     * 构建标准曲线excel
     * @return workbook
     */
    public static HSSFWorkbook buildCurveExcel() {
        // 创建工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表
        HSSFSheet sheet = workbook.createSheet("标准曲线");

        HSSFRow row_1 = sheet.createRow(0);
        row_1.createCell(0).setCellValue("基本信息：");

        HSSFRow row_2 = sheet.createRow(1);
        row_2.createCell(0).setCellValue("曲线名");
        row_2.createCell(1).setCellValue("样本名");
        row_2.createCell(2).setCellValue("检测类型");
        row_2.createCell(3).setCellValue("x轴单位");
        row_2.createCell(4).setCellValue("y轴单位");
        row_2.createCell(5).setCellValue("最小浓度值");
        row_2.createCell(6).setCellValue("最大浓度值");
        row_2.createCell(7).setCellValue("最小拟合系数");
        row_2.createCell(8).setCellValue("备注");

        HSSFRow row_3 = sheet.createRow(4);
        row_3.createCell(0).setCellValue("核心信息：");

        HSSFRow row_4 = sheet.createRow(5);
        row_4.createCell(0).setCellValue("拟合系数");
        row_4.createCell(1).setCellValue("K值");
        row_4.createCell(2).setCellValue("B值");
        row_4.createCell(3).setCellValue("标准曲线公式");


        HSSFRow row_5 = sheet.createRow(8);
        row_5.createCell(0).setCellValue("散点集：");

        HSSFRow row_6 = sheet.createRow(9);
        row_6.createCell(0).setCellValue("序号");
        row_6.createCell(1).setCellValue("x");
        row_6.createCell(2).setCellValue("y");

        return workbook;
    }

    /***
     * 填充标准曲线数据
     * @param curve_workbook workbook
     * @param curve 标准曲线
     * @return workbook
     */
    public static HSSFWorkbook fillCurveData(HSSFWorkbook curve_workbook,StandardCurve curve,String experimenterName) {
        if (curve == null) return null;
        HSSFSheet sheet = curve_workbook.getSheetAt(0);

        //获取点集信息
        List<Point> points = curve.getPointList();
        //获取公式信息
        Expression formula = curve.getFormula();
        //获取样本信息
        Sample sample = curve.getSample();

        HSSFRow row_title1 = sheet.createRow(2);
        row_title1.createCell(0).setCellValue(curve.getName());
        row_title1.createCell(1).setCellValue(sample.getName());
        row_title1.createCell(2).setCellValue(StandardCurve.getCurveType(curve.getType()));
        row_title1.createCell(3).setCellValue(curve.getX_axis_unit());
        row_title1.createCell(4).setCellValue(curve.getY_axis_unit());
        row_title1.createCell(5).setCellValue(curve.getMin_CO());
        row_title1.createCell(6).setCellValue(curve.getMax_CO());
        row_title1.createCell(7).setCellValue(curve.getMinCorr());
        row_title1.createCell(8).setCellValue(curve.getDescription());

        HSSFRow row_title2 = sheet.createRow(6);
        row_title2.createCell(0).setCellValue(curve.getCORR());
        row_title2.createCell(1).setCellValue(formula.getK());
        row_title2.createCell(2).setCellValue(formula.getB());
        row_title2.createCell(3).setCellValue(formula.toString());

        for (int i = 0; i < points.size(); i++) {
            HSSFRow row_data = sheet.createRow(i+10);
            row_data.createCell(0).setCellValue(i+1);
            row_data.createCell(1).setCellValue(points.get(i).getX_value());
            row_data.createCell(2).setCellValue(points.get(i).getY_value());
        }

        HSSFRow lastRow = sheet.createRow(10 + points.size());
        lastRow.createCell(0).setCellValue("#");
        lastRow.createCell(1).setCellValue("#");
        lastRow.createCell(2).setCellValue("#");

        HSSFRow row = sheet.createRow(12 + points.size());
        row.createCell(0).setCellValue("实验员：");
        row.createCell(1).setCellValue(experimenterName);

/*//        List<Experimenter> experimenters = MyApplication.DATABASE_INSTANCE.getExperimenterDao().getAll();
        if (experimenters!=null && !experimenters.isEmpty()){
            HSSFRow row = sheet.createRow(12 + points.size());
            row.createCell(0).setCellValue("实验员：");
            row.createCell(1).setCellValue(experimenters.get(0).getName());
        }*/
        return curve_workbook;
    }

    /***
     * 导出标准曲线数据为excel文件
     * @param context 上下文
     * @param curve 标准曲线
     */
    public static void exportCurve(Context context,StandardCurve curve) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (curve == null) return;
            //创建工作簿excel
            HSSFWorkbook workbook = buildCurveExcel();
            //填充数据
            SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(context);
            String experimenterName = sharePreferencesManager.getUserName();
            HSSFWorkbook curve_workbook = fillCurveData(workbook,curve,experimenterName);
            // 保存 Excel 文件
            try {
                //构造文件名
                String fileName = "样品检测-标准曲线_" + System.currentTimeMillis() + ".xlsx"; // 默认PNG格式
                // 创建文件夹路径
                File file = new File(StorageUtils.getSaveTextPath(), fileName);
                FileOutputStream fileOut = new FileOutputStream(file);
                curve_workbook.write(fileOut);
                fileOut.close();
                curve_workbook.close();
                runOnUiThread(()->{
                    showFilePathErrorDialog(context,file.getAbsolutePath());
                });

                Log.d("ExportUtils", "数据已成功导出到 " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(()->{
                    Toast.makeText(context, "导出数据时出错", android.widget.Toast.LENGTH_SHORT).show();
                });

            }
        });
    }

    /***
     * 构建历史记录excel
     * @param header 表头
     * @return workbook
     */
    public static HSSFWorkbook buildHistoryExcel(String header) {
        // 创建工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表
        HSSFSheet sheet = workbook.createSheet("历史记录");

        // 创建表头
        // 定义合并区域（起始行索引、结束行索引、起始列索引、结束列索引）
        CellRangeAddress mergedRegion = new CellRangeAddress(0, 0, 0, 8);
        // 合并单元格
        int mergedRegionIndex = sheet.addMergedRegion(mergedRegion);
        // 获取合并后的单元格 表头
        HSSFRow headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue(header);
        // 创建样式
  /*      HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);//设置单元格的水平对齐类型
        style.setVerticalAlignment(VerticalAlignment.CENTER);//设置单元格的竖直对齐类型
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());//设置前景色填充颜色
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);//设置为 1 将使用前景色填充单元格
        // 获取合并区域的第一个单元格并设置样式
        CellRangeAddress region = sheet.getMergedRegion(mergedRegionIndex);
        int firstRow = region.getFirstRow();
        int firstCol = region.getFirstColumn();
        Cell firstCell = sheet.getRow(firstRow).getCell(firstCol);
        firstCell.setCellStyle(style);*/

        Row Row_title1 = sheet.createRow(1);
        Row_title1.createCell(0).setCellValue("保存时间");
        Row_title1.createCell(1).setCellValue("样品名称");
        Row_title1.createCell(2).setCellValue("可信度");
        Row_title1.createCell(3).setCellValue("备注");

        Row Row_title2 = sheet.createRow(4);
        Row_title2.createCell(0).setCellValue("电信号检查结果：");

        Row Row_title3 = sheet.createRow(5);
        Row_title3.createCell(0).setCellValue("检测时间");
        Row_title3.createCell(1).setCellValue("标准曲线名");
        Row_title3.createCell(2).setCellValue("标准曲线公式");
        Row_title3.createCell(3).setCellValue("万用表设备信息");
        Row_title3.createCell(4).setCellValue("14次测量值");
        Row_title3.createCell(5).setCellValue("14次测量时间");
        Row_title3.createCell(6).setCellValue("最大值");
        Row_title3.createCell(7).setCellValue("对应浓度");
        Row_title3.createCell(8).setCellValue("病害分析");

        Row Row_title4 = sheet.createRow(8);
        Row_title4.createCell(0).setCellValue("比色图像检测结果");

        Row Row_title5 = sheet.createRow(9);
        Row_title5.createCell(0).setCellValue("检测时间");
        Row_title5.createCell(1).setCellValue("标准曲线名");
        Row_title5.createCell(2).setCellValue("标准曲线公式");
        Row_title5.createCell(3).setCellValue("原始图片路径");
        Row_title5.createCell(4).setCellValue("截图路径");
        Row_title5.createCell(5).setCellValue("校准后的颜色值");
        Row_title5.createCell(6).setCellValue("HSV值");
        Row_title5.createCell(7).setCellValue("对应浓度");
        Row_title5.createCell(8).setCellValue("病害分析");

        Row Row_title6 = sheet.createRow(12);
        Row_title6.createCell(0).setCellValue("万用表测温度检测结果");

        Row Row_title7 = sheet.createRow(13);
        Row_title7.createCell(0).setCellValue("检测时间");
        Row_title7.createCell(1).setCellValue("标准曲线名");
        Row_title7.createCell(2).setCellValue("标准曲线公式");
        Row_title7.createCell(3).setCellValue("万用表设备信息");
        Row_title7.createCell(4).setCellValue("温度");
        Row_title7.createCell(5).setCellValue("对应浓度");
        Row_title7.createCell(6).setCellValue("病害分析");

        Row Row_title8 = sheet.createRow(16);
        Row_title8.createCell(0).setCellValue("光热图像检测结果");

        Row Row_title9 = sheet.createRow(17);
        Row_title9.createCell(0).setCellValue("检测时间");
        Row_title9.createCell(1).setCellValue("标准曲线名");
        Row_title9.createCell(2).setCellValue("标准曲线公式");
        Row_title9.createCell(3).setCellValue("光热图片路径");
        Row_title9.createCell(4).setCellValue("中心温度");
        Row_title9.createCell(5).setCellValue("对应浓度");
        Row_title9.createCell(6).setCellValue("病害分析");

        return workbook;
    }

    /***
     *  填充历史记录excel
     * @param history_workbook workbook
     * @param history_multiple 历史记录
     * @return workbook
     */
    public static HSSFWorkbook fillHistoryData(HSSFWorkbook history_workbook,History_multiple history_multiple,String experimenterName) {
        if (history_multiple == null) return null;
        HSSFSheet sheet = history_workbook.getSheetAt(0);

        //获取历史信息
        ElecTestResult elec_result = history_multiple.getElecTestResult();
        ColoTestResult colo_result = history_multiple.getColoTestResult();
        Temperature_Elec temperature_elec = history_multiple.getTemperature_elec();
        ThermalTestResult thermal_result = history_multiple.getThermalTestResult();

        HSSFRow row_title1 = sheet.createRow(2);
        row_title1.createCell(0).setCellValue(history_multiple.getSaveTime());
        row_title1.createCell(1).setCellValue(history_multiple.getSampleName());
        row_title1.createCell(2).setCellValue(history_multiple.getCredibility());
        row_title1.createCell(3).setCellValue(history_multiple.getRemarks());

        if (elec_result != null){
            HSSFRow row = sheet.createRow(6);
            row.createCell(0).setCellValue(elec_result.getDateTime());
            if (elec_result.getStandardCurve() != null){
                row.createCell(1).setCellValue(elec_result.getStandardCurve().getName());
                if (elec_result.getStandardCurve().getFormula() != null)
                    row.createCell(2).setCellValue(elec_result.getStandardCurve().getFormula().toString());
            }
            if (elec_result.getBleDeviceInfo() != null)
                row.createCell(3).setCellValue(
                        showBleDeviceInfo(elec_result.getBleDeviceInfo()));
            if (elec_result.getFourteen_measurements_list() != null)
                row.createCell(4).setCellValue(elec_result.getFourteen_measurements_list());
            if (elec_result.getFourteen_times_list() != null)
                row.createCell(5).setCellValue(elec_result.getFourteen_times_list());
            if (elec_result.getMaxValue_4() != null)
                row.createCell(6).setCellValue(elec_result.getMaxValue_4());
            if (elec_result.getDetectionCo() != null)
                row.createCell(7).setCellValue(elec_result.getDetectionCo());
            if (elec_result.getDiseaseAnal() != null)
                row.createCell(8).setCellValue(elec_result.getDiseaseAnal());
        }

        if (colo_result != null){
            HSSFRow row = sheet.createRow(10);
            row.createCell(0).setCellValue(colo_result.getDateTime());
            if (colo_result.getStandardCurve() != null){
                row.createCell(1).setCellValue(colo_result.getStandardCurve().getName());
                if (colo_result.getStandardCurve().getFormula() != null)
                    row.createCell(2).setCellValue(colo_result.getStandardCurve().getFormula().toString());
            }
            if (colo_result.getOriginalImage_path() != null)
                row.createCell(3).setCellValue(colo_result.getOriginalImage_path());
            if (colo_result.getCropImage_path() != null)
                row.createCell(4).setCellValue(colo_result.getCropImage_path());
            if (colo_result.getCorrectedColor() != null)
                row.createCell(5).setCellValue(colo_result.getCorrectedColor());
            if (colo_result.getHSV_value() != null)
                row.createCell(6).setCellValue(colo_result.getHSV_value());
            if (colo_result.getDetectionCo() != null)
                row.createCell(7).setCellValue(colo_result.getDetectionCo());
            if (colo_result.getDiseaseAnal() != null)
                row.createCell(8).setCellValue(colo_result.getDiseaseAnal());
        }

        if (temperature_elec != null){
            HSSFRow row = sheet.createRow(14);
            row.createCell(0).setCellValue(temperature_elec.getDateTime());
            if (temperature_elec.getStandardCurve() != null){
                row.createCell(1).setCellValue(temperature_elec.getStandardCurve().getName());
                if (temperature_elec.getStandardCurve().getFormula() != null)
                    row.createCell(2).setCellValue(temperature_elec.getStandardCurve().getFormula().toString());
            }
            if (temperature_elec.getBleDeviceInfo() != null)
                row.createCell(3).setCellValue(showBleDeviceInfo(temperature_elec.getBleDeviceInfo()));
            if (temperature_elec.getTemperature() != null)
                row.createCell(4).setCellValue(temperature_elec.getTemperature());
            if (temperature_elec.getDetectionCo() != null)
                row.createCell(5).setCellValue(temperature_elec.getDetectionCo());
            if (temperature_elec.getDiseaseAnal() != null)
                row.createCell(6).setCellValue(temperature_elec.getDiseaseAnal());
        }

        if (thermal_result != null){
            HSSFRow row = sheet.createRow(18);
            row.createCell(0).setCellValue(thermal_result.getDateTime());
            if (thermal_result.getStandardCurve() != null){
                row.createCell(1).setCellValue(thermal_result.getStandardCurve().getName());
                if (thermal_result.getStandardCurve().getFormula() != null)
                    row.createCell(2).setCellValue(thermal_result.getStandardCurve().getFormula().toString());
            }
            if (thermal_result.getThermalBitmap_path() != null)
                row.createCell(3).setCellValue(thermal_result.getThermalBitmap_path());
            if (thermal_result.getCentralTemperature() != null)
                row.createCell(4).setCellValue(thermal_result.getCentralTemperature());
            if (thermal_result.getDetectionCo() != null)
                row.createCell(5).setCellValue(thermal_result.getDetectionCo());
            if (thermal_result.getDiseaseAnal() != null)
                row.createCell(6).setCellValue(thermal_result.getDiseaseAnal());
        }

        HSSFRow row = sheet.createRow(20);
        row.createCell(0).setCellValue("实验员：");
        row.createCell(1).setCellValue(experimenterName);

       /* List<Experimenter> experimenters = MyApplication.DATABASE_INSTANCE.getExperimenterDao().getAll();
        if (experimenters!=null && !experimenters.isEmpty()){
            HSSFRow row = sheet.createRow(20);
            row.createCell(0).setCellValue("实验员：");
            row.createCell(1).setCellValue(experimenters.get(0).getName());
        }*/
        return history_workbook;
    }
    private static String showBleDeviceInfo(BleDeviceInfo bleDeviceInfo){
        if (bleDeviceInfo!=null){
            return getString(R.string.text_gear_0)+"("+bleDeviceInfo.getGear()+")"+
                    "  "+getString(R.string.text_mileage_0)+"("+bleDeviceInfo.getMileage()+")";
        }
        else
            return null;
    }

    /***
     *  将history导出为excel表
     * @param context 上下文对象
     * @param history 历史记录
     */
    public static void exportHistoryToExcel(Context context, History_multiple history){
        if (history == null) return;
        //创建工作簿excel
        HSSFWorkbook workbook = buildHistoryExcel(history.getHistoryName());
        //填充数据
        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(context);
        String experimenterName = sharePreferencesManager.getUserName();
        HSSFWorkbook history_workbook = fillHistoryData(workbook,history,experimenterName);
        // 保存 Excel 文件
        try {
            //构造文件名
            String fileName = "样品检测-历史记录_" + System.currentTimeMillis() + ".xlsx"; // 默认PNG格式
            // 创建文件夹路径
            File file = new File(StorageUtils.getSaveTextPath(), fileName);
            FileOutputStream fileOut = new FileOutputStream(file);
            history_workbook.write(fileOut);
            fileOut.close();
            history_workbook.close();
            showFilePathErrorDialog(context,file.getAbsolutePath());
            Log.d("ExportUtils", "数据已成功导出到 " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "导出数据时出错", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    private static void showFilePathErrorDialog(Context context,String filePath) {
        new AlertDialog.Builder(context)
                .setTitle("文件路径")
                .setMessage(filePath)
                .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /***
     * 读取曲线excel文件
     * @param context 上下文对象
     * @param uri excel文件路径
     * @return 曲线对象
     * @throws IOException IO异常
     */
    public static StandardCurve  readCurveExcel(Context context, Uri uri ) throws IOException {
        //检查uri
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            Toast.makeText(context, "无法打开文件！", Toast.LENGTH_SHORT).show();
            Log.e("ExcelReader", "无法打开文件");
            return null;
        }
        String fileExtension = getFileExtensionFromUri(uri, context);
        Workbook workbook;
        if ("xls".equals(fileExtension)) {
            workbook = new HSSFWorkbook(inputStream);
        } else if ("xlsx".equals(fileExtension)) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            Toast.makeText(context, "不支持的文件类型！", Toast.LENGTH_SHORT).show();
            Log.e("ExcelReader", "不支持的文件类型");
            return null;
        }
        Sheet sheet = workbook.getSheetAt(0);

        StandardCurve curve = new StandardCurve();
        //获取直线基本信息
        Row basicInfoRow = sheet.getRow(2);
        if (basicInfoRow != null) {
            //获取曲线名
            String name = basicInfoRow.getCell(0).getStringCellValue();
            Log.d(TAG, "name: " + name);
            if (isExistCurve(name)){
                Toast.makeText(context, "曲线名已存在！", Toast.LENGTH_SHORT).show();
                return null;
            }
            curve.setName(name);
            //获取样品名
            String sampleName = basicInfoRow.getCell(1).getStringCellValue();
            Log.d(TAG, "sampleName: " + sampleName);
            Sample sample = MyApplication.DATABASE_INSTANCE.getSampleDao().findBy_name(sampleName);
            if (sample==null){
                Toast.makeText(context, "样品名不存在！", Toast.LENGTH_SHORT).show();
                return null;
            }
            curve.setSample(sample);
            curve.setSample_id(sample.getId());
            //获取曲线类型
            String type = basicInfoRow.getCell(2).getStringCellValue();
            Log.d(TAG, "type: " + type);
            if (!StandardCurve.isCurveType(type)){
                Toast.makeText(context, "类型不存在！", Toast.LENGTH_SHORT).show();
                return null;
            }
            curve.setType(StandardCurve.getTypeId(type));
            //获取曲线x轴单位
            curve.setX_axis_unit(basicInfoRow.getCell(3).getStringCellValue());
            Log.d(TAG, "x_axis_unit: " + curve.getX_axis_unit());
            //获取曲线y轴单位
            curve.setY_axis_unit(basicInfoRow.getCell(4).getStringCellValue());
            Log.d(TAG, "y_axis_unit: " + curve.getY_axis_unit());
            //获取曲线最小CO值和最大CO值
            if (basicInfoRow.getCell(5).getCellType() != CellType.NUMERIC){
                Toast.makeText(context, "最小浓度值有问题！", Toast.LENGTH_SHORT).show();
                return null;
            }
            curve.setMin_CO((float)basicInfoRow.getCell(5).getNumericCellValue());
            Log.d(TAG, "min_CO: " + curve.getMin_CO());
            if (basicInfoRow.getCell(6).getCellType() != CellType.NUMERIC){
                Toast.makeText(context, "最大浓度值有问题！", Toast.LENGTH_SHORT).show();
                return null;
            }
            curve.setMax_CO((float)basicInfoRow.getCell(6).getNumericCellValue());
            Log.d(TAG, "max_CO: " + curve.getMax_CO());
            //获取曲线最小Corr值
            if (basicInfoRow.getCell(7).getCellType() != CellType.NUMERIC){
                Toast.makeText(context, "最小拟合系数值有问题！", Toast.LENGTH_SHORT).show();
                return null;
            }
            curve.setMinCorr((float)basicInfoRow.getCell(7).getNumericCellValue());
            Log.d(TAG, "minCorr: " + curve.getMinCorr());
            //获取曲线描述
            curve.setDescription(basicInfoRow.getCell(8).getStringCellValue());
            Log.d(TAG, "description: " + curve.getDescription());
        }
        //获取点集信息
        //1. 先判断有多少个点
        int pointCount = 0;
        boolean isEnd = false;
        int consecutiveHashCount = 0;
        for (Row row : sheet) {
            for (Cell cell : row) {
                //若连续出现三个”#“，则结束
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    if ("#".equals(cellValue)) {
                        consecutiveHashCount++;
                        if (consecutiveHashCount == 3) {
                            isEnd = true;
                            pointCount = row.getRowNum() - 7;
                            break;
                        }
                    } else {
                        consecutiveHashCount = 0;
                    }
                }
            }
            if (isEnd) {
                break;
            }
        }
        //2. 获取点集信息
        List<Point> pointList = new ArrayList<>();
        //获取当前时间
        String dateString = TimeUtil.getCurrentDateTime();
        for (int i = 6; i < 7 + pointCount; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Point point = new Point();
                if (row.getCell(0).getCellType() != CellType.NUMERIC || row.getCell(1).getCellType() != CellType.NUMERIC){
                    Toast.makeText(context, "点集有问题！", Toast.LENGTH_SHORT).show();
                    return null;
                }
                float x_value = (float) row.getCell(1).getNumericCellValue();
                float y_value = (float) row.getCell(2).getNumericCellValue();
                point.setX_value((float) Math.log(x_value));
                point.setY_value(y_value);
                point.setAdd_time(dateString);
                pointList.add(point);
            }
        }
        curve.setPointList(pointList);
        inputStream.close();
        return curve;
    }
    private static String getFileExtensionFromUri(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);// 获取文件的 MIME 类型
        if (mimeType == null) {
            return "";
        }
        //从 MIME 类型获取扩展名
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        return extension != null ? extension.toLowerCase() : "";
    }

    /***
     * 判断是否已存在该曲线
     * @param curveName 曲线名
     * @return true/false
     */
    private static Boolean isExistCurve(String curveName){
        StandardCurve curves = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findByName(curveName);
        return curves!=null;
    }
}
