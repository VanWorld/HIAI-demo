package com.tys.hiai.model.recycler;

import android.graphics.Bitmap;

public class ScoreModel implements Comparable<ScoreModel> {
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
