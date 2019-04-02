package com.licheedev.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.licheedev.demo.base.BaseActivity;
import com.licheedev.demo.base.ByteUtil;
import com.licheedev.demo.modbus.DeviceConfig;
import com.licheedev.demo.modbus.ModbusManager;
import com.licheedev.modbus4android.ModbusCallback;
import com.licheedev.modbus4android.ModbusParam;
import com.licheedev.modbus4android.param.SerialParam;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.WriteRegistersResponse;
import java.util.Arrays;
import org.angmarch.views.NiceSpinner;

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
    RadioGroup mRgFunc;
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
    private String[] mDevicePaths;
    private String[] mBaudrateStrs;
    private DeviceConfig mDeviceConfig;
    private int mDeviceIndex;
    private int mBaudrateIndex;
    private int[] mBaudrates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRgFunc.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                onRadioGroupUpdate(checkedId);
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
                mEtMultiValue.setVisibility(View.VISIBLE);
                break;
            case R.id.rb_func16:
                mLabelFun.setText(getString(R.string.gongnengma_s, "16（0x10）写多个寄存器"));
                mAreaAmount.setVisibility(View.GONE);
                mCbCoilState.setVisibility(View.GONE);
                mCbHex.setVisibility(View.VISIBLE);
                mAreaValue.setVisibility(View.VISIBLE);
                mEtSingleValue.setVisibility(View.GONE);
                mEtMultiValue.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick({
        R.id.spinner_devices, R.id.spinner_baudrate, R.id.btn_switch, R.id.label_fun, R.id.btn_send,
        R.id.rg_func, R.id.et_offset, R.id.et_amount, R.id.area_amount, R.id.cb_coil_state,
        R.id.cb_hex, R.id.label_value, R.id.et_single_value, R.id.et_multi_value, R.id.area_value,
        R.id.tv_console, R.id.btn_clear_record
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.spinner_devices:
                break;
            case R.id.spinner_baudrate:
                break;
            case R.id.btn_switch:
                openDevice();
                break;
            case R.id.label_fun:
                break;
            case R.id.btn_send:
                trySend();
                break;
            case R.id.rg_func:
                break;
            case R.id.et_offset:
                break;
            case R.id.et_amount:
                break;
            case R.id.area_amount:
                break;
            case R.id.cb_coil_state:
                break;
            case R.id.cb_hex:
                break;
            case R.id.label_value:
                break;
            case R.id.et_single_value:
                break;
            case R.id.et_multi_value:
                break;
            case R.id.area_value:
                break;
            case R.id.tv_console:
                break;
            case R.id.btn_clear_record:
                mTvConsole.setText("");
                break;
        }
    }

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

        // TCP
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
        } else {
            mBtnSwitch.setText("连接");
            mSpinnerDevices.setEnabled(true);
            mSpinnerBaudrate.setEnabled(true);
        }
    }

    private void trySend() {
        if (!ModbusManager.get().isModbusOpened()) {
            showOneToast("未打开设备");
            return;
        }

        switch (mRgFunc.getCheckedRadioButtonId()) {
            case R.id.rb_func03:
                send03();
                break;
            case R.id.rb_func16:
                send16();
                break;
            default:
                showOneToast("TODO");
                break;
        }
    }

    private void send16() {

        ModbusManager.get()
            .writeRegisters(0x01, 0x1d, new short[] { 0x0001 },
                new ModbusCallback<WriteRegistersResponse>() {
                    @Override
                    public void onSuccess(WriteRegistersResponse writeRegistersResponse) {
                        // 发送成功
                    }

                    @Override
                    public void onFailure(Throwable tr) {

                    }

                    @Override
                    public void onFinally() {

                    }
                });
    }

    private void send03() {

        ModbusManager.get()
            .readHoldingRegisters(0x01, 0x01, 2,
                new ModbusCallback<ReadHoldingRegistersResponse>() {
                    @Override
                    public void onSuccess(
                        ReadHoldingRegistersResponse readHoldingRegistersResponse) {
                        //short[] shortData = readHoldingRegistersResponse.getShortData();
                        byte[] data = readHoldingRegistersResponse.getData();
                        mTvConsole.append("F03读取：" + ByteUtil.bytes2HexStr(data)+"\n");
                    }

                    @Override
                    public void onFailure(Throwable tr) {

                    }

                    @Override
                    public void onFinally() {

                    }
                });
    }
}
