package cn.iam007.coser.account;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.SignUpCallback;
import com.avos.avoscloud.UpdatePasswordCallback;

import cn.iam007.base.utils.LogUtil;
import cn.iam007.coser.Iam007Application;
import cn.iam007.coser.R;
import cn.iam007.coser.utils.ResourceManager;

/**
 * Created by Administrator on 2015/8/5.
 */
public class AccountManager {

    private static AccountManager _instance = new AccountManager();

    private AccountManager() {

    }

    public static AccountManager getInstance() {
        return _instance;
    }

    /**
     * 异步账户登录
     *
     * @param username 用户名
     * @param password 用户密码
     * @param callback 登录结果回调
     */
    public void login(final String username, String password, final LoginCallback callback) {
        if (TextUtils.isEmpty(username)) {
            if (callback != null) {
                callback.onFailed(AVException.OTHER_CAUSE,
                        ResourceManager.getString(R.string.login_hint_username_is_null));
            }
            return;
        }

        if (TextUtils.isEmpty(password)) {
            if (callback != null) {
                callback.onFailed(AVException.OTHER_CAUSE,
                        ResourceManager.getString(R.string.login_hint_password_is_null));
            }
            return;
        }

        AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (avUser != null) {
                    // 登录成功
                    LogUtil.d("Login Succ:" + avUser.getUsername());
                    if (callback != null) {
                        callback.onSucc();
                    }
                } else {
                    // 登录失败
                    LogUtil.d("Login Failed:" + e);
                    if (callback != null) {
                        callback.onFailed(e.getCode(), handleException(username, e));
                    }
                }
            }
        });
    }

    /**
     * 登录回调接口
     */
    public interface LoginCallback {
        /**
         * 登录成功
         */
        void onSucc();

        /**
         * 登录失败
         *
         * @param code 失败代码
         * @param msg  失败原因
         */
        void onFailed(int code, String msg);
    }

    /**
     * 注册新用户
     *
     * @param phoneNumber 用户手机号码，作为登录名
     * @param password    登录密码
     * @param callback    注册回调
     */
    public void register(String phoneNumber, String password, final RegisterCallback callback) {
        if (TextUtils.isEmpty(phoneNumber)) {
            if (callback != null) {
                callback.onFailed(
                        ResourceManager.getString(R.string.register_hint_username_is_null));
            }
            return;
        }

        if (TextUtils.isEmpty(password)) {
            if (callback != null) {
                callback.onFailed(
                        ResourceManager.getString(R.string.register_hint_password_is_null));
            }
            return;
        }

        AVUser user = new AVUser();
        user.setUsername(phoneNumber);
        user.setMobilePhoneNumber(phoneNumber);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 登录成功
                    LogUtil.d("Register Succ!");
                    if (callback != null) {
                        callback.onSucc();
                    }
                } else {
                    // 登录失败
                    LogUtil.d("Register Failed:" + e);
                    if (callback != null) {
                        callback.onFailed("" + e.getCode() + ":" + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * 注册回调接口
     */
    public interface RegisterCallback {
        /**
         * 注册成功
         */
        void onSucc();

        /**
         * 注册失败
         *
         * @param msg 失败原因
         */
        void onFailed(String msg);
    }

    /**
     * 验证手机验证码
     *
     * @param code     手机验证码
     * @param callback 验证回调
     */
    public void verifySmsCode(String code, final VerifyCallback callback) {
        if (TextUtils.isEmpty(code)) {
            if (callback != null) {
                callback.onFailed(
                        ResourceManager.getString(R.string.verify_hint_code_is_null));
            }
            return;
        }

        AVUser.verifyMobilePhoneInBackground(code, new AVMobilePhoneVerifyCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 验证成功
                    LogUtil.d("Verify Succ!");
                    if (callback != null) {
                        callback.onSucc();
                    }
                } else {
                    // 验证失败
                    LogUtil.d("Verify Failed:" + e);
                    if (callback != null) {
                        callback.onFailed(handleException(null, e));
                    }
                }
            }
        });
    }

    /**
     * 验证回调接口
     */
    public interface VerifyCallback {
        /**
         * 注册成功
         */
        void onSucc();

        /**
         * 注册失败
         *
         * @param msg 失败原因
         */
        void onFailed(String msg);
    }

    /**
     * 重新获取验证码
     *
     * @param phoneNumber 获取验证码的手机号码
     * @param callback    获取验证码回调
     */
    public void requestVerifyCode(String phoneNumber, final RequestVerifyCallback callback) {
        if (TextUtils.isEmpty(phoneNumber)) {
            if (callback != null) {
                callback.onFailed(
                        ResourceManager.getString(R.string.verify_hint_code_is_null));
            }
            return;
        }

        AVUser.requestMobilePhoneVerifyInBackground(phoneNumber, new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 获取验证码成功
                    LogUtil.d("Verify Succ!");
                    if (callback != null) {
                        callback.onSucc();
                    }
                } else {
                    // 获取验证码失败
                    LogUtil.d("Verify Failed:" + e.getCode() + " " + e.getMessage());
                    if (callback != null) {
                        callback.onFailed(handleException(null, e));
                    }
                }
            }
        });
    }

    /**
     * 获取验证回调接口
     */
    public interface RequestVerifyCallback {
        /**
         * 注册成功
         */
        void onSucc();

        /**
         * 注册失败
         *
         * @param msg 失败原因
         */
        void onFailed(String msg);
    }

    /**
     * 将该异常信息转换为用户友好的字符串，并处理该异常
     *
     * @param exception
     * @return
     */
    public String handleException(String phone, AVException exception) {
        String result = null;

        if (exception != null) {
            int resId = 0;
            switch (exception.getCode()) {
                case AVException.USERNAME_PASSWORD_MISMATCH:
                    resId = R.string.exception_user_or_password_is_wrong;
                    break;

                case AVException.USER_DOESNOT_EXIST:
                    resId = R.string.exception_user_or_password_is_wrong;
                    break;

                case AVException.USER_MOBILEPHONE_MISSING:
                    break;

                case AVException.USER_WITH_MOBILEPHONE_NOT_FOUND:
                    break;

                case AVException.USER_MOBILE_PHONENUMBER_TAKEN:
                    resId = R.string.exception_phone_already_registered;
                    break;

                case AVException.USER_MOBILEPHONE_NOT_VERIFIED:
                    resId = R.string.exception_user_phone_not_verified;
                    startVerify(phone);
                    break;
            }

            if (resId == 0) {
                result = exception.getMessage();
            } else {
                result = ResourceManager.getString(resId);
            }
        }

        return result;
    }

    /**
     * 打开手机验证界面
     *
     * @param phone
     */
    public void startVerify(String phone) {
        startVerify(phone, false);
    }

    /**
     * 打开手机验证界面
     *
     * @param phone         手机号码
     * @param resetPassword 是否是重置密码验证界面
     */
    public void startVerify(String phone, boolean resetPassword) {
        Application application = Iam007Application.getApplication();
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(application, VerifyActivity.class);
        intent.putExtra(VerifyActivity.KEY_VERIFY_PHONE_NUMBER, phone);
        intent.putExtra(VerifyActivity.KEY_VERIFY_PASSWORD_RESET, resetPassword);
        application.startActivity(intent);
    }

    /**
     * 请求重置密码
     *
     * @param phone
     * @param callback
     */
    public void requestPasswordReset(String phone, final RequestPasswordResetCallback callback) {
        if (TextUtils.isEmpty(phone)) {
            if (callback != null) {
                callback.onFailed(
                        ResourceManager.getString(R.string.request_password_reset_phone_is_null));
            }
            return;
        }

        AVUser.requestPasswordResetBySmsCodeInBackground(phone, new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 获取验证码成功
                    LogUtil.d("Reset password request succ!");
                    if (callback != null) {
                        callback.onSucc();
                    }
                } else {
                    // 获取验证码失败
                    LogUtil.d(
                            "Reset password request failed:" + e.getCode() + " " + e.getMessage());
                    if (callback != null) {
                        callback.onFailed(handleException(null, e));
                    }
                }
            }
        });
    }

    /**
     * 获取验证回调接口
     */
    public interface RequestPasswordResetCallback {
        /**
         * 注册成功
         */
        void onSucc();

        /**
         * 注册失败
         *
         * @param msg 失败原因
         */
        void onFailed(String msg);
    }

    /**
     * 重置为新密码
     *
     * @param newPassword
     * @param code
     * @param callback
     */
    public void resetPassword(String newPassword, String code,
                              final ResetPasswordCallback callback) {
        if (TextUtils.isEmpty(newPassword)) {
            if (callback != null) {
                callback.onFailed(
                        ResourceManager.getString(R.string.verify_hint_input_new_password));
            }
            return;
        }

        if (TextUtils.isEmpty(code)) {
            if (callback != null) {
                callback.onFailed(
                        ResourceManager.getString(R.string.verify_hint_code_is_null));
            }
            return;
        }

        AVUser.resetPasswordBySmsCodeInBackground(code, newPassword, new UpdatePasswordCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 获取验证码成功
                    LogUtil.d("Reset password succ!");
                    if (callback != null) {
                        callback.onSucc();
                    }
                } else {
                    // 获取验证码失败
                    LogUtil.d(
                            "Reset password failed:" + e.getCode() + " " + e.getMessage());
                    if (callback != null) {
                        callback.onFailed(handleException(null, e));
                    }
                }
            }
        });
    }

    /**
     * 获取验证回调接口
     */
    public interface ResetPasswordCallback {
        /**
         * 注册成功
         */
        void onSucc();

        /**
         * 注册失败
         *
         * @param msg 失败原因
         */
        void onFailed(String msg);
    }
}
