package pl.com.gemstones.tileartist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import PEngine.Networking;

public class Settings {

    private static SharedPreferences sharedPreferences = null;

    public enum Name {
        HOLD_AND_SWIPE,
        ZOOM_OUT_ON_IMAGE_OPEN,
        SHOW_NUMBERS_ON_TILES,
        IN_SANDBOX_ADD_TILES_ENDLESSLY,
        AUTOSAVE_SANDBOX,
        FULLSCREEN
    }
    private static boolean[] defaultSettings = {true, true, false, false, true, true};

    private Activity activity;
    public Settings(Activity activity) {
        this.activity = activity;

        if(sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void start() {

        ((TextView)activity.findViewById(R.id.menu_title)).setText(R.string.menu_title_settings);
        LinearLayout ll = (LinearLayout) activity.findViewById(R.id.menu_sv_ll);
        ll.removeAllViews();
        LayoutInflater inflater = activity.getLayoutInflater();
        ll.addView(inflater.inflate(R.layout.menu_settings_layout, null));

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switch( (int)compoundButton.getTag() ){
                    case 1:
                        sharedPreferences.edit().putBoolean("menu_settings_1", compoundButton.isChecked()).apply();
                        break;
                    case 2:
                        sharedPreferences.edit().putBoolean("menu_settings_2", compoundButton.isChecked()).apply();
                        break;
                    case 3:
                        sharedPreferences.edit().putBoolean("menu_settings_3", compoundButton.isChecked()).apply();
                        break;
                    case 4:
                        sharedPreferences.edit().putBoolean("menu_settings_4", compoundButton.isChecked()).apply();
                        break;
                    case 5:
                        sharedPreferences.edit().putBoolean("menu_settings_5", compoundButton.isChecked()).apply();
                        break;
                    case 6:
                        sharedPreferences.edit().putBoolean("menu_settings_6", compoundButton.isChecked()).apply();
                        activity.recreate();
                        break;
                }
            }
        };

        ((Switch)activity.findViewById(R.id.menu_settings_1)).setTag(1);
        ((Switch)activity.findViewById(R.id.menu_settings_1)).setChecked(getSetting(Name.values()[0]));
        ((Switch)activity.findViewById(R.id.menu_settings_1)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((Switch)activity.findViewById(R.id.menu_settings_2)).setTag(2);
        ((Switch)activity.findViewById(R.id.menu_settings_2)).setChecked(getSetting(Name.values()[1]));
        ((Switch)activity.findViewById(R.id.menu_settings_2)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((Switch)activity.findViewById(R.id.menu_settings_3)).setTag(3);
        ((Switch)activity.findViewById(R.id.menu_settings_3)).setChecked(getSetting(Name.values()[2]));
        ((Switch)activity.findViewById(R.id.menu_settings_3)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((Switch)activity.findViewById(R.id.menu_settings_4)).setTag(4);
        ((Switch)activity.findViewById(R.id.menu_settings_4)).setChecked(getSetting(Name.values()[3]));
        ((Switch)activity.findViewById(R.id.menu_settings_4)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((Switch)activity.findViewById(R.id.menu_settings_5)).setTag(5);
        ((Switch)activity.findViewById(R.id.menu_settings_5)).setChecked(getSetting(Name.values()[4]));
        ((Switch)activity.findViewById(R.id.menu_settings_5)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((Switch)activity.findViewById(R.id.menu_settings_6)).setTag(6);
        ((Switch)activity.findViewById(R.id.menu_settings_6)).setChecked(getSetting(Name.values()[5]));
        ((Switch)activity.findViewById(R.id.menu_settings_6)).setOnCheckedChangeListener(onCheckedChangeListener);

        ((Button)activity.findViewById(R.id.menu_settings_b_pp)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Networking.URL_privacy_policy));
                activity.startActivity(browserIntent);
            }
        });
    }

    public static boolean getSetting(Name name)
    {
        switch(name)
        {
            case HOLD_AND_SWIPE:
                return sharedPreferences.getBoolean("menu_settings_1", defaultSettings[0]);
            case ZOOM_OUT_ON_IMAGE_OPEN:
                return sharedPreferences.getBoolean("menu_settings_2", defaultSettings[1]);
            case SHOW_NUMBERS_ON_TILES:
                return sharedPreferences.getBoolean("menu_settings_3", defaultSettings[2]);
            case IN_SANDBOX_ADD_TILES_ENDLESSLY:
                return sharedPreferences.getBoolean("menu_settings_4", defaultSettings[3]);
            case AUTOSAVE_SANDBOX:
                return sharedPreferences.getBoolean("menu_settings_5", defaultSettings[4]);
            case FULLSCREEN:
                return sharedPreferences.getBoolean("menu_settings_6", defaultSettings[5]);
        }
        return false;
    }

    public void debugAll()
    {
        Log.d("Settings", "All settings:");
        Log.d("Settings", "HOLD_AND_SWIPE = " + getSetting(Name.HOLD_AND_SWIPE));
        Log.d("Settings", "ZOOM_OUT_ON_IMAGE_OPEN = " + getSetting(Name.ZOOM_OUT_ON_IMAGE_OPEN));
        Log.d("Settings", "SHOW_NUMBERS_ON_TILES = " + getSetting(Name.SHOW_NUMBERS_ON_TILES));
        Log.d("Settings", "IN_SANDBOX_ADD_TILES_ENDLESSLY = " + getSetting(Name.IN_SANDBOX_ADD_TILES_ENDLESSLY));
        Log.d("Settings", "AUTOSAVE_SANDBOX = " + getSetting(Name.AUTOSAVE_SANDBOX));
        Log.d("Settings", "FULLSCREEN = " + getSetting(Name.FULLSCREEN));
    }
}
