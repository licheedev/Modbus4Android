package com.licheedev.modbus4android;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.msg.ReadCoilsRequest;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsRequest;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;
import com.serotonin.modbus4j.msg.WriteCoilRequest;
import com.serotonin.modbus4j.msg.WriteCoilResponse;
import com.serotonin.modbus4j.msg.WriteCoilsRequest;
import com.serotonin.modbus4j.msg.WriteCoilsResponse;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;
import com.serotonin.modbus4j.msg.WriteRegistersRequest;
import com.serotonin.modbus4j.msg.WriteRegistersResponse;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

/**
 * ModbusWorker实现，实现了初始化modbus，并增加了线圈、离散量输入、寄存器的读写方法
 */
public class ModbusWorker implements IModbusWorker {

    private static final String TAG = "IModbusWorker";

    private static final String NO_INIT_MESSAGE = "ModbusMaster hasn't been inited!";
    private static final String MODBUS_THREAD_RELEASE_MESSAGE =
        "Modbus-working-thread hasn't been released!";

    protected final Handler mModbusHandler;
    protected final HandlerThread mModbusThread;
    protected final Scheduler mModbusScheduler;

    protected ModbusMaster mModbusMaster;

    public ModbusWorker() {
        mModbusThread = new HandlerThread("modbus-working-thread");
        mModbusThread.start();
        mModbusHandler = new Handler(mModbusThread.getLooper());
        mModbusScheduler = AndroidSchedulers.from(mModbusThread.getLooper());
    }

    /**
     * 关掉ModbusMaster
     */
    @Override
    public synchronized void closeModbusMaster() {
        if (mModbusMaster != null) {
            mModbusMaster.destroy();
            mModbusMaster = null;
        }
    }

    /**
     * 释放整个ModbusWorker，ModbusScheduler会被释放，ModbusMaster无法再次被打开
     */
    @Override
    public synchronized void release() {
        mModbusThread.quitSafely();
        if (mModbusMaster != null) {
            mModbusMaster.destroy();
            mModbusMaster = null;
        }
    }

    @Override
    public synchronized ModbusMaster getModbusMaster() {
        return mModbusMaster;
    }

    @Override
    public synchronized boolean isModbusOpened() {
        return getModbusMaster() != null;
    }

    public Scheduler getModbusScheduler() {
        return mModbusScheduler;
    }

    public Handler getModbusHandler() {
        return mModbusHandler;
    }

    @Override
    public void checkWorkingState() throws ModbusInitException, IllegalStateException {

        if (mModbusMaster == null) {
            throw new ModbusInitException(NO_INIT_MESSAGE);
        }

        if (mModbusThread.getLooper() == null) {
            throw new IllegalStateException(MODBUS_THREAD_RELEASE_MESSAGE);
        }
    }

    //<editor-fold desc="初始化Modbbus代码">

