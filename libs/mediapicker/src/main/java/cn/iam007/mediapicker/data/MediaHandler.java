package cn.iam007.mediapicker.data;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class MediaHandler extends Handler {

	private final WeakReference<Context> mContext;

	public MediaHandler(Context context) {
		mContext = new WeakReference<Context>(context);
	}

	@Override
	public void handleMessage(Message msg) {
		Context context = mContext.get();
		if (context != null) {
			onHandleMessage(msg);
		}
	}

	public void onHandleMessage(Message msg) {
	}

}
