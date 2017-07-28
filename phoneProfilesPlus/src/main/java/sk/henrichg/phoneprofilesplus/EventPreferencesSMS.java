package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

class EventPreferencesSMS extends EventPreferences {

    //public int _smsEvent;
    String _contacts;
    String _contactGroups;
    int _contactListType;
    long _startTime;
    boolean _permanentRun;
    int _duration;

    static final String PREF_EVENT_SMS_ENABLED = "eventSMSEnabled";
    //static final String PREF_EVENT_SMS_EVENT = "eventSMSEvent";
    private static final String PREF_EVENT_SMS_CONTACTS = "eventSMSContacts";
    private static final String PREF_EVENT_SMS_CONTACT_GROUPS = "eventSMSContactGroups";
    private static final String PREF_EVENT_SMS_CONTACT_LIST_TYPE = "eventSMSContactListType";
    private static final String PREF_EVENT_SMS_PERMANENT_RUN = "eventSMSPermanentRun";
    private static final String PREF_EVENT_SMS_DURATION = "eventSMSDuration";

    private static final String PREF_EVENT_SMS_CATEGORY = "eventSMSCategory";

    //static final int SMS_EVENT_UNDEFINED = -1;
    //static final int SMS_EVENT_INCOMING = 0;
    //static final int SMS_EVENT_OUTGOING = 1;

    //static final int CONTACT_LIST_TYPE_WHITE_LIST = 0;
    //static final int CONTACT_LIST_TYPE_BLACK_LIST = 1;
    private static final int CONTACT_LIST_TYPE_NOT_USE = 2;

