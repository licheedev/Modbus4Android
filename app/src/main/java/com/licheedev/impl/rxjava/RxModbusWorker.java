package com.licheedev.impl.rxjava;

import com.licheedev.modbus4android.ModbusCallback;
import com.licheedev.modbus4android.ModbusParam;
import com.licheedev.modbus4android.ModbusWorker;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;
import com.serotonin.modbus4j.msg.WriteCoilResponse;
import com.serotonin.modbus4j.msg.WriteCoilsResponse;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;
import com.serotonin.modbus4j.msg.WriteRegistersResponse;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Action;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;

/**
 * ModbusWorker实现，RxJava扩展
 */
public class RxModbusWorker extends ModbusWorker {

    //<editor-fold desc="一些通用的方法">

    /**
     * Rx发送数据源
     *
     * @return
     */
    private <T> Observable<T> getRxObservable(final Callable<T> callable) {

        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                boolean terminated = false;
                try {
                    T t = doSync(callable);
                    if (!emitter.isDisposed()) {
                        terminated = true;
                        emitter.onNext(t);
                        emitter.onComplete();
                    }
                } catch (Throwable t) {
                    if (terminated) {
                        RxJavaPlugins.onError(t);
                    } else if (!emitter.isDisposed()) {
                        try {
                            emitter.onError(t);
                        } catch (Throwable inner) {
                            RxJavaPlugins.onError(new CompositeException(t, inner));
                        }
                    }
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 通用订阅方法
     *
     * @param observable
     * @param callback
     * @param <M>
     */
    private <M extends ModbusResponse> void subscribe(
        Observable<M> observable,
        final ModbusCallback<M> callback
    ) {
        observable
            // 切换UI线程
            .observeOn(AndroidSchedulers.mainThread()).doFinally(new Action() {
                @Override
                public void run() throws Exception {
                    try {
                        callback.onFinally();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).subscribe(new ModbusObserver<M>() {

                @Override
                public void onSuccess(M r) {
                    try {
                        callback.onSuccess(r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable tr) {
                    try {
                        callback.onFailure(tr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
    }
    //</editor-fold>

    //<editor-fold desc="初始化Modbbus代码">

    /**
     * 初始化modbus
     *
     * @param param
     * @return
     */
    public Observable<ModbusMaster> rxInit(final ModbusParam param) {
        return getRxObservable(callableInit(param)).subscribeOn(Schedulers.io());
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
    public Observable<ReadCoilsResponse> rxReadCoil(
        final int slaveId, final int start,
        final int len
    ) {
        return getRxObservable(callableReadCoil(slaveId, start, len));
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
    public Observable<ReadDiscreteInputsResponse> rxReadDiscreteInput(
        final int slaveId,
        final int start,
        final int len
    ) {
        return getRxObservable(callableReadDiscreteInput(slaveId, start, len));
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
    public Observable<ReadHoldingRegistersResponse> rxReadHoldingRegisters(
        final int slaveId,
        final int start,
        final int len
    ) {
        return getRxObservable(callableReadHoldingRegisters(slaveId, start, len));
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
    public Observable<ReadInputRegistersResponse> rxReadInputRegisters(
        final int slaveId,
        final int start,
        final int len
    ) {
        return getRxObservable(callableReadInputRegisters(slaveId, start, len));
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
    public Observable<WriteCoilResponse> rxWriteCoil(
        final int slaveId,
        final int offset,
        final boolean value
    ) {

        return getRxObservable(callableWriteCoil(slaveId, offset, value));
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
    public Observable<WriteRegisterResponse> rxWriteSingleRegister(
        final int slaveId,
        final int offset,
        final int value
    ) {
        return getRxObservable(callableWriteSingleRegister(slaveId, offset, value));
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
    public Observable<WriteCoilsResponse> rxWriteCoils(
        final int slaveId,
        final int start,
        final boolean[] values
    ) {

        return getRxObservable(callableWriteCoils(slaveId, start, values));
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
    public Observable<WriteRegistersResponse> rxWriteRegisters(
        final int slaveId,
        final int start,
        final short[] values
    ) {
        return getRxObservable(callableWriteRegisters(slaveId, start, values));
    }

    //</editor-fold>

    //<editor-fold desc="16（0x10）写多个寄存器，但只写1个">

    /**
     * 16（0x10）写多个寄存器，但只写1个
     *
     * @param slaveId 从设备ID
     * @param start 开始寄存器地址
     * @param value 寄存器值
     * @return
     */
    public Observable<WriteRegistersResponse> rxWriteRegistersButOne(
        final int slaveId,
        final int start,
        final int value
    ) {
        return rxWriteRegisters(slaveId, start, new short[] { (short) value });
    }
    //</editor-fold>
}
