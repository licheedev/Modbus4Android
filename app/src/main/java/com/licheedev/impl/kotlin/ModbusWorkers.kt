package com.licheedev.impl.kotlin

import android.os.AsyncTask
import com.licheedev.modbus4android.ModbusParam
import com.licheedev.modbus4android.ModbusRespException
import com.licheedev.modbus4android.ModbusWorker
import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.exception.ModbusInitException
import com.serotonin.modbus4j.exception.ModbusTransportException
import com.serotonin.modbus4j.msg.ReadCoilsResponse
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse
import com.serotonin.modbus4j.msg.WriteCoilResponse
import com.serotonin.modbus4j.msg.WriteCoilsResponse
import com.serotonin.modbus4j.msg.WriteRegisterResponse
import com.serotonin.modbus4j.msg.WriteRegistersResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/** 使用协程执行 */
suspend fun <T> ModbusWorker.awaitRun(callable: Callable<T>): T {
    return suspendCancellableCoroutine { continuation ->
        val task = object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    val result = doSync(callable)
                    continuation.resume(result)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
                return null
            }
        }

        continuation.invokeOnCancellation {
            task.cancel(false)
        }
        task.execute()
    }
}

//<editor-fold desc="初始化Modbbus代码">
/**
 * 初始化modbus
 *
 * @param param
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitInit(param: ModbusParam<*>): ModbusMaster {
    return awaitRun(callableInit(param))
}
//</editor-fold>

//<editor-fold desc="01 (0x01)读线圈">
/**
 * 01 (0x01)读线圈
 *
 * @param slaveId 从设备ID
 * @param start 起始地址
 * @param len 线圈数量
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitReadCoil(
    slaveId: Int,
    start: Int,
    len: Int
): ReadCoilsResponse {
    return awaitRun(callableReadCoil(slaveId, start, len))
}
//</editor-fold>

//<editor-fold desc="02（0x02）读离散量输入">
/**
 * 02（0x02）读离散量输入
 *
 * @param slaveId 从设备ID
 * @param start 起始地址
 * @param len 输入数量
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitReadDiscreteInput(
    slaveId: Int,
    start: Int,
    len: Int
): ReadDiscreteInputsResponse {
    return awaitRun(callableReadDiscreteInput(slaveId, start, len))
}
//</editor-fold>

//<editor-fold desc="03 (0x03)读保持寄存器">

/**
 * 03 (0x03)读保持寄存器
 *
 * @param slaveId 从设备ID
 * @param start 起始地址
 * @param len 寄存器数量
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitReadHoldingRegisters(
    slaveId: Int,
    start: Int,
    len: Int
): ReadHoldingRegistersResponse {
    return awaitRun(callableReadHoldingRegisters(slaveId, start, len))
}

//</editor-fold>

//<editor-fold desc="04（0x04）读输入寄存器">

/**
 * 04（0x04）读输入寄存器
 *
 * @param slaveId 从设备ID
 * @param start 起始地址
 * @param len 寄存器数量
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitReadInputRegisters(
    slaveId: Int,
    start: Int,
    len: Int
): ReadInputRegistersResponse {
    return awaitRun(callableReadInputRegisters(slaveId, start, len))
}
//</editor-fold>

//<editor-fold desc="05（0x05）写单个线圈">
/**
 * 05（0x05）写单个线圈
 *
 * @param slaveId 从设备ID
 * @param offset 输出地址
 * @param value 输出值
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitWriteCoil(
    slaveId: Int,
    offset: Int,
    value: Boolean
): WriteCoilResponse {
    return awaitRun(callableWriteCoil(slaveId, offset, value))
}

//</editor-fold>

//<editor-fold desc="06（0x06）写单个寄存器">

/**
 * 06 (0x06) 写单个寄存器
 *
 * @param slaveId 从设备ID
 * @param offset 寄存器地址
 * @param value 寄存器值
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitWriteSingleRegister(
    slaveId: Int,
    offset: Int,
    value: Int
): WriteRegisterResponse {
    return awaitRun(callableWriteSingleRegister(slaveId, offset, value))
}

//</editor-fold>

//<editor-fold desc="15（0x0F）写多个线圈">

/**
 * 15（0x0F）写多个线圈
 *
 * @param slaveId 从设备ID
 * @param start 起始地址
 * @param values 输出值
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitWriteCoils(
    slaveId: Int,
    start: Int,
    values: BooleanArray
): WriteCoilsResponse {
    return awaitRun(callableWriteCoils(slaveId, start, values))
}

//</editor-fold>

//<editor-fold desc="16（0x10）写多个寄存器">

/**
 * 16 (0x10) 写多个寄存器
 *
 * @param slaveId 从设备ID
 * @param start 开始寄存器地址
 * @param values 寄存器值
 * @return
 */
@Throws(
    InterruptedException::class,
    ExecutionException::class,
    ModbusTransportException::class,
    ModbusInitException::class,
    ModbusRespException::class
)
suspend fun ModbusWorker.awaitWriteRegisters(
    slaveId: Int,
    start: Int,
    values: ShortArray
): WriteRegistersResponse {
    return awaitRun(callableWriteRegisters(slaveId, start, values))
}

//</editor-fold>

//<editor-fold desc="16（0x10）写多个寄存器，但只写1个">
/**
 * 16（0x10）写多个寄存器，但只写1个, 同步，需在子线程运行
 *
 * @param slaveId 从设备ID
 * @param offset 寄存器地址
 * @param value 寄存器值
 * @return
 */
@Throws(java.lang.Exception::class)
suspend fun ModbusWorker.awaitWriteRegistersButOne(
    slaveId: Int,
    offset: Int,
    value: Int
): WriteRegistersResponse {
    val shorts = shortArrayOf(value.toShort())
    return awaitWriteRegisters(slaveId, offset, shorts)
}
//</editor-fold>