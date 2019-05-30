package com.licheedev.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.licheedev.demo.base.BaseActivity;
import com.licheedev.demo.base.ByteUtil;
import com.licheedev.demo.modbus.DeviceConfig;
import com.licheedev.demo.modbus.ModbusManager;
import com.licheedev.modbus4android.ModbusCallback;
import com.licheedev.modbus4android.ModbusObserver;
import com.licheedev.modbus4android.ModbusParam;
import com.licheedev.modbus4android.param.SerialParam;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;
import com.serotonin.modbus4j.msg.WriteCoilResponse;
import com.serotonin.modbus4j.msg.WriteCoilsResponse;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;
import com.serotonin.modbus4j.msg.WriteRegistersResponse;
import com.trello.rxlifecycle2.android.ActivityEvent;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.ArrayList;
import java.util.Arrays;
import me.shihao.library.XRadioGroup;
import org.angmarch.views.NiceSpinner;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class MainActivity extends BaseActivity {

    @BindView(R.id.spinner_devices)
    NiceSpinner mSpinnerDevices;
    @BindView(R.id.spinner_baudrate)
    NiceSpinner mSpinnerBaudrate;
    @BindView(R.id.btn_switch)
    Button mBtnSwitch;
    @BindView(R.id.area_device)
    LinearLayout mAreaDevice;
    @BindView(R.id.label_fun)
    TextView mLabelFun;
    @BindView(R.id.rb_func01)
    RadioButton mRbFunc01;
    @BindView(R.id.rb_func02)
    RadioButton mRbFunc02;
    @BindView(R.id.rb_func03)
    RadioButton mRbFunc03;
    @BindView(R.id.rb_func04)
    RadioButton mRbFunc04;
    @BindView(R.id.rb_func05)
    RadioButton mRbFunc05;
    @BindView(R.id.rb_func06)
    RadioButton mRbFunc06;
    @BindView(R.id.rb_func15)
    RadioButton mRbFunc15;
    @BindView(R.id.rb_func16)
    RadioButton mRbFunc16;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    @BindView(R.id.rg_func)
    XRadioGroup mRgFunc;
    @BindView(R.id.et_offset)
    EditText mEtOffset;
    @BindView(R.id.area_address)
    LinearLayout mAreaAddress;
    @BindView(R.id.et_amount)
    EditText mEtAmount;
    @BindView(R.id.area_amount)
    LinearLayout mAreaAmount;
    @BindView(R.id.cb_coil_state)
    CheckBox mCbCoilState;
    @BindView(R.id.cb_hex)
    CheckBox mCbHex;
    @BindView(R.id.label_value)
    TextView mLabelValue;
    @BindView(R.id.et_single_value)
    EditText mEtSingleValue;
    @BindView(R.id.et_multi_value)
    EditText mEtMultiValue;
    @BindView(R.id.area_value)
    LinearLayout mAreaValue;
    @BindView(R.id.tv_console)
    TextView mTvConsole;
    @BindView(R.id.area_console)
    LinearLayout mAreaConsole;
    @BindView(R.id.btn_clear_record)
    Button mBtnClearRecord;
    @BindView(R.id.et_slave_id)
    EditText mEtSlaveId;
    private String[] mDevicePaths;
    private String[] mBaudrateStrs;
    private DeviceConfig mDeviceConfig;
    private int mDeviceIndex;
    private int mBaudrateIndex;
    private int[] mBaudrates;
    private int mOffset;
    private int mAmount;
    private int mRegValue;
    private boolean[] mCoilValues;
    private short[] mRegValues;
    private int mSalveId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRgFunc.setOnCheckedChangeListener(new XRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(XRadioGroup xRadioGroup, int i) {
                onRadioGroupUpdate(i);
            }
        });

        onRadioGroupUpdate(mRgFunc.getCheckedRadioButtonId());

        mDeviceConfig = DeviceConfig.get();
        mDevicePaths = mDeviceConfig.getDevicePaths();
        mBaudrateStrs = mDeviceConfig.getBaudrateStrs();
        mBaudrates = mDeviceConfig.getBaudrates();

        mSpinnerDevices.attachDataSource(Arrays.asList(mDevicePaths));
        mSpinnerBaudrate.attachDataSource(Arrays.asList(mBaudrateStrs));

        mDeviceIndex = mDeviceConfig.findDeviceIndex(mDeviceConfig.getDevice());
        mBaudrateIndex = mDeviceConfig.findBaudrateIndex(mDeviceConfig.getBaudrate());

        mSpinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDeviceIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinnerBaudrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBaudrateIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinnerDevices.setSelectedIndex(mDeviceIndex);
        mSpinnerBaudrate.setSelectedIndex(mBaudrateIndex);

        updateDeviceSwitchButton();
    }

    @Override
    protected void onDestroy() {
        ModbusManager.get().release();
        super.onDestroy();
    }

    private void onRadioGroupUpdate(int checkedId) {
        switch (checkedId) {
            case R.id.rb_func01:
                mLabelFun.setText(getString(R.string.gongnengma_s, "01（0x01）读线圈"));
                mAreaAmount.setVisibility(View.VISIBLE);
                mCbCoilState.setVisibility(View.GONE);
                mCbHex.setVisibility(View.GONE);
                mAreaValue.setVisibility(View.GONE);
                break;
            case R.id.rb_func02:
                mLabelFun.setText(getString(R.string.gongnengma_s, "02（0x02）读离散量输入"));
                mAreaAmount.setVisibility(View.VISIBLE);
                mCbCoilState.setVisibility(View.GONE);
                mCbHex.setVisibility(View.GONE);
                mAreaValue.setVisibility(View.GONE);
                break;
            case R.id.rb_func03:
                mLabelFun.setText(getString(R.string.gongnengma_s, "03（0x03）读保持寄存器"));
                mAreaAmount.setVisibility(View.VISIBLE);
                mCbCoilState.setVisibility(View.GONE);
                mCbHex.setVisibility(View.GONE);
                mAreaValue.setVisibility(View.GONE);
                break;
            case R.id.rb_func04:
                mLabelFun.setText(getString(R.string.gongnengma_s, "04（0x04）读输入寄存器"));
                mAreaAmount.setVisibility(View.VISIBLE);
                mCbCoilState.setVisibility(View.GONE);
                mCbHex.setVisibility(View.GONE);
                mAreaValue.setVisibility(View.GONE);
                break;
            case R.id.rb_func05:
                mLabelFun.setText(getString(R.string.gongnengma_s, "05（0x05）写单个线圈"));
                mAreaAmount.setVisibility(View.GONE);
                mCbCoilState.setVisibility(View.VISIBLE);
                mCbHex.setVisibility(View.GONE);
                mAreaValue.setVisibility(View.GONE);
                break;
            case R.id.rb_func06:
                mLabelFun.setText(getString(R.string.gongnengma_s, "06（0x06）写单个寄存器"));
                mAreaAmount.setVisibility(View.GONE);
                mCbCoilState.setVisibility(View.GONE);
                mCbHex.setVisibility(View.VISIBLE);
                mAreaValue.setVisibility(View.VISIBLE);
                mEtSingleValue.setVisibility(View.VISIBLE);
                mEtMultiValue.setVisibility(View.GONE);
                break;
            case R.id.rb_func15:
                mLabelFun.setText(getString(R.string.gongnengma_s, "15（0x0F）写多个线圈"));
                mAreaAmount.setVisibility(View.GONE);
                mCbCoilState.setVisibility(View.GONE);
                mCbHex.setVisibility(View.GONE);
                mAreaValue.setVisibility(View.VISIBLE);
                mEtSingleValue.setVisibility(View.GONE);
                mEtMultiValue.setHint(R.string.multi_coil_hint);
                mEtMultiValue.setVisibility(View.VISIBLE);
                break;
            case R.id.rb_func16:
                mLabelFun.setText(getString(R.string.gongnengma_s, "16（0x10）写多个寄存器"));
                mAreaAmount.setVisibility(View.GONE);
                mCbCoilState.setVisibility(View.GONE);
                mCbHex.setVisibility(View.VISIBLE);
                mAreaValue.setVisibility(View.VISIBLE);
                mEtSingleValue.setVisibility(View.GONE);
                mEtMultiValue.setHint(R.string.multi_reg_hint);
                mEtMultiValue.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick({
        R.id.btn_switch, R.id.btn_send, R.id.btn_clear_record
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_switch:
                openDevice();
                break;
            case R.id.btn_send:
                trySend();
                break;
            case R.id.btn_clear_record:
                mTvConsole.setText("");
                break;
        }
    }

    /**
     * 打开设备
     */
    private void openDevice() {
        if (ModbusManager.get().isModbusOpened()) {
            // 关闭设备
            ModbusManager.get().closeModbusMaster();
            updateDeviceSwitchButton();
            return;
        }

        String path = mDevicePaths[mDeviceIndex];
        int baudrate = mBaudrates[mBaudrateIndex];

        mDeviceConfig.updateSerialConfig(path, baudrate);

        // 串口
        ModbusParam serialParam =
            SerialParam.create(path, baudrate).setTimeout(1000).setRetries(0); // 不重试

        ModbusManager.get().closeModbusMaster();
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
                updateDeviceSwitchButton();
            }
        });
    }

    /**
     * 切换界面状态
     */
    private void updateDeviceSwitchButton() {
        if (ModbusManager.get().isModbusOpened()) {
            mBtnSwitch.setText("断开");
            mSpinnerDevices.setEnabled(false);
            mSpinnerBaudrate.setEnabled(false);
            mBtnSend.setEnabled(true);
        } else {
            mBtnSwitch.setText("连接");
            mSpinnerDevices.setEnabled(true);
            mSpinnerBaudrate.setEnabled(true);
            mBtnSend.setEnabled(false);
        }
    }

    /**
     * 发送数据
     */
    private void trySend() {
        if (!ModbusManager.get().isModbusOpened()) {
            showOneToast("未打开设备");
            return;
        }

        updteValues();

        switch (mRgFunc.getCheckedRadioButtonId()) {
            case R.id.rb_func01:
                send01();
                break;
            case R.id.rb_func02:
                send02();
                break;
            case R.id.rb_func03:
                send03();
                break;
            case R.id.rb_func04:
                send04();
                break;
            case R.id.rb_func05:
                send05();
                break;
            case R.id.rb_func06:
                send06();
                break;
            case R.id.rb_func15:
                send15();
                break;
            case R.id.rb_func16:
                send16();
                break;
            default:
                showOneToast("TODO");
                break;
        }
    }

    private void appendError(String func, Throwable tr) {
        mTvConsole.append(func + "异常:" + tr + "\n");
    }

    private void send01() {

        if (checkSlave() && checkOffset() && checkAmount()) {

            final int amount = mAmount;

            ModbusManager.get()
                .readCoil(mSalveId, mOffset, mAmount, new ModbusCallback<ReadCoilsResponse>() {
                    @Override
                    public void onSuccess(ReadCoilsResponse readCoilsResponse) {

                        boolean[] sub =
                            ArrayUtils.subarray(readCoilsResponse.getBooleanData(), 0, amount);
                        mTvConsole.append("F01读取：" + ArrayUtils.toString(sub) + "\n");
                    }

                    @Override
                    public void onFailure(Throwable tr) {
                        appendError("F01", tr);
                    }

                    @Override
                    public void onFinally() {

                    }
                });
        }
    }

    private void send02() {
        if (checkSlave() && checkOffset() && checkAmount()) {

            final int amount = mAmount;

            ModbusManager.get()
                .readDiscreteInput(mSalveId, mOffset, mAmount,
                    new ModbusCallback<ReadDiscreteInputsResponse>() {
                        @Override
                        public void onSuccess(
                            ReadDiscreteInputsResponse readDiscreteInputsResponse) {

                            boolean[] sub =
                                ArrayUtils.subarray(readDiscreteInputsResponse.getBooleanData(), 0,
                                    amount);
                            mTvConsole.append("F02读取：" + ArrayUtils.toString(sub) + "\n");
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            appendError("F02", tr);
                        }

                        @Override
                        public void onFinally() {

                        }
                    });
        }
    }

    private void send03() {

        if (checkSlave() && checkOffset() && checkAmount()) {

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

          
        }
    }

    private void send04() {

        if (checkSlave() && checkOffset() && checkAmount()) {

            ModbusManager.get()
                .readInputRegisters(mSalveId, mOffset, mAmount,
                    new ModbusCallback<ReadInputRegistersResponse>() {
                        @Override
                        public void onSuccess(
                            ReadInputRegistersResponse readInputRegistersResponse) {
                            byte[] data = readInputRegistersResponse.getData();
                            mTvConsole.append("F04读取：" + ByteUtil.bytes2HexStr(data) + "\n");
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            appendError("F04", tr);
                        }

                        @Override
                        public void onFinally() {

                        }
                    });
        }
    }

    private void send05() {

        if (checkSlave() && checkOffset()) {

            ModbusManager.get()
                .writeCoil(mSalveId, mOffset, mCbCoilState.isChecked(),
                    new ModbusCallback<WriteCoilResponse>() {
                        @Override
                        public void onSuccess(WriteCoilResponse writeCoilResponse) {
                            mTvConsole.append("F05写入成功\n");
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            appendError("F05", tr);
                        }

                        @Override
                        public void onFinally() {

                        }
                    });
        }
    }

    private void send06() {
        if (checkSlave() && checkOffset() && checkRegValue()) {

            ModbusManager.get()
                .writeSingleRegister(mSalveId, mOffset, mRegValue,
                    new ModbusCallback<WriteRegisterResponse>() {
                        @Override
                        public void onSuccess(WriteRegisterResponse writeRegisterResponse) {
                            mTvConsole.append("F06写入成功\n");
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            appendError("F06", tr);
                        }

                        @Override
                        public void onFinally() {

                        }
                    });
        }
    }

    private void send15() {
        if (checkSlave() && checkOffset() && checkCoilValues()) {

            ModbusManager.get()
                .writeCoils(mSalveId, mOffset, mCoilValues,
                    new ModbusCallback<WriteCoilsResponse>() {
                        @Override
                        public void onSuccess(WriteCoilsResponse writeCoilsResponse) {
                            mTvConsole.append("F15写入成功\n");
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            appendError("F15", tr);
                        }

                        @Override
                        public void onFinally() {

                        }
                    });
        }
    }

    private void send16() {

        if (checkSlave() && checkOffset() && checkRegValues()) {

            ModbusManager.get()
                .writeRegisters(mSalveId, mOffset, mRegValues,
                    new ModbusCallback<WriteRegistersResponse>() {
                        @Override
                        public void onSuccess(WriteRegistersResponse writeRegistersResponse) {
                            // 发送成功
                            mTvConsole.append("F16写入成功\n");
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            appendError("F07", tr);
                        }

                        @Override
                        public void onFinally() {

                        }
                    });
        }
    }

    /**
     * 更新解析数值
     */
    private void updteValues() {

        // 设备地址
        mSalveId = Integer.MIN_VALUE;
        try {
            mSalveId = Integer.parseInt(mEtSlaveId.getText().toString().trim());
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }

        // 数据地址
        mOffset = Integer.MIN_VALUE;
        try {
            mOffset = Integer.parseInt(mEtOffset.getText().toString().trim());
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }

        // 寄存器/线圈数量
        mAmount = Integer.MIN_VALUE;
        try {
            mAmount = Integer.parseInt(mEtAmount.getText().toString().trim());
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }

        // 进制
        int radix = mCbHex.isChecked() ? 16 : 10;

        mRegValue = Integer.MIN_VALUE;
        try {
            int value = Integer.parseInt(mEtSingleValue.getText().toString().trim(), radix);
            if (value >= 0 && value <= 0xFFFF) {
                mRegValue = value;
            }
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }

        mCoilValues = null;
        try {
            String[] split = StringUtils.split(mEtMultiValue.getText().toString().trim(), ',');
            ArrayList<Integer> result = new ArrayList<>();
            for (String s : split) {
                result.add(Integer.parseInt(s.trim()));
            }
            boolean[] values = new boolean[result.size()];
            for (int i = 0; i < values.length; i++) {
                int v = result.get(i);
                if (v == 0 || v == 1) {
                    values[i] = v == 1;
                } else {
                    throw new RuntimeException();
                }
            }

            if (values.length > 0) {
                mCoilValues = values;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        mRegValues = null;
        try {
            String[] split = StringUtils.split(mEtMultiValue.getText().toString().trim(), ',');
            ArrayList<Integer> result = new ArrayList<>();
            for (String s : split) {
                result.add(Integer.parseInt(s.trim(), radix));
            }
            short[] values = new short[result.size()];
            for (int i = 0; i < values.length; i++) {
                int v = result.get(i);
                if (v >= 0 && v <= 0xffff) {
                    values[i] = (short) v;
                } else {
                    throw new RuntimeException();
                }
            }
            if (values.length > 0) {
                mRegValues = values;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /**
     * 检查设备地址
     *
     * @return
     */
    private boolean checkSlave() {
        if (mSalveId == Integer.MIN_VALUE) {
            showOneToast("无效设备地址");
            return false;
        }
        return true;
    }

    /**
     * 检查数据地址
     *
     * @return
     */
    private boolean checkOffset() {
        if (mOffset == Integer.MIN_VALUE) {
            showOneToast("无效地址");
            return false;
        }
        return true;
    }

    /**
     * 检查数量
     */
    private boolean checkAmount() {

        if (mAmount == Integer.MIN_VALUE) {
            showOneToast("无效数量");
            return false;
        }
        return true;
    }

    /**
     * 检查单（寄存器）数值
     *
     * @return
     */
    private boolean checkRegValue() {
        if (mRegValue == Integer.MIN_VALUE) {
            showOneToast("无效输出值");
            return false;
        }
        return true;
    }

    /**
     * 检查多个线圈数值
     *
     * @return
     */
    private boolean checkCoilValues() {
        if (mCoilValues == null) {
            showOneToast("无效输出值");
            return false;
        }
        return true;
    }

    /**
     * 检查多个线圈输出值
     *
     * @return
     */
    private boolean checkRegValues() {
        if (mRegValues == null) {
            showOneToast("无效输出值");
            return false;
        }
        return true;
    }
}
