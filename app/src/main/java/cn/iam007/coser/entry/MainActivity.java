package cn.iam007.coser.entry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.iam007.base.BaseActivity;
import cn.iam007.base.utils.LogUtil;
import cn.iam007.coser.R;
import cn.iam007.mediapicker.MediaPicker;
import cn.iam007.mediapicker.ui.MediaPickerActivity;

/**
 * Created by Administrator on 2015/8/5.
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.haha).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPicker.showAll();
                        MediaPicker.showCamera(true);
                        MediaPicker.setSelectionLimit(9);
                        MediaPicker.setSelectedMediaCount(0);

                        Intent intent = new Intent(MainActivity.this,
                                MediaPickerActivity.class);
                        startActivityForResult(intent, 0x100);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d("requestCode:" + requestCode);
        LogUtil.d("resultCode:" + resultCode);
//        LogUtil.d("data:" + data.getExtras());
    }
}
