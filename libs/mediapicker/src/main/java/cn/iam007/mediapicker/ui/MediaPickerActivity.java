package cn.iam007.mediapicker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.iam007.base.BaseActivity;
import cn.iam007.mediapicker.MediaPicker;
import cn.iam007.mediapicker.MediaPickerConstants;
import cn.iam007.mediapicker.R;
import cn.iam007.mediapicker.model.Album;
import cn.iam007.mediapicker.model.Media;
import cn.iam007.mediapicker.ui.fragment.AlbumFragment;
import cn.iam007.mediapicker.ui.fragment.MediaFragment;
import cn.iam007.mediapicker.util.AnimationUtil;


public class MediaPickerActivity extends BaseActivity implements
        AlbumFragment.OnAlbumSelectedListener, MediaFragment.OnMediaSelectedListener,
        OnClickListener {

    private android.support.v7.app.ActionBar mActionBar;
    private MediaFragment mMediaFragment;

    private FrameLayout mAlbumLayout;
    private View mAlbumFragmentView;

    private TextView mAlbumTv;
    private TextView mPreviewTv;

    private Album mAlbum = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_activity_media_picker);

        mActionBar = getSupportActionBar();

        mMediaFragment = (MediaFragment) getSupportFragmentManager()
                .findFragmentById(R.id.media_fragment);

        mAlbumLayout = (FrameLayout) findViewById(R.id.album_layout);
        mAlbumFragmentView = findViewById(R.id.album_fragment);
        mAlbumTv = (TextView) findViewById(R.id.album_tv);
        mPreviewTv = (TextView) findViewById(R.id.preview_tv);

        mAlbumLayout.setOnClickListener(this);
        mAlbumTv.setOnClickListener(this);
        mPreviewTv.setOnClickListener(this);

        if (MediaPickerConstants.SHOW_IMAGE && MediaPickerConstants.SHOW_VIDEO) {
            mActionBar.setTitle(R.string.mp_video_and_pic);
            mAlbumTv.setText(R.string.mp_video_and_pic);
            mMediaFragment.updateMediaView();
        } else {
            if (MediaPickerConstants.SHOW_VIDEO) {
                mActionBar.setTitle(R.string.mp_video);
                mAlbumTv.setText(R.string.mp_video_all);
                mMediaFragment.updateMediaView();
            } else {
                mActionBar.setTitle(R.string.mp_pic);
                mAlbumTv.setText(R.string.mp_pic_all);
                mMediaFragment.updateMediaView();
            }
        }
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

    private void updateMenu(MenuItem item) {
        List<Media> selectedMedias = mMediaFragment.getSelectedMedias();

        if (selectedMedias.size() == 0) {
            item.setTitle(getColoredText(getString(R.string.mp_confirm), "#bebebe"));
            item.setEnabled(false);
            return;
        }

        String title = getString(R.string.mp_confirm_format, selectedMedias.size(),
                MediaPickerConstants.MAX_MEDIA_LIMIT);
        item.setTitle(getColoredText(title, "#ec5d0f"));
        item.setEnabled(true);
    }

    private Spanned getColoredText(String text, String color) {
        return Html.fromHtml(String.format("<font color='%1$s'>%2$s</font>",
                color, text));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

        } else if (i == R.id.yes_item) {
            setCallback(RESULT_OK);
            finish();

        } else {
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCallback(int resultCode) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedMedias",
                mMediaFragment.getSelectedMedias());
        intent.putExtras(bundle);
        setResult(resultCode, intent);
    }

    @Override
    public void onAlbumSelected(Album album) {
        hideAlbumSet();

        if (album.equals(mAlbum))
            return;

        String bucketName = album.getBucketName();
        mAlbumTv.setText(bucketName);

        if (bucketName == null
                || getString(R.string.mp_video_and_pic).equals(bucketName)
                || getString(R.string.mp_video_all).equals(bucketName)
                || getString(R.string.mp_pic_all).equals(bucketName)) {
            MediaPicker.showCamera(true);
            mMediaFragment.updateMediaView();
        } else {
            MediaPicker.showCamera(false);
            mMediaFragment.updateMediaView(album);
        }

        mAlbum = album;
    }

    @Override
    public void onMediaSelected(List<Media> selectedMedias) {
        int size = selectedMedias.size();
        if (size > 0) {
            mPreviewTv.setText(getString(R.string.mp_preview_format, size));
            mPreviewTv.setEnabled(true);
        } else {
            mPreviewTv.setText(R.string.mp_preview);
            mPreviewTv.setEnabled(false);
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.album_layout) {
            hideAlbumSet();
        } else if (i == R.id.album_tv) {
            if (isAlbumSetShowing()) {
                hideAlbumSet();
            } else {
                showAlbumSet();
            }
        } else if (i == R.id.preview_tv) {
            if (isAlbumSetShowing()) {
                hideAlbumSet();
            }
            launchPreviewActivity(mMediaFragment.getSelectedMedias(),
                    mMediaFragment.getSelectedMedias(), 0);
        } else {
        }
    }

    private void launchPreviewActivity(ArrayList<Media> medias,
                                       ArrayList<Media> selectedMedias, int position) {
        Intent intent = new Intent(this, MediaPreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("medias", medias);
        bundle.putSerializable("selectedMedias", selectedMedias);
        bundle.putInt("position", position);
        intent.putExtras(bundle);
        startActivityForResult(intent,
                MediaPickerConstants.PREVIEW_SELECTED_MEDIAS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MediaPickerConstants.PREVIEW_SELECTED_MEDIAS_REQUEST_CODE) {
            if (resultCode == MediaPickerConstants.PREVIEW_MEDIAS_BACK_RESULT_CODE) {
                ArrayList<Media> selectedMedias = (ArrayList<Media>) data
                        .getSerializableExtra("selectedMedias");
                onMediaSelected(selectedMedias);

                mMediaFragment.setSelectedMedias(selectedMedias);
            } else if (resultCode == RESULT_OK) {
                ArrayList<Media> selectedMedias = (ArrayList<Media>) data
                        .getSerializableExtra("selectedMedias");
                onMediaSelected(selectedMedias);

                setCallback(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isAlbumSetShowing()) {
            hideAlbumSet();
        } else {
            super.onBackPressed();
        }
    }

    private void hideAlbumSet() {
        new AnimationUtil(this, R.anim.mp_translate_down).setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mAlbumLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                }).startAnimation(mAlbumFragmentView);

    }

    private void showAlbumSet() {
        mAlbumLayout.setVisibility(View.VISIBLE);
        new AnimationUtil(this, R.anim.mp_translate_up_current).startAnimation(mAlbumFragmentView);
    }

    private boolean isAlbumSetShowing() {
        return mAlbumLayout.getVisibility() == View.VISIBLE;
    }

}
