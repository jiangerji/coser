package cn.iam007.coser.account;

import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;

import cn.iam007.base.utils.LogUtil;
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
    public void login(String username, String password, final LoginCallback callback) {
        if (TextUtils.isEmpty(username)) {
            if (callback != null) {
                callback.onFailed(ResourceManager.getString(R.string.login_hint_username_is_null));
            }
            return;
        }

        if (TextUtils.isEmpty(password)) {
            if (callback != null) {
                callback.onFailed(ResourceManager.getString(R.string.login_hint_password_is_null));
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
                        callback.onFailed("" + e);
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
         * @param msg 失败原因
         */
        void onFailed(String msg);
    }
}
