package com.blb.mmwd.uclient.util;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.manager.UpgradeManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class NotificationUtil {
    private static final int APP_DOWNLOAD_PROGRESS_NOTIFY_ID = 1;
    private static NotificationManager sNotificationManager;

    /**
     * 
     * @param progress
     * @param success
     */
    public static void sendDownloadProgressNotification(int downloadedBytes,
            int totalBytes, boolean finish, boolean success) {
        Context context = ClientApplication.sSharedInstance;
        if (sNotificationManager == null) {
            sNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.notification_upgrade_download);
        PendingIntent pi = null;
        if (finish) {
            remoteViews.setViewVisibility(R.id.notify_progress_bar, View.GONE);
            if (success) {
                remoteViews.setViewVisibility(R.id.notify_download_succ_text,
                        View.VISIBLE);
                remoteViews.setTextViewText(
                        R.id.notify_progress_text,
                        context.getResources().getString(
                                R.string.new_app_download_prog_text, "100%",
                                Util.formatSize(totalBytes),
                                Util.formatSize(totalBytes)));
                pi = PendingIntent.getActivity(context, 0, UpgradeManager
                        .getInstance().getInstallNewApkIntent(), 0);
            } else {
                pi = PendingIntent.getActivity(context, 0, UpgradeManager
                        .getInstance().getBrowserInstallApkIntent(), 0);
                remoteViews.setViewVisibility(R.id.notify_download_fail_text,
                        View.VISIBLE);

            }
        } else {
            int progress = totalBytes == 0 ? 0 : (downloadedBytes * 100)
                    / totalBytes;
            remoteViews.setTextViewText(
                    R.id.notify_progress_text,
                    context.getResources().getString(
                            R.string.new_app_download_prog_text,
                            progress + "%", Util.formatSize(downloadedBytes),
                            Util.formatSize(totalBytes)));

            remoteViews.setProgressBar(R.id.notify_progress_bar, 100, progress,
                    false);
        }
        NotificationCompat.Builder builder = new Builder(context);
        Notification notification = builder.setContent(remoteViews)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(context.getString(R.string.new_app_download_start))
                .setContentIntent(pi).build();

        sNotificationManager.notify(APP_DOWNLOAD_PROGRESS_NOTIFY_ID,
                notification);
    }
    
    public static void cancelDownloadProgressNotification() {
        Context context = ClientApplication.sSharedInstance;
        if (sNotificationManager == null) {
            sNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        sNotificationManager.cancel(APP_DOWNLOAD_PROGRESS_NOTIFY_ID);
    }
}
