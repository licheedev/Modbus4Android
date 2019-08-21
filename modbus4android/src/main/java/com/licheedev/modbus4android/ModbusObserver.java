package com.licheedev.modbus4android;

import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ModbusResponse;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class ModbusObserver<T extends ModbusResponse> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        if (t.isException()) {
            onFailure(new ModbusRespException(t));
        } else {
            onSuccess(t);
        }
    }

    @Override
    public void onError(Throwable e) {
        onFailure(e);
    }

    @Override
    public void onComplete() {

    }

    public abstract void onSuccess(T t);

    /**
     * 失败
     *
     * @param tr
     * @see ModbusInitException 初始化失败或者没有初始化时的异常
     * @see ModbusTransportException modbus请求失败的异常
     * @see ModbusRespException modbus有响应，但是包含错误
     */
    public abstract void onFailure(Throwable tr);
}