    /**
     * 初始化modbus
     *
     * @param param
     * @return
     */
    @Override
    public Observable<ModbusMaster> rxInit(final ModbusParam param) {
        return Observable.create(new ObservableOnSubscribe<ModbusMaster>() {
            @Override
            public void subscribe(ObservableEmitter<ModbusMaster> emitter) throws Exception {
                
                ModbusMaster master = param.createModbusMaster();

                try {
                    if (master == null) {
                        throw new ModbusInitException("Invalid ModbusParam!");
                    }
                    master.init();

                    mModbusMaster = master;

                    emitter.onNext(master);
                } catch (ModbusInitException e) {

                    Log.w(TAG, "ModbusMaster init failed", e);

                    if (master != null) {
                        master.destroy();
                        mModbusMaster = null;
                    }

                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 初始化modbus
     *
     * @param param
     * @param callback
     */
    @Override
    public void init(final ModbusParam param, final ModbusCallback<ModbusMaster> callback) {

        rxInit(param).observeOn(AndroidSchedulers.mainThread()).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                try {
                    callback.onFinally();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).subscribe(new Observer<ModbusMaster>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ModbusMaster modbusMaster) {
                try {
                    callback.onSuccess(modbusMaster);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                try {
                    callback.onFailure(e);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onComplete() {

            }
        });
    }
    //</editor-fold>

    /**
     * 通用订阅方法
     *
     * @param observable
     * @param callback
     * @param <M>
     */
    private <M extends ModbusResponse> void subscribe(Observable<M> observable,
        final ModbusCallback<M> callback) {
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

    //<editor-fold desc="01 (0x01)读线圈">

    /**
     * 01 (0x01)读线圈，同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 线圈数量
     * @return
     * @throws ModbusInitException
     * @throws ModbusTransportException
     * @throws ModbusRespException
     */
    public ReadCoilsResponse syncReadCoil(int slaveId, int start, int len)
        throws ModbusInitException, ModbusTransportException, ModbusRespException {

        checkWorkingState();

        ReadCoilsRequest request = new ReadCoilsRequest(slaveId, start, len);
        ReadCoilsResponse response = (ReadCoilsResponse) mModbusMaster.send(request);

        if (response.isException()) {
            throw new ModbusRespException(response);
        }
        return response;
    }

    /**
     * 01 (0x01)读线圈
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 线圈数量
     * @return
     */
    public Observable<ReadCoilsResponse> rxReadCoil(final int slaveId, final int start,
        final int len) {

        return Observable.create(new ObservableOnSubscribe<ReadCoilsResponse>() {
            @Override
            public void subscribe(ObservableEmitter<ReadCoilsResponse> emitter) throws Exception {

                try {
                    ReadCoilsResponse response = syncReadCoil(slaveId, start, len);
                    emitter.onNext(response);
                } catch (Exception e) {

                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }
                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 01 (0x01)读线圈
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 线圈数量
     * @param callback
     */
    public void readCoil(final int slaveId, final int start, final int len,
        final ModbusCallback<ReadCoilsResponse> callback) {

        Observable<ReadCoilsResponse> observable = rxReadCoil(slaveId, start, len);
        subscribe(observable, callback);
    }
    //</editor-fold>

    //<editor-fold desc="02（0x02）读离散量输入">

    /**
     * 02（0x02）读离散量输入，同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 输入数量
     * @return
     * @throws ModbusInitException
     * @throws ModbusTransportException
     * @throws ModbusRespException
     */
    public ReadDiscreteInputsResponse syncReadDiscreteInput(int slaveId, int start, int len)
        throws ModbusInitException, ModbusTransportException, ModbusRespException {

        checkWorkingState();

        ReadDiscreteInputsRequest request = new ReadDiscreteInputsRequest(slaveId, start, len);
        ReadDiscreteInputsResponse response =
            (ReadDiscreteInputsResponse) mModbusMaster.send(request);

        if (response.isException()) {
            throw new ModbusRespException(response);
        }
        return response;
    }

    /**
     * 02（0x02）读离散量输入
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 输入数量
     * @return
     */
    public Observable<ReadDiscreteInputsResponse> rxReadDiscreteInput(final int slaveId,
        final int start, final int len) {

        return Observable.create(new ObservableOnSubscribe<ReadDiscreteInputsResponse>() {
            @Override
            public void subscribe(ObservableEmitter<ReadDiscreteInputsResponse> emitter)
                throws Exception {

                try {
                    ReadDiscreteInputsResponse response =
                        syncReadDiscreteInput(slaveId, start, len);
                    emitter.onNext(response);
                } catch (Exception e) {

                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }
                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 02（0x02）读离散量输入
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 输入数量
     * @param callback
     */
    public void readDiscreteInput(final int slaveId, final int start, final int len,
        final ModbusCallback<ReadDiscreteInputsResponse> callback) {

        Observable<ReadDiscreteInputsResponse> observable =
            rxReadDiscreteInput(slaveId, start, len);
        subscribe(observable, callback);
    }
    //</editor-fold>

    //<editor-fold desc="03 (0x03)读保持寄存器">

    /**
     * 03 (0x03)读保持寄存器，同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 寄存器数量
     * @return
     * @throws ModbusInitException
     * @throws ModbusTransportException
     * @throws ModbusRespException
     */
    public ReadHoldingRegistersResponse syncReadHoldingRegisters(final int slaveId, final int start,
        final int len) throws ModbusInitException, ModbusTransportException, ModbusRespException {

        checkWorkingState();

        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, start, len);

        ReadHoldingRegistersResponse response =
            (ReadHoldingRegistersResponse) mModbusMaster.send(request);

        if (response.isException()) {
            throw new ModbusRespException(response);
        }
        return response;
    }

    /**
     * 03 (0x03)读保持寄存器
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 寄存器数量
     * @return
     */
    public Observable<ReadHoldingRegistersResponse> rxReadHoldingRegisters(final int slaveId,
        final int start, final int len) {

        return Observable.create(new ObservableOnSubscribe<ReadHoldingRegistersResponse>() {
            @Override
            public void subscribe(ObservableEmitter<ReadHoldingRegistersResponse> emitter)
                throws Exception {

                try {

                    ReadHoldingRegistersResponse response =
                        syncReadHoldingRegisters(slaveId, start, len);

                    emitter.onNext(response);
                } catch (Exception e) {

                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 03 (0x03)读保持寄存器
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 寄存器数量
     * @param callback
     */
    public void readHoldingRegisters(final int slaveId, final int start, final int len,
        final ModbusCallback<ReadHoldingRegistersResponse> callback) {

        Observable<ReadHoldingRegistersResponse> observable =
            rxReadHoldingRegisters(slaveId, start, len);
        subscribe(observable, callback);
    }
    //</editor-fold>

    //<editor-fold desc="04（0x04）读输入寄存器">

    /**
     * 04（0x04）读输入寄存器，同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 寄存器数量
     * @return
     * @throws ModbusInitException
     * @throws ModbusTransportException
     * @throws ModbusRespException
     */
    public ReadInputRegistersResponse syncReadInputRegisters(final int slaveId, final int start,
        final int len) throws ModbusInitException, ModbusTransportException, ModbusRespException {

        checkWorkingState();

        ReadInputRegistersRequest request = new ReadInputRegistersRequest(slaveId, start, len);
        ReadInputRegistersResponse response =
            (ReadInputRegistersResponse) mModbusMaster.send(request);

        if (response.isException()) {
            throw new ModbusRespException(response);
        }
        return response;
    }

    /**
     * 04（0x04）读输入寄存器
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 寄存器数量
     * @return
     */
    public Observable<ReadInputRegistersResponse> rxReadInputRegisters(final int slaveId,
        final int start, final int len) {

        return Observable.create(new ObservableOnSubscribe<ReadInputRegistersResponse>() {
            @Override
            public void subscribe(ObservableEmitter<ReadInputRegistersResponse> emitter)
                throws Exception {

                try {

                    ReadInputRegistersResponse response =
                        syncReadInputRegisters(slaveId, start, len);

                    emitter.onNext(response);
                } catch (Exception e) {

                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 04（0x04）读输入寄存器
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param len 寄存器数量
     * @param callback
     */
    public void readInputRegisters(final int slaveId, final int start, final int len,
        final ModbusCallback<ReadInputRegistersResponse> callback) {

        Observable<ReadInputRegistersResponse> observable =
            rxReadInputRegisters(slaveId, start, len);
        subscribe(observable, callback);
    }
    //</editor-fold>

    //<editor-fold desc="05（0x05）写单个线圈">

    /**
     * 05（0x05）写单个线圈，同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param offset 输出地址
     * @param value 输出值
     * @return
     * @throws ModbusInitException
     * @throws ModbusTransportException
     * @throws ModbusRespException
     */
    public WriteCoilResponse syncWriteCoil(final int slaveId, final int offset, final boolean value)
        throws ModbusInitException, ModbusTransportException, ModbusRespException {

        checkWorkingState();

        WriteCoilRequest request = new WriteCoilRequest(slaveId, offset, value);
        WriteCoilResponse response = (WriteCoilResponse) mModbusMaster.send(request);

        if (response.isException()) {
            throw new ModbusRespException(response);
        }

        return response;
    }

    /**
     * 05（0x05）写单个线圈，同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param offset 输出地址
     * @param value 输出值
     * @return
     */
    public Observable<WriteCoilResponse> rxWriteCoil(final int slaveId, final int offset,
        final boolean value) {

        return Observable.create(new ObservableOnSubscribe<WriteCoilResponse>() {
            @Override
            public void subscribe(ObservableEmitter<WriteCoilResponse> emitter) throws Exception {

                try {
                    WriteCoilResponse response = syncWriteCoil(slaveId, offset, value);
                    emitter.onNext(response);
                } catch (Exception e) {

                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 05（0x05）写单个线圈，同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param offset 输出地址
     * @param value 输出值
     * @param callback
     */
    public void writeCoil(final int slaveId, final int offset, final boolean value,
        final ModbusCallback<WriteCoilResponse> callback) {

        Observable<WriteCoilResponse> observable = rxWriteCoil(slaveId, offset, value);
        subscribe(observable, callback);
    }
    //</editor-fold>

    //<editor-fold desc="06（0x06）写单个寄存器">

    /**
     * 06 (0x06) 写单个寄存器, 同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param offset 寄存器地址
     * @param value 寄存器值
     * @return
     */
    public WriteRegisterResponse syncWriteSingleRegister(final int slaveId, final int offset,
        final int value) throws ModbusInitException, ModbusTransportException, ModbusRespException {

        checkWorkingState();

        WriteRegisterRequest request = new WriteRegisterRequest(slaveId, offset, value);
        WriteRegisterResponse response = (WriteRegisterResponse) mModbusMaster.send(request);

        if (response.isException()) {
            throw new ModbusRespException(response);
        }

        return response;
    }

    /**
     * 06 (0x06) 写单个寄存器
     *
     * @param slaveId 从设备ID
     * @param offset 寄存器地址
     * @param value 寄存器值
     * @return
     */
    public Observable<WriteRegisterResponse> rxWriteSingleRegister(final int slaveId,
        final int offset, final int value) {

        return Observable.create(new ObservableOnSubscribe<WriteRegisterResponse>() {
            @Override
            public void subscribe(ObservableEmitter<WriteRegisterResponse> emitter)
                throws Exception {

                //LogPlus.i("发送06，offset=" + offset);

                try {

                    WriteRegisterResponse response =
                        syncWriteSingleRegister(slaveId, offset, value);

                    emitter.onNext(response);
                } catch (Exception e) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 06 (0x06) 写单个寄存器
     *
     * @param slaveId 从设备ID
     * @param offset 寄存器地址
     * @param value 寄存器值
     */
    public void writeSingleRegister(final int slaveId, final int offset, final int value,
        final ModbusCallback<WriteRegisterResponse> callback) {

        Observable<WriteRegisterResponse> observable =
            rxWriteSingleRegister(slaveId, offset, value);
        subscribe(observable, callback);
    }

    //</editor-fold>

    //<editor-fold desc="15（0x0F）写多个线圈">

    /**
     * 15（0x0F）写多个线圈, 同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param values 输出值
     * @return
     * @throws ModbusInitException
     * @throws ModbusTransportException
     * @throws ModbusRespException
     */
    public WriteCoilsResponse syncWriteCoils(final int slaveId, final int start,
        final boolean[] values)
        throws ModbusInitException, ModbusTransportException, ModbusRespException {

        checkWorkingState();

        WriteCoilsRequest request = new WriteCoilsRequest(slaveId, start, values);
        WriteCoilsResponse response = (WriteCoilsResponse) mModbusMaster.send(request);

        if (response.isException()) {
            throw new ModbusRespException(response);
        }

        return response;
    }

    /**
     * 15（0x0F）写多个线圈
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param values 输出值
     * @return
     */
    public Observable<WriteCoilsResponse> rxWriteCoils(final int slaveId, final int start,
        final boolean[] values) {

        return Observable.create(new ObservableOnSubscribe<WriteCoilsResponse>() {
            @Override
            public void subscribe(ObservableEmitter<WriteCoilsResponse> emitter) throws Exception {

                try {

                    WriteCoilsResponse response = syncWriteCoils(slaveId, start, values);

                    emitter.onNext(response);
                } catch (Exception e) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 15（0x0F）写多个线圈
     *
     * @param slaveId 从设备ID
     * @param start 起始地址
     * @param values 输出值
     */
    public void writeCoils(final int slaveId, final int start, final boolean[] values,
        final ModbusCallback<WriteCoilsResponse> callback) {

        Observable<WriteCoilsResponse> observable = rxWriteCoils(slaveId, start, values);
        subscribe(observable, callback);
    }

    //</editor-fold>

    //<editor-fold desc="16（0x10）写多个寄存器">

    /**
     * 16 (0x10) 写多个寄存器, 同步，需在子线程运行
     *
     * @param slaveId 从设备ID
     * @param start 开始寄存器地址
     * @param values 寄存器值
     * @return
     */
    public WriteRegistersResponse syncWriteRegisters(final int slaveId, final int start,
        final short[] values)
        throws ModbusInitException, ModbusTransportException, ModbusRespException {

        checkWorkingState();

        WriteRegistersRequest request = new WriteRegistersRequest(slaveId, start, values);
        WriteRegistersResponse response = (WriteRegistersResponse) mModbusMaster.send(request);

        if (response.isException()) {
            throw new ModbusRespException(response);
        }

        return response;
    }

    /**
     * 16 (0x10) 写多个寄存器
     *
     * @param slaveId 从设备ID
     * @param start 开始寄存器地址
     * @param values 寄存器值
     * @return
     */
    public Observable<WriteRegistersResponse> rxWriteRegisters(final int slaveId, final int start,
        final short[] values) {

        return Observable.create(new ObservableOnSubscribe<WriteRegistersResponse>() {
            @Override
            public void subscribe(ObservableEmitter<WriteRegistersResponse> emitter)
                throws Exception {

                try {

                    WriteRegistersResponse response = syncWriteRegisters(slaveId, start, values);

                    emitter.onNext(response);
                } catch (Exception e) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 16 (0x10) 写多个寄存器
     *
     * @param slaveId 从设备ID
     * @param start 开始寄存器地址
     * @param values 寄存器值
     */
    public void writeRegisters(final int slaveId, final int start, final short[] values,
        final ModbusCallback<WriteRegistersResponse> callback) {

        Observable<WriteRegistersResponse> observable = rxWriteRegisters(slaveId, start, values);
        subscribe(observable, callback);
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
    public WriteRegistersResponse syncWriteRegistersButOne(final int slaveId, final int offset,
        final int value) throws ModbusInitException, ModbusTransportException, ModbusRespException {

        short[] shorts = { (short) value };
        return syncWriteRegisters(slaveId, offset, shorts);
    }

    /**
     * 16（0x10）写多个寄存器，但只写1个
     *
     * @param slaveId 从设备ID
     * @param start 开始寄存器地址
     * @param value 寄存器值
     * @return
     */
    public Observable<WriteRegistersResponse> rxWriteRegistersButOne(final int slaveId,
        final int start, final int value) {

        return Observable.create(new ObservableOnSubscribe<WriteRegistersResponse>() {
            @Override
            public void subscribe(ObservableEmitter<WriteRegistersResponse> emitter)
                throws Exception {

                try {

                    WriteRegistersResponse response =
                        syncWriteRegistersButOne(slaveId, start, value);

                    emitter.onNext(response);
                } catch (Exception e) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(mModbusScheduler);
    }

    /**
     * 16（0x10）写多个寄存器，但只写1个
     *
     * @param slaveId 从设备ID
     * @param start 开始寄存器地址
     * @param value 寄存器值
     */
    public void writeRegistersButOne(final int slaveId, final int start, final int value,
        final ModbusCallback<WriteRegistersResponse> callback) {

        Observable<WriteRegistersResponse> observable =
            rxWriteRegistersButOne(slaveId, start, value);
        subscribe(observable, callback);
    }
    //</editor-fold>
}
