package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.Collections;
import java.util.Iterator;

class EditorEventListAdapter extends RecyclerView.Adapter<EditorEventListViewHolder>
                                 implements ItemTouchHelperAdapter
{

    private EditorEventListFragment fragment;
    private DataWrapper activityDataWrapper;
    private final int filterType;
    //boolean released = false;
    //private int defaultColor;

    private final OnStartDragItemListener mDragStartListener;

    //private boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "editor_event_list_adapter_start_target_helps";
    static final String PREF_START_TARGET_HELPS_ORDER = "editor_event_list_adapter_start_target_helps_order";

    EditorEventListAdapter(EditorEventListFragment f, DataWrapper pdw, int filterType,
                           OnStartDragItemListener dragStartListener)
    {
        fragment = f;
        activityDataWrapper = pdw;
        this.filterType = filterType;
        this.mDragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public EditorEventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            if (ApplicationPreferences.applicationEditorPrefIndicator(fragment.getActivity()))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_event_list_item_with_order, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_event_list_item_no_indicator_with_order, parent, false);
        }
        else {
            if (ApplicationPreferences.applicationEditorPrefIndicator(fragment.getActivity()))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_event_list_item, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_event_list_item_no_indicator, parent, false);
        }

        return new EditorEventListViewHolder(view, fragment, fragment.getActivity(), filterType);
    }

    @Override
    public void onBindViewHolder(@NonNull final EditorEventListViewHolder holder, int position) {
        Event event = getItem(position);
        holder.bindEvent(event);

        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            if (holder.dragHandle != null) {
                holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mDragStartListener.onStartDrag(holder);
                                break;
                            case MotionEvent.ACTION_UP:
                                v.performClick();
                                break;
                            default:
                                break;
                        }
                    /*if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }*/
                        return false;
                    }
                });
            }
        }
    }

    public void release()
    {
        //released = true;

        fragment = null;
        activityDataWrapper = null;
    }

    @Override
    public int getItemCount() {
        synchronized (activityDataWrapper.eventList) {
            fragment.textViewNoData.setVisibility(
                    ((activityDataWrapper.eventListFilled) &&
                     (activityDataWrapper.eventList.size() > 0))
                    ? View.GONE : View.VISIBLE);

            if (!activityDataWrapper.eventListFilled)
                return 0;

            if ((filterType == EditorEventListFragment.FILTER_TYPE_ALL) ||
                    (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER))
                return activityDataWrapper.eventList.size();

            int count = 0;
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                switch (filterType) {
                    case EditorEventListFragment.FILTER_TYPE_RUNNING:
                        //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                        if (event.getStatus() == Event.ESTATUS_RUNNING)
                            ++count;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_PAUSED:
                        //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                        if (event.getStatus() == Event.ESTATUS_PAUSE)
                            ++count;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_STOPPED:
                        //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                        if (event.getStatus() == Event.ESTATUS_STOP)
                            ++count;
                        break;
                }
            }
            return count;
        }
    }

    private Event getItem(int position)
    {
        if (getItemCount() == 0)
            return null;
        else
        {
            synchronized (activityDataWrapper.eventList) {
                if ((filterType == EditorEventListFragment.FILTER_TYPE_ALL) ||
                        (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER))
                    return activityDataWrapper.eventList.get(position);

                Event _event = null;

                int pos = -1;
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    switch (filterType) {
                        case EditorEventListFragment.FILTER_TYPE_RUNNING:
                            //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                            if (event.getStatus() == Event.ESTATUS_RUNNING)
                                ++pos;
                            break;
                        case EditorEventListFragment.FILTER_TYPE_PAUSED:
                            //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                            if (event.getStatus() == Event.ESTATUS_PAUSE)
                                ++pos;
                            break;
                        case EditorEventListFragment.FILTER_TYPE_STOPPED:
                            //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                            if (event.getStatus() == Event.ESTATUS_STOP)
                                ++pos;
                            break;
                    }
                    if (pos == position) {
                        _event = event;
                        break;
                    }
                }

                return _event;
            }
        }
    }

    int getItemPosition(Event event)
    {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return -1;

            if (event == null)
                return -1;

            int pos = -1;

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                Event _event = it.next();
                switch (filterType) {
                    case EditorEventListFragment.FILTER_TYPE_ALL:
                    case EditorEventListFragment.FILTER_TYPE_START_ORDER:
                        ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_RUNNING:
                        //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                        if (event.getStatus() == Event.ESTATUS_RUNNING)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_PAUSED:
                        //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                        if (event.getStatus() == Event.ESTATUS_PAUSE)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_STOPPED:
                        //if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                        if (event.getStatus() == Event.ESTATUS_STOP)
                            ++pos;
                        break;
                }

                if (_event._id == event._id)
                    return pos;
            }
            return -1;
        }
    }

    /*
    public void setList(List<Event> el)
    {
        eventList = el;
        notifyDataSetChanged();
    }
    */

    void addItem(Event event/*, boolean refresh*/)
    {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return;

            //if (refresh)
            //    fragment.listView.getRecycledViewPool().clear();
            activityDataWrapper.eventList.add(event);
        }
        //if (refresh)
        //    notifyDataSetChanged();
    }

    void deleteItemNoNotify(Event event)
    {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return;

            activityDataWrapper.eventList.remove(event);
        }
    }

    public void clear()
    {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return;

            fragment.listView.getRecycledViewPool().clear();
            activityDataWrapper.eventList.clear();
        }
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            synchronized (activityDataWrapper.eventList) {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    Profile profile = activityDataWrapper.getProfileById(event._fkProfileStart, true,
                            ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context), false);
                    activityDataWrapper.refreshProfileIcon(profile, true,
                            ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context));
                    profile = activityDataWrapper.getProfileById(event._fkProfileEnd, true,
                            ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context), false);
                    activityDataWrapper.refreshProfileIcon(profile, true,
                            ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
        synchronized (activityDataWrapper.eventList) {
            activityDataWrapper.eventList.remove(position);
        }
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return false;

            //Log.d("----- EditorEventListAdapter.onItemMove", "fromPosition="+fromPosition);
            //Log.d("----- EditorEventListAdapter.onItemMove", "toPosition="+toPosition);

            if ((fromPosition < 0) || (toPosition < 0))
                return false;

            // convert positions from adapter into profileList
            int plFrom = activityDataWrapper.eventList.indexOf(getItem(fromPosition));
            int plTo = activityDataWrapper.eventList.indexOf(getItem(toPosition));

            if (plFrom < plTo) {
                for (int i = plFrom; i < plTo; i++) {
                    Collections.swap(activityDataWrapper.eventList, i, i + 1);
                }
            } else {
                for (int i = plFrom; i > plTo; i--) {
                    Collections.swap(activityDataWrapper.eventList, i, i - 1);
                }
            }

            DatabaseHandler.getInstance(activityDataWrapper.context).setEventStartOrder(activityDataWrapper.eventList);  // set events _startOrder and write it into db
            activityDataWrapper.restartEventsWithDelay(15, false, true, DatabaseHandler.ALTYPE_EVENTPREFERENCESCHANGED);
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    void showTargetHelps(Activity activity, EditorEventListFragment fragment, View listItemView) {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (fragment.targetHelpsSequenceStarted)
            return;

        ApplicationPreferences.getSharedPreferences(activity);

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true) || ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_ORDER, true)) {

            //Log.d("EditorEventListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            int circleColor = R.color.tabTargetHelpCircleColor;
            if (ApplicationPreferences.applicationTheme(activity).equals("dark"))
                circleColor = R.color.tabTargetHelpCircleColor_dark;
            int textColor = R.color.tabTargetHelpTextColor;
            if (ApplicationPreferences.applicationTheme(activity).equals("white"))
                textColor = R.color.tabTargetHelpTextColor_white;
            boolean tintTarget = !ApplicationPreferences.applicationTheme(activity).equals("white");

            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
                //Log.d("EditorEventListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                Rect eventItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
                int[] screenLocation = new int[2];
                //listItemView.getLocationOnScreen(screenLocation);
                listItemView.getLocationInWindow(screenLocation);
                eventItemTarget.offset(screenLocation[0] + listItemView.getWidth() / 2 - listItemView.getHeight() / 2, screenLocation[1]);

                final TapTargetSequence sequence = new TapTargetSequence(activity);

                if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
                    editor.putBoolean(PREF_START_TARGET_HELPS_ORDER, false);
                    editor.apply();

                    sequence.targets(
                            TapTarget.forBounds(eventItemTarget, activity.getString(R.string.editor_activity_targetHelps_eventPreferences_title), activity.getString(R.string.editor_activity_targetHelps_eventPreferences_description))
                                    .transparentTarget(true)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_eventMenu_title), activity.getString(R.string.editor_activity_targetHelps_eventMenu_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(2),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(3)
                    );
                } else {
                    sequence.targets(
                            TapTarget.forBounds(eventItemTarget, activity.getString(R.string.editor_activity_targetHelps_eventPreferences_title), activity.getString(R.string.editor_activity_targetHelps_eventPreferences_description))
                                    .transparentTarget(true)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_eventMenu_title), activity.getString(R.string.editor_activity_targetHelps_eventMenu_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(2)
                    );
                }
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //targetHelpsSequenceStarted = false;
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        //targetHelpsSequenceStarted = false;
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                //targetHelpsSequenceStarted = true;
                sequence.start();
            }
            else
            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_ORDER, true)) {
                //Log.d("EditorEventListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putBoolean(PREF_START_TARGET_HELPS_ORDER, false);
                    editor.apply();

                    final TapTargetSequence sequence = new TapTargetSequence(activity);
                    sequence.targets(
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1)
                    );
                    sequence.listener(new TapTargetSequence.Listener() {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        @Override
                        public void onSequenceFinish() {
                            //targetHelpsSequenceStarted = false;
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            //targetHelpsSequenceStarted = false;
                        }
                    });
                    sequence.continueOnCancel(true)
                            .considerOuterCircleCanceled(true);
                    //targetHelpsSequenceStarted = true;
                    sequence.start();
                }
            }
        }
    }

}
