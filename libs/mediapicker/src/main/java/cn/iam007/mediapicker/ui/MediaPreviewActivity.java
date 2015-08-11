package cn.iam007.mediapicker.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cn.iam007.base.BaseActivity;
import cn.iam007.mediapicker.MediaPickerConstants;
import cn.iam007.mediapicker.R;
import cn.iam007.mediapicker.data.MediaType;
import cn.iam007.mediapicker.model.Media;
import cn.iam007.mediapicker.ui.adapter.MediaPagerAdapter;
import cn.iam007.mediapicker.util.ByteUtil;


public class MediaPreviewActivity extends BaseActivity implements
        MediaPagerAdapter.OnMediaTapListener, MediaPagerAdapter.OnVideoClickedListener,
        OnPageChangeListener, OnClickListener {

    private android.support.v7.app.ActionBar mActionBar;
    private ViewPager mViewPager;
    private TextView mSizeTv;
    private CheckBox mIndexCb;

    private ArrayList<Media> mMedias;
    private ArrayList<Media> mSelectedMedias;
    private MediaPagerAdapter mPagerAdapter;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_preview);

        mActionBar = getSupportActionBar();

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mSizeTv = (TextView) findViewById(R.id.size_tv);
        mIndexCb = (CheckBox) findViewById(R.id.index_cb);

        Intent intent = getIntent();
        mMedias = (ArrayList<Media>) intent.getSerializableExtra("medias");
        mSelectedMedias = (ArrayList<Media>) intent
                .getSerializableExtra("selectedMedias");
        mPosition = intent.getIntExtra("position", 0);

        mPagerAdapter = new MediaPagerAdapter(this, mMedias);
        mPagerAdapter.setOnMediaTapListener(this);
        mPagerAdapter.setOnVideoClickedListener(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mPosition);
        mViewPager.addOnPageChangeListener(this);

        mIndexCb.setOnClickListener(this);

        updateTitle();
        updateSize();
        updateIndex();
    }

    private void updateTitle() {
        mActionBar.setTitle(getString(R.string.mp_preview_format_two, mPosition + 1,
                mMedias.size()));
    }

    private void updateSize() {
        mSizeTv.setText(ByteUtil.format(mMedias.get(mPosition).getSize()));
    }

    private void updateIndex() {
        mIndexCb.setChecked(contains(mMedias.get(mPosition)));
    }

    private boolean contains(Media media) {
        if (mSelectedMedias == null)
            return false;

        return mSelectedMedias.contains(media);
    }

    private void updateMenu(MenuItem item) {
        if (mSelectedMedias.size() == 0) {
            item.setTitle(getColoredText(getString(R.string.mp_confirm), "#bebebe"));
            item.setEnabled(false);
            return;
        }

        String title = getString(R.string.mp_confirm_format, mSelectedMedias.size(),
                MediaPickerConstants.MAX_MEDIA_LIMIT);
        item.setTitle(getColoredText(title, "#ec5d0f"));
        item.setEnabled(true);
    }

    private Spanned getColoredText(String text, String color) {
        return Html.fromHtml(String.format("<font color='%1$s'>%2$s</font>",
                color, text));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPagerAdapter.cleanup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mp_menu_media_picker, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem yesItem = menu.findItem(R.id.yes_item);
        updateMenu(yesItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();

        } else if (i == R.id.yes_item) {
            setCallback(RESULT_OK);
            finish();

        } else {
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        // fix android formatted title bug
//        // http://stackoverflow.com/questions/7658725/android-java-lang-illegalargumentexception-invalid-payload-item-type/
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
//                && item.getTitleCondensed() != null) {
//            item.setTitleCondensed(item.getTitleCondensed().toString());
//        }
//
//        return super.onMenuItemSelected(featureId, item);
//    }

    @Override
    public void onMediaTap(View view, float x, float y, int position) {
        onBackPressed();
    }

    private void setCallback(int resultCode) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedMedias", mSelectedMedias);
        intent.putExtras(bundle);
        setResult(resultCode, intent);
    }

    @Override
    public void onVideoClicked(View view, int position) {
        Media media = mMedias.get(position);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(media.getData())),
                "video/*");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        setCallback(MediaPickerConstants.PREVIEW_MEDIAS_BACK_RESULT_CODE);
        super.onBackPressed();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mPosition = position;
        updateTitle();
        updateSize();
        updateIndex();
    }

    @Override
    public void onClick(View v) {
        Media media = mMedias.get(mPosition);

        if (!contains(media)) {
            if (media.getMediaType() == MediaType.MEDIA_TYPE_VIDEO) {
                if (media.getSize() > MediaPickerConstants.SELECTED_VIDEO_SIZE_IN_MB
                        * ByteUtil.MB) {
                    Toast.makeText(
                            this,
                            getString(
                                    R.string.mp_video_size_too_big,
                                    MediaPickerConstants.SELECTED_VIDEO_SIZE_IN_MB),
                            Toast.LENGTH_SHORT).show();
                    mIndexCb.setChecked(false);
                    return;
                }
            }

            if (MediaPickerConstants.MAX_MEDIA_LIMIT == 1
                    && MediaPickerConstants.SELECTED_MEDIA_COUNT == 1) {
                mSelectedMedias.remove(0);
                MediaPickerConstants.SELECTED_MEDIA_COUNT--;
            } else {
                if ((MediaPickerConstants.SELECTED_MEDIA_COUNT == MediaPickerConstants.MAX_MEDIA_LIMIT)) {
                    Toast.makeText(
                            this,
                            getString(R.string.mp_file_count_overflow_hint,
                                    MediaPickerConstants.SELECTED_MEDIA_COUNT),
                            Toast.LENGTH_SHORT).show();
                    mIndexCb.setChecked(false);
                    return;
                }
            }

            mSelectedMedias.add(media);
            MediaPickerConstants.SELECTED_MEDIA_COUNT++;
        } else {
            mSelectedMedias.remove(media);
            MediaPickerConstants.SELECTED_MEDIA_COUNT--;
        }

        invalidateOptionsMenu();
    }

}
