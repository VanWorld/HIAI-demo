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
import android.widget.Toast;

import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.detector.AestheticsScoreDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.detector.AestheticsScore;
import com.tys.hiai.GridFragment.ScoreModel;
import com.tys.hiai.model.score.AestheticScoreViewModel;
import com.tys.hiai.util.HiAILog;
import com.zhihu.matisse.Matisse;

import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_CHOOSE = 1;
    public static final int REQUEST_EXTERNAL_STORAGE = 3;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean canDetect = false;
    private AestheticsScoreDetector detector;

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
        HiAILog.i("is connect" + this.canDetect);
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
        VisionBase.init(MainActivity.this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                HiAILog.i("service connect");
                detector = new AestheticsScoreDetector(MainActivity.this);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (detector.prepare() == 0) {
                            canDetect = true;
                            HiAILog.i("this device support aestheticsScores");
                        } else {
                            HiAILog.i("this device does not support aestheticsScores");
                        }
                        return null;
                    }
                }.execute();

            }

            @Override
            public void onServiceDisconnect() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> uris = Matisse.obtainPathResult(data);
            if (uris != null && uris.size() > 0) {
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
        } else {
            AestheticScoreViewModel model = ViewModelProviders.of(MainActivity.this).get(AestheticScoreViewModel.class);
            model.getScores().setValue(new LinkedList<ScoreModel>());
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
                detector.release();
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
    }

    @Override
    protected void onDestroy() {
        VisionBase.destroy();
        super.onDestroy();
    }

    public boolean isCanDetect() {
        return canDetect;
    }

    public void setCanDetect(boolean canDetect) {
        this.canDetect = canDetect;
    }
}
