package com.kuruvatech.fpcvendor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;

import com.kuruvatech.fpcvendor.Utils.Constants;
import com.kuruvatech.fpcvendor.Utils.SessionManager;


//Class extending service as it is a service that will run in background
public class NotificationListener extends Service {

    private String sendingMes;
    SessionManager session;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //When the service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Opening sharedpreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE);

        //Getting the firebase id from sharedpreferences
        String id = sharedPreferences.getString(Constants.UNIQUE_ID, null);

        Firebase.setAndroidContext(getApplicationContext());
        //Creating a firebase object
        Firebase firebase = new Firebase(Constants.FIREBASE_APP + '/' + id);

        //Adding a valueevent listener to firebase
        //this will help us to  track the value changes on firebase
        firebase.addValueEventListener(new ValueEventListener() {

            //This method is called whenever we change the value in firebase
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the value from firebase
                //We stored none as a initial value
                session = new SessionManager(getApplicationContext());
                if(snapshot.child("msg").exists()) {
                    String msg = snapshot.child("msg").getValue().toString();
                    sendingMes=msg ;

                    //So if the value is none we will not create any notification
                    if (msg.equals("none"))
                        return;

                    if(msg.compareTo(session.getlastpn()) != 0) {
                        session.setlastpn(msg);
                        String message  = "New Order Received : " + msg;
                        showNotification(Calendar.getInstance().getTimeInMillis(),message,1);
                    }
                }
                else if(snapshot.child("update").exists())
                {
                    String msg = snapshot.child("update").getValue().toString();
                    if(msg.compareTo(session.getlastpn()) != 0) {
                        session.setlastpn(msg);
                        showNotification(Calendar.getInstance().getTimeInMillis(), msg, 2);
                    }
                }
                else if(snapshot.child("info").exists())
                {
                    String msg = snapshot.child("info").getValue().toString();
                    if(msg.compareTo(session.getlastpn()) != 0) {
                        session.setlastpn(msg);
                        showNotification(Calendar.getInstance().getTimeInMillis(), msg, 3);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });

        return START_STICKY;

    }



    private void showNotification(long when, String msg,int intent_type){
        //Creating a notification
        final String GROUP_KEY_ORDER_IDS = "group_order_ids";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //Vibration
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        //LED
        builder.setLights(Color.RED, 3000, 3000);
        //Sound
        builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        Intent intent;
        if(intent_type == 1 ) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("notificationFragment", sendingMes);
        }else if(intent_type ==3) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("notificationFragment", msg);
        }
        else
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.kuruvatech.fpcvendor"));

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("Khaanavali");
        builder.setContentText(msg);
        builder.setAutoCancel(true);
        builder.setWhen(when);
//        builder.setGroup(GROUP_KEY_ORDER_IDS);
//        builder.setGroupSummary(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) when, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
