package com.licheedev.demo

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import com.licheedev.demo.base.BaseActivity
import com.licheedev.demo.base.ByteUtil
import com.licheedev.demo.modbus.ModbusManager
import com.licheedev.impl.kotlin.awaitReadHoldingRegisters
import com.licheedev.impl.rxjava.ModbusObserver
import com.licheedev.modbus4android.ModbusCallback
import com.licheedev.myutils.LogPlus
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse
import com.trello.rxlifecycle2.android.ActivityEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch
import java.util.*

class MainActivityKt : BaseActivity() {

    @BindView(R.id.tv_console)
    var mTvConsole: TextView? = null

    private val mOffset = 0
    private val mAmount = 0
    private val mSalveId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun appendError(func: String, tr: Throwable) {
        LogPlus.e("出现异常", tr)
        appendText(func + "异常:\n" + tr + "\n")
    }

    private fun appendText(text: String) {
        mTvConsole!!.append(text)
    }


    /** 写法示例 */
    private fun send03() {
        if (checkSlave() && checkOffset() && checkAmount()) {
            //// 普通写法
            ModbusManager.get()
                .readHoldingRegisters(mSalveId, mOffset, mAmount,
                    object : ModbusCallback<ReadHoldingRegistersResponse> {
                        override fun onSuccess(response: ReadHoldingRegistersResponse) {
                            val data: ByteArray = response.data
                            appendText("F03读取：" + ByteUtil.bytes2HexStr(data) + "\n")
                        }

                        override fun onFailure(tr: Throwable) {
                            appendError("F03", tr)
                        }

                        override fun onFinally() {
                        }
                    })

            // Rx写法
            ModbusManager.get()
                .rxReadHoldingRegisters(mSalveId, mOffset, mAmount)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(object : ModbusObserver<ReadHoldingRegistersResponse>() {
                    override fun onSuccess(response: ReadHoldingRegistersResponse) {
                        val data: ByteArray = response.data
                        appendText("F03读取：" + ByteUtil.bytes2HexStr(data) + "\n")
                    }

                    override fun onFailure(tr: Throwable) {
                        appendError("F03", tr)
                    }
                })

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
        }
    }


    /**
     * 检查设备地址
     *
     * @return
     */
    private fun checkSlave(): Boolean {
        return true
    }

    /**
     * 检查数据地址
     *
     * @return
     */
    private fun checkOffset(): Boolean {
        return true
    }

    /**
     * 检查数量
     */
    private fun checkAmount(): Boolean {
        return true
    }

    /**
     * 检查单（寄存器）数值
     *
     * @return
     */
    private fun checkRegValue(): Boolean {
        return true
    }

    /**
     * 检查多个线圈数值
     *
     * @return
     */
    private fun checkCoilValues(): Boolean {
        return true
    }

    /**
     * 检查多个线圈输出值
     *
     * @return
     */
    private fun checkRegValues(): Boolean {
        return true
    }
}