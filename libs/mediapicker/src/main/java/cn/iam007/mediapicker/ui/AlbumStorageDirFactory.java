package cn.iam007.mediapicker.ui;

import java.io.File;

public interface AlbumStorageDirFactory {
	File getAlbumStorageDir(String albumName);
}
