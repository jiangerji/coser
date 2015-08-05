package cn.iam007.coser.utils;

import cn.iam007.coser.Iam007Application;

/**
 * Created by Administrator on 2015/8/5.
 */
public class ResourceManager {

    public static String getString(int resId) {
        return Iam007Application.getApplication().getString(resId);
    }
}
