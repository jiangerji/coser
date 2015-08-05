package cn.iam007.coser;

import com.avos.avoscloud.AVAnalytics;

import cn.iam007.base.BaseApplication;

/**
 * Created by Administrator on 2015/8/5.
 */
public class Iam007Application extends BaseApplication {

    private static Iam007Application mApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplication = this;

        AVAnalytics.setDebugMode(BuildConfig.DEBUG);
    }

    public static Iam007Application getApplication() {
        return mApplication;
    }
}
