package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.RemoteViews;

import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;

public class SamsungEdgeProvider extends SlookCocktailProvider {

    private DataWrapper dataWrapper;

    public static final String INTENT_REFRESH_EDGEPANEL = "sk.henrichg.phoneprofilesplus.REFRESH_EDGEPANEL";

    private RemoteViews buildLayout(Context context/*, SlookCocktailManager cocktailBarManager, int appWidgetId*/)
    {
        Intent svcIntent=new Intent(context, SamsungEdgeService.class);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget;

        if (ApplicationPreferences.applicationSamsungEdgeHeader(context))
        {
            /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context))
            {
                if (ApplicationPreferences.applicationSamsungEdgePrefIndicator(context))
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget);
                else
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_indicator);
            }
            else
            {
                if (ApplicationPreferences.applicationSamsungEdgePrefIndicator(context))
                    widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget);
                else*/
                    widget=new RemoteViews(context.getPackageName(), R.layout.samsung_edge);
            //}
        }
        else
        {
            /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context))
                widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_header);
            else*/
                widget=new RemoteViews(context.getPackageName(), R.layout.samsung_edge_no_header);
        }

        // set background
        int red = 0x00;
        int green;
        int blue;
        if (ApplicationPreferences.applicationSamsungEdgeBackgroundType(context)) {
            int bgColor = Integer.valueOf(ApplicationPreferences.applicationSamsungEdgeBackgroundColor(context));
            red = Color.red(bgColor);
            green = Color.green(bgColor);
            blue = Color.blue(bgColor);
        }
        else {
            String applicationWidgetListLightnessB = ApplicationPreferences.applicationSamsungEdgeLightnessB(context);
            //if (applicationWidgetListLightnessB.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessB.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessB.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessB.equals("75")) red = 0xC0;
            if (applicationWidgetListLightnessB.equals("100")) red = 0xFF;
            green = red;
            blue = red;
        }
        int alpha = 0x40;
        String applicationWidgetListBackground = ApplicationPreferences.applicationSamsungEdgeBackground(context);
        if (applicationWidgetListBackground.equals("0")) alpha = 0x00;
        //if (applicationWidgetListBackground.equals("25")) alpha = 0x40;
        if (applicationWidgetListBackground.equals("50")) alpha = 0x80;
        if (applicationWidgetListBackground.equals("75")) alpha = 0xC0;
        if (applicationWidgetListBackground.equals("100")) alpha = 0xFF;
        widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));


        // header
        if (ApplicationPreferences.applicationSamsungEdgeHeader(context))
        {
            int monochromeValue = 0xFF;
            String applicationWidgetListIconLightness = ApplicationPreferences.applicationSamsungEdgeIconLightness(context);
            if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
            if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
            if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
            if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
            //if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;

            Profile profile = DatabaseHandler.getInstance(dataWrapper.context).getActivatedProfile();

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            if (profile != null)
            {
                profile.generateIconBitmap(context,
                        ApplicationPreferences.applicationSamsungEdgeIconColor(context).equals("1"),
                        monochromeValue,
                        ApplicationPreferences.applicationSamsungEdgeCustomIconLightness(context));
                /*profile.generatePreferencesIndicator(context,
                        ApplicationPreferences.applicationSamsungEdgeIconColor(context).equals("1"),
                        monochromeValue);*/
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, dataWrapper, false);
            }
            else
            {
                // create empty profile and set icon resource
                profile = new Profile();
                profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                profile._icon = Profile.PROFILE_ICON_DEFAULT+"|1|0|0";

                profile.generateIconBitmap(context,
                        ApplicationPreferences.applicationSamsungEdgeIconColor(context).equals("1"),
                        monochromeValue,
                        ApplicationPreferences.applicationSamsungEdgeCustomIconLightness(context));
                /*profile.generatePreferencesIndicator(context,
                        ApplicationPreferences.applicationSamsungEdgeIconColor(context).equals("1"),
                        monochromeValue);*/
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = new SpannableString(profile._name);
            }
            if (isIconResourceID)
            {
                if (profile._iconBitmap != null)
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, profile._iconBitmap);
                else {
                    //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                    int iconResource = Profile.getIconResource(iconIdentifier);
                    widget.setImageViewResource(R.id.widget_profile_list_header_profile_icon, iconResource);
                }
            }
            else
            {
                widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, profile._iconBitmap);
            }

            red = 0xFF;
            String applicationWidgetListLightnessT = ApplicationPreferences.applicationSamsungEdgeLightnessT(context);
            if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
            //if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
            green = red; blue = red;
            widget.setTextColor(R.id.widget_profile_list_header_profile_name, Color.argb(0xFF, red, green, blue));

            widget.setTextViewText(R.id.widget_profile_list_header_profile_name, profileName);
            /*if (ApplicationPreferences.applicationSamsungEdgePrefIndicator(context))
            {
                if (profile._preferencesIndicator != null)
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_pref_indicator, profile._preferencesIndicator);
                else
                    widget.setImageViewResource(R.id.widget_profile_list_header_profile_pref_indicator, R.drawable.ic_empty);
            }*/

            red = 0xFF;
            if (applicationWidgetListLightnessT.equals("0")) red = 0x00;
            if (applicationWidgetListLightnessT.equals("25")) red = 0x40;
            if (applicationWidgetListLightnessT.equals("50")) red = 0x80;
            if (applicationWidgetListLightnessT.equals("75")) red = 0xC0;
            //if (applicationWidgetListLightnessT.equals("100")) red = 0xFF;
            green = red; blue = red;
            widget.setInt(R.id.widget_profile_list_header_separator, "setBackgroundColor", Color.argb(0xFF, red, green, blue));

            /*
            if (Event.getGlobalEventsRunning(context)) {
                if (ApplicationPreferences.applicationSamsungEdgeIconColor(context).equals("1")) {
                    monochromeValue = 0xFF;
                    if (applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
                    if (applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
                    if (applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
                    if (applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
                    //if (applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_widget_restart_events);
                    bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue);
                    widget.setImageViewBitmap(R.id.widget_profile_list_header_restart_events, bitmap);
                }
                else
                    widget.setImageViewResource(R.id.widget_profile_list_header_restart_events, R.drawable.ic_widget_restart_events);
            }
            */

        }
        ////////////////////////////////////////////////

        // clicks
        Intent intent = new Intent(context, EditorProfilesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.widget_profile_list_header, pendingIntent);

        /*
        if (Event.getGlobalEventsRunning(context)) {
            widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.VISIBLE);
            Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
            PendingIntent pIntentRE = PendingIntent.getActivity(context, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.widget_profile_list_header_restart_events, pIntentRE);
        }
        else
            widget.setViewVisibility(R.id.widget_profile_list_header_restart_events, View.GONE);
        */

        /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context))
            widget.setRemoteAdapter(R.id.widget_profile_list, svcIntent);
        else*/
            widget.setRemoteAdapter(R.id.widget_profile_grid, svcIntent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context))
            widget.setEmptyView(R.id.widget_profile_list, R.id.widget_profiles_list_empty);
        else*/
            widget.setEmptyView(R.id.widget_profile_grid, R.id.widget_profiles_list_empty);

        Intent clickIntent=new Intent(context, BackgroundActivateProfileActivity.class);
        clickIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
        PendingIntent clickPI=PendingIntent.getActivity(context, 400,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context))
            widget.setPendingIntentTemplate(R.id.widget_profile_list, clickPI);
        else*/
            widget.setPendingIntentTemplate(R.id.widget_profile_grid, clickPI);

        return widget;
    }

    private void createProfilesDataWrapper(Context context)
    {
        if (dataWrapper == null)
        {
            dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
        }
    }

    private void doOnUpdate(Context context, SlookCocktailManager cocktailBarManager, int cocktailId)
    {
        RemoteViews widget = buildLayout(context);
        try {
            cocktailBarManager.updateCocktail(cocktailId, widget);
        } catch (Exception ignored) {}
    }

    @Override
    public void onUpdate(Context context, SlookCocktailManager cocktailBarManager, int[] cocktailIds) {
        createProfilesDataWrapper(context);

        for (int cocktailId : cocktailIds) {
            doOnUpdate(context, cocktailBarManager, cocktailId);
        }

        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (EditorProfilesActivity.doImport)
            return;

        String action = intent.getAction();

        createProfilesDataWrapper(context);

        if ((action != null) &&
                (action.equalsIgnoreCase(INTENT_REFRESH_EDGEPANEL))) {
            updateWidgets(context);
        }

        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    private void updateWidget(Context context, int cocktailId) {
        try {
            SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);

            doOnUpdate(context, cocktailManager, cocktailId);

            /*if (!ApplicationPreferences.applicationSamsungEdgeGridLayout(context))
                cocktailManager.notifyCocktailViewDataChanged(cocktailId, R.id.widget_profile_list);
            else*/
                cocktailManager.notifyCocktailViewDataChanged(cocktailId, R.id.widget_profile_grid);
        } catch (Exception ignored) {}
    }

    private void updateWidgets(Context context) {
        try {
            SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
            int cocktailIds[] = cocktailManager.getCocktailIds(new ComponentName(context, SamsungEdgeProvider.class));

            if (cocktailIds != null) {
                for (int cocktailId : cocktailIds) {
                    updateWidget(context, cocktailId);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onVisibilityChanged(Context context, int cocktailId, int visibility) {

    }

}
