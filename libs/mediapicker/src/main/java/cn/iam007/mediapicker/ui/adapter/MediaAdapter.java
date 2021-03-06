package cn.iam007.mediapicker.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.iam007.base.utils.PlatformUtils;
import cn.iam007.base.utils.ViewUtils;
import cn.iam007.mediapicker.R;
import cn.iam007.mediapicker.data.MediaType;
import cn.iam007.mediapicker.model.Media;
import cn.iam007.mediapicker.util.ByteUtil;

public class MediaAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_COUNT = 11;

    public static final int TYPE_CAMERA = 0;// 相机
    public static final int TYPE_MEDIA = 1;// 媒体

    private Context mContext;
    private List<Media> mMedias;
    private List<Media> mSelectedMedias;
    private LayoutInflater mInflater;
    private int mWidth;

    private OnItemCheckedListener mOnItemCheckedListener;

    public MediaAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mWidth = context.getResources().getDisplayMetrics().widthPixels;
    }

    public void setMedias(List<Media> medias) {
        if (medias == null) {
            return;
        }

        mMedias = medias;
        notifyDataSetChanged();
    }

    public List<Media> getMedias() {
        return mMedias;
    }

    public void setSelectedMedias(List<Media> selectedMedias) {
        if (selectedMedias == null) {
            return;
        }

        mSelectedMedias = selectedMedias;
        notifyDataSetChanged();
    }

    private boolean contains(Media media) {
        if (mSelectedMedias == null) {
            return false;
        }

        return mSelectedMedias.contains(media);
    }

    @Override
    public int getCount() {
        if (mMedias == null) {
            return 0;
        }
        return mMedias.size();
    }

    @Override
    public Media getItem(int position) {
        if (mMedias == null) {
            return null;
        }
        return mMedias.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        Media media = getItem(position);
        if (media.getData().startsWith("camera://camera")) {
            return TYPE_CAMERA;
        } else {
            return TYPE_MEDIA;
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        int viewType = getItemViewType(position);
        int width = (mWidth - 2 * getPixelSize(R.dimen.mp_media_item_horizontal_spacing)) / 3;

        if (viewType == TYPE_CAMERA) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.mp_media_camera_item, parent, false);
                AbsListView.LayoutParams params = (AbsListView.LayoutParams) convertView
                        .getLayoutParams();
                params.width = width;
                params.height = width;
                convertView.setLayoutParams(params);

                PlatformUtils.applyFonts(mContext, convertView);
            }
        } else {
            MediaViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.mp_media_item, parent, false);
                AbsListView.LayoutParams params = (AbsListView.LayoutParams) convertView
                        .getLayoutParams();
                params.width = width;
                params.height = width;
                convertView.setLayoutParams(params);

                holder = new MediaViewHolder(convertView);
                convertView.setTag(holder);
                PlatformUtils.applyFonts(mContext, convertView);
            } else {
                holder = (MediaViewHolder) convertView.getTag();
            }

            final Media media = mMedias.get(position);

            setImage(holder.photoIv, media.getData());
            setIndex(holder, contains(media));
            setText(holder.sizeTv, ByteUtil.format(media.getSize()));
            setVisibility(holder.indexTv, true);
            setVisibility(holder.videoLayout, media.getMediaType() == MediaType.MEDIA_TYPE_VIDEO);

//            final View view = convertView;
            final CheckBox view = holder.indexTv;
            holder.indexTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemCheckedListener == null) {
                        return;
                    }

                    mOnItemCheckedListener.onItemChecked(view.isChecked(), position);
                }
            });

//            holder.indexTv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (mOnItemCheckedListener == null) {
//                        return;
//                    }
//
//                    mOnItemCheckedListener.onItemChecked(isChecked, position);
//                }
//            });
        }

        return convertView;
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        mOnItemCheckedListener = listener;
    }

    private int getPixelSize(int resId) {
        return mContext.getResources().getDimensionPixelSize(resId);
    }

    private void setImage(ImageView imageView, String url) {
        if (url.startsWith("file://")) {
            loadImage(imageView, url);
        } else {
            loadImage(imageView, "file://" + url);
        }
    }

    private void loadImage(ImageView imageView, String url) {
        Glide.with(mContext).load(url).error(R.drawable.mp_image_load_failed).placeholder(
                R.drawable.mp_image_load_failed)
                .into(imageView);
    }

    private void setText(TextView textView, String text) {
        textView.setText(text);
    }

    private void setIndex(MediaViewHolder holder, boolean isChecked) {
        holder.indexTv.setChecked(isChecked);
        if (isChecked) {
            holder.checkedMask.setVisibility(View.VISIBLE);
        } else {
            holder.checkedMask.setVisibility(View.INVISIBLE);
        }
    }

    private void setVisibility(View view, boolean visibility) {
        view.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public void updateItemState(AdapterView<?> parent, int position) {
        int firstVisiblePosition = parent.getFirstVisiblePosition();
        int lastVisiblePosition = parent.getLastVisiblePosition();
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            View view = parent.getChildAt(position - firstVisiblePosition);
            if (view.getTag() instanceof MediaViewHolder) {
                MediaViewHolder holder = (MediaViewHolder) view.getTag();
                Media media = getItem(position);
                setIndex(holder, contains(media));
            }
        }
    }

    public void resetItemState(AdapterView<?> parent, int position) {
        int firstVisiblePosition = parent.getFirstVisiblePosition();
        int lastVisiblePosition = parent.getLastVisiblePosition();
        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
            View view = parent.getChildAt(i - firstVisiblePosition);
            if (view.getTag() instanceof MediaViewHolder) {
                MediaViewHolder holder = (MediaViewHolder) view.getTag();
                setIndex(holder, false);
            }
        }
    }

    static class MediaViewHolder {
        ImageView photoIv;
        CheckBox indexTv;
        RelativeLayout videoLayout;
        TextView sizeTv;
        View checkedMask;

        public MediaViewHolder(View view) {
            photoIv = (ImageView) view.findViewById(R.id.photo_iv);
            indexTv = (CheckBox) view.findViewById(R.id.index_tv);
            videoLayout = (RelativeLayout) view.findViewById(R.id.video_layout);
            sizeTv = (TextView) view.findViewById(R.id.size_tv);
            checkedMask = view.findViewById(R.id.checked_mask);
        }
    }

    public interface OnItemCheckedListener {
        void onItemChecked(boolean checked, int position);
    }

}
