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

import androidx.annotation.NonNull;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

/***
 * 导出工具类
 */
public class ExportUtils {
    private static final String TAG = "ExportUtils";

    /***
     * 批量填充标准曲线数据
     * @param curves 标准曲线
     * @return workbook
     */
    public static HSSFWorkbook fillCurvesData(@NonNull List<StandardCurve> curves,@NonNull String experimenterName) {
        // 参数校验
        Objects.requireNonNull(curves, "Curves cannot be null");
        Objects.requireNonNull(experimenterName, "experimenterName cannot be null");

        HSSFWorkbook workbook = new HSSFWorkbook();// 创建工作簿
        HSSFSheet sheet = workbook.createSheet("标准曲线");// 创建工作表
        Integer current_row = 0;

        for (int i = 0; i < curves.size(); i++){
            // 获取标准曲线信息
            StandardCurve curve = curves.get(i);
            List<Point> points = curve.getPointList();//获取点集信息
            Expression formula = curve.getFormula();//获取公式信息
            Sample sample = curve.getSample();//获取样本信息

            //1.1 曲线基本信息
            HSSFRow row_1 = sheet.createRow(current_row);
            row_1.createCell(0).setCellValue("基本信息：");
            current_row++;
            HSSFRow row_2 = sheet.createRow(current_row);
            row_2.createCell(0).setCellValue("曲线名");
            row_2.createCell(1).setCellValue("样本名");
            row_2.createCell(2).setCellValue("检测类型");
            row_2.createCell(3).setCellValue("x轴单位");
            row_2.createCell(4).setCellValue("y轴单位");
            row_2.createCell(5).setCellValue("最小浓度值");
            row_2.createCell(6).setCellValue("最大浓度值");
            row_2.createCell(7).setCellValue("最小拟合系数");
            row_2.createCell(8).setCellValue("备注");
            current_row++;
            HSSFRow row_2_1 = sheet.createRow(current_row);
            row_2_1.createCell(0).setCellValue(curve.getName());
            row_2_1.createCell(1).setCellValue(sample.getName());
            row_2_1.createCell(2).setCellValue(StandardCurve.getCurveType(curve.getType()));
            row_2_1.createCell(3).setCellValue(curve.getX_axis_unit());
            row_2_1.createCell(4).setCellValue(curve.getY_axis_unit());
            row_2_1.createCell(5).setCellValue(curve.getMin_CO());
            row_2_1.createCell(6).setCellValue(curve.getMax_CO());
            row_2_1.createCell(7).setCellValue(curve.getMinCorr());
            row_2_1.createCell(8).setCellValue(curve.getDescription());
            current_row++;

            //1.2 曲线核心信息
            HSSFRow row_3 = sheet.createRow(current_row);
            row_3.createCell(0).setCellValue("核心信息：");
            current_row++;
            HSSFRow row_4 = sheet.createRow(current_row);
            row_4.createCell(0).setCellValue("拟合系数");
            row_4.createCell(1).setCellValue("K值");
            row_4.createCell(2).setCellValue("B值");
            row_4.createCell(3).setCellValue("标准曲线公式");
            current_row++;
            HSSFRow row_4_1 = sheet.createRow(current_row);
            row_4_1.createCell(0).setCellValue(curve.getCORR());
            row_4_1.createCell(1).setCellValue(formula.getK());
            row_4_1.createCell(2).setCellValue(formula.getB());
            row_4_1.createCell(3).setCellValue(formula.toString());
            current_row++;

            //1.3 曲线散点数据
            HSSFRow row_5 = sheet.createRow(current_row);
            row_5.createCell(0).setCellValue("散点集：");
            current_row++;
            HSSFRow row_6 = sheet.createRow(current_row);
            row_6.createCell(0).setCellValue("序号");
            current_row++;
            HSSFRow row_7 = sheet.createRow(current_row);
            row_7.createCell(0).setCellValue("点");

            for (int j = 0; j < points.size(); j++) {
                row_6.createCell(j+1).setCellValue(j+1);//序号
                row_7.createCell(j+1).setCellValue(points.get(j).getX_value()+","+points.get(j).getY_value());// 点(x,y)
            }
            current_row = current_row + 2;
        }
        // 添加实验员信息
        current_row++;
        HSSFRow row_experimenterName = sheet.createRow(current_row);
        row_experimenterName.createCell(0).setCellValue("实验员：");
        row_experimenterName.createCell(1).setCellValue(experimenterName);
        current_row++;
        HSSFRow row_dateTime = sheet.createRow(current_row);
        row_dateTime.createCell(0).setCellValue("日期：");
        row_dateTime.createCell(1).setCellValue(TimeUtil.getCurrentDateTime());
        return workbook;
    }

