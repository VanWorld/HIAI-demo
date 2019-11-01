package com.tys.hiai;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.tys.hiai.engine.Glide4Engine;
import com.tys.hiai.engine.MyGlide4Engine;
import com.tys.hiai.model.score.AestheticScoreViewModel;
import com.tys.hiai.util.HiAILog;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private RecyclerView mRecyclerView;
    private AestheticAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grid_fragment, container, false);
        setUpToolbar(view);

        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new AestheticAdapter(new LinkedList<>());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new DividerGridItemDecoration());
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    HiAILog.i("single tap up");
                    View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    HiAILog.i(childView + "");
                    if (childView != null && childView instanceof MaterialCardView) {
                        int pos = mRecyclerView.getChildAdapterPosition(childView);
                        ScoreModel mScoremodel = ((AestheticAdapter)mRecyclerView.getAdapter()).getScoreModel(pos);
                        HiAILog.i(" " + mScoremodel);
                        ImageView imageView = view.findViewById(R.id.zoom_out_imageView);
                        Glide.with(getContext()).load(mScoremodel.getImagePath()).into(imageView);
                        imageView.setVisibility(View.VISIBLE);
                        AppBarLayout appBarLayout = view.findViewById(R.id.app_bar_layout);
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
                        return true;
                    }
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    HiAILog.i("long press");
                }
            });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return gestureDetector.onTouchEvent(e);
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                HiAILog.i("on touch event");
                gestureDetector.onTouchEvent(e);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        AestheticScoreViewModel model = ViewModelProviders.of(getActivity()).get(AestheticScoreViewModel.class);
        model.getScores().observe(getActivity(), new Observer<LinkedList<ScoreModel>>() {
            @Override
            public void onChanged(LinkedList<ScoreModel> scoreModels) {
                HiAILog.i("onChanged");
                mAdapter.setScoreModels(scoreModels);
                mAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.hiai_toolbar_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.select_picture);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                selectPhotoByMatisse();
//                Toast.makeText(getActivity(), "hello", Toast.LENGTH_LONG).show();
                return true;
            }
        });

    }

    public void selectPhotoByMatisse() {
        Matisse.from(getActivity())
                .choose(MimeType.ofImage())
                .countable(true)
                .theme(R.style.Matisse_Dracula)
                .maxSelectable(9)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(MainActivity.REQUEST_CODE_CHOOSE);
    }

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            appCompatActivity.setSupportActionBar(toolbar);
        }
    }

    /**
     *
     */
    class AestheticAdapter extends RecyclerView.Adapter<AestheticHolder> {
        private LinkedList<ScoreModel> scoreModels;

        public AestheticAdapter(LinkedList<ScoreModel> scoreModels) {
            this.scoreModels = scoreModels;
        }

        @NonNull
        @Override
        public AestheticHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            HiAILog.i("on create view holder");
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
            return new AestheticHolder(itemView, parent.getContext());
        }

        @Override
        public void onBindViewHolder(@NonNull AestheticHolder holder, int position) {
            holder.bindData(scoreModels.get(position));
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
    }

    /**
     *
     */
    class AestheticHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvScore;
        private Context context;

        public AestheticHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.ivImage = (ImageView) itemView.findViewById(R.id.score_image);
            this.tvScore = (TextView) itemView.findViewById(R.id.score);
            this.context = context;
        }

        public void bindData(ScoreModel scoreModel) {
            HiAILog.i(scoreModel.toString());
            MyGlide4Engine.loadThumbnail(context, context.getResources().getDimensionPixelOffset(R.dimen.grid_expected_size),
                    context.getResources().getDrawable(R.drawable.ic_spinner_of_dots), this.ivImage,
                    Uri.fromFile(new File(scoreModel.getImagePath())));
            this.tvScore.setText(scoreModel.getScore() + "");
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
