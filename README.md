# Modbus4Android
Modbus的Android实现，添加对Android串口（RTU）的支持

[![](https://jitpack.io/v/licheedev/Modbus4Android.svg)](https://jitpack.io/#licheedev/Modbus4Android)

![APP](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/app.png)
![TCP](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/tcp.png)

不了解Modbus的，可以阅读一下 [MODBUS通讯协议](https://github.com/licheedev/Modbus4Android/blob/master/imgs/modbus_proto_cn.pdf)

参考
> https://github.com/infiniteautomation/modbus4j
>
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
	        implementation 'com.github.licheedev:Modbus4Android:3.0.0'
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
ModbusParam param;

if (mMode == MODE_SERIAL) {
    // 串口
    String path = mDevicePaths[mDeviceIndex];
    int baudrate = mBaudrates[mBaudrateIndex];

    mDeviceConfig.updateSerialConfig(path, baudrate);
    param = SerialParam.create(path, baudrate) // 串口地址和波特率
        .setDataBits(mDataBits) // 数据位
        .setParity(mParity) // 校验位
        .setStopBits(mStopBits) // 停止位
        .setTimeout(1000).setRetries(0); // 不重试
} else {
    // TCP
    String host = mEtHost.getText().toString().trim();
    int port = 0;
    try {
        port = Integer.parseInt(mEtPort.getText().toString().trim());
    } catch (NumberFormatException e) {
        //e.printStackTrace();
    }
    param = TcpParam.create(host, port)
        .setTimeout(1000)
        .setRetries(0)
        .setEncapsulated(false)
        .setKeepAlive(true);
}

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


### 可选配置
```java
// 启用rtu的crc校验（默认就启用）
ModbusConfig.setEnableRtuCrc(true);
// 打印数据log（默认全禁用）
// System.out: MessagingControl.send: 01030000000305cb
// System.out: MessagingConnection.read: 010306000100020000bd75
ModbusConfig.setEnableDataLog(true, true);
```

### 功能码操作示例
#### 普通用法
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
```

其他功能码的用法，可以参考Demo的[MainActivity.java](https://github.com/licheedev/Modbus4Android/blob/master/app/src/main/java/com/licheedev/demo/MainActivity.java)

#### RxJava用法（2.0，其他版本按需修改）
复制[rxjava](https://github.com/licheedev/Modbus4Android/tree/master/app/src/main/java/com/licheedev/impl/rxjava)相关文件到自己的项目中

```java
// 修改实现方式
//public class ModbusManager extends ModbusWorker {}
public class ModbusManager extends RxModbusWorker {}

// Rx写法
ModbusManager.get()
    .rxReadHoldingRegisters(mSalveId, mOffset, mAmount)
    .observeOn(AndroidSchedulers.mainThread())
    .compose(this.<ReadHoldingRegistersResponse>bindUntilEvent(ActivityEvent.DESTROY))
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

#### Kotlin协程用法
复制[ModbusWorkers.kt](https://github.com/licheedev/Modbus4Android/tree/master/app/src/main/java/com/licheedev/impl/kotlin/ModbusWorkers.kt)文件到自己的项目中
```kotlin
// 协程写法
lifecycleScope.launch {
    try {
        val response =
            ModbusManager.get().awaitReadHoldingRegisters(mSalveId, mOffset, mAmount)
        val data: ByteArray = response.data
        appendText("F03读取：" + ByteUtil.bytes2HexStr(data) + "\n")
    } catch (e: Exception) {
        appendError("F03", e)
    }
}
```



## 截图
![F01](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/01.png)
![F02](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/02.png)
![F03](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/03.png)
![F04](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/04.png)
![F05](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/05.gif)
![F06](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/06.gif)
![F15](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/15.gif)
![F16](https://raw.githubusercontent.com/licheedev/Modbus4Android/master/imgs/16.gif)

