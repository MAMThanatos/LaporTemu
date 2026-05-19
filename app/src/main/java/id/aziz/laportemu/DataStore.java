package id.aziz.laportemu;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    public static List<Barang> barangList = new ArrayList<>();
    private static final String PREF_NAME = "LaporTemuPrefs";
    private static final String KEY_BARANG_LIST = "BarangList";

    public static void saveData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(barangList);
        editor.putString(KEY_BARANG_LIST, json);
        editor.apply();
    }

    public static void loadData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_BARANG_LIST, null);
        Type type = new TypeToken<ArrayList<Barang>>() {}.getType();
        
        if (json != null) {
            barangList = gson.fromJson(json, type);
        } else {
            barangList = new ArrayList<>();
        }
    }

    public static void clearData(Context context) {
        barangList.clear();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }
}