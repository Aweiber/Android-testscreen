package com.example.testscreen

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.RelativeLayout
import com.ncy.sensorhubdriver.SensorDataBlock
import java.lang.reflect.Method
import android.Manifest
import android.content.pm.PackageManager
import android.webkit.JavascriptInterface
import kotlinx.coroutines.delay
import kotlin.concurrent.thread
import java.io.DataOutputStream
import java.io.IOException
import java.io.File
import android.os.Environment
import java.io.RandomAccessFile
import java.io.FileWriter
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.security.MessageDigest
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : Activity(), OnTouchListener {
//
//    static {
//        System.loadLibrary("libsensorHub-lib");
//    }
    private var touchPoint: View? = null
    private var rootView: RelativeLayout? = null
    private var sensorDataBlock: SensorDataBlock? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        touchPoint = findViewById(R.id.touchPoint)
        rootView = findViewById<RelativeLayout>(R.id.rootView)
        rootView?.setOnTouchListener(this)
        sensorDataBlock = SensorDataBlock()
        val deviceName = getDeviceName("ro.serialno", "")

        val statedata = ByteArray(1)
        println(sensorDataBlock?.ReadSensorsStatues(statedata))

        val kernelappid = ByteArray(32)
        println(sensorDataBlock?.ReadKernelAPPID(kernelappid))
        val asciiString = kernelappid.map { it.toChar() }.joinToString("")
        println("kernelappid: $asciiString")

        val snesorappid = ByteArray(32)
        println(sensorDataBlock?.ReadSensorAPPID(snesorappid))
        val asciiStrings = snesorappid.map { it.toChar() }.joinToString("")
        println("snesorappid: $asciiStrings")

        val versiondata = ByteArray(208)
        println(sensorDataBlock?.ReadVersionData(versiondata))
        val asciiStringv = versiondata.map { it.toChar() }.joinToString("")
        println("versiondata: $asciiStringv")


        val filePath = "/sdcard/Android/data/com.example.testscreen/txj_sensorhubv1.0.0.8.bin" // 请替换为实际的文件路径
        val file = File(filePath)
        val fota_start = ByteArray(36)
        val filelenth = file.length()
        println("lenth:${filelenth}")
        fota_start[0] = ((filelenth shr 24) and 0xFF).toByte()
        fota_start[1] = ((filelenth shr 16) and 0xFF).toByte()
        fota_start[2] = ((filelenth shr 8) and 0xFF).toByte()
        fota_start[3] = (filelenth and 0xFF).toByte()
//                println("fota_start content: ${fota_start.joinToString(",")}")

        if (file.exists()) {
            val md5Hash = calculateFileMD5(file)
            println("MD5 Hash of '$filePath' is: $md5Hash")
            val md5ByteArray = md5Hash.toByteArray()
            if (md5ByteArray.size <= fota_start.size) {
                System.arraycopy(md5ByteArray, 0, fota_start, 4, md5ByteArray.size)
                println("MD5 Hash copied to fota_start")
            } else {
                println("MD5 Hash is too long to fit into fota_start")
            }
        } else {
            println("File '$filePath' does not exist.")
        }
        println("ota_flag---${sensorDataBlock?.WriteSensorData(4, fota_start, 36)}")

//        start_fota_sendfile()


//        val setled = ByteArray(1);
//        setled[0] = 1;
//        sensorDataBlock?.WriteSensorData(8, setled, 1)

//        val setblinkled = ByteArray(3);
//        setblinkled[0] = 0;
//        setblinkled[1] = 0;
//        setblinkled[2] = 1;
//        println("{set blink}${sensorDataBlock?.WriteSensorData(10, setblinkled, 3)}")

//        getEcgData()
    }
    fun getDeviceName(key: String?, defaultValue: String?): String? {
        var value = defaultValue
        try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java, String::class.java)
            value = get.invoke(c, key, defaultValue) as? String
            Log.d("Main", "反射方式获取设备SN号成功 value = $value")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
        return value
    }

    fun getEcgData(){
        thread {
            while (true){
                val test_data = ByteArray(3596*10)
                val ecg_index = sensorDataBlock?.ReadECGData(test_data, 3596*10)
                val ecg_times = ecg_index?.div(3596) ?: 0
                val ecg_hz = 256
//                for (t in 0 until ecg_times)
//                {
//                    val ecgtimes = ((test_data[0].toInt() and 0xFF shl 24) or
//                            (test_data[1].toInt() and 0xFF shl 16) or
//                            (test_data[2].toInt() and 0xFF shl 8) or
//                            (test_data[3].toInt() and 0xFF))
//                    Log.d("tag", "time: $ecgtimes")
//                    for (m in 0 until ecg_hz)
//                    {
//                        val ecg0 = ((test_data[12 + m*14 + t*3596].toInt() and 0xFF shl 8) or (test_data[13 + m*14 + t*3596].toInt() and 0xFF))
//                        val ecg1 = ((test_data[14 + m*14 + t*3596].toInt() and 0xFF shl 8) or (test_data[15 + m*14 + t*3596].toInt() and 0xFF))
//                        val ecg2 = ((test_data[16 + m*14 + t*3596].toInt() and 0xFF shl 8) or (test_data[17 + m*14 + t*3596].toInt() and 0xFF))
//                        val ecg3 = ((test_data[18 + m*14 + t*3596].toInt() and 0xFF shl 8) or (test_data[19 + m*14 + t*3596].toInt() and 0xFF))
//                        val ecg4 = ((test_data[20 + m*14 + t*3596].toInt() and 0xFF shl 8) or (test_data[21 + m*14 + t*3596].toInt() and 0xFF))
//                        val ecg5 = ((test_data[22 + m*14 + t*3596].toInt() and 0xFF shl 8) or (test_data[23 + m*14 + t*3596].toInt() and 0xFF))
//                        val ecg6 = ((test_data[24 + m*14 + t*3596].toInt() and 0xFF shl 8) or (test_data[25 + m*14 + t*3596].toInt() and 0xFF))
//                        Log.d("tag", "ecg0: $ecg0")
//                        Log.d("tag", "ecg1: $ecg1")
//                        Log.d("tag", "ecg2: $ecg2")
//                        Log.d("tag", "ecg3: $ecg3")
//                        Log.d("tag", "ecg4: $ecg4")
//                        Log.d("tag", "ecg5: $ecg5")
//                        Log.d("tag", "ecg6: $ecg6")
//                        Thread.sleep(1)
//                    }
//                }

                val test_data1 = ByteArray(388*10)
                val index = sensorDataBlock?.ReadSportData(test_data1, 388*10)
                val cont = 32
                val times = index?.div(388)?:0

                for (j in 0 until times) {
                    val time = ((test_data1[0 + j*388].toInt() and 0xFF shl 24) or
                            (test_data1[1 + j*388].toInt() and 0xFF shl 16) or
                            (test_data1[2 + j*388].toInt() and 0xFF shl 8) or
                            (test_data1[3 + j*388].toInt() and 0xFF))
                    Log.d("tag", "cont:$cont")
                    for (i in 0 until cont) {
                        val accx =
                            ((test_data1[4 + i * 12 + j*388].toInt() and 0xFF shl 8) or (test_data1[5 + i * 12 + j*388].toInt() and 0xFF))
                        val accy =
                            ((test_data1[6 + i * 12 + j*388].toInt() and 0xFF shl 8) or (test_data1[7 + i * 12 + j*388].toInt() and 0xFF))
                        val accz =
                            ((test_data1[8 + i * 12 + j*388].toInt() and 0xFF shl 8) or (test_data1[9 + i * 12 + j*388].toInt() and 0xFF))
                        val groyx =
                            ((test_data1[10 + i * 12 + j*388].toInt() and 0xFF shl 8) or (test_data1[11 + i * 12 + j*388].toInt() and 0xFF))
                        val groyy =
                            ((test_data1[12 + i * 12 + j*388].toInt() and 0xFF shl 8) or (test_data1[13 + i * 12 + j*388].toInt() and 0xFF))
                        val groyz =
                            ((test_data1[14 + i * 12 + j*388].toInt() and 0xFF shl 8) or (test_data1[15 + i * 12 + j*388].toInt() and 0xFF))

                        val accxSigned = accx.toShort()
                        val accySigned = accy.toShort()
                        val acczSigned = accz.toShort()
                        val groyxSigned = groyx.toShort()
                        val groyySigned = groyy.toShort()
                        val groyzSigned = groyz.toShort()

//                        Log.d("tag", "i:$i")
//                        Log.d("tag", "time2: $time")
//                        Log.d("tag", "accx: $accxSigned")
//                        Log.d("tag", "accy: $accySigned")
//                        Log.d("tag", "accz: $acczSigned")
//                        Log.d("tag", "groyx: $groyxSigned")
//                        Log.d("tag", "groyy: $groyySigned")
//                        Log.d("tag", "groyz: $groyzSigned")

                        val my_data = """
                        time2: $time
                        accx: $accxSigned
                        accy: $accySigned
                        accz: $acczSigned
                        groyx: $groyxSigned
                        groyy: $groyySigned
                        groyz: $groyzSigned
                        """.trimIndent()+"\n"

                        val sportfile = File(getExternalFilesDir(null), "sportdata.txt")
                        if (!sportfile.exists()) sportfile.createNewFile()
                        try {
                            FileOutputStream(sportfile, true).use { outputStream ->
                                val bufferedOutputStream = BufferedOutputStream(outputStream)
                                bufferedOutputStream.write(my_data.toByteArray())
                                bufferedOutputStream.flush()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }
    }

    fun longToByteArrayBigEndian(value: Long): ByteArray {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.putLong(value)
        return buffer.array()
    }

    fun start_fota_sendfile()
    {
        val filePath = "/sdcard/Android/data/com.example.testscreen/txj_sensorhubv1.0.0.8.bin" // 请替换为实际的文件路径
        val file = File(filePath)
        val filelenth = file.length()
        println("filelenth=${filelenth}")
        val otadata = ByteArray(filelenth.toInt())
        val filelenthdiv = filelenth/4096
        val filelenthremain = filelenth%4096
        println("filelenthremain=${filelenthremain}")

        if (file.exists())
        {
            println("have file")
            val bytesRead = file.inputStream().use { input ->
                input.read(otadata)
            }
            println("otadata content in hexadecimal: ${otadata.joinToString(" ") { "%02X".format(it) }}")
        }

        thread {
            var running = true
            while(running)
            {
                Thread.sleep(1000)
                for(j in 0 until filelenthdiv) {
                    sensorDataBlock?.WriteSensorData(5, otadata.copyOfRange(j.toInt()*4096, (j.toInt() + 1)*4096), 4096)
                    Thread.sleep(500)
                }

                if (filelenthremain > 0)
                {
                    sensorDataBlock?.WriteSensorData(5, otadata.copyOfRange(filelenthdiv.toInt()*4096, filelenthdiv.toInt()*4096+filelenthremain.toInt()), filelenthremain.toInt())
                    Thread.sleep(500)
                }
                sensorDataBlock?.WriteSensorData(6, otadata, 0)
                running = false
            }
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                updateTouchPointPosition(x, y)
            }
            MotionEvent.ACTION_UP -> touchPoint!!.visibility = View.INVISIBLE
        }
        return true
    }

    private fun updateTouchPointPosition(x: Int, y: Int) {
        touchPoint!!.x = (x - touchPoint!!.width / 2).toFloat()
        touchPoint!!.y = (y - touchPoint!!.height / 2).toFloat()
        touchPoint!!.visibility = View.VISIBLE
    }

    private fun shutdown() {
        thread{
            while(true) {
                try {
                    val processBuilder = ProcessBuilder("su")
                    val process = processBuilder.start()
                    val out = DataOutputStream(process.outputStream)

                    out.writeBytes("reboot -p\n")
                    out.writeBytes("exit\n")
                    out.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                Thread.sleep(3000);
            }
        }
    }

    fun calculateFileMD5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192) // 8 KB buffer for reading file

        FileInputStream(file).use { inputStream ->
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                md.update(buffer, 0, bytesRead)
            }
        }

        val digestBytes = md.digest()

        val result = StringBuilder()
        for (byte in digestBytes) {
            result.append(String.format("%02x", byte))
        }

        return result.toString()
    }
}






