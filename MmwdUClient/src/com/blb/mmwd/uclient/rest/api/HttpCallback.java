package com.blb.mmwd.uclient.rest.api;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.util.Util;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class HttpCallback<T> implements Callback<T> {
    private final static String TAG = "HttpCallback";

    private final static int HTTP_OK = 200;
    private final static int HTTP_CREATED = 201;
    // private final static int HTTP_NOT_AUTH = 401;
    // private final static int HTTP_FORBIDDEN = 403;
    // private final static int HTTP_NOT_FOUND = 404;
    private final static int RES_NOT_AUTHORIZED = 99;

    private Runnable mSuccAction;
    private Runnable mFailAction;

    public HttpCallback() {
        
    }
    public HttpCallback(Runnable failAction) {
        mSuccAction = null;
        mFailAction = failAction;
    }
    
    public HttpCallback(Runnable succAction, Runnable failAction) {
        mSuccAction = succAction;
        mFailAction = failAction;
    }

    @Override
    public void failure(RetrofitError arg0) {
        //Log.e(TAG, "failure:" + arg0);
        Util.sendToast(Util.getString(R.string.msg_network_error));
        if (mFailAction != null) {
            mFailAction.run();
        }
    }

    @Override
    public void success(T t, Response r) {
        // First, handle http error
        if (handleHttpError(r)) {
            return;
        }
        
        // process, response head
        if (handleDataError(t)) {
            return;
        }
        
        // process data
        if (processData(t)) {
            if (mSuccAction != null) {
                mSuccAction.run();
            }
        } else if (mFailAction != null) {
            mFailAction.run();
        }
    }

    /**
     * http status code exception
     * @param httpStatus
     * @return
     */
    protected boolean handleHttpError(Response r) {
        int httpStatus = r.getStatus(); // only check status code
        if (httpStatus != HTTP_OK && httpStatus != HTTP_CREATED) {
            //Log.e(TAG, "handleHttpError, httpStatus:" + httpStatus);
            Util.sendToast(Util.getString(R.string.msg_network_error));
            if (mFailAction != null) {
                mFailAction.run();
            }
            return true;
        }
        return false;
    }

    // ResponseData, it will be called in processData
    // status:
    // reason:
    /**
     * 
     * @param resStatus
     * @param reason
     * @return true: has error, false: no error
     */
    private boolean handleDataError(T t) {
        if (! (t instanceof ResponseHead)) {
            return false;
        }
        
        ResponseHead head = (ResponseHead)t;
        int status = head.status;
        String reason = head.reason;
        if (status != 0) {
            String msg = reason;
            if (TextUtils.isEmpty(msg)) {
                if (RES_NOT_AUTHORIZED == status) {
                    msg = Util.getString(R.string.msg_user_unauthorized);
                } else {
                    msg = Util.getString(R.string.msg_network_data_error);
                }
            }
            Util.sendToast(msg);
            Log.e(TAG, "handleDataError, handleDataError:" + ",status:" + status
                    + ", reason:" + reason);

            if (mFailAction != null) {
                mFailAction.run();
            }
            return true;
        }
        return false;
    }

    /**
     * 
     * @param t
     * @param r
     * @return true, expected data processed, false, not expected data
     */
    protected abstract boolean processData(T t);

}
