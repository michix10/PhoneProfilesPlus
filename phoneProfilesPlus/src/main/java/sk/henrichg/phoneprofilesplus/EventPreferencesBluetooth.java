package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;

import java.util.Arrays;

public class EventPreferencesBluetooth extends EventPreferences {

    public String _adapterName;
    public int _connectionType;
    public int _devicesType;

    static final int CTYPE_CONNECTED = 0;
    static final int CTYPE_INFRONT = 1;
    static final int CTYPE_NOTCONNECTED = 2;
    static final int CTYPE_NOTINFRONT = 3;

    static final int DTYPE_CLASSIC = 0;
    static final int DTYPE_LE = 1;

    static final String PREF_EVENT_BLUETOOTH_ENABLE_SCANNING_APP_SETTINGS = "eventEnableBluetoothScaningAppSettings";
    static final String PREF_EVENT_BLUETOOTH_ENABLED = "eventBluetoothEnabled";
    static final String PREF_EVENT_BLUETOOTH_ADAPTER_NAME = "eventBluetoothAdapterNAME";
    static final String PREF_EVENT_BLUETOOTH_CONNECTION_TYPE = "eventBluetoothConnectionType";
    static final String PREF_EVENT_BLUETOOTH_DEVICES_TYPE = "eventBluetoothDevicesType";

    static final String PREF_EVENT_BLUETOOTH_CATEGORY = "eventBluetoothCategory";

    public EventPreferencesBluetooth(Event event,
                                    boolean enabled,
                                    String adapterName,
                                    int connectionType,
                                    int devicesType)
    {
        super(event, enabled);

        this._adapterName = adapterName;
        this._connectionType = connectionType;
        this._devicesType = devicesType;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesBluetooth)fromEvent._eventPreferencesBluetooth)._enabled;
        this._adapterName = ((EventPreferencesBluetooth)fromEvent._eventPreferencesBluetooth)._adapterName;
        this._connectionType = ((EventPreferencesBluetooth)fromEvent._eventPreferencesBluetooth)._connectionType;
        this._devicesType = ((EventPreferencesBluetooth)fromEvent._eventPreferencesBluetooth)._devicesType;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_BLUETOOTH_ENABLED, _enabled);
        editor.putString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, this._adapterName);
        editor.putString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, String.valueOf(this._connectionType));
        editor.putString(PREF_EVENT_BLUETOOTH_DEVICES_TYPE, String.valueOf(this._devicesType));
        editor.commit();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
        this._adapterName = preferences.getString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, "");
        this._connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, "1"));
        this._devicesType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_DEVICES_TYPE, "0"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled)
        {
            //descr = descr + context.getString(R.string.event_type_bluetooth) + ": ";
            //descr = descr + context.getString(R.string.event_preferences_not_enabled);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_bluetooth) + ": " + "</b>";
            }

            descr = descr + context.getString(R.string.pref_event_bluetooth_connectionType);
            String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeArray);
            String[] connectionListTypes = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeValues);
            int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
            descr = descr + ": " + connectionListTypeNames[index] + "; ";
            descr = descr + context.getString(R.string.pref_event_bluetooth_adapterName);
            descr = descr + ": ";
            if ((this._connectionType == CTYPE_INFRONT) || (this._connectionType == CTYPE_NOTINFRONT)) {
                if (ScannerService.bluetoothLESupported(context)) {
                    if (this._devicesType == DTYPE_CLASSIC)
                        descr = descr + "[CL] ";
                    else if (this._devicesType == DTYPE_LE)
                        descr = descr + "[LE] ";
                }
            }
            descr = descr + this._adapterName;
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLE_SCANNING_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            preference.setSummary(context.getResources().getString(R.string.menu_settings) + ": " +
                    context.getResources().getString(R.string.phone_profiles_pref_applicationEventBluetoothEnableBluetooth));
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            preference.setSummary(value);
            GUIData.setPreferenceTitleStyle(preference, false, true, false);
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(value);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);

            boolean btLESupported = ScannerService.bluetoothLESupported(context);
            listPreference = (ListPreference)prefMng.findPreference(PREF_EVENT_BLUETOOTH_DEVICES_TYPE);
            if ((!btLESupported) || value.equals("0") || value.equals("2"))
                listPreference.setEnabled(false);
            else
                listPreference.setEnabled(true);
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_DEVICES_TYPE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            boolean btLESupported = ScannerService.bluetoothLESupported(context);

            if (!btLESupported) {
                listPreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed));
            }
            else {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME) ||
            key.equals(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE)||
            key.equals(PREF_EVENT_BLUETOOTH_DEVICES_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_ENABLE_SCANNING_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_ADAPTER_NAME, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_DEVICES_TYPE, preferences, context);

        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context)
                != GlobalData.PREFERENCE_ALLOWED)
        {
            prefMng.findPreference(PREF_EVENT_BLUETOOTH_ENABLED).setEnabled(false);
            prefMng.findPreference(PREF_EVENT_BLUETOOTH_ADAPTER_NAME).setEnabled(false);
            prefMng.findPreference(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE).setEnabled(false);
            prefMng.findPreference(PREF_EVENT_BLUETOOTH_DEVICES_TYPE).setEnabled(false);
        }

    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (key.isEmpty() ||
                key.equals(PREF_EVENT_BLUETOOTH_ENABLED)) {
            boolean preferenceChanged = false;
            if (preferences == null) {
                preferenceChanged = this._enabled;
            } else {
                preferenceChanged = preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
            }
            boolean bold = preferenceChanged;
            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CATEGORY);
            if (preference != null) {
                GUIData.setPreferenceTitleStyle(preference, bold, false, !isRunable());
                if (bold)
                    preference.setSummary(Html.fromHtml(getPreferencesDescription(false, context)));
            }
        }
    }

    @Override
    public boolean isRunable()
    {
        return super.isRunable() && (!this._adapterName.isEmpty());
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    @Override
    public void setSystemRunningEvent(Context context)
    {
        if ((_connectionType == CTYPE_INFRONT) &&
            (!BluetoothScanAlarmBroadcastReceiver.isAlarmSet(context, false)))
            BluetoothScanAlarmBroadcastReceiver.setAlarm(context, false, true);
    }

    @Override
    public void setSystemPauseEvent(Context context)
    {
        if ((_connectionType == CTYPE_INFRONT) &&
            (!BluetoothScanAlarmBroadcastReceiver.isAlarmSet(context, false)))
            BluetoothScanAlarmBroadcastReceiver.setAlarm(context, false, true);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
    }

}
