package com.tarian.memorease;

import android.content.Context;
import android.content.SharedPreferences;

import com.tarian.memorease.model.Memorea;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Tommy on 8/5/2015.
 */
public class MemoreaSharedPreferences {

    private static final String MEMOREA_ORDER = "memoreaOrder";
    private static final String NOTIFICATION_TIME = "%s_notification_time";

    /**
     * Creates an ArrayList of memoreas from the shared preferences
     */
    public static Collection<Memorea> getAll(Context context) {
        final Map<String,?> allKeys = getSharedPreferences(context).getAll();
        final ArrayList<Memorea> memoreInfoCards = new ArrayList<>(allKeys.size());
        for (Map.Entry<String,?> entry : allKeys.entrySet()) {
            if (entry.getValue().getClass() == String.class) {
                final Memorea memorea = initializeMemoreaFromSharedPref(entry);
                if (memorea != null) {
                    memoreInfoCards.add(memorea);
                }
            }
        }

        return memoreInfoCards;
    }

    public static void add(Context context, Memorea addedMemorea) {
        add(context, addedMemorea.mId.toString(), addedMemorea.getFields());
    }

    public static void add(Context context, String id, String[] fields) {
        final SharedPreferences.Editor sharedPreferencesEditor =
                getSharedPreferences(context).edit();
        sharedPreferencesEditor.putString(id, getJSONStringFromArray(fields).toString());
        sharedPreferencesEditor.apply();
    }

    public static void remove(Context context, Memorea removeMemorea) {
        remove(context, removeMemorea.mId.toString());
    }

    public static void remove(Context context, String id) {
        final SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(context)
                .edit();
        sharedPreferencesEditor.remove(id);
        sharedPreferencesEditor.remove(String.format(NOTIFICATION_TIME, id));
        sharedPreferencesEditor.apply();
    }

    public static void update(Context context, Memorea updateMemorea) {
        add(context, updateMemorea.mId.toString(), updateMemorea.getFields());
    }

    public static void update(Context context, String id, String fields[]) {
        add(context, id, fields);
    }

    public static void setNotificationTime(Context context, String id, long time) {
        getSharedPreferences(context).edit().putLong(String.format(NOTIFICATION_TIME, id), time)
                .apply();
    }

    public static long getNotificationTime(Context context, String id) {
        return getSharedPreferences(context).getLong(String.format(NOTIFICATION_TIME, id), 0);
    }

    public static void setMemoreaOrder(Context context, String memoreaOrder) {
        getSharedPreferences(context).edit().putString(MEMOREA_ORDER, memoreaOrder).apply();
    }

    public static String getMemoreaOrder(Context context) {
        return getSharedPreferences(context).getString(MEMOREA_ORDER, "");
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context
                .getString(R.string.prefence_file_key), Context.MODE_PRIVATE);
    }

    /**
     * Initializes a memorea from the JSONArray entry in shared preferences<br>
     * Returns null if there is some type of error
     */
    private static Memorea initializeMemoreaFromSharedPref(final Map.Entry<String, ?> entry) {
        try {
            String[] fields = getStringArrayFromJSON(new JSONArray((String)entry.getValue()));
            if (fields != null && fields.length == 6) {
                final Memorea memorea = new Memorea(fields[0], fields[1], fields[2],
                        fields[3], Integer.parseInt(fields[4]));
                memorea.mNotificationId = Integer.parseInt(fields[5]);
                memorea.mId = UUID.fromString(entry.getKey());
                return memorea;
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a String array from a JSON array
     * @param array JSON array to convert to a String array
     */
    private static String[] getStringArrayFromJSON(final JSONArray array) {
        try {
            final String[] stringArray = new String[array.length()];
            for (int i = 0; i < array.length(); ++i) {
                    stringArray[i] = array.getString(i);
            }
            return stringArray;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a JSON array from a String array
     * @param values String array to convert to JSON
     */
    private static JSONArray getJSONStringFromArray(final String[] values) {
        final JSONArray array = new JSONArray();
        for (String value : values) {
            array.put(value);
        }
        return array;
    }
}
