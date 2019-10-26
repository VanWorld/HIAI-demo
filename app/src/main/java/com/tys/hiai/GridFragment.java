package com.tys.hiai;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.tys.hiai.engine.Glide4Engine;
import com.tys.hiai.model.recycler.AestheticAdapter;
import com.tys.hiai.model.recycler.ScoreModel;
import com.tys.hiai.model.score.AestheticScoreViewModel;
import com.tys.hiai.util.HiAILog;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridFragment extends Fragment {
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

        AestheticScoreViewModel model = ViewModelProviders.of(getActivity()).get(AestheticScoreViewModel.class);
        model.getScores().observe(getActivity(), new Observer<LinkedList<ScoreModel>>() {
            @Override
            public void onChanged(LinkedList<ScoreModel> scoreModels) {
                HiAILog.i("onChanged");
                RecyclerView recyclerView = view.findViewById(R.id.my_recycler_view);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(new AestheticAdapter(scoreModels));
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3, GridLayoutManager.VERTICAL, false));
                recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                        return false;
                    }

                    @Override
                    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                    }
                });
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

}
