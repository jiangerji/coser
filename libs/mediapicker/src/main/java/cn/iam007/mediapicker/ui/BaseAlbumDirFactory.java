package cn.iam007.mediapicker.ui;

import java.io.File;

import android.os.Environment;

import cn.iam007.mediapicker.ui.AlbumStorageDirFactory;

public final class BaseAlbumDirFactory implements AlbumStorageDirFactory {

    private static final String CAMERA_DIR = "/dcim/";

    @Override
    public File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStorageDirectory() + CAMERA_DIR
                + albumName);
    }

}
