package ca.on.conestogac.clo.taxcalculator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";

    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");

        return null;
    }

    @Override
    public void onCreate() {

        final Timer timer = new Timer(true);

        final NotificationManager manager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final Intent intent = new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (networkInfo != null && networkInfo.isConnected()) {
            //todo get random quote from the Internet
            //todo ERROR D/NotificationService: org.json.JSONException: No value for content
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("https://quotes.rest/qod?language=en");
                        InputStream inputStream = url.openStream();

                        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                        String result = scanner.hasNext() ? scanner.next() : "";

                        final Notification notification = new Notification.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText(new JSONObject(result).getJSONObject("content")
                                        .getJSONArray("quotes")
                                        .getJSONObject(0)
                                        .getString("quote"))
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent)
                                .build();

                        manager.notify(1, notification);
                    } catch (MalformedURLException e) {
                        Log.d(TAG, e.toString());
                    } catch (IOException e) {
                        Log.d(TAG, e.toString());
                    } catch (JSONException e) {
                        Log.d(TAG, e.toString());
                    } finally {
                        timer.cancel();
                        stopSelf();
                    }
                }
            }, 3000);
        } else {
            stopSelf();
        }

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}