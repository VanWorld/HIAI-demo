package com.tys.hiai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.detector.AestheticsScoreDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.detector.AestheticsScore;
import com.tys.hiai.aesthetic.VisionConnectionCallback;
import com.tys.hiai.model.score.AestheticScoreViewModel;
import com.tys.hiai.util.HiAILog;
import com.tys.hiai.GridFragment.ScoreModel;
import com.zhihu.matisse.Matisse;

import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_CHOOSE = 1;
    public static final int REQUEST_EXTERNAL_STORAGE = 3;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.verifyStoragePermissions(this);
        setContentView(R.layout.activity_main);
        initVisionBaseInit();
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new GridFragment())
                    .commit();
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void initVisionBaseInit() {
        VisionConnectionCallback cb = new VisionConnectionCallback(MainActivity.this);
        VisionBase.init(MainActivity.this, cb);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> uris = Matisse.obtainPathResult(data);
            List<ScoreModel> scoreModels = new ArrayList<>(uris.size());
            for (String item : uris) {
                HiAILog.i(item);
                Bitmap bitmap = getBitmapFromPath(item);
                ScoreModel scoreModel = new ScoreModel();
                scoreModel.setImagePath(item);
                scoreModel.setBitmap(bitmap);
                scoreModels.add(scoreModel);
            }
            score(scoreModels.toArray(new ScoreModel[]{}));
        }
    }

    private Bitmap getBitmapFromPath(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void score(ScoreModel... scoreModels) {
        for (ScoreModel item : scoreModels) {
            HiAILog.i("item: " + item);
        }
        AestheticsScoreDetector detector = new AestheticsScoreDetector(MainActivity.this);
        new AsyncTask<ScoreModel, Integer, LinkedList<ScoreModel>>() {
            @Override
            protected LinkedList<ScoreModel> doInBackground(ScoreModel... scoreModels) {
                LinkedList<ScoreModel> result = new LinkedList<>();
                for (ScoreModel item : scoreModels) {
                    Frame frame = new Frame();
                    frame.setBitmap(item.getBitmap());
                    JSONObject jsonObject = detector.detect(frame, null);
                    HiAILog.i("detect result:" + jsonObject);
                    AestheticsScore aestheticsScores = detector.convertResult(jsonObject);
                    if (aestheticsScores != null) {
                        item.setScore(new BigDecimal(Float.toString(aestheticsScores.getScore()))
                                .setScale(4, BigDecimal.ROUND_HALF_UP).floatValue());
                    }
                    result.add(item);
                }

                return result;
            }

            @Override
            protected void onPostExecute(LinkedList<ScoreModel> result) {
                HiAILog.i("set view model");
                Collections.sort(result);
                AestheticScoreViewModel model = ViewModelProviders.of(MainActivity.this).get(AestheticScoreViewModel.class);
                model.getScores().setValue(result);
            }
        }.execute(scoreModels);

        detector.release();
    }

    @Override
    protected void onDestroy() {
        VisionBase.destroy();
        super.onDestroy();
    }
}
