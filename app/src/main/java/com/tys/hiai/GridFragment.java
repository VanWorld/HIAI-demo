package com.tys.hiai;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.tys.hiai.engine.Glide4Engine;
import com.tys.hiai.model.score.AestheticScoreViewModel;
import com.tys.hiai.util.HiAILog;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class GridFragment extends Fragment {
    private View gridView;
    private RecyclerView mRecyclerView;
    private AestheticAdapter mAdapter;
    private HashMap<String, ScoreModel> selectScoreModels;
    private Toolbar toolbar;
    private MenuItem selectPictureMenu;
    private MenuItem deleteMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        selectScoreModels = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        gridView = inflater.inflate(R.layout.grid_fragment, container, false);
        initToolbar(gridView);
        initRecyclerView(gridView);
        refreshUI(new LinkedList<>());
//        initDescription(gridView);
        initBackgroundView(gridView);


        AestheticScoreViewModel model = ViewModelProviders.of(getActivity()).get(AestheticScoreViewModel.class);
        model.getScores().observe(getActivity(), new Observer<LinkedList<ScoreModel>>() {
            @Override
            public void onChanged(LinkedList<ScoreModel> scoreModels) {
                HiAILog.i("onChanged");
                refreshUI(scoreModels);
            }
        });
        return gridView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.hiai_toolbar_menu, menu);
        selectPictureMenu = menu.findItem(R.id.select_picture);
        selectPictureMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                selectPhotoByMatisse();
                return true;
            }
        });

        deleteMenu = menu.findItem(R.id.delete);
        deleteMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (selectScoreModels == null || selectScoreModels.size() == 0) {
                    Toast.makeText(getContext(), R.string.none_select, Toast.LENGTH_LONG).show();
                    return true;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);

                String message = String.format(getActivity().getResources().getString(R.string.delete_select_count_items), selectScoreModels.size());
                builder.setTitle(R.string.delete_select_items)
                        .setMessage(message)
                        .setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HiAILog.i("hello, I am confirm");
                                selectPictureMenu.setVisible(true);
                                deleteMenu.setVisible(false);
                                toolbar.setNavigationOnClickListener(null);
                                toolbar.setNavigationIcon(null);
                                toolbar.setTitle(R.string.app_name);

                                for (ScoreModel item2 : selectScoreModels.values()) {
                                    File file = new File(item2.getImagePath());
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    mAdapter.scoreModels.remove(item2);
                                }
                                String deleteConfirmMessage = String.format(getActivity().getResources().getString(R.string.delete_confirm_message), selectScoreModels.size());
                                selectScoreModels.clear();
                                mAdapter.setShowSelected(false);
                                refreshUI(mAdapter.scoreModels);
                                Toast.makeText(getContext(), deleteConfirmMessage, Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });
        deleteMenu.setVisible(false);

    }

    public void selectPhotoByMatisse() {
        Matisse.from(getActivity())
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.BMP, MimeType.WEBP))
                .showSingleMediaType(true)
                .countable(true)
                .theme(R.style.Matisse_Dracula)
                .maxSelectable(9)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(MainActivity.REQUEST_CODE_CHOOSE);
    }

    private void initToolbar(@NonNull View view) {
        toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            appCompatActivity.setSupportActionBar(toolbar);
        }
    }

    private void initRecyclerView(@NonNull View view) {
        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void refreshUI(LinkedList<ScoreModel> scoreModels) {
        if (mAdapter == null) {
            mAdapter = new AestheticAdapter(scoreModels);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setScoreModels(scoreModels);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     *
     */
    private void initDescription(@NonNull View view) {
        TextView descriptionView = view.findViewById(R.id.description);
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String des = getContext().getResources().getString(R.string.description);
        ssb.append(des);
        ImageSpan imageSpan = new ImageSpan(getContext(), R.drawable.ic_select_picture);
        ssb.setSpan(imageSpan, 2, 3, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        descriptionView.setText(ssb);
    }

    private void initBackgroundView(View view) {
        ImageView descriptionImageView = view.findViewById(R.id.descriptionView);
        Glide.with(getContext()).asGif().load("file:///android_asset/yanjin.gif").into(descriptionImageView);
    }

    /**
     *
     */
    class AestheticAdapter extends RecyclerView.Adapter<AestheticHolder> {
        private LinkedList<ScoreModel> scoreModels;
        private boolean showSelected = false;

        public AestheticAdapter(LinkedList<ScoreModel> scoreModels) {
            this.scoreModels = scoreModels;
        }

        @NonNull
        @Override
        public AestheticHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            HiAILog.i("on create view holder");
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
            HiAILog.i((itemView.toString()));
            return new AestheticHolder(itemView, parent.getContext());
        }

        @Override
        public void onBindViewHolder(@NonNull AestheticHolder holder, int position) {
            ScoreModel mScoremodel = scoreModels.get(position);
            holder.bindData(mScoremodel);

            if (isShowSelected() && !holder.isCheckBoxShow()) {
                holder.checkBox.setVisibility(View.VISIBLE);
//                holder.checkBox.setChecked(selectScoreModels.containsKey(Integer.toString(position)));
            } else if (!isShowSelected() && holder.isCheckBoxShow()) {
                holder.checkBox.setVisibility(View.GONE);
                holder.checkBox.setChecked(selectScoreModels.containsKey(Integer.toString(position)));
            }

            holder.ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HiAILog.i(" " + mScoremodel);
                    ImageView imageView = gridView.findViewById(R.id.zoom_out_imageView);
                    Glide.with(getContext()).load(mScoremodel.getImagePath()).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                    AppBarLayout appBarLayout = gridView.findViewById(R.id.app_bar_layout);
                    appBarLayout.setVisibility(View.GONE);

                    View decorView = getActivity().getWindow().getDecorView();
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(uiOptions);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageView.setVisibility(View.GONE);
                            appBarLayout.setVisibility(View.VISIBLE);
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        }
                    });
                }
            });

            holder.ivImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isShowSelected()) {
                        showSelected = true;
                    }

                    selectPictureMenu.setVisible(false);
                    deleteMenu.setVisible(true);
                    toolbar.setNavigationIcon(R.drawable.ic_close);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectPictureMenu.setVisible(true);
                            deleteMenu.setVisible(false);
                            toolbar.setNavigationOnClickListener(null);
                            toolbar.setNavigationIcon(null);
                            toolbar.setTitle(R.string.app_name);

                            showSelected = false;
                            selectScoreModels.clear();
                            refreshUI(scoreModels);
                        }
                    });

                    holder.checkBox.setVisibility(View.VISIBLE);
                    holder.checkBox.setChecked(true);
                    refreshUI(scoreModels);
                    return true;
                }
            });

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectScoreModels.put(Integer.toString(position), scoreModels.get(position));
                    } else {
                        selectScoreModels.remove(Integer.toString(position));
                    }

                    if (isShowSelected()) {
                        if (selectScoreModels.size() > 0) {
                            String newTile = getContext().getResources().getString(R.string.select_count_string);
                            toolbar.setTitle(String.format(newTile, selectScoreModels.size()));
                        } else {
                            toolbar.setTitle(R.string.none_select);
                        }
                    }

                    HiAILog.i("select models count is:" + selectScoreModels.size());
                }
            });
        }

        @Override
        public int getItemCount() {
            return scoreModels.size();
        }

        public LinkedList<ScoreModel> getScoreModels() {
            return scoreModels;
        }

        public void setScoreModels(LinkedList<ScoreModel> scoreModels) {
            this.scoreModels = scoreModels;
        }

        public ScoreModel getScoreModel(int position) {
            if (this.scoreModels != null && this.scoreModels.size() > 0) {
                return scoreModels.get(position);
            }

            return null;
        }

        public boolean isShowSelected() {
            return this.showSelected;
        }

        public void setShowSelected(boolean showSelected) {
            this.showSelected = showSelected;
        }

    }

    /**
     *
     */
    class AestheticHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvScore;
        private CheckBox checkBox;

        public AestheticHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.ivImage = itemView.findViewById(R.id.score_image);
            this.tvScore = itemView.findViewById(R.id.score);
            this.checkBox = itemView.findViewById(R.id.cb_item);
        }

        public void bindData(ScoreModel scoreModel) {
            HiAILog.i(scoreModel.toString());
            Glide.with(getContext()).load(scoreModel.getImagePath()).into(this.ivImage);
            this.tvScore.setText(scoreModel.getScore() + "");
        }

        public ImageView getIvImage() {
            return ivImage;
        }

        public void setIvImage(ImageView ivImage) {
            this.ivImage = ivImage;
        }

        public TextView getTvScore() {
            return tvScore;
        }

        public void setTvScore(TextView tvScore) {
            this.tvScore = tvScore;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }

        public boolean isCheckBoxShow() {
            if (this.checkBox.getVisibility() == View.VISIBLE) {
                return true;
            }
            return false;
        }
    }

    /**
     *
     */
    public static class ScoreModel implements Comparable<ScoreModel> {
        private String imagePath;
        private Bitmap bitmap;
        private float score = 0.0F;

        public ScoreModel() {

        }

        public ScoreModel(String imagePath, Bitmap bitmap, float score) {
            this.imagePath = imagePath;
            this.bitmap = bitmap;
            this.score = score;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        @Override
        public int compareTo(ScoreModel o) {
            if (this.score > o.getScore()) {
                return -1;
            } else if (this.score == o.getScore()) {
                return 0;
            } else {
                return 1;
            }

        }

        @Override
        public String toString() {
            return "ScoreModel{" +
                    "imagePath='" + imagePath + '\'' +
                    ", bitmap=" + bitmap +
                    ", score=" + score +
                    '}';
        }
    }

    /**
     *
     */
    public class DividerGridItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{android.R.attr.listDivider};
        private Drawable mDivider;
        private int lineWidth = 1;

        public DividerGridItemDecoration(Context context) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
        }

        public DividerGridItemDecoration(int color) {
            mDivider = new ColorDrawable(color);
        }

        public DividerGridItemDecoration() {
            this(Color.parseColor("#cccccc"));
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            drawHorizontal(c, parent);
            drawVertical(c, parent);
        }

        private int getSpanCount(RecyclerView parent) {
            // 列数
            int spanCount = -1;
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {

                spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                spanCount = ((StaggeredGridLayoutManager) layoutManager)
                        .getSpanCount();
            }
            return spanCount;
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin
                        + lineWidth;
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + lineWidth;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getTop() - params.topMargin;
                final int bottom = child.getBottom() + params.bottomMargin;
                final int left = child.getRight() + params.rightMargin;
                final int right = left + lineWidth;

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private boolean isLastColum(RecyclerView parent, int pos, int spanCount, int childCount) {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager)
                        .getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                } else {
                    childCount = childCount - childCount % spanCount;
                    if (pos >= childCount) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount) {
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager)
                        .getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    childCount = childCount - childCount % spanCount;
                    if (pos >= childCount)
                        return true;
                } else {
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            boolean b = state.willRunPredictiveAnimations();
            int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
            int spanCount = getSpanCount(parent);
            int childCount = parent.getAdapter().getItemCount();
//        if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
//            outRect.set(0, 0, lineWidth, 0);
//        }
//        else if (isLastColum(parent, itemPosition, spanCount, childCount)) {
////            if (b){
////                outRect.set(0, 0, lineWidth, lineWidth);
////            }else {
//                outRect.set(0, 0, 0, lineWidth);
////            }
//        }
//        else {
            outRect.set(0, 0, lineWidth, lineWidth);
//        }
        }
    }


}
