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

import cn.iam007.base.BaseActivity;
import cn.iam007.coser.R;

/**
 * Created by Administrator on 2015/8/5.
 */
public class RegisterActivity extends BaseActivity {

    private Button mGetVerifyCodeBtn;
    private EditText mPhoneNumberET;
    private EditText mPasswordET;
    private Button mSignUpBtn;
    private TextView mErrorHint;

    private long mStartLoginTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        mGetVerifyCodeBtn = (Button) findViewById(R.id.get_verify_code);
        mPhoneNumberET = (EditText) findViewById(R.id.username);
        mPasswordET = (EditText) findViewById(R.id.password);
        mSignUpBtn = (Button) findViewById(R.id.sign_up);
        mSignUpBtn.setOnClickListener(mSignUpListener);
        mErrorHint = (TextView) findViewById(R.id.error_hint);
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(mPhoneNumberET.getText().toString())) {
            showErrorHint(getString(R.string.login_hint_username_is_null));
            return false;
        }

        if (TextUtils.isEmpty(mPasswordET.getText().toString())) {
            showErrorHint(getString(R.string.login_hint_password_is_null));
            return false;
        }

        return true;
    }

    private View.OnClickListener mSignUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validateInput()) {
                mStartLoginTime = System.currentTimeMillis();
                showProgressDialog(R.string.register_hint_waiting);
                String username = mPhoneNumberET.getText().toString();
                String password = mPasswordET.getText().toString();
                AccountManager.getInstance().register(username, password, mCallback);
            }
        }
    };

    private void showErrorHint(String string) {
        mErrorHint.setVisibility(View.VISIBLE);
        mErrorHint.setText(string);

        mHandler.removeCallbacks(mHideErrorHintAnim);
        mHandler.postDelayed(mHideErrorHintAnim, 10 * 1000);
    }

    private Runnable mHideErrorHintAnim = new Runnable() {
        @Override
        public void run() {
            mErrorHint.startAnimation(
                    AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.abc_fade_out));
            mErrorHint.setVisibility(View.INVISIBLE);
        }
    };

    private AccountManager.RegisterCallback mCallback = new AccountManager.RegisterCallback() {
        @Override
        public void onSucc() {
            long time = System.currentTimeMillis();
            if (time - mStartLoginTime > 750) {
                mHandler.sendEmptyMessage(REGISTER_SUCC);
            } else {
                mHandler.sendEmptyMessageDelayed(REGISTER_SUCC, 750 - (time - mStartLoginTime));
            }
        }

        @Override
        public void onFailed(String msg) {
            long time = System.currentTimeMillis();
            Message message = mHandler.obtainMessage(REGISTER_FAILED);
            message.obj = msg;
            if (time - mStartLoginTime > 750) {
                mHandler.sendMessage(message);
            } else {
                mHandler.sendMessageDelayed(message, 750 - (time - mStartLoginTime));
            }
        }
    };

    private void doRegisterSucc() {
        dismissProgressDialog();
        Intent intent = new Intent();
        intent.setClass(this, VerifyActivity.class);
        intent.putExtra("phone", mPhoneNumberET.getText());
        startActivityForResult(intent, 0x01);
    }

    private void doRegisterFailed(String msg) {
        dismissProgressDialog();
        showErrorHint(msg);
    }

    private final static int REGISTER_SUCC = 0x01;
    private final static int REGISTER_FAILED = 0x02;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case REGISTER_SUCC:
                    doRegisterSucc();
                    break;

                case REGISTER_FAILED:
                    doRegisterFailed((String) msg.obj);
                    break;
            }
            return false;
        }
    });
}
