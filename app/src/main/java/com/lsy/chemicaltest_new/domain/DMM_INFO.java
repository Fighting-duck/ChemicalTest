package com.lsy.chemicaltest_new.domain;

import static com.blankj.utilcode.util.StringUtils.getString;

import androidx.room.Ignore;

import com.lsy.chemicaltest_new.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/***
 * 万用表信息
 */
public class DMM_INFO {
     /*DMM名称*/
    public final static String NAME = "UT60BT";
    /*启用通知描述符的标准 UUID*/
    public final static String UUID_OPEN_NOTIFY = "00002902-0000-1000-8000-00805f9b34fb";

    // Notify UUID
    public static final String UUID_SERVICE_NOTIFY = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    public static final String UUID_CHARACTERISTIC_NOTIFY = "49535343-1e4d-4bd9-ba61-23c647249616";//Notify
    //public static final String UUID_CHARACTERISTIC_NOTIFY = "49535343-aca3-481c-91ec-d85e28a60318";//Write no response、Notify

    //Indicate UUID
    public static final String UUID_SERVICE_INDICATE = "00001801-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARACTERISTIC_INDICATE = "00002a05-0000-1000-8000-00805f9b34fb";

    // Write UUID
    public static final String UUID_SERVICE_WRITE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
//    private static final String UUID_CHARACTERISTIC_WRITE = "49535343-6daa-4d02-abf6-19569aca69fe";
//    private static final String UUID_CHARACTERISTIC_WRITE = "49535343-aca3-481c-91ec-d85e28a60318";
    public static final String UUID_CHARACTERISTIC_WRITE = "49535343-8841-43f4-a8d4-ecbe34729bb3";

    // 使万用表发送显示值给手机命令
    public final static byte COMMAND_SEND = 0x43;
    // 万用表发给手机信息MSG
    // 1.MSG[3] 功能挡位
    public final static byte Celsius = 0x0A;//摄氏温度
    public final static byte Fahrenheit = 0x0B;//华氏温度
    public final static byte DcuA = 0x0C;//直流微安档
    public final static byte DcmA = 0x0E;//直流毫安档
    // 2.MSG[4] 里程
    public final static byte mileage_1 = 0x30;//量程:0-600 uA 或 量程:0-60 mA
    public final static byte mileage_2 = 0x31;//量程:0-6000 uA 或 量程:0-600 mA

/*    public byte[] buildJar(byte jar){
        byte[] bytes = new byte[6];
        bytes[0] = (byte)0xAB;//头
        bytes[1] = (byte)0xCD;//头
        bytes[2] = (byte)0x03;//长度
        bytes[3] = jar;//命令 0x5e表示使能万用表或钳形发送显示值给手机
        // 计算检验和
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            sum += bytes[i];
        }
        // 将检验和分成两个字节
        bytes[4] = (byte) (sum & 0xFF); // 低字节
        bytes[5] = (byte) ((sum >> 8) & 0xFF); // 高字节

        return bytes;
    }*/

    /***
     * 根据协议格式，动态生成包含校验和的字节数组
     * @param command 命令
     * @return “头+长度+命令+校验和”的字节数据
     */
    public final static byte[] createCommand(byte command) {
        // 包头（0xAB 0xCD）
        byte[] header = new byte[]{(byte) 0xAB, (byte) 0xCD};
        // 长度（命令+校验和占3字节，固定0x03）
        byte length = 0x03;
        // 命令（0x5E）
        byte cmd = command;

        // 计算校验和（头+长度+命令的总和）
        int sum = 0;
        for (byte b : header) {
            sum += (b & 0xFF); // 转换为无符号整数
        }
        sum += (length & 0xFF);
        sum += (cmd & 0xFF);

        // 将校验和拆分为高8位和低8位（大端模式）
        byte[] checksum = new byte[]{
                (byte) ((sum >> 8) & 0xFF), // 高字节
                (byte) (sum & 0xFF)        // 低字节
        };

        // 组合完整数据包
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        try {
            packet.write(header);
            packet.write(length);
            packet.write(cmd);
            packet.write(checksum);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return packet.toByteArray();
    }

    /***
     * 对万能表发送给手机数据进行分析
     * @param data 数据
     * @return 分析后打包数据
     */
    public static DMM_ReturnResult analysisResult(byte[] data) {
        String gear = null, mileage = null, unit = null;
        byte gear_byte = data[3];//挡位
        byte mileage_byte = data[4];//里程
        //Msg[3] 功能挡位
        switch (gear_byte){
            case Celsius:
                unit = getString(R.string.unit_degree);
                gear = getString(R.string.multimeter_Celsius);
                //Mg[4] 量程
                mileage = mileage_byte == mileage_1 ? getString(R.string.multimeter_Celsius_mileage_1)
                        : getString(R.string.multimeter_Celsius_mileage_2);
                break;
//            case Fahrenheit:
//                break;
            case DcuA:
                unit = getString(R.string.unit_uA);
                gear = getString(R.string.multimeter_DcuA);
                //Mg[4] 量程
                mileage = mileage_byte == mileage_1 ? getString(R.string.multimeter_DcuA_mileage_1)
                        : getString(R.string.multimeter_DcuA_mileage_2);
                break;
            case DcmA:
                unit = getString(R.string.unit_mA);
                gear = getString(R.string.multimeter_DcmA);
                //Mg[4] 量程
                mileage = mileage_byte == mileage_1 ? getString(R.string.multimeter_DcuA_mileage_2)
                        : getString(R.string.multimeter_DcmA_mileage_2);
                break;
            default:
                return null;
        }
        //Msg[5]-Msg[11] 测量值
        byte[] value_byte = new byte[6];//测量值 byte
        System.arraycopy(data, 5, value_byte, 0, 6);//Msg[5]-Msg[11]
        String value_str = new String(value_byte).trim();
        float resultValue;
        if ("OL".equals(value_str)) {
            // 处理温度探针未插入情况
            resultValue = Float.MAX_VALUE;
        } else {
            resultValue = Float.parseFloat(value_str);//测量值 Float
        }
        BleDeviceInfo bleDeviceInfo = new BleDeviceInfo(gear,mileage,unit);
        TestValue result = new TestValue(resultValue,unit);
        return new DMM_ReturnResult(bleDeviceInfo,result);
    }
}
