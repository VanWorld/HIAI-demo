package com.tys.hiai.aesthetic;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.huawei.hiai.vision.visionkit.IVisionCallback;
import com.huawei.hiai.vision.visionkit.internal.AnnotateResult;
import com.huawei.hiai.vision.visionkit.internal.ErrorResult;
import com.huawei.hiai.vision.visionkit.internal.InfoResult;

public class DetectErrorCallback implements IVisionCallback {
    @Override
    public void onDetectedResult(AnnotateResult annotateResult) throws RemoteException {

    }

    @Override
    public void onDetectedInfo(InfoResult infoResult) throws RemoteException {

    }

    @Override
    public void onDetectedError(ErrorResult errorResult) throws RemoteException {
        Log.i("hiai---->", "" + errorResult.getResultCode());
    }

    @Override
    public String getRequestID() throws RemoteException {
        return null;
    }

    @Override
    public IBinder asBinder() {
        return null;
    }
}
