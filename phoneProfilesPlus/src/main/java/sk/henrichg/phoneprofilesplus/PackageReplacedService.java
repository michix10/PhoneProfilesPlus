package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PackageReplacedService extends WakefulIntentService {

    public PackageReplacedService() {
        super("PackageReplacedService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            final Context appContext = getApplicationContext();

            // start delayed bootup broadcast
            PPApplication.startedOnBoot = true;
            final Handler handler = new Handler(appContext.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PackageReplacedService.doWakefulWork", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 10000);

            Permissions.setShowRequestAccessNotificationPolicyPermission(appContext, true);
            Permissions.setShowRequestWriteSettingsPermission(appContext, true);
            ScannerService.setShowEnableLocationNotification(appContext, true);
            //ActivateProfileHelper.setScreenUnlocked(appContext, true);

            int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
            PPApplication.logE("@@@ PackageReplacedService.doWakefulWork", "oldVersionCode="+oldVersionCode);
            int actualVersionCode;
            try {
                PackageInfo pinfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                actualVersionCode = pinfo.versionCode;
                PPApplication.logE("@@@ PackageReplacedService.doWakefulWork", "actualVersionCode=" + actualVersionCode);

                if (oldVersionCode < actualVersionCode) {
                    if (actualVersionCode <= 2322) {
                        // for old packages use Priority in events
                        ApplicationPreferences.getSharedPreferences(appContext);
                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                        PPApplication.logE("@@@ PackageReplacedService.doWakefulWork", "applicationEventUsePriority=true");
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                        editor.apply();
                        //PPApplication.loadPreferences(appContext);
                    }
                    if (actualVersionCode <= 2400) {
                        PPApplication.logE("@@@ PackageReplacedService.doWakefulWork", "donation alarm restart");
                        PPApplication.setDaysAfterFirstStart(appContext, 0);
                        AboutApplicationJob.scheduleJob();
                    }
                    if (actualVersionCode <= 2500) {
                        // for old packages hide profile notification from status bar if notification is disabled
                        ApplicationPreferences.getSharedPreferences(appContext);
                        if (!ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true)) {
                            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                            PPApplication.logE("@@@ PackageReplacedService.doWakefulWork", "notificationShowInStatusBar=false");
                            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                            editor.apply();
                            //PPApplication.loadPreferences(appContext);
                        }
                    }
                    if (actualVersionCode <= 2700) {
                        ApplicationPreferences.getSharedPreferences(appContext);
                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();

                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);

                        editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS_SAVE, false);
                        editor.putBoolean(EventPreferencesActivity.PREF_START_TARGET_HELPS, false);
                        editor.apply();
                    }
                    if (actualVersionCode <= 3200) {
                        ApplicationPreferences.getSharedPreferences(appContext);
                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, true);
                        editor.apply();
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }

            PPApplication.logE("PackageReplacedService.doWakefulWork","PhoneProfilesService.instance="+PhoneProfilesService.instance);

            if (PPApplication.getApplicationStarted(appContext, false))
            {
                PPApplication.logE("@@@ PackageReplacedService.doWakefulWork", "start PhoneProfilesService");

                if (PhoneProfilesService.instance != null) {
                    // stop PhoneProfilesService
                    appContext.stopService(new Intent(appContext, PhoneProfilesService.class));
                    Handler _handler = new Handler(appContext.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            startService(appContext);
                        }
                    };
                    _handler.postDelayed(r, 2000);
                }
                else
                    startService(appContext);
            }

        }
    }

    private void startService(Context context) {
        // must by false for avoiding starts/pause events before restart events
        PPApplication.setApplicationStarted(context, false);

        // start PhoneProfilesService
        Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        context.startService(serviceIntent);
    }

}