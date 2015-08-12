package cn.iam007.mediapicker.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cn.iam007.base.BaseActivity;
import cn.iam007.base.utils.LogUtil;
import cn.iam007.mediapicker.MediaPickerConstants;
import cn.iam007.mediapicker.R;
import cn.iam007.mediapicker.data.MediaType;
import cn.iam007.mediapicker.model.Media;
import cn.iam007.mediapicker.ui.adapter.MediaPagerAdapter;
import cn.iam007.mediapicker.util.ByteUtil;


public class MediaPreviewActivity extends BaseActivity implements
        MediaPagerAdapter.OnMediaTapListener, MediaPagerAdapter.OnVideoClickedListener,
        OnPageChangeListener, OnClickListener {

    public final static String KEY_MEDIAS = "medias";
    public final static String KEY_SELECTED_MEDIAS = "selectedMedias";
    public final static String KEY_POSITION = "position";

    private Toolbar mActionBar;
    private ViewPager mViewPager;
    private TextView mSizeTv;
    private CheckBox mIndexCb;
    private View mMediaToolbar;

    private ArrayList<Media> mMedias;
    private ArrayList<Media> mSelectedMedias;
    private MediaPagerAdapter mPagerAdapter;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_activity_media_preview);

        mActionBar = getToolbar();

        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) mActionBar.getLayoutParams();
        layoutParams.topMargin = getStatusBarHeight();

        ViewGroup.MarginLayoutParams layoutParams1 =
                (ViewGroup.MarginLayoutParams) findViewById(R.id.root).getLayoutParams();
        layoutParams1.bottomMargin = getNavBarHeight();

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mSizeTv = (TextView) findViewById(R.id.size_tv);
        mIndexCb = (CheckBox) findViewById(R.id.index_cb);
        mMediaToolbar = findViewById(R.id.mp_toolbar);

        Intent intent = getIntent();
        mMedias = (ArrayList<Media>) intent.getSerializableExtra(KEY_MEDIAS);
        mSelectedMedias = (ArrayList<Media>) intent.getSerializableExtra(KEY_SELECTED_MEDIAS);
        mPosition = intent.getIntExtra(KEY_POSITION, 0);

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

    @Override
    protected boolean toolbarOverlay() {
        return true;
    }

    @Override
    protected boolean notDisplayStatusbar() {
        return false;
    }

    @Override
    protected boolean useDefaultSystemBar() {
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void updateTitle() {
        mActionBar.setTitle(
                getString(R.string.mp_preview_format_two, mPosition + 1, mMedias.size()));
    }

    private void updateSize() {
        mSizeTv.setText(ByteUtil.format(mMedias.get(mPosition).getSize()));
    }

    private void updateIndex() {
        mIndexCb.setChecked(contains(mMedias.get(mPosition)));
    }

    private boolean contains(Media media) {
        if (mSelectedMedias == null) {
            return false;
        }

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
        return Html.fromHtml(String.format("<font color='%1$s'>%2$s</font>", color, text));
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
        toggleStatusBar();
    }

    private void hideBars() {
        mActionBar.setVisibility(View.INVISIBLE);
        mActionBar.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.iam007_translate_up_out));
        mMediaToolbar.setVisibility(View.INVISIBLE);
        mMediaToolbar.startAnimation(
                AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
        requestStatusBarVisibility(false);
    }

    private void showBars() {
        mActionBar.setVisibility(View.VISIBLE);
        mActionBar.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.iam007_translate_up_in));
        mMediaToolbar.setVisibility(View.VISIBLE);
        mMediaToolbar.startAnimation(
                AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        requestStatusBarVisibility(true);
    }

    // 状态栏是否隐藏，低于4.1版本使用
    private boolean mStatusBarHiden = false;

    public void toggleStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            if (mStatusBarHiden) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                showBars();
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                hideBars();
            }
            mStatusBarHiden = !mStatusBarHiden;
        } else {
            toggleHideyBar();
        }
    }

    private void toggleHideyBar() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            showBars();
        } else {
            hideBars();
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        // if (Build.VERSION.SDK_INT >= 14) {
        //     newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        // }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        if (isImmersiveModeEnabled) {
            newUiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }

        LogUtil.d("options:" + newUiOptions);
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }

    private void setCallback(int resultCode) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_SELECTED_MEDIAS, mSelectedMedias);
        intent.putExtras(bundle);
        setResult(resultCode, intent);
    }

    @Override
    public void onVideoClicked(View view, int position) {
        Media media = mMedias.get(position);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(media.getData())), "video/*");
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
