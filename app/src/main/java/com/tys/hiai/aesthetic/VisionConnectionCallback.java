package com.tys.hiai.aesthetic;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.detector.AestheticsScoreDetector;
import com.tys.hiai.util.HiAILog;

public class VisionConnectionCallback implements ConnectionCallback {
    private Context mContext;

    public VisionConnectionCallback(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onServiceConnect() {
        HiAILog.i("onServiceConnect");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AestheticsScoreDetector detector = new AestheticsScoreDetector(mContext);
                if (detector.prepare() == 0) {
                    HiAILog.i("this device support aesthetic detect");
                } else {
                    HiAILog.i("this device dose not support aesthetic detect!");
                    Toast.makeText(mContext, " 无法启动美学评分引擎，不能进行评分操作，所有照片均为零分", Toast.LENGTH_LONG).show();
                    VisionBase.destroy();
                }
                detector.release();

                return null;
            }
        };

    }

    @Override
    public void onServiceDisconnect() {

    }

}