    EventPreferencesSMS(Event event,
                                    boolean enabled,
                                    //int smsEvent,
                                    String contacts,
                                    String contactGroups,
                                    int contactListType,
                                    boolean permanentRun,
                                    int duration)
    {
        super(event, enabled);

        //this._smsEvent = smsEvent;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._contactListType = contactListType;
        this._permanentRun = permanentRun;
        this._duration = duration;

        this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesSMS._enabled;
        //this._smsEvent = fromEvent._eventPreferencesSMS._smsEvent;
        this._contacts = fromEvent._eventPreferencesSMS._contacts;
        this._contactGroups = fromEvent._eventPreferencesSMS._contactGroups;
        this._contactListType = fromEvent._eventPreferencesSMS._contactListType;
        this._permanentRun = fromEvent._eventPreferencesSMS._permanentRun;
        this._duration = fromEvent._eventPreferencesSMS._duration;

        this._startTime = 0;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SMS_ENABLED, _enabled);
        //editor.putString(PREF_EVENT_SMS_EVENT, String.valueOf(this._smsEvent));
        editor.putString(PREF_EVENT_SMS_CONTACTS, this._contacts);
        editor.putString(PREF_EVENT_SMS_CONTACT_GROUPS, this._contactGroups);
        editor.putString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
        editor.putBoolean(PREF_EVENT_SMS_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_SMS_DURATION, String.valueOf(this._duration));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
        //this._smsEvent = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_EVENT, "0"));
        this._contacts = preferences.getString(PREF_EVENT_SMS_CONTACTS, "");
        this._contactGroups = preferences.getString(PREF_EVENT_SMS_CONTACT_GROUPS, "");
        this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, "0"));
        this._permanentRun = preferences.getBoolean(PREF_EVENT_SMS_PERMANENT_RUN, false);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_DURATION, "5"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_sms_summary);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_sms) + ": " + "</b>";
            }

            //descr = descr + context.getString(R.string.pref_event_sms_event);
            //String[] smsEvents = context.getResources().getStringArray(R.array.eventSMSEventsArray);
            //descr = descr + ": " + smsEvents[tmp._smsEvent] + "; ";
            descr = descr + context.getString(R.string.pref_event_sms_contactListType);
            String[] contactListTypes = context.getResources().getStringArray(R.array.eventSMSContactListTypeArray);
            descr = descr + ": " + contactListTypes[this._contactListType] + "; ";
            if (this._permanentRun)
                descr = descr + context.getString(R.string.pref_event_permanentRun);
            else
                descr = descr + context.getString(R.string.pref_event_duration) + ": " + GlobalGUIRoutines.getDurationString(this._duration);
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (/*key.equals(PREF_EVENT_SMS_EVENT) ||*/ key.equals(PREF_EVENT_SMS_CONTACT_LIST_TYPE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_SMS_CONTACTS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
            }
        }
        if (key.equals(PREF_EVENT_SMS_CONTACT_GROUPS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
            }
        }
        if (key.equals(PREF_EVENT_SMS_PERMANENT_RUN)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals("false"));
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (/*key.equals(PREF_EVENT_SMS_EVENT) ||*/
            key.equals(PREF_EVENT_SMS_CONTACT_LIST_TYPE) ||
            key.equals(PREF_EVENT_SMS_CONTACTS) ||
            key.equals(PREF_EVENT_SMS_CONTACT_GROUPS) ||
            key.equals(PREF_EVENT_SMS_DURATION))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_SMS_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        //setSummary(prefMng, PREF_EVENT_SMS_EVENT, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACT_LIST_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_DURATION, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (Event.isEventPreferenceAllowed(PREF_EVENT_SMS_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {
            EventPreferencesSMS tmp = new EventPreferencesSMS(this._event, this._enabled, this._contacts, this._contactGroups, this._contactListType,
                                                                this._permanentRun, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && ((_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||
                              (!(_contacts.isEmpty() && _contactGroups.isEmpty())));

        return runnable;
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    long computeAlarm()
    {
        PPApplication.logE("EventPreferencesSMS.computeAlarm","xxx");

        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    public void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsService

        PPApplication.logE("EventPreferencesSMS.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsService

        PPApplication.logE("EventPreferencesSMS.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        PPApplication.logE("EventPreferencesSMS.removeSystemEvent", "xxx");
    }

    private void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            PPApplication.logE("EventPreferencesSMS.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("EventPreferencesSMS.setAlarm", "endTime=" + result);

            Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= 23)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            else if (android.os.Build.VERSION.SDK_INT >= 19)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
        }
    }

    void saveStartTime(DataWrapper dataWrapper, String phoneNumber, long startTime) {
        if (Permissions.checkContacts(dataWrapper.context)) {

            boolean phoneNumberFound = false;

            if (this._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {
                // find phone number in groups
                String[] splits = this._contactGroups.split("\\|");
                for (String split : splits) {
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};
                    String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=? AND "
                            + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                    String[] selectionArgs = new String[]{split};
                    Cursor mCursor = dataWrapper.context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
                    if (mCursor != null) {
                        while (mCursor.moveToNext()) {
                            String contactId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                            String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                            String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " +
                                    ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1";
                            String[] selection2Args = new String[]{contactId};
                            Cursor phones = dataWrapper.context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                            if (phones != null) {
                                while (phones.moveToNext()) {
                                    String _phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                        phoneNumberFound = true;
                                        break;
                                    }
                                }
                                phones.close();
                            }
                            if (phoneNumberFound)
                                break;
                        }
                        mCursor.close();
                    }
                    if (phoneNumberFound)
                        break;
                }

                if (!phoneNumberFound) {
                    // find phone number in contacts
                    splits = this._contacts.split("\\|");
                    for (String split : splits) {
                        String[] splits2 = split.split("#");

                        // get phone number from contacts
                        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER};
                        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1' and " + ContactsContract.Contacts._ID + "=?";
                        String[] selectionArgs = new String[]{splits2[0]};
                        Cursor mCursor = dataWrapper.context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs, null);
                        if (mCursor != null) {
                            while (mCursor.moveToNext()) {
                                String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
                                String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " + ContactsContract.CommonDataKinds.Phone._ID + "=?";
                                String[] selection2Args = new String[]{splits2[0], splits2[1]};
                                Cursor phones = dataWrapper.context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                                if (phones != null) {
                                    while (phones.moveToNext()) {
                                        String _phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                            phoneNumberFound = true;
                                            break;
                                        }
                                    }
                                    phones.close();
                                }
                                if (phoneNumberFound)
                                    break;
                            }
                            mCursor.close();
                        }
                        if (phoneNumberFound)
                            break;
                    }
                }

                if (this._contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                    phoneNumberFound = !phoneNumberFound;
            } else
                phoneNumberFound = true;

            if (phoneNumberFound)
                this._startTime = startTime;
            else
            if (this._permanentRun)
                this._startTime = 0;

            dataWrapper.getDatabaseHandler().updateSMSStartTime(_event);

            if (phoneNumberFound) {
                if (_event.getStatus() == Event.ESTATUS_RUNNING)
                    setSystemEventForPause(dataWrapper.context);
            }
        }
    }

}
