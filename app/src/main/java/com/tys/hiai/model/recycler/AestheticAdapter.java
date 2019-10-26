package com.tys.hiai.model.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tys.hiai.engine.Glide4Engine;
import com.tys.hiai.R;
import com.tys.hiai.util.HiAILog;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AestheticAdapter extends RecyclerView.Adapter<AestheticHolder> {
    private LinkedList<ScoreModel> scoreModels;

    public AestheticAdapter(LinkedList<ScoreModel> scoreModels) {
        this.scoreModels = scoreModels;
    }

    @NonNull
    @Override
    public AestheticHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HiAILog.i("on create view holder");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ImageView imageView = itemView.findViewById(R.id.score_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView iv = (ImageView) v;
                iv.get

            }
        });
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
}
