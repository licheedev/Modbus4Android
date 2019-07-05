package com.licheedev.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.licheedev.demo.base.BaseActivity;

public class ChooseModeActivity extends BaseActivity {

    @BindView(R.id.btn_serial_port)
    Button mBtnSerialPort;
    @BindView(R.id.btn_tcp)
    Button mBtnTcp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);
        ButterKnife.bind(this);
    }

    @OnClick({ R.id.btn_serial_port, R.id.btn_tcp })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_serial_port:
                startActivity(MainActivity.newIntent(this, MainActivity.MODE_SERIAL));
                break;
            case R.id.btn_tcp:
                startActivity(MainActivity.newIntent(this, MainActivity.MODE_TCP));
                break;
        }
        finish();
    }
}
