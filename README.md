# Modbus4Android
Modbus的Android实现，添加对Android串口的支持，支持RxJava操作


![APP](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/app.png)

不了解Modbus的，可以阅读一下 [MODBUS通讯协议](https://github.com/licheedev/Modbus4Android/blob/master/imgs/modbus_proto_cn.pdf)

参考
> https://github.com/infiniteautomation/modbus4j
> https://github.com/zgkxzx/Modbus4Android

## 使用

### 添加依赖
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

  dependencies {
        implementation 'com.github.licheedev:Modbus4Android:0.2'
}

```

### 创建管理类单例

```java
public class ModbusManager extends ModbusWorker {

    private static volatile ModbusManager sInstance;

    public static ModbusManager get() {
        ModbusManager manager = sInstance;
        if (manager == null) {
            synchronized (ModbusManager.class) {
                manager = sInstance;
                if (manager == null) {
                    manager = new ModbusManager();
                    sInstance = manager;
                }
            }
        }
        return manager;
    }

    private ModbusManager() {
    }

    /**
     * 释放整个ModbusManager，单例会被置null
     */
    public synchronized void release() {
        super.release();
        sInstance = null;
    }
}

```

### 开启设备
```java
ModbusParam serialParam =
    SerialParam.create(devicePath, baudrate).setTimeout(1000).setRetries(0); // 不重试

ModbusManager.get().closeModbusMaster(); // 先关闭一下
ModbusManager.get().init(serialParam, new ModbusCallback<ModbusMaster>() {
    @Override
    public void onSuccess(ModbusMaster modbusMaster) {
        showOneToast("打开成功");
    }

    @Override
    public void onFailure(Throwable tr) {
        showOneToast("打开失败");
    }

    @Override
    public void onFinally() {
        // todo updateDeviceSwitchButton();
    }
});
```

### 功能码操作示例
```java
// 普通写法
ModbusManager.get()
    .readHoldingRegisters(mSalveId, mOffset, mAmount,
        new ModbusCallback<ReadHoldingRegistersResponse>() {
            @Override
            public void onSuccess(
                ReadHoldingRegistersResponse readHoldingRegistersResponse) {
                byte[] data = readHoldingRegistersResponse.getData();
                mTvConsole.append("F03读取：" + ByteUtil.bytes2HexStr(data) + "\n");
            }

            @Override
            public void onFailure(Throwable tr) {
                appendError("F03", tr);
            }

            @Override
            public void onFinally() {

            }
        });

// Rx写法
ModbusManager.get()
    .rxReadHoldingRegisters(mSalveId, mOffset, mAmount)
    .compose(this.<ReadHoldingRegistersResponse>bindUntilEvent(ActivityEvent.DESTROY))
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new ModbusObserver<ReadHoldingRegistersResponse>() {
        @Override
        public void onSuccess(
            ReadHoldingRegistersResponse readHoldingRegistersResponse) {
            byte[] data = readHoldingRegistersResponse.getData();
            mTvConsole.append("F03读取：" + ByteUtil.bytes2HexStr(data) + "\n");
        }

        @Override
        public void onFailure(Throwable tr) {
            appendError("F03", tr);
        }
    });
```

其他功能码的用法，可以参考Demo的[MainActivity.java](https://github.com/licheedev/Modbus4Android/blob/master/app/src/main/java/com/licheedev/demo/MainActivity.java)


## 截图
![F01](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/01.png)
![F02](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/02.png)
![F03](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/03.png)
![F04](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/04.png)
![F05](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/05.gif)
![F06](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/06.gif)
![F15](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/15.gif)
![F16](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/16.gif)

