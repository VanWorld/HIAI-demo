package com.tys.hiai.model.score;

import com.tys.hiai.model.recycler.ScoreModel;

import java.util.LinkedList;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AestheticScoreViewModel extends ViewModel {
    private MutableLiveData<LinkedList<ScoreModel>> scores;

    public MutableLiveData<LinkedList<ScoreModel>> getScores() {
        if (scores == null) {
            scores = new MutableLiveData<LinkedList<ScoreModel>>();
        }

        return scores;
    }
}
