package com.licheedev.demo.base;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

public class BaseActivity extends RxAppCompatActivity {

    private KProgressHUD mPleaseWait;

    @Override
    protected void onStop() {
        super.onStop();
        dismissWaitingProgress();
    }

    public void showToast(int resId) {
        ToastUtil.show(this, resId);
    }

    public void showToast(String text) {
        ToastUtil.show(this, text);
    }

    public void showOneToast(int resId) {
        ToastUtil.showOne(this, resId);
    }

    public void showOneToast(String text) {
        ToastUtil.showOne(this, text);
    }

    public void showWaitingProgress(String text, boolean cancelable) {

        if (mPleaseWait == null) {
            mPleaseWait = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(text)
                .setCancellable(cancelable)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        } else {
            mPleaseWait.setLabel(text);
        }

        mPleaseWait.setCancellable(cancelable);
        mPleaseWait.show();
    }

    public void dismissWaitingProgress() {
        if (mPleaseWait != null && mPleaseWait.isShowing()) {
            mPleaseWait.dismiss();
        }
    }
}
