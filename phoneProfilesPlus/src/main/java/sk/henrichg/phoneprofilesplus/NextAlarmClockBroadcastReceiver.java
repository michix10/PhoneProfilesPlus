package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.POWER_SERVICE;

public class NextAlarmClockBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NextAlarmClockBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "NextAlarmClockBroadcastReceiver.onReceive", "NextAlarmClockBroadcastReceiver_onReceive");

        if (intent == null)
            return;

        //if (!PPApplication.getApplicationStarted(context.getApplicationContext(), true))
        //    return;

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            String action = intent.getAction();
            if ((action != null) && action.equals(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    removeAlarm(alarmManager, context);

                    AlarmManager.AlarmClockInfo alarmClockInfo = alarmManager.getNextAlarmClock();
                    if (alarmClockInfo != null) {
                        long _time = alarmClockInfo.getTriggerTime();
                        if (PPApplication.logEnabled()) {
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                            String result = sdf.format(_time);
                            PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "_time=" + result);
                        }

                        PendingIntent infoPendingIntent = alarmClockInfo.getShowIntent();
                        // infoPendingIntent == null - Xiaomi Clock :-/
                        // infoPendingIntent == null - LG Clock :-/
                        // infoPendingIntent == null - Huawei Clock :-/

                        if (infoPendingIntent != null) {
                            String packageName = infoPendingIntent.getCreatorPackage();
                            if (packageName != null) {
                                PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName=" + packageName);

                                // com.google.android.deskclock - Google Clock
                                // com.sec.android.app.clockpackage - Samsung Clock
                                // com.sonyericsson.organizer - Sony Clock

                                if (packageName.equals("com.google.android.deskclock") ||
                                    packageName.equals("com.sec.android.app.clockpackage")  ||
                                    packageName.equals("com.sonyericsson.organizer")) {
                                    setAlarm(_time, alarmManager, context);
                                }
                            } else {
                                PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName == null");
                                setAlarm(_time, alarmManager, context);
                            }
                        } else {
                            PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "infoPendingIntent == null");
                            setAlarm(_time, alarmManager, context);
                        }
                    }
                    else {
                        PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "alarmClockInfo == null");
                        //removeAlarm(alarmManager, context);
                    }
                }
            }
        }
    }

    private void removeAlarm(AlarmManager alarmManager, Context context) {
        //Intent intent = new Intent(context, AlarmClockBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
        //intent.setClass(context, AlarmClockBroadcastReceiver.class);

        // cancel alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9998, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            PPApplication.logE("NextAlarmClockBroadcastReceiver.removeAlarm", "alarm found");
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setAlarm(long time, AlarmManager alarmManager, Context context) {
        //removeAlarm(alarmManager, context);

        long alarmTime = time;// - Event.EVENT_ALARM_TIME_SOFT_OFFSET;

        if (PPApplication.logEnabled()) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("NextAlarmClockBroadcastReceiver.setAlarm", "alarmTime=" + result);
        }

        //Intent intent = new Intent(context, AlarmClockBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
        //intent.setClass(context, AlarmClockBroadcastReceiver.class);

        // set alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9998, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // !!! DO NOT USE ALARM CLOCK !!!
        if (android.os.Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        else //if (android.os.Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        //else
        //    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }
}
