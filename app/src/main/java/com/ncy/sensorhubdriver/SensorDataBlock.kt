package com.ncy.sensorhubdriver

import android.util.Log

class SensorDataBlock {
    companion object {
        init {
            Log.e("Main", "init start")
            System.loadLibrary("sensorHub-lib")
            Log.e("Main", "init end")
        }
    }

    //读取ECG数据，每次按秒读取，数据存入data，读取长度lenth（s），返回实际读取长度
    //ECG数据格式如下
    /*
        struct ecg_block
        {
            UINT32 id;//id,这里使用UTC秒数，根据初始UTC时间自增
            UINT32 loff;//导联状态
            UINT32 undefine;//保留字段
            UINT16 data[ECG_SAMPLING_RATE*LEAD_NUM];//按导联依次循环排列
        };
     */
    external fun ReadECGData(data: ByteArray, lenth: Int): Int

    //读取IMU数据，每次按秒读取，数据存入data，读取长度lenth（s），返回实际读取长度
    //SPORT数据格式如下
    /*
        struct sport_data_def
        {
            INT16 gyro_x;
            INT16 gyro_y;
            INT16 gyro_z;
            INT16 accel_x;
            INT16 accel_y;
            INT16 accel_z;
        };
        struct sport_block
        {
            UINT32 id;//id,这里使用UTC秒数，根据初始UTC时间自增
            struct sport_data_def data[SPORT_SAMPLING_RATE];//按导联依次循环排列
        };
     */
    external fun ReadSportData(data: ByteArray, length: Int): Int

    //读取温度和气压数据，固定八字节，前面四字节为温度，后四字节为气压，返回是否操作成功0成功，非0失败
    external fun ReadTempAndPressData(data: ByteArray): Boolean

    //获得软件版本号，前4个字节，表示JNI lib的版本号，后面的204个字节表示sensorhub的版本号，返回是否操作成功0成功，非0失败
    //sensor版本号格式如下
    /*
        struct version_info
        {
            int code;//版本号code 唯一标志版本等级
            char version_str[200];//版本号字符串
        };
     */
    external fun ReadVersionData(data: ByteArray): Boolean

    //获取各类传感器的状态，固定1字节格式如下，回是否操作成功0成功，非0失败
    /* 0为正常，非0为不正常
        typedef union
        {
            uint8_t data;
            struct
            {
               uint8_t ADS8866Status:1;
               uint8_t TCA6424AStatus:1;
               uint8_t BMI160Status:1;
               uint8_t BMP280Status:1;
               uint8_t unused:4;
            }info
        }
     */
    external fun ReadSensorsStatues(data: ByteArray): Boolean

    //往sensor写入数据，imode为操作指令，data为写入数据，lenth为写入数据长度，返回是否操作成功0成功，非0失败
    //imode定义如下
    /*
        enum
        {
            e_WriteData_SensorPowerON,              //打开sensor电源
            e_WriteData_SensorPowerOFF,             //关闭sensor电源
            e_WriteData_SensorReset,                //重启sensor
            e_WriteData_toSensor,					//传给sensor的数据
            e_WriteData_UpdateSensorStart,			//升级Sensor 开始,buffer 前4个字节表示升级文件长度，余下的为文件MD值，iLen = MD + 4.
            e_WriteData_UpdateSensorWriteBlock,		//升级Sensor 写一块数据
            e_WriteData_UpdateSensorEnd,			//升级Sensor 结束
            e_ReadData_UpdateSensorIsSuccess,		//升级Sensor 是否成功
            e_WriteData_SetLight,                   //设置灯的颜色，一个字节 0 = off，1= red，2= blue，3 = green
            e_WriteData_SensorSleep,                //一个字节,1休眠,0退出休眠
            e_WriteData_APPOtaIng,                  //三个字节，字节0红灯（0为off,1为打开）字节1蓝灯（0为off,1为打开）字节2绿灯（0为off,1为打开）
        }
    */
    external fun WriteSensorData(imode: Int, data: ByteArray, length: Int): Boolean

    //获取内核的APPID，为固定长度以及固定内容
    external fun ReadKernelAPPID(data: ByteArray): Boolean

    //获取Sensor的APPID，为固定长度以及固定内容
    external fun ReadSensorAPPID(data: ByteArray?): Boolean

    /*
    升级完发送end后通过该接口查询升级状态，数据一个字节，成功为1，非1失败
     */
    external fun GetSensorOTAInfo(data: ByteArray): Boolean
}