package cn.iam007.coser.account;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.iam007.base.BaseActivity;
import cn.iam007.base.utils.LogUtil;
import cn.iam007.base.utils.SharedPreferenceUtil;
import cn.iam007.coser.R;

/**
 * Created by Administrator on 2015/8/6.
 */
public class VerifyActivity extends BaseActivity {

    public final static String KEY_VERIFY_PHONE_NUMBER = "KEY_VERIFY_PHONE_NUMBER";

    private final static String KEY_LAST_REQUEST_VERIFY_TIME = "KEY_LAST_REQUEST_VERIFY_TIME";

    private Button mGetVerifyCodeBtn;
    private EditText mVerifyCodeET;
    private Button mVerifyBtn;
    private TextView mHint;

    private String mPhoneNumber;

    public long mStartLoginTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        mGetVerifyCodeBtn = (Button) findViewById(R.id.get_verify_code);
        mGetVerifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVerifyCode();
            }
        });
        mVerifyCodeET = (EditText) findViewById(R.id.verify_code);
        mVerifyBtn = (Button) findViewById(R.id.verify);
        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    mStartLoginTime = System.currentTimeMillis();
                    showProgressDialog(R.string.login_hint_waiting);
                    String code = mVerifyCodeET.getText().toString();
                    AccountManager.getInstance().verifySmsCode(code, mCallback);
                }
            }
        });
        mHint = (TextView) findViewById(R.id.error_hint);
        Intent intent = getIntent();
        if (intent != null) {
            mPhoneNumber = intent.getStringExtra(KEY_VERIFY_PHONE_NUMBER);
        }

        mLastRequestTime =
                SharedPreferenceUtil.getLong(KEY_LAST_REQUEST_VERIFY_TIME, 0L);
        requestVerifyCode();
    }

    private long mLastRequestTime = 0;

    private void requestVerifyCode() {
        long time = System.currentTimeMillis();
        if (time - mLastRequestTime > 30 * 1000) {
            countDown(30);
            showProgressDialog(R.string.verify_sending_code);
            AccountManager.getInstance().requestVerifyCode(mPhoneNumber,
                    new AccountManager.RequestVerifyCallback() {
                        @Override
                        public void onSucc() {
                            mHandler.sendEmptyMessage(REQUEST_VERIFY_SUCC);
                        }

                        @Override
                        public void onFailed(String msg) {
                            Message message = mHandler.obtainMessage(REQUEST_VERIFY_FAILED);
                            message.obj = msg;
                            mHandler.sendMessage(message);
                        }
                    });

        } else {
            // 请求太频繁
            LogUtil.d("请求验证码太频繁");
            countDown((int) (30 - (time - mLastRequestTime) / 1000));
            showHint(getString(R.string.verify_please_check_sms));
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(mVerifyCodeET.getText().toString())) {
            showHint(getString(R.string.verify_hint_code_is_null));
            return false;
        }

        return true;
    }

    private void showHint(String string) {
        mHint.setVisibility(View.VISIBLE);
        mHint.setText(string);

        mHandler.removeCallbacks(mHideErrorHintAnim);
        mHandler.postDelayed(mHideErrorHintAnim, 10 * 1000);
    }

    private Runnable mHideErrorHintAnim = new Runnable() {
        @Override
        public void run() {
            mHint.startAnimation(
                    AnimationUtils.loadAnimation(VerifyActivity.this, R.anim.abc_fade_out));
            mHint.setVisibility(View.INVISIBLE);
        }
    };

    private AccountManager.VerifyCallback mCallback = new AccountManager.VerifyCallback() {

        @Override
        public void onSucc() {
            long time = System.currentTimeMillis();
            if (time - mStartLoginTime > 750) {
                mHandler.sendEmptyMessage(VERIFY_SUCC);
            } else {
                mHandler.sendEmptyMessageDelayed(VERIFY_SUCC, 750 - (time - mStartLoginTime));
            }
        }

        @Override
        public void onFailed(String msg) {
            long time = System.currentTimeMillis();
            Message message = mHandler.obtainMessage(VERIFY_FAILED);
            message.obj = msg;
            if (time - mStartLoginTime > 750) {
                mHandler.sendMessage(message);
            } else {
                mHandler.sendMessageDelayed(message, 750 - (time - mStartLoginTime));
            }
        }
    };

    private void doVerifySucc() {
        dismissProgressDialog();
        finish();
    }

    private void doVerifyFailed(String msg) {
        dismissProgressDialog();
        showHint(msg);
    }

    private void doRequestVerifySucc() {
        SharedPreferenceUtil.setLong(KEY_LAST_REQUEST_VERIFY_TIME, System.currentTimeMillis());
        dismissProgressDialog();
        Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
    }

    private void doRequestVerifyFailed(String msg) {
        dismissProgressDialog();
        showHint(msg);
        resetRequestVerifyBtn();
    }

    private int mCountDown = 30;

    private void countDown(int time) {
        mCountDown = time;
        mGetVerifyCodeBtn.setEnabled(false);
        mHandler.postDelayed(mCountDownRunnable, 1000);
    }

    private Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            mGetVerifyCodeBtn.setText(
                    getString(R.string.verify_get_verify_code_waiting, mCountDown));
            mCountDown--;
            if (mCountDown > 0) {
                mHandler.postDelayed(mCountDownRunnable, 1000);
            } else {
                resetRequestVerifyBtn();
            }
        }
    };

    private void resetRequestVerifyBtn() {
        mGetVerifyCodeBtn.setEnabled(true);
        mGetVerifyCodeBtn.setText(R.string.register_get_verify_code);
        mHandler.removeCallbacks(mCountDownRunnable);
    }

    private final static int VERIFY_SUCC = 0x01;
    private final static int VERIFY_FAILED = 0x02;
    private final static int REQUEST_VERIFY_SUCC = 0x03;
    private final static int REQUEST_VERIFY_FAILED = 0x04;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case VERIFY_SUCC:
                    doVerifySucc();
                    break;

                case VERIFY_FAILED:
                    doVerifyFailed((String) msg.obj);
                    break;

                case REQUEST_VERIFY_SUCC:
                    doRequestVerifySucc();
                    break;

                case REQUEST_VERIFY_FAILED:
                    doRequestVerifyFailed((String) msg.obj);
                    break;
            }
            return false;
        }
    });
}
