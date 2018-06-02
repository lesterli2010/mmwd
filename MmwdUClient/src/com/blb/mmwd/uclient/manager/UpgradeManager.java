package com.blb.mmwd.uclient.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.response.UpgradeInfo;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.dialog.InformationDialog;
import com.blb.mmwd.uclient.util.NotificationUtil;
import com.blb.mmwd.uclient.util.Util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class UpgradeManager {
    private final static String TAG = "UpgradeManager";

    private static UpgradeManager sUpgradeManager;

    public static UpgradeManager getInstance() {
        if (sUpgradeManager == null) {
            sUpgradeManager = new UpgradeManager();
        }
        return sUpgradeManager;
    }

    private String DOWNLOADED_APP_DIR = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + "/tmp/";
    private String mDownloadedAppPath;
    private String mDownloadNewApkUrl;

    //private String mDownloadNewApkUrl = "http://www.wxingwsu.com/MmwdUClient.apk";// tmp, it shall get from server
    /**
     * Check if we need upgrade
     * 
     * @param context
     */
    public void checkUpgrade(final Context context, final boolean inBackground) {
       // new CheckVersionTask(context, inBackground).execute();
        mDownloadNewApkUrl = null;
        mDownloadedAppPath = null;
        
        final Dialog checkingDialog = new InformationDialog(context, R.string.msg_checking);
        if (!inBackground) {
            checkingDialog.show();
        }
        HttpManager.getInstance().getRestAPIClient().checkUpgrade("user", new HttpCallback<UpgradeInfo>(new Runnable() {

            @Override
            public void run() {
                if (!inBackground) {
                    checkingDialog.dismiss();
                    Util.sendToast(R.string.current_version_already_new);
                }
            }
            
        }) {

            @Override
            protected boolean processData(UpgradeInfo t) {
                int currentVersionCode = ConfigManager.getInstance()
                        .getCurrentVersionCode();
                if (!inBackground) {
                    checkingDialog.dismiss();
                }
                
                if (t.versionCode > currentVersionCode) {
                    mDownloadNewApkUrl = t.url;
                    final String version = t.version;
                    //Context context, String title, String content, String positiveStr, Runnable listener
                    Dialog dialog = new ConfirmationDialog(
                            context,
                            Util.getString(R.string.find_new_version, t.version),
                            t.descr,
                            Util.getString(R.string.dialog_upgrade), new Runnable() {

                                @Override
                                public void run() {
                                    NotificationUtil
                                            .sendDownloadProgressNotification(0, 0,
                                                    false, false);
                                    new AppDownloadTask()
                                            .execute(mDownloadNewApkUrl, version);
                                }
                            });
                    dialog.show();
                } else if (!inBackground) {
                    Util.sendToast(R.string.current_version_already_new);
                }
                return true;
            }
            
        });
        NotificationUtil.cancelDownloadProgressNotification();
    }
    
    public Intent getInstallNewApkIntent() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + mDownloadedAppPath), "application/vnd.android.package-archive");
        return i;
    }
    
    
    public Intent getBrowserInstallApkIntent() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(mDownloadNewApkUrl);  
        i.setData(content_url);  
        return i;
    }
    /*
    private class CheckVersionTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Context> mContext;
        private boolean mInBackground;

        public CheckVersionTask(Context context, boolean inBackground) {
            mContext = new WeakReference<Context>(context);
            mInBackground = inBackground;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            int currentVersionCode = ConfigManager.getInstance()
                    .getCurrentVersionCode();
            // Get new version from network
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // Access Http
            if (result) {
                // Open dialog to upgrade
                Dialog dialog = new ConfirmationDialog(
                        mContext.get(),
                        "发现新版本",
                        "该版本包括以下更新：\n1.XXXXXXXXXXXXXXXXXX\n2.YYYYYYYYYYYYYYYYYY",
                        "升级", new Runnable() {

                            @Override
                            public void run() {
                                NotificationUtil
                                        .sendDownloadProgressNotification(0, 0,
                                                false, false);
                                new AppDownloadTask()
                                        .execute(mDownloadNewApkUrl);
                            }
                        });
                dialog.show();
            } else if (!mInBackground) {
                Toast.makeText(mContext.get(), mContext.get().getString(R.string.current_version_already_new), Toast.LENGTH_LONG).show();
            }
        }
    }*/

    private class AppDownloadTask extends AsyncTask<String, Integer, Boolean> {
        private int mTotalBytes;
        

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = getInstallNewApkIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ClientApplication.sSharedInstance.startActivity(intent);
            }
            NotificationUtil.sendDownloadProgressNotification(mTotalBytes, mTotalBytes, true, result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            NotificationUtil.sendDownloadProgressNotification(values[0], mTotalBytes, false, false);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            OutputStream output = null;
            InputStream is = null;
            try {

                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();

                if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
                    Log.e(TAG, "connection failed:" + params[0]);
                    return Boolean.FALSE;
                }

                mTotalBytes = conn.getContentLength();
                
                is = conn.getInputStream();
                mDownloadedAppPath = DOWNLOADED_APP_DIR + "MmwdUClient_" + params[1] + ".apk";
                File file = new File(mDownloadedAppPath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();

                output = new FileOutputStream(file);

                byte[] buffer = new byte[(int) (4 * Util._1K)];
                int current;
                int downloaded = 0;
                while ((current = is.read(buffer)) != -1) {
                    output.write(buffer, 0, current);
                    downloaded += current;
                    publishProgress(downloaded);
                }
                output.flush();

                // Check MD5 ?
                Log.d(TAG, "new app size:" + mTotalBytes + ", downloaded file length:" + file.length());
                return mTotalBytes == file.length() ? Boolean.TRUE : Boolean.FALSE;
            } catch (Exception e) {
                Log.e(TAG, "Exception:", e);
                return Boolean.FALSE;
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    output = null;
                }

                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    is = null;
                }

            }
        }
    }

}
