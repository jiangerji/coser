package cn.iam007.coser.account;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import cn.iam007.base.BaseActivity;
import cn.iam007.coser.R;
import cn.iam007.coser.test.HalfActivity;

/**
 * Created by Administrator on 2015/7/1.
 */
public class LoginActivity extends BaseActivity {

    private long mStartLoginTime = 0;
    private EditText mUsername;
    private EditText mPassword;
    private View mForgetPassword;
    private TextView mErrorHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mErrorHint = (TextView) findViewById(R.id.error_hint);
        mForgetPassword = findViewById(R.id.forget);
        mForgetPassword.setOnClickListener(mForgetPasswordListener);

        View view = findViewById(R.id.login);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    mStartLoginTime = System.currentTimeMillis();
                    showProgressDialog(R.string.login_hint_waiting);
                    String username = mUsername.getText().toString();
                    String password = mPassword.getText().toString();
                    AccountManager.getInstance().login(username, password, mCallback);
                }
            }
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(mUsername.getText().toString())) {
            showErrorHint(getString(R.string.login_hint_username_is_null));
            return false;
        }

        if (TextUtils.isEmpty(mPassword.getText().toString())) {
            showErrorHint(getString(R.string.login_hint_password_is_null));
            return false;
        }

        return true;
    }

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
                    AnimationUtils.loadAnimation(LoginActivity.this, R.anim.abc_fade_out));
            mErrorHint.setVisibility(View.INVISIBLE);
        }
    };

    private AccountManager.LoginCallback mCallback = new AccountManager.LoginCallback() {
        @Override
        public void onSucc() {
            long time = System.currentTimeMillis();
            if (time - mStartLoginTime > 750) {
                mHandler.sendEmptyMessage(LOGIN_SUCC);
            } else {
                mHandler.sendEmptyMessageDelayed(LOGIN_SUCC, 750 - (time - mStartLoginTime));
            }
        }

        @Override
        public void onFailed(int code, String msg) {
            long time = System.currentTimeMillis();
            Message message = mHandler.obtainMessage(LOGIN_FAILED);
            message.obj = msg;
            message.arg1 = code;
            if (time - mStartLoginTime > 750) {
                mHandler.sendMessage(message);
            } else {
                mHandler.sendMessageDelayed(message, 750 - (time - mStartLoginTime));
            }
        }
    };

    private void doLoginSucc() {
        dismissProgressDialog();
//        finish();
        Intent intent = new Intent();
        intent.setClass(this, HalfActivity.class);
        startActivity(intent);
    }

    private void doLoginFailed(int code, String msg) {
        dismissProgressDialog();
        showErrorHint(msg);
    }

    private void doRequestResetPasswordSucc() {
        dismissProgressDialog();
        AccountManager.getInstance().startVerify(mUsername.getText().toString(), true);
    }

    private void doRequestResetPasswordFailed(int code, String msg) {
        dismissProgressDialog();
        showErrorHint(msg);
    }

    private final static int LOGIN_SUCC = 0x01;
    private final static int LOGIN_FAILED = 0x02;
    private final static int REQUEST_RESET_PASSWORD_SUCC = 0x03;
    private final static int REQUEST_RESET_PASSWORD_FAILED = 0x04;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_SUCC:
                    doLoginSucc();
                    break;

                case LOGIN_FAILED:
                    doLoginFailed(msg.arg1, (String) msg.obj);
                    break;

                case REQUEST_RESET_PASSWORD_SUCC:
                    doRequestResetPasswordSucc();
                    break;

                case REQUEST_RESET_PASSWORD_FAILED:
                    doRequestResetPasswordFailed(msg.arg1, (String) msg.obj);
                    break;
            }
            return false;
        }
    });

    private View.OnClickListener mForgetPasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgressDialog();
            AccountManager.getInstance().requestPasswordReset(mUsername.getText().toString(),
                    new AccountManager.RequestPasswordResetCallback() {
                        @Override
                        public void onSucc() {
                            mHandler.sendEmptyMessage(REQUEST_RESET_PASSWORD_SUCC);
                        }

                        @Override
                        public void onFailed(String msg) {
                            Message message = mHandler.obtainMessage(REQUEST_RESET_PASSWORD_FAILED);
                            message.obj = msg;
                            mHandler.sendMessage(message);
                        }
                    });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.register:
                Intent intent = new Intent();
                intent.setClass(this, RegisterActivity.class);
                startActivityForResult(intent, 0x01);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
