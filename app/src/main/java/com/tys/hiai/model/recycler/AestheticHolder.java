package com.tys.hiai.model.recycler;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tys.hiai.R;
import com.tys.hiai.engine.MyGlide4Engine;
import com.tys.hiai.util.HiAILog;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AestheticHolder extends RecyclerView.ViewHolder {
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