    /***
     * 批量导出标准曲线数据为excel文件
     * @param context 上下文
     * @param curves 标准曲线
     */
    public static void exportCurvesToExcel(@NonNull Context context, @NonNull List<StandardCurve> curves) {
        // 参数校验
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(curves, "curves cannot be null");

        Executors.newSingleThreadExecutor().execute(() -> {
            // 提取实验员信息
            SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(context);
            String experimenterName = sharePreferencesManager.getUserName();
            // 创建并填充工作簿excel
            HSSFWorkbook curve_workbook = fillCurvesData(curves, experimenterName);
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
     *  批量将history导出为excel表
     * @param context 上下文对象
     * @param histories 历史记录
     */
    public static void exportHistoriesToExcel(@NonNull Context context, @NonNull List<History_multiple> histories) {
        // 参数校验
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(histories, "histories cannot be null");

        Executors.newSingleThreadExecutor().execute(() -> {
            // 提取实验员信息
            SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(context);
            String experimenterName = sharePreferencesManager.getUserName();
            // 创建并填充工作簿excel
            HSSFWorkbook curve_workbook = fillHistoriesData(histories, experimenterName);
            // 保存 Excel 文件
            try {
                //构造文件名
                String fileName = "样品检测-历史记录_" + System.currentTimeMillis() + ".xlsx"; // 默认PNG格式
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

    private static HSSFWorkbook fillHistoriesData(List<History_multiple> histories, String experimenterName) {
        // 创建工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        int current_row = 0;
        // 创建工作表
        HSSFSheet sheet = workbook.createSheet("历史记录");
        sheet.setHorizontallyCenter(true);
        for (int i = 0; i < histories.size(); i++){
            // 获取历史记录
            History_multiple history_multiple = histories.get(i);
            ElecTestResult elec_result = history_multiple.getElecTestResult();
            ColoTestResult colo_result = history_multiple.getColoTestResult();
            Temperature_Elec temperature_elec = history_multiple.getTemperature_elec();
            ThermalTestResult thermal_result = history_multiple.getThermalTestResult();

            //1.1 创建表头
            CellRangeAddress mergedRegion = new CellRangeAddress(i, i, 0, 8);// 合并单元格（起始行索引、结束行索引、起始列索引、结束列索引）
            sheet.addMergedRegion(mergedRegion);// 合并单元格
            // 获取合并后的单元格 表头
            HSSFRow headerRow = sheet.createRow(current_row);
            Cell cell = headerRow.createCell(0);
            cell.setCellValue(history_multiple.getHistoryName());
            current_row++;

            //1.2 基本信息
            Row row_1 = sheet.createRow(current_row);
            row_1.createCell(1).setCellValue("保存时间");
            row_1.createCell(2).setCellValue("样品名称");
            row_1.createCell(3).setCellValue("可信度");
            row_1.createCell(4).setCellValue("备注");
            current_row++;
            HSSFRow row_1_1 = sheet.createRow(current_row);
            row_1_1.createCell(1).setCellValue(history_multiple.getSaveTime());
            row_1_1.createCell(2).setCellValue(history_multiple.getSampleName());
            if (history_multiple.getCredibility()!=null)
                row_1_1.createCell(3).setCellValue(history_multiple.getCredibility());
            if (history_multiple.getRemarks() != null)
                row_1_1.createCell(4).setCellValue(history_multiple.getRemarks());
            current_row++;

            //1.3 电信号检查结果
            if (elec_result != null){
                Row row_2 = sheet.createRow(current_row);
                row_2.createCell(0).setCellValue("电信号检查结果：");
                current_row++;
                //1.3.1 基本信息
                Row row_3 = sheet.createRow(current_row);
                row_3.createCell(1).setCellValue("检测时间");
                row_3.createCell(2).setCellValue("标准曲线名");
                row_3.createCell(3).setCellValue("标准曲线公式");
                row_3.createCell(4).setCellValue("万用表设备信息");
                current_row++;
                HSSFRow row_3_1 = sheet.createRow(current_row);
                row_3_1.createCell(1).setCellValue(elec_result.getDateTime());
                if (elec_result.getStandardCurve() != null){
                    row_3_1.createCell(2).setCellValue(elec_result.getStandardCurve().getName());
                    if (elec_result.getStandardCurve().getFormula() != null)
                        row_3_1.createCell(3).setCellValue(elec_result.getStandardCurve().getFormula().toString());
                }
                if (elec_result.getBleDeviceInfo() != null)
                    row_3_1.createCell(4).setCellValue(
                            showBleDeviceInfo(elec_result.getBleDeviceInfo()));
                current_row++;
                //1.3.2 测量值信息
                Row row_4 = sheet.createRow(current_row);
                Row row_5 = sheet.createRow(++current_row);
                row_4.createCell(1).setCellValue("14次测量值:");
                row_5.createCell(1).setCellValue("14次测量时间:");
                if (elec_result.getFourteen_measurements_list()!=null && elec_result.getFourteen_times_list()!=null) {
                    List<String> strList = ElecTestResult.getPointList(elec_result.getFourteen_measurements_list());
                    List<String> strTimeList = ElecTestResult.getPointList(elec_result.getFourteen_times_list());
                    for (int j = 0; j < strList.size(); j++) {
                        row_4.createCell(j + 2).setCellValue(strList.get(j));
                        row_5.createCell(j + 2).setCellValue(strTimeList.get(j));
                    }
                }
                current_row++;
                //1.3.3 实验结果信息
                Row row_6 = sheet.createRow(current_row);
                row_6.createCell(1).setCellValue("最大值");
                row_6.createCell(2).setCellValue("对应浓度");
                row_6.createCell(3).setCellValue("病害分析");
                current_row++;
                Row row_6_1 = sheet.createRow(current_row);
                if (elec_result.getMaxValue_4() != null)
                    row_6_1.createCell(1).setCellValue(elec_result.getMaxValue_4());
                if (elec_result.getDetectionCo() != null)
                    row_6_1.createCell(2).setCellValue(elec_result.getDetectionCo());
                if (elec_result.getDiseaseAnal() != null)
                    row_6_1.createCell(3).setCellValue(elec_result.getDiseaseAnal());
                current_row++;
            }

            //1.4 比色图像检测结果
            if (colo_result != null){
                Row row_4 = sheet.createRow(current_row);
                row_4.createCell(0).setCellValue("比色图像检测结果：");
                current_row++;
                // 1.4.1 基本信息
                Row row_5 = sheet.createRow(current_row);
                row_5.createCell(1).setCellValue("检测时间");
                row_5.createCell(2).setCellValue("标准曲线名");
                row_5.createCell(3).setCellValue("标准曲线公式");
                row_5.createCell(4).setCellValue("原始图片路径");
                row_5.createCell(5).setCellValue("截图路径");
                current_row++;
                HSSFRow row_5_1 = sheet.createRow(current_row);
                row_5_1.createCell(1).setCellValue(colo_result.getDateTime());
                if (colo_result.getStandardCurve() != null){
                    row_5_1.createCell(2).setCellValue(colo_result.getStandardCurve().getName());
                    if (colo_result.getStandardCurve().getFormula() != null)
                        row_5_1.createCell(3).setCellValue(colo_result.getStandardCurve().getFormula().toString());
                }
                if (colo_result.getOriginalImage_path() != null)
                    row_5_1.createCell(4).setCellValue(colo_result.getOriginalImage_path());
                if (colo_result.getCropImage_path() != null)
                    row_5_1.createCell(5).setCellValue(colo_result.getCropImage_path());
                current_row++;
                // 1.4.2 检测结果信息
                Row row_6 = sheet.createRow(current_row);
                row_6.createCell(1).setCellValue("校准后的颜色值");
                row_6.createCell(2).setCellValue("HSV值");
                row_6.createCell(3).setCellValue("对应浓度");
                row_6.createCell(4).setCellValue("病害分析");
                current_row++;
                Row row_6_1 = sheet.createRow(current_row);
                if (colo_result.getCorrectedColor() != null)
                    row_6_1.createCell(1).setCellValue(colo_result.getCorrectedColor());
                if (colo_result.getHSV_value() != null)
                    row_6_1.createCell(2).setCellValue(colo_result.getHSV_value());
                if (colo_result.getDetectionCo() != null)
                    row_6_1.createCell(3).setCellValue(colo_result.getDetectionCo());
                if (colo_result.getDiseaseAnal() != null)
                    row_6_1.createCell(4).setCellValue(colo_result.getDiseaseAnal());
                current_row++;
            }

            //1.5 万用表测温度检测结果
            if (temperature_elec != null){
                Row row_6 = sheet.createRow(current_row);
                row_6.createCell(0).setCellValue("万用表测温度检测结果:");
                current_row++;
                // 1.5.1 基本信息
                Row row_7 = sheet.createRow(current_row);
                row_7.createCell(1).setCellValue("检测时间");
                row_7.createCell(2).setCellValue("标准曲线名");
                row_7.createCell(3).setCellValue("标准曲线公式");
                row_7.createCell(4).setCellValue("万用表设备信息");
                current_row++;
                HSSFRow row_7_1 = sheet.createRow(current_row);
                row_7_1.createCell(1).setCellValue(temperature_elec.getDateTime());
                if (temperature_elec.getStandardCurve() != null){
                    row_7_1.createCell(2).setCellValue(temperature_elec.getStandardCurve().getName());
                    if (temperature_elec.getStandardCurve().getFormula() != null)
                        row_7_1.createCell(3).setCellValue(temperature_elec.getStandardCurve().getFormula().toString());
                }
                if (temperature_elec.getBleDeviceInfo() != null)
                    row_7_1.createCell(4).setCellValue(showBleDeviceInfo(temperature_elec.getBleDeviceInfo()));
                current_row++;
                // 1.5.2 检测结果信息
                Row row_8 = sheet.createRow(current_row);
                row_8.createCell(1).setCellValue("温度");
                row_8.createCell(2).setCellValue("对应浓度");
                row_8.createCell(3).setCellValue("病害分析");
                current_row++;
                Row row_8_1 = sheet.createRow(current_row);
                if (temperature_elec.getTemperature() != null)
                    row_8_1.createCell(1).setCellValue(temperature_elec.getTemperature());
                if (temperature_elec.getDetectionCo() != null)
                    row_8_1.createCell(2).setCellValue(temperature_elec.getDetectionCo());
                if (temperature_elec.getDiseaseAnal() != null)
                    row_8_1.createCell(3).setCellValue(temperature_elec.getDiseaseAnal());
                current_row++;
            }

            //1.6 光热图像检测结果
            if (thermal_result != null){
                Row row_8 = sheet.createRow(current_row);
                row_8.createCell(0).setCellValue("光热图像检测结果:");
                current_row++;
                // 1.6.1 基本信息
                Row row_9 = sheet.createRow(current_row);
                row_9.createCell(1).setCellValue("检测时间");
                row_9.createCell(2).setCellValue("标准曲线名");
                row_9.createCell(3).setCellValue("标准曲线公式");
                row_9.createCell(4).setCellValue("光热图片路径");
                current_row++;
                HSSFRow row_9_1 = sheet.createRow(current_row);
                row_9_1.createCell(1).setCellValue(thermal_result.getDateTime());
                if (thermal_result.getStandardCurve() != null){
                    row_9_1.createCell(2).setCellValue(thermal_result.getStandardCurve().getName());
                    if (thermal_result.getStandardCurve().getFormula() != null)
                        row_9_1.createCell(3).setCellValue(thermal_result.getStandardCurve().getFormula().toString());
                }
                if (thermal_result.getThermalBitmap_path() != null)
                    row_9_1.createCell(4).setCellValue(thermal_result.getThermalBitmap_path());
                current_row++;
                // 1.6.2 检测结果信息
                Row row_10 = sheet.createRow(current_row);
                row_10.createCell(1).setCellValue("中心温度");
                row_10.createCell(2).setCellValue("对应浓度");
                row_10.createCell(3).setCellValue("病害分析");
                current_row++;
                Row row_10_1 = sheet.createRow(current_row);
                if (thermal_result.getCentralTemperature() != null)
                    row_10_1.createCell(1).setCellValue(thermal_result.getCentralTemperature());
                if (thermal_result.getDetectionCo() != null)
                    row_10_1.createCell(2).setCellValue(thermal_result.getDetectionCo());
                if (thermal_result.getDiseaseAnal() != null)
                    row_10_1.createCell(3).setCellValue(thermal_result.getDiseaseAnal());
                current_row++;
            }
            current_row++;
        }
        current_row++;
        // 添加实验员信息
        HSSFRow row_experimenterName = sheet.createRow(current_row);
        row_experimenterName.createCell(0).setCellValue("实验员：");
        row_experimenterName.createCell(1).setCellValue(experimenterName);
        current_row++;
        HSSFRow row_dateTime = sheet.createRow(current_row);
        row_dateTime.createCell(0).setCellValue("导出日期：");
        row_dateTime.createCell(1).setCellValue(TimeUtil.getCurrentDateTime());
        return workbook;
    }
    /***
     * 打印万用表信息
     * @param bleDeviceInfo 万用表信息
     * @return 万用表信息
     */

    private static String showBleDeviceInfo(BleDeviceInfo bleDeviceInfo){
        if (bleDeviceInfo!=null){
            return getString(R.string.historyPreview_gear)+"("+bleDeviceInfo.getGear()+")"+
                    "  "+getString(R.string.historyPreview_mileage)+"("+bleDeviceInfo.getMileage()+")";
        }
        else
            return null;
    }

    /**
     * 向用户显示导出文件路径
     * @param context 上下文对象
     * @param filePath 文件路径
     */
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
    public static StandardCurve  readCurveExcel(@NonNull Context context, @NonNull Uri uri,@NonNull StandardCurve curve) throws IOException {
        //1. 参数校验
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(uri, "uri cannot be null");
        Objects.requireNonNull(curve, "curve cannot be null");

        //2. 检查uri
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
        //3. 从工作簿获取直线信息
        Sheet sheet = workbook.getSheetAt(0);

        //3.1 获取直线基本信息
        Row basicInfoRow = sheet.getRow(2);
        if (basicInfoRow != null){
            //3.1.1 检查曲线名和样品名是否存在
            String name = basicInfoRow.getCell(0).getStringCellValue();//获取曲线名
            Log.d(TAG, "name: " + name);
            if (isExistCurve(name)){
                Toast.makeText(context, "曲线名已存在！仅导入散点！", Toast.LENGTH_SHORT).show();
                //获取曲线点集
                List<Point> pointList = getPointListToSheet(context,sheet);
                if (pointList!=null && !pointList.isEmpty())
                    curve.setPointList(pointList);
                else Toast.makeText(context, "散点集为空！", Toast.LENGTH_SHORT).show();
                return curve;
            }
            String sampleName = basicInfoRow.getCell(1).getStringCellValue();//获取样品名
            Log.d(TAG, "sampleName: " + sampleName);
            Sample sample = MyApplication.DATABASE_INSTANCE.getSampleDao().findBy_name(sampleName);
            if (sample==null){
                Toast.makeText(context, "样品名不存在！仅导入散点！", Toast.LENGTH_SHORT).show();
                //获取曲线点集
                List<Point> pointList = getPointListToSheet(context,sheet);
                if (pointList!=null && !pointList.isEmpty())
                    curve.setPointList(pointList);
                else Toast.makeText(context, "散点集为空！", Toast.LENGTH_SHORT).show();
                return curve;
            }
            //3.1.2 若曲线名不重复，样品名存在，则导入曲线全部基本信息
            curve.setName(name);
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
        //3.2 获取点集信息
        List<Point> pointList = getPointListToSheet(context,sheet);
        if (pointList!=null && !pointList.isEmpty())
            curve.setPointList(pointList);
        else Toast.makeText(context, "散点集为空！", Toast.LENGTH_SHORT).show();
        inputStream.close();
        return curve;
    }
    private static List<Point> getPointListToSheet(Context context,Sheet sheet){
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
        if (pointCount == 0) return null;
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
        return pointList;
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
