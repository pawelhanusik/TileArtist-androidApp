package pl.com.gemstones.tileartist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import PEngine.DatabaseEntry;
import PEngine.GetPermission;
import PEngine.Networking;
import PEngine.SaveManager;
import PEngine.Utils;


///TailArtist game :D
public class MainActivity extends Activity {

    //Game game;
    public static boolean showAds = false;
    private static short menu_selectedID = 0;
    Networking networking;

    SharedPreferences sharedPreferences;
    public static final int GAMEACTIVITY_BACKCODE = 1337;
    public static final int GAMEACTIVITY_MULTIPLAYER_BACKCODE = 1338;
    public static final int CAMERAATIVITY_BACKCODE = 1339;

    public static boolean reload = false;
    Handler reloadHandler;
    Runnable reloadRunnable;
    Handler multiplayerFinishCheckerHandler;
    Handler multiplayerSyncHandler;
    Runnable multiplayerSyncRunnable = new Runnable() {
        @Override
        public void run() {
            if(menu_selectedID == 2){
                networking.multiplayer_check();
                multiplayerSyncHandler.postDelayed(this, 20000);
            }
        }
    };
    Settings settingsActivity;


    public static int widthInPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetPermission.init(this);
        settingsActivity = new Settings(this);

        if(Settings.getSetting(Settings.Name.FULLSCREEN)) {
            //requestWindowFeature(Window.FEATURE_NO_TITLE);
            //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        setContentView(R.layout.menu_layout);

        reloadHandler = new Handler();
        reloadRunnable = new Runnable() {
            @Override
            public void run() {
                if(reload){
                    reload = false;
                    generateSubMenu();
                }else {
                    reloadHandler.postDelayed(this, 100);
                }
            }
        };
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthInPixels = displayMetrics.widthPixels;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        networking = new Networking(this);
        if(sharedPreferences.getBoolean("isAccountSet", false)){
            networking.autologin(
                    sharedPreferences.getString("username", "Unknown"),
                    sharedPreferences.getString("password", "None")
            );
        }else{
            networking.checkAdsVisibility();
        }
        SaveManager.syncImage(this, networking);

        ///TODO: In settings submenu set email EditText's text to preferences.getString("email")

        generateMenu();

        recreateAds();
    }
    public void recreateAds() {
        if(MainActivity.showAds) {
            final AdRequest adRequest = new AdRequest.Builder().build();
            final AdView adView = (AdView) findViewById(R.id.menu_adView);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    try {
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        layoutParams.addRule(RelativeLayout.ABOVE, R.id.menu_adView);
                        ((LinearLayout) ((RelativeLayout) adView.getParent()).getChildAt(0)).setLayoutParams(layoutParams);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                }
            });
            adView.loadAd(adRequest);
        }
    }
    public void recreate()
    {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Settings.getSetting(Settings.Name.FULLSCREEN) && hasFocus) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if(Build.VERSION.SDK_INT >= 19) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        SaveManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    ///===MENU======================================================================================
    public void generateMenu()
    {
        //generate menu 2.0
        final int imgsPerRow = 3;
        final int padding = 5;

        //static stuff
        LinearLayout header = (LinearLayout)findViewById(R.id.menu_header);//new LinearLayout(this);
        header.removeAllViews();

        ImageView imgv1 = new ImageView(this);
        imgv1.setAdjustViewBounds(true);
        imgv1.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgv1.setCropToPadding(true);
        imgv1.setPadding(padding, padding, padding, padding);
        imgv1.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f / imgsPerRow
        ));
        imgv1.setBackground(getResources().getDrawable((menu_selectedID==0) ? R.drawable.background_selected : R.drawable.background));
        imgv1.setImageResource(R.drawable.menu_header_images);
        imgv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
                gameIntent.putExtra("isSandbox", true);
                gameIntent.putExtra("imgID", -1L );
                startActivity(gameIntent);*/
                handleBgChange(view);
                generateMenuImages();
            }
        });
        //===============================================
        ImageView imgv2 = new ImageView(this);
        imgv2.setAdjustViewBounds(true);
        imgv2.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgv2.setCropToPadding(true);
        imgv2.setPadding(padding, padding, padding, padding);
        imgv2.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f / imgsPerRow
        ));
        imgv2.setBackground(getResources().getDrawable((menu_selectedID==1) ? R.drawable.background_selected : R.drawable.background));
        imgv2.setImageResource(R.drawable.menu_header_sandbox);
        imgv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleBgChange(view);
                generateMenuSandbox();
            }
        });
        //=================================================
        ImageView imgv3 = new ImageView(this);
        imgv3.setAdjustViewBounds(true);
        imgv3.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgv3.setCropToPadding(true);
        imgv3.setPadding(padding, padding, padding, padding);
        imgv3.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f / imgsPerRow
        ));
        imgv3.setBackground(getResources().getDrawable((menu_selectedID==2) ? R.drawable.background_selected : R.drawable.background));
        imgv3.setImageResource(R.drawable.menu_header_multiplayer);
        imgv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleBgChange(view);
                generateMenuMultiplayer();
            }
        });
        //===================================================
        ImageView imgv4 = new ImageView(this);
        imgv4.setAdjustViewBounds(true);
        imgv4.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgv4.setCropToPadding(true);
        imgv4.setPadding(padding, padding, padding, padding);
        imgv4.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f / imgsPerRow
        ));
        imgv4.setBackground(getResources().getDrawable((menu_selectedID==3) ? R.drawable.background_selected : R.drawable.background));
        imgv4.setImageResource(R.drawable.menu_header_account);
        imgv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleBgChange(view);
                generateMenuAccount();
            }
        });
        //===================================================
        ImageView imgv5 = new ImageView(this);
        imgv5.setAdjustViewBounds(true);
        imgv5.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgv5.setCropToPadding(true);
        imgv5.setPadding(padding, padding, padding, padding);
        imgv5.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f / imgsPerRow
        ));
        imgv5.setBackground(getResources().getDrawable((menu_selectedID==4) ? R.drawable.background_selected : R.drawable.background));
        imgv5.setImageResource(R.drawable.menu_header_settings);
        imgv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleBgChange(view);
                generateMenuSettings();
            }
        });

        header.addView(imgv1);
        header.addView(imgv2);
        header.addView(imgv3);
        header.addView(imgv4);
        header.addView(imgv5);

        generateSubMenu();
    }
    private void handleBgChange(View view)
    {
        //Changes previus selecred BG to non selected and view to selected
        //USE BEFOR CHANGE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        ((LinearLayout)findViewById(R.id.menu_header)).getChildAt(menu_selectedID).setBackground(getResources().getDrawable(R.drawable.background));
        view.setBackground(getResources().getDrawable(R.drawable.background_selected));
    }
    private void generateSubMenu()
    {
        switch (menu_selectedID){
            case 0:
                generateMenuImages();
                break;
            case 1:
                generateMenuSandbox();
                break;
            case 2:
                generateMenuMultiplayer();
                break;
            case 3:
                generateMenuAccount();
                break;
            case 4:
                generateMenuSettings();
        }
    }
    private void generateMenuImages()
    {
        menu_selectedID = 0;

        final int imgsPerRow = 3;
        final int padding = 15;
        LinearLayout ll = (LinearLayout) findViewById(R.id.menu_sv_ll);
        ll.removeAllViews();

        //IMAGES
        ((TextView)findViewById(R.id.menu_title)).setText(R.string.menu_title_images);
        Map<String, Vector<Long>> menuEntries = new HashMap<>();

        /*for(String category : SaveManager.getAllSavedCategories_images(this)){
            for(DatabaseEntry dbEntry : SaveManager.getSavedEntries_images(this, category)){
                //Add new menuEntry
                if(!menuEntries.containsKey(category)){
                    menuEntries.put(category, new Vector<Long>());
                }
                menuEntries.get(category).add(dbEntry.id);
            }
        }*/
        String category_tmp = "Uncategorized";
        menuEntries.put(category_tmp, new Vector<Long>());
        for(long l : SaveManager.getAllSaved_images(this)){
            menuEntries.get(category_tmp).add(l);
            Log.d("MA gesSubmenu()", "adding entry with imageID=" + l);
        }


        for(String category : menuEntries.keySet()) {
            /*TextView category_tv = new TextView(this);
            category_tv.setText(category);
            category_tv.setTextSize(24.f);
            category_tv.setTypeface(null, Typeface.BOLD);
            ll.addView(category_tv);*/

            for (int i = 0; i < menuEntries.get(category).size(); i += imgsPerRow) {
                LinearLayout newRow = new LinearLayout(this);
                newRow.setOrientation(LinearLayout.HORIZONTAL);
                newRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                for (int j = 0; j < imgsPerRow; ++j) {
                    ImageView imgv = new ImageView(this);

                    imgv.setAdjustViewBounds(true);
                    imgv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imgv.setCropToPadding(true);
                    imgv.setPadding(padding, padding, padding, padding);
                    imgv.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.f / imgsPerRow
                    ));

                    if(menuEntries.get(category).size() <= i+j){
                        imgv.setImageBitmap(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
                    }else{
                        long imgID =  menuEntries.get(category).get(i+j);
                        imgv.setBackground(getResources().getDrawable(R.drawable.background));
                        imgv.setTag(imgID);

                        if(SaveManager.was_autosaved(this, imgID)){
                            imgv.setImageBitmap(
                                    Utils.upscale(
                                    Utils.join(
                                            Utils.toGrayscale(SaveManager.load_image(this, imgID)) ,
                                            SaveManager.load_imageAutosave(this, imgID)
                                    ), widthInPixels/imgsPerRow
                                    )
                            );
                        }else {
                            imgv.setImageBitmap(Utils.upscale(Utils.toGrayscale( SaveManager.load_image(this, imgID) ), widthInPixels/imgsPerRow));
                        }
                        imgv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showContinueImageDialog( (long)view.getTag() );
                            }
                        });
                        imgv.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
                                gameIntent.putExtra("isSandbox", false);
                                gameIntent.putExtra("imgID", (long)view.getTag() );
                                //startActivity(gameIntent);
                                startActivityForResult(gameIntent, GAMEACTIVITY_BACKCODE);
                                return false;
                            }
                        });
                    }

                    newRow.addView(imgv);
                }

                ll.addView(newRow);
            }
        }
    }
    private void generateMenuSandbox()
    {
        menu_selectedID = 1;

        final int imgsPerRow = 3;
        final int padding = 15;
        LinearLayout ll = (LinearLayout) findViewById(R.id.menu_sv_ll);
        ll.removeAllViews();

        LinearLayout topLayer = new LinearLayout(this);
        ImageView imgv1 = new ImageView(this);
        imgv1.setAdjustViewBounds(true);
        imgv1.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgv1.setCropToPadding(true);
        imgv1.setPadding(padding, padding, padding, padding);
        imgv1.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f / imgsPerRow
        ));
        imgv1.setBackground(getResources().getDrawable(R.drawable.background));
        imgv1.setImageResource(R.drawable.menu_header_sandbox_add);
        imgv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
                gameIntent.putExtra("isSandbox", true);
                gameIntent.putExtra("imgID", -1L );
                //startActivity(gameIntent);
                startActivityForResult(gameIntent, GAMEACTIVITY_BACKCODE);
            }
        });

        ImageView imgv2 = new ImageView(this);
        imgv2.setAdjustViewBounds(true);
        imgv2.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgv2.setCropToPadding(true);
        imgv2.setPadding(padding, padding, padding, padding);
        imgv2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,1.f / imgsPerRow));
        imgv2.setBackground(getResources().getDrawable(R.drawable.background));
        imgv2.setImageResource(R.drawable.menu_header_sandbox_camera);
        imgv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameIntent = new Intent(getBaseContext() ,CameraActivity.class);
                //startActivity(gameIntent);
                startActivityForResult(gameIntent, CAMERAATIVITY_BACKCODE);
            }
        });
        ImageView imgv3 = new ImageView(this);
        imgv3.setAdjustViewBounds(true);
        imgv3.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgv3.setCropToPadding(true);
        imgv3.setPadding(padding, padding, padding, padding);
        imgv3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,1.f / imgsPerRow));

        topLayer.addView(imgv1);
        topLayer.addView(imgv2);
        topLayer.addView(imgv3);

        ll.addView(topLayer);

        //SANDBOX
        ((TextView)findViewById(R.id.menu_title)).setText(R.string.menu_title_sandbox);
        Map<String, Vector<Long>> menuEntries = new HashMap<>();

        /*for(String category : SaveManager.getAllSavedCategories_sandbox(this)){
            for(DatabaseEntry dbEntry : SaveManager.getSavedEntries_sandbox(this, category)){
                //Add new menuEntry
                if(!menuEntries.containsKey(category)){
                    menuEntries.put(category, new Vector<Long>());
                }
                menuEntries.get(category).add(dbEntry.id);
            }
        }*/
        String category_tmp = "Uncategorized";
        menuEntries.put(category_tmp, new Vector<Long>());
        for(long l : SaveManager.getAllSaved_sandboxes(this)){
            menuEntries.get(category_tmp).add(l);
        }

        for(String category : menuEntries.keySet()) {
            TextView category_tv = new TextView(this);
            //category_tv.setText(category);
            category_tv.setTextSize(24.f);
            category_tv.setTypeface(null, Typeface.BOLD);
            ll.addView(category_tv);

            for (int i = 0; i < menuEntries.get(category).size(); i += imgsPerRow) {
                LinearLayout newRow = new LinearLayout(this);
                newRow.setOrientation(LinearLayout.HORIZONTAL);
                newRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                for (int j = 0; j < imgsPerRow; ++j) {
                    ImageView imgv = new ImageView(this);

                    imgv.setAdjustViewBounds(true);
                    imgv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imgv.setCropToPadding(true);
                    imgv.setPadding(padding, padding, padding, padding);
                    imgv.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.f / imgsPerRow
                    ));

                    if(menuEntries.get(category).size() <= i+j){
                        imgv.setImageBitmap(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
                    }else{
                        long imgID =  menuEntries.get(category).get(i+j);
                        imgv.setBackground(getResources().getDrawable(R.drawable.background));
                        imgv.setTag(imgID);


                        imgv.setImageBitmap(
                                Utils.upscale(
                                    SaveManager.load_sandbox(this, imgID)
                                , 250)
                        );

                        imgv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showSandboxMenuDialog( (long)view.getTag() );
                            }
                        });
                        imgv.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
                                gameIntent.putExtra("isSandbox", true);
                                gameIntent.putExtra("imgID", (long)view.getTag() );
                                //startActivity(gameIntent);
                                startActivityForResult(gameIntent, GAMEACTIVITY_BACKCODE);
                                return false;
                            }
                        });
                    }

                    newRow.addView(imgv);
                }

                ll.addView(newRow);
            }
        }
    }
    private void generateMenuMultiplayer()
    {
        menu_selectedID = 2;

        LinearLayout ll = (LinearLayout) findViewById(R.id.menu_sv_ll);
        ll.removeAllViews();
        ((TextView)findViewById(R.id.menu_title)).setText(R.string.menu_title_multiplayer);

        LayoutInflater inflater = this.getLayoutInflater();
        View submenuLayout = inflater.inflate(R.layout.menu_multiplayer_layout, null);

        ll.addView(submenuLayout);

        multiplayerSyncHandler = new Handler();
        multiplayerSyncHandler.post(multiplayerSyncRunnable);
    }

    private void generateMenuAccount()
    {
        menu_selectedID = 3;

        LinearLayout ll = (LinearLayout) findViewById(R.id.menu_sv_ll);
        ll.removeAllViews();
        ((TextView)findViewById(R.id.menu_title)).setText(R.string.menu_title_account);

        LayoutInflater inflater = this.getLayoutInflater();
        if( !sharedPreferences.getBoolean("isAccountSet", false) ) {
            ll.addView(inflater.inflate(R.layout.menu_account_login_layout, null));
        }else if( !networking.autologgedin ){
            ll.addView(inflater.inflate(R.layout.menu_account_login_layout, null));
            ((TextView)findViewById(R.id.menu_account_username)).setText(
                    sharedPreferences.getString("username", "Unknown")
            );
            ((TextView)findViewById(R.id.menu_account_password)).setText(
                    sharedPreferences.getString("password", "None")
            );
            onMenuAccountButtonSelectionLoginClick(null);
        }else{
            ll.addView(inflater.inflate(R.layout.menu_account_layout, null));
            ((TextView)findViewById(R.id.menu_account_loggedin_username)).setText(
                    sharedPreferences.getString("username", "Unknown")
            );
            networking.profileMine();
        }

    }
    private void generateMenuSettings()
    {
        menu_selectedID = 4;

        /*LinearLayout ll = (LinearLayout) findViewById(R.id.menu_sv_ll);
        ll.removeAllViews();
        ((TextView)findViewById(R.id.menu_title)).setText(R.string.menu_title_settings);

        TextView msg = new TextView(this);
        msg.setText(R.string.feature_not_avaible_yet);
        msg.setTextSize(30);

        ll.addView(msg);*/

        settingsActivity.start();
    }
    ///===ON=CLICKS==============================================
    public void onMenuAccountButtonSelectionLoginClick(View view) {
        ((EditText)findViewById(R.id.menu_account_password)).setVisibility(View.VISIBLE);

        ((LinearLayout)findViewById(R.id.menu_account_selectionLL)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.menu_account_button_selection_continue_without)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.menu_account_button_login)).setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.menu_account_button_back)).setVisibility(View.VISIBLE);

        ((TextView)findViewById(R.id.menu_account_title)).setText(R.string.account_login_title);
        ((TextView)findViewById(R.id.menu_account_title)).setVisibility(View.VISIBLE);
    }
    public void onMenuAccountButtonSelectionRegisterClick(View view) {
        ((EditText)findViewById(R.id.menu_account_password)).setVisibility(View.VISIBLE);
        ((EditText)findViewById(R.id.menu_account_password2)).setVisibility(View.VISIBLE);

        ((LinearLayout)findViewById(R.id.menu_account_selectionLL)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.menu_account_button_selection_continue_without)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.menu_account_button_register)).setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.menu_account_button_back)).setVisibility(View.VISIBLE);

        ((TextView)findViewById(R.id.menu_account_title)).setText(R.string.account_register_title);
        ((TextView)findViewById(R.id.menu_account_title)).setVisibility(View.VISIBLE);
    }
    public void onMenuAccountButtonSelectionContinueWithoutClick(View view) {
        AlertDialog dialog_continueWithout;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.account_dialog_continue_without_title)
                .setPositiveButton(R.string.account_dialog_continue_without_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        onMenuContinueWithoutAccount();
                    }
                })
                .setNegativeButton(R.string.account_dialog_continue_without_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        onMenuAccountButtonSelectionRegisterClick(null);
                    }
                })
                .setMessage(R.string.account_dialog_continue_without_message)
        ;
        dialog_continueWithout = builder.create();
        dialog_continueWithout.show();
    }
    private void onMenuContinueWithoutAccount()
    {
        if(((EditText)findViewById(R.id.menu_account_username)).getText().toString().equals("")){
            Toast.makeText(this, R.string.account_empty_username, Toast.LENGTH_SHORT).show();
            return;
        }

        generateLoggedInSubmenu();
    }

    public void onMenuAccountButtonBackClick(View view) {
        ((EditText)findViewById(R.id.menu_account_password)).setVisibility(View.GONE);
        ((EditText)findViewById(R.id.menu_account_password2)).setVisibility(View.GONE);

        ((LinearLayout)findViewById(R.id.menu_account_selectionLL)).setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.menu_account_button_selection_continue_without)).setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.menu_account_button_login)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.menu_account_button_register)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.menu_account_button_back)).setVisibility(View.GONE);

        ((TextView)findViewById(R.id.menu_account_title)).setVisibility(View.GONE);
    }
    public void onMenuAccountButtonLoginClick(View view) {
        String username = ((EditText)findViewById(R.id.menu_account_username)).getText().toString();
        String password = ((EditText)findViewById(R.id.menu_account_password)).getText().toString();

        if(username.equals("")){
            Toast.makeText(this, this.getResources().getText(R.string.account_empty_username), Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.equals("")){
            Toast.makeText(this, this.getResources().getText(R.string.account_empty_password), Toast.LENGTH_SHORT).show();
            return;
        }

        networking.login(username, password);
    }
    public void onMenuAccountButtonRegisterClick(View view) {
        String username = ((EditText)findViewById(R.id.menu_account_username)).getText().toString();
        String password = ((EditText)findViewById(R.id.menu_account_password)).getText().toString();
        String password2 = ((EditText)findViewById(R.id.menu_account_password2)).getText().toString();

        if(username.equals("")){
            Toast.makeText(this, this.getResources().getText(R.string.account_empty_username), Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.equals("")){
            Toast.makeText(this, this.getResources().getText(R.string.account_empty_password), Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.equals(password2)){
            networking.register(username, password);
        }else{
            Toast.makeText(this, R.string.account_register_password_missmatch, Toast.LENGTH_SHORT).show();
        }
    }

    public void onMenuAccountLogoutClick(View view) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean("isAccountSet", false);
        sharedPreferencesEditor.putString("username","Unknown");
        sharedPreferencesEditor.putString("password","none");
        sharedPreferencesEditor.apply();

        networking.logout();

        generateMenuAccount();
    }

    public void generateLoggedInSubmenu()
    {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean("isAccountSet", true);
        sharedPreferencesEditor.putString("username",
                ((EditText)findViewById(R.id.menu_account_username)).getText().toString()
        );
        sharedPreferencesEditor.putString("password",
                ((EditText)findViewById(R.id.menu_account_password)).getText().toString()
        );
        sharedPreferencesEditor.apply();

        //refresh submenu
        generateMenuAccount();
    }

    public void onMenuAccountFollowsActivatorClick(View view) {
        if(view.getTag() == null){
            view.setTag(false);
        }

        if( (boolean)view.getTag() ){
            findViewById(R.id.menu_account_loggedin_search_et).setVisibility(View.GONE);
            findViewById(R.id.menu_account_loggedin_search_button).setVisibility(View.GONE);
            findViewById(R.id.menu_account_loggedin_search_sv).setVisibility(View.GONE);
            findViewById(R.id.menu_account_loggedin_follows_sv).setVisibility(View.GONE);

            findViewById(R.id.menu_account_loggedin_profile_sv).setVisibility(View.VISIBLE);
            networking.profileMine();
            ((ImageView) findViewById(R.id.menu_account_loggedin_search_activator)).setImageResource(R.drawable.account_search);
            ((ImageView) findViewById(R.id.menu_account_loggedin_follows_activator)).setImageResource(R.drawable.account_follows);

            view.setTag(false);
            findViewById(R.id.menu_account_loggedin_search_activator).setTag(false);
        }else {
            findViewById(R.id.menu_account_loggedin_follows_sv).setVisibility(View.VISIBLE);
            networking.getFollows();

            findViewById(R.id.menu_account_loggedin_search_sv).setVisibility(View.GONE);
            findViewById(R.id.menu_account_loggedin_profile_sv).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.menu_account_loggedin_search_activator)).setImageResource(R.drawable.account_search);
            ((ImageView) findViewById(R.id.menu_account_loggedin_follows_activator)).setImageResource(R.drawable.account_search2);

            view.setTag(true);
            findViewById(R.id.menu_account_loggedin_search_activator).setTag(false);
        }
    }
    public void onMenuAccountSearchActivatorClick(View view) {
        if(view.getTag() == null){
            view.setTag(false);
        }

        if( (boolean)view.getTag() ){
            findViewById(R.id.menu_account_loggedin_search_et).setVisibility(View.GONE);
            findViewById(R.id.menu_account_loggedin_search_button).setVisibility(View.GONE);
            findViewById(R.id.menu_account_loggedin_search_sv).setVisibility(View.GONE);
            findViewById(R.id.menu_account_loggedin_follows_sv).setVisibility(View.GONE);

            findViewById(R.id.menu_account_loggedin_profile_sv).setVisibility(View.VISIBLE);
            networking.profileMine();
            ((ImageView) findViewById(R.id.menu_account_loggedin_search_activator)).setImageResource(R.drawable.account_search);
            ((ImageView) findViewById(R.id.menu_account_loggedin_follows_activator)).setImageResource(R.drawable.account_follows);

            view.setTag(false);
            findViewById(R.id.menu_account_loggedin_follows_activator).setTag(false);
        }else {
            findViewById(R.id.menu_account_loggedin_search_et).setVisibility(View.VISIBLE);
            findViewById(R.id.menu_account_loggedin_search_button).setVisibility(View.VISIBLE);
            findViewById(R.id.menu_account_loggedin_search_sv).setVisibility(View.VISIBLE);
            networking.searchFriend("");

            findViewById(R.id.menu_account_loggedin_follows_sv).setVisibility(View.GONE);
            findViewById(R.id.menu_account_loggedin_profile_sv).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.menu_account_loggedin_follows_activator)).setImageResource(R.drawable.account_follows);
            ((ImageView) findViewById(R.id.menu_account_loggedin_search_activator)).setImageResource(R.drawable.account_search2);

            view.setTag(true);
            findViewById(R.id.menu_account_loggedin_follows_activator).setTag(false);
        }
    }
    public void onMenuAccountSearchButtonClick(View view) {
        String searchUsername = ((EditText)findViewById(R.id.menu_account_loggedin_search_et)).getText().toString();
        networking.searchFriend(searchUsername);
    }
    public void onMenuAccountProfileBackClick(View view) {
        findViewById(R.id.menu_account_loggedin_profile_sv).setVisibility(View.GONE);

        findViewById(R.id.menu_account_loggedin_search_button).setVisibility(View.VISIBLE);
        findViewById(R.id.menu_account_loggedin_search_et).setVisibility(View.VISIBLE);
        findViewById(R.id.menu_account_loggedin_search_sv).setVisibility(View.VISIBLE);
    }
    public void onMenuAccountFriendSearchCompleted(String data){
        LinearLayout searchResults = (LinearLayout)findViewById(R.id.menu_account_loggedin_search_sv);
        searchResults.removeAllViews();

        String[] positions = data.split("\n");
        for(String p : positions)
        {
            int commaPos = p.indexOf(';');
            long pUserID = -1;
            try {
                pUserID = Long.parseLong(p.substring(0, commaPos));
            }catch (Exception e){
                e.printStackTrace();
                Log.e("MainActivity", "onMenuAccountFriendsSearchCompleted: " + "error reading searchedUser ID.");
            }
            String pUserName = p.substring(commaPos+1);

            LinearLayout searchEntry = new LinearLayout(this);
            Button username_b = new Button(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = Utils.dp2px(this, 5);
            layoutParams.setMargins(margin, margin, margin, margin);
            username_b.setLayoutParams(layoutParams);
            username_b.setBackgroundResource(R.drawable.background);
            username_b.setTag(pUserID);
            username_b.setAllCaps(false);
            username_b.setText(pUserName);
            username_b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    networking.profileFriend((long)view.getTag());
                }
            });

            searchEntry.addView(username_b);

            searchResults.addView(searchEntry);
        }
    }
    public void onMenuAccountFollowsGot(final String data[]) {
        final Context context = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LinearLayout follows_sv_ll = (LinearLayout) findViewById(R.id.menu_account_loggedin_follows_sv);
                    follows_sv_ll.removeAllViews();
                    for (String row : data) {
                        long followedID = -1;
                        try {
                            followedID = Long.parseLong(row.split(";")[0]);
                        } catch (NumberFormatException e) {
                        }
                        String followedUsername = row.split(";")[1];

                        Button username_b = new Button(context);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        int margin = Utils.dp2px(context, 5);
                        layoutParams.setMargins(margin, margin, margin, margin);
                        username_b.setLayoutParams(layoutParams);
                        username_b.setBackgroundResource(R.drawable.background);
                        username_b.setTag(followedID);
                        username_b.setAllCaps(false);
                        username_b.setText(followedUsername);
                        username_b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                networking.profileFriend((long) view.getTag());
                            }
                        });

                        follows_sv_ll.addView(username_b);
                    }
                }catch (NullPointerException e){
                    //e.printStackTrace();
                }
            }
        });
    }
    public void onMenuAccountFriendProfileGot(final String data[]) {
        final LayoutInflater inflater = this.getLayoutInflater();
        final Context context = this;
        final int padding = 15;
        final int imgsPerRow = 3;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    findViewById(R.id.menu_account_loggedin_search_button).setVisibility(View.GONE);
                    findViewById(R.id.menu_account_loggedin_search_et).setVisibility(View.GONE);
                    findViewById(R.id.menu_account_loggedin_search_sv).setVisibility(View.GONE);

                    View profileView = inflater.inflate(R.layout.menu_account_search_profile_layout, null);
                    LinearLayout parentView = (LinearLayout) findViewById(R.id.menu_account_loggedin_profile_sv);
                    parentView.setVisibility(View.VISIBLE);
                    parentView.removeAllViews();
                    parentView.addView(profileView);

                    long userID_tmp = -1;
                    try {
                        userID_tmp = Long.parseLong(data[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final long userID = userID_tmp;
                    final String username = data[1];
                    final boolean following = !data[2].equals("0");

                    //Don't show follow button if viewing your own profile
                    if (data[2].equals("2")) {
                        profileView.findViewById(R.id.profile_back_and_follow_buttons_ll).setVisibility(View.GONE);
                        profileView.findViewById(R.id.profile_username).setVisibility(View.GONE);
                    }

                    ((TextView) profileView.findViewById(R.id.profile_username)).setText(username + (following ? "    following <3" : ""));
                    ((Button) profileView.findViewById(R.id.profile_follow_button)).setText(following ? "-> unfollow <-" : "-> follow <-");
                    ((Button) profileView.findViewById(R.id.profile_follow_button)).setTag(userID);
                    if (!following) {
                        profileView.findViewById(R.id.profile_follow_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                networking.addFriend(userID);
                            }
                        });
                    } else {
                        profileView.findViewById(R.id.profile_follow_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                networking.removeFriend(userID);
                            }
                        });
                    }

                    LinearLayout ll = (LinearLayout) profileView.findViewById(R.id.profile_sv_ll);
                    ll.removeAllViews();

                    int index = 3;
                    int entryID = 0;
                    LinearLayout row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    while (index + 3 < data.length) {
                        long imageID = -1;
                        try {
                            imageID = Long.parseLong(data[index++]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final String imageTitle = data[index++];
                        String imageCategory = data[index++];

                        LinearLayout masterpieceEntry = new LinearLayout(context);
                        masterpieceEntry.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1.f / imgsPerRow
                        ));
                        masterpieceEntry.setOrientation(LinearLayout.VERTICAL);
                        masterpieceEntry.setBackgroundResource(R.drawable.background);
                        masterpieceEntry.setTag(imageID);
                        TextView tv_title = new TextView(context);
                        tv_title.setTextSize(18);
                        tv_title.setPadding(padding, padding, padding, padding);
                        tv_title.setText(imageTitle);
                    /*TextView tv_category = new TextView(context);
                    tv_category.setTextSize(16);
                    tv_category.setPadding(padding, padding, padding, padding);
                    tv_category.setText(imageCategory);*/
                        final ImageView tv_image = new ImageView(context);
                        tv_image.setAdjustViewBounds(true);
                        tv_image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        tv_image.setCropToPadding(true);
                        tv_image.setPadding(padding, padding, padding, padding);
                        tv_image.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                        ));
                        tv_image.setTag("ImageView");
                        tv_image.setImageBitmap(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
                        if (data[2].equals("2")) {
                            final long tmpImageID = imageID;
                            tv_image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bitmap bitmap;
                                    try {
                                        bitmap = ((BitmapDrawable) tv_image.getDrawable()).getBitmap();
                                    }catch (ClassCastException e){
                                        bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
                                        e.printStackTrace();
                                    }
                                    showLocalProfileMenuDialog(tmpImageID, imageTitle, bitmap);
                                }
                            });
                        }
                        Space space = new Space(context);
                        space.setMinimumHeight(5);

                        masterpieceEntry.addView(tv_title);
                        //masterpieceEntry.addView(tv_category);
                        masterpieceEntry.addView(tv_image);
                        masterpieceEntry.addView(space);

                        row.addView(masterpieceEntry);
                        if (++entryID >= imgsPerRow) {
                            ll.addView(row);
                            row = new LinearLayout(context);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            entryID = 0;
                        }


                        networking.getProfileImageBitmap(imageID);

                        //skip "=====" string
                        index++;
                    }
                    while (entryID != 0) {
                        LinearLayout masterpieceEntry = new LinearLayout(context);
                        TextView tv_title = new TextView(context);
                        tv_title.setText("");
                        TextView tv_category = new TextView(context);
                        tv_category.setText("");
                        ImageView tv_image = new ImageView(context);
                        tv_image.setAdjustViewBounds(true);
                        tv_image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        tv_image.setCropToPadding(true);
                        tv_image.setPadding(padding, padding, padding, padding);
                        tv_image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.f / imgsPerRow));
                        tv_image.setImageBitmap(Bitmap.createBitmap(widthInPixels / imgsPerRow, widthInPixels / imgsPerRow, Bitmap.Config.ARGB_8888));
                        Space space = new Space(context);
                        space.setMinimumHeight(5);

                        masterpieceEntry.addView(tv_title);
                        masterpieceEntry.addView(tv_category);
                        masterpieceEntry.addView(tv_image);
                        masterpieceEntry.addView(space);

                        row.addView(masterpieceEntry);
                        if (++entryID >= imgsPerRow) {
                            ll.addView(row);
                            row = new LinearLayout(context);
                            entryID = 0;
                        }
                    }
                }catch (NullPointerException e){
                    //e.printStackTrace();
                }
            }
        });
    }
    public void onMenuAccountProfileBitmapReceived(final long imageID, final Bitmap bitmap) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.profile_sv_ll);
                    View entry;
                    for (int i = 0; i < ll.getChildCount(); ++i) {
                        if ((entry = ll.getChildAt(i).findViewWithTag(imageID)) != null) {
                            ((ImageView) entry.findViewWithTag("ImageView")).setImageBitmap(
                                    Utils.upscale(Utils.toGrayscale(bitmap), widthInPixels / 3)
                            );
                            break;
                        }
                    }
                    //((ImageView)ll.findViewWithTag(imageID)).setImageBitmap(bitmap);
                }catch (NullPointerException e){
                    //e.printStackTrace();
                }
            }
        });
    }
    public void onMenuAccountFriendAddSuccessfullyCompleted() {
        ((Button)findViewById(R.id.profile_follow_button)).setText("-> unfollow <-");
        findViewById(R.id.profile_follow_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                networking.removeFriend((long)view.getTag());
            }
        });
        SaveManager.syncImage(this, networking);
    }
    public void onMenuAccountFriendRemoveSuccessfullyCompleted() {
        ((Button)findViewById(R.id.profile_follow_button)).setText("-> follow <-");
        findViewById(R.id.profile_follow_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                networking.addFriend((long)view.getTag());
            }
        });
    }
    ///=====MULTIPLAYER===ONCLICKS========================
    public void onMenuMultiplayerCreate(String opponentsName, long imageID) {
        networking.multiplayer_init(opponentsName, imageID);
    }

    public void onMenuMultiplayerGameNetworkingCreated() {

    }
    public void onMenuMultiplayerGameNetworkingStarted(long imageID) {
        Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
        gameIntent.putExtra("isSandbox", false);
        gameIntent.putExtra("isMultiplayer", true);
        gameIntent.putExtra("imgID", imageID );
        startActivityForResult(gameIntent, GAMEACTIVITY_MULTIPLAYER_BACKCODE);


        /*multiplayerFinishCheckerHandler = new Handler();
        Runnable multiplayerFinishCheckerRunnable = new Runnable() {
            @Override
            public void run() {
                if(Tile.tiles_left == 0){
                    Log.d("GameActivity", "Multiplayer Finished!!!");
                    networking.multiplayer_finish();
                }else{
                    multiplayerFinishCheckerHandler.postDelayed(this, 1);
                }
            }
        };
        multiplayerFinishCheckerHandler.postDelayed(multiplayerFinishCheckerRunnable, 100);*/
    }
    public void onMenuMultiplayerGameNetworkingFinished() {
        //showMultiplayerGameFinishedDialog();
        if(menu_selectedID == 2) {
            networking.multiplayer_check();
        }
    }
    public void onMenuMultiplayerGameNetworkingChecked(String[] raw_data) {
        final String localUsername = sharedPreferences.getString("username", "None");
        //change data in a way, that unfinished games are showing first
        Vector<String> dataV = new Vector<>(raw_data.length);
        int count_unfinished = 0;
        int count_waiting = 0;
        for(String s : raw_data){
            String[] splitted = s.split(";");
            /*final long gameID = Long.parseLong(splitted[0]);
            final long imageID = Long.parseLong(splitted[1]);*/
            final String player1_name = splitted[2];
            final String player2_name = splitted[3];
            int player1_time = 0;
            int player2_time = 0;
            try{
                player1_time = Integer.parseInt(splitted[4]);
            }catch (Exception e){
            }
            try{
                player2_time = Integer.parseInt(splitted[5]);
            }catch (Exception e){
            }

            if(player1_time > 0 && player2_time > 0){
                dataV.add(count_unfinished + count_waiting, s);
                Log.d("MA", "Adding finished");
            }else if( (player1_name.equals(localUsername) && player1_time > 0)
                    || (player2_name.equals(localUsername) && player2_time > 0)
                    ){
                dataV.add(count_unfinished, s);
                count_waiting++;
                Log.d("MA", "Adding waiting at " + count_unfinished);
            }else{
                dataV.add(0, s);
                count_unfinished++;
                Log.d("MA", "Adding unfinished");
            }
        }
        final String[] data = dataV.toArray(new String[raw_data.length]);

        //process data
        final Context context = this;
        final LayoutInflater inflater = this.getLayoutInflater();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int imgsPerRow = 2;
                    LinearLayout gamesScrollLayout = (LinearLayout) findViewById(R.id.menu_multiplayer_games_scroll);
                    gamesScrollLayout.removeAllViews();
                    LinearLayout row = new LinearLayout(context);
                    int rowItemID = 0;
                    LinearLayout createGame = (LinearLayout) inflater.inflate(R.layout.menu_multiplayer_game_entry, null);
                    createGame.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f / imgsPerRow));
                    ((ImageView) createGame.findViewById(R.id.menu_multiplayer_game_entry_img)).setImageBitmap(
                            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.menu_header_sandbox_add), widthInPixels / imgsPerRow, widthInPixels / imgsPerRow, false)
                    );
                    ((ImageView) createGame.findViewById(R.id.menu_multiplayer_game_entry_img)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            generateMenuMultiplayerCreateGameDialog();
                        }
                    });
                    ((TextView) createGame.findViewById(R.id.menu_multiplayer_game_entry_text)).setText("Create a game.");
                    row.addView(createGame);
                    rowItemID++;
                    for (String game : data) {
                        String[] splitted = game.split(";");
                        if (splitted.length < 4) {
                            Log.w("MainActivity", "onMenuMultiplayerGameNetworkingChecked(): dataLine contains too less information to be processed.");
                            continue;
                        }
                        final long gameID = Long.parseLong(splitted[0]);
                        final long imageID = Long.parseLong(splitted[1]);
                        final String player1_name = splitted[2];
                        final String player2_name = splitted[3];
                        int player1_time = 0;
                        int player2_time = 0;
                        try {
                            player1_time = Integer.parseInt(splitted[4]);
                        } catch (Exception e) {
                        }
                        try {
                            player2_time = Integer.parseInt(splitted[5]);
                        } catch (Exception e) {
                        }
                        final LinearLayout gameEntry = (LinearLayout) inflater.inflate(R.layout.menu_multiplayer_game_entry, null);
                        gameEntry.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f / imgsPerRow));
                        if (player1_time <= 0 && player2_time <= 0) {
                            ((ImageView) gameEntry.findViewById(R.id.menu_multiplayer_game_entry_img)).setImageBitmap(
                                    Utils.upscale(Utils.toGrayscale(SaveManager.load_image(context, imageID)), widthInPixels / imgsPerRow)
                            );
                        } else {
                            ((ImageView) gameEntry.findViewById(R.id.menu_multiplayer_game_entry_img)).setImageBitmap(
                                    Utils.upscale(SaveManager.load_image(context, imageID), widthInPixels / imgsPerRow)
                            );
                        }

                    /*((ImageView)gameEntry.findViewById(R.id.menu_multiplayer_game_entry_img)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //networking.multiplayer_start(gameID);
                            shotMultiplayerMenuEntryClickedDialog(gameEntry);
                        }
                    });*/
                        android.text.Spanned tv_text;
                        if (player1_time <= 0 || player2_time <= 0) {
                            tv_text = Html.fromHtml(player1_name + " VS " + player2_name);
                        } else if (player1_time < player2_time) {
                            tv_text = Html.fromHtml("<b>" + player1_name + "</b>" + " VS " + player2_name);
                        } else if (player1_time > player2_time) {
                            tv_text = Html.fromHtml(player1_name + " VS " + "<b>" + player2_name + "</b>");
                        } else {
                            tv_text = Html.fromHtml("<b>" + player1_name + "</b>" + " VS " + "<b>" + player2_name + "</b>");
                        }
                        ((TextView) gameEntry.findViewById(R.id.menu_multiplayer_game_entry_text)).setText(tv_text);
                        if (player1_time > 0 || player2_time > 0) {
                            String tv_textTime = Utils.milisToTimeStirng(player1_time) + " VS " + Utils.milisToTimeStirng(player2_time);
                            ((TextView) gameEntry.findViewById(R.id.menu_multiplayer_game_entry_textTime)).setText(tv_textTime);
                            ((TextView) gameEntry.findViewById(R.id.menu_multiplayer_game_entry_textTime)).setVisibility(View.VISIBLE);
                        }
                        if ((player1_name.equals(localUsername) && player1_time == 0)
                                || (player2_name.equals(localUsername) && player2_time == 0)
                                ) {
                            ((Button) gameEntry.findViewById(R.id.menu_multiplayer_game_entry_button_start)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    gameEntry.setBackgroundResource(R.drawable.background_selected);
                                    networking.multiplayer_start(gameID);
                                }
                            });
                            ((Button) gameEntry.findViewById(R.id.menu_multiplayer_game_entry_button_start)).setVisibility(View.VISIBLE);
                        }

                        row.addView(gameEntry);
                        if (++rowItemID >= imgsPerRow) {
                            gamesScrollLayout.addView(row);
                            row = new LinearLayout(context);
                            rowItemID = 0;
                        }
                    }
                    while (rowItemID != 0) {
                        LinearLayout gameEntry = (LinearLayout) inflater.inflate(R.layout.menu_multiplayer_game_entry, null);
                        gameEntry.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f / imgsPerRow));
                        gameEntry.setVisibility(View.INVISIBLE);
                        ((ImageView) gameEntry.findViewById(R.id.menu_multiplayer_game_entry_img)).setImageBitmap(
                                Bitmap.createBitmap(widthInPixels / imgsPerRow, 10, Bitmap.Config.ARGB_8888)
                        );
                        row.addView(gameEntry);
                        if (++rowItemID >= imgsPerRow) {
                            gamesScrollLayout.addView(row);
                            row = new LinearLayout(context);
                            rowItemID = 0;
                        }
                    }
                }catch (NullPointerException e){
                    //e.printStackTrace();
                }
            }
        });
    }

    ///=====SETTINGS===ONCLICKS===========================
    public void onMenuSettingsAccSetClick(View view) {
        String email = "";
        String password = "";
        EditText et_email = (EditText)findViewById(R.id.menu_settings_account_email);
        EditText et_password = (EditText)findViewById(R.id.menu_settings_account_new_password);
        EditText et_password2 = (EditText)findViewById(R.id.menu_settings_account_new_password2);
        if( et_email.getText().toString().length() > 0 ) {
            email = et_email.getText().toString();
        }
        if(et_password.getText().toString().length() > 0) {
                if(et_password2.getText().toString().length() > 0){
                    if(et_password.getText().toString().equals( et_password2.getText().toString() )){
                        password = et_password.getText().toString();
                    }else{
                        Toast.makeText(this, R.string.settings_options_account_set_error_passwords_doesnt_match, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, R.string.settings_options_account_set_error_password2_empty, Toast.LENGTH_SHORT).show();
                }
        }

        if(!password.equals("") || !email.equals("")) {
            et_email.setFocusable(false);
            et_email.setFocusableInTouchMode(false);
            et_email.setClickable(false);
            et_password.setFocusable(false);
            et_password.setFocusableInTouchMode(false);
            et_password.setClickable(false);
            et_password2.setFocusable(false);
            et_password2.setFocusableInTouchMode(false);
            et_password2.setClickable(false);

            networking.updateAccountInfo(email, password);
        }
    }
    public void onMenuSettingsAccSetCompleted(String response) {
        String[] infoBack = response.split("\n");

        EditText et_email = (EditText)findViewById(R.id.menu_settings_account_email);
        EditText et_password = (EditText)findViewById(R.id.menu_settings_account_new_password);
        EditText et_password2 = (EditText)findViewById(R.id.menu_settings_account_new_password2);
        et_email.setFocusable(true); et_email.setFocusableInTouchMode(true); et_email.setClickable(true);
        et_password.setFocusable(true); et_password.setFocusableInTouchMode(true); et_password.setClickable(true);
        et_password2.setFocusable(true); et_password2.setFocusableInTouchMode(true); et_password2.setClickable(true);

        if(infoBack[0].startsWith("Success"))
        {
            String email = "", password = "";
            if( infoBack[1].startsWith("Email") && et_email.getText().toString().length() > 0 ) {
                email = et_email.getText().toString();
            }
            if( infoBack[1].startsWith("Password") && et_password.getText().toString().length() > 0) {
                password = et_password.getText().toString();
            }

            if(!email.equals("") && email.length() > 0) {
                sharedPreferences.edit().putString("email", email).apply();
                Toast.makeText(this, R.string.settings_options_account_set_success_email, Toast.LENGTH_SHORT).show();
            }
            if(!password.equals("") && password.length() > 0) {
                sharedPreferences.edit().putString("password", password).apply();
                Toast.makeText(this, R.string.settings_options_account_set_success_password, Toast.LENGTH_SHORT).show();
                et_password.setText("");
                et_password2.setText("");
            }
        }
    }
    ///===END===MENU================================================================================

    private void showContinueImageDialog(final long imgID){
        final Context context = this;
        final AlertDialog dialog_continueImage;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_menu_image_menu, null));
        dialog_continueImage = builder.create();
        dialog_continueImage.show();

        //TITLE & PREVIEW
        ((TextView)dialog_continueImage.findViewById(R.id.menu_dialog_image_menu_title)).setText(
                SaveManager.load_db_image(getBaseContext(), imgID).title
        );
        if(SaveManager.was_autosaved(this, imgID)) {
            ((ImageView) dialog_continueImage.findViewById(R.id.menu_dialog_image_menu_img)).setImageBitmap(
                    Utils.upscale(Utils.join(Utils.toGrayscale(SaveManager.load_image(this, imgID)), SaveManager.load_imageAutosave(this, imgID)), widthInPixels)
            );
        }else{
            ((ImageView) dialog_continueImage.findViewById(R.id.menu_dialog_image_menu_img)).setImageBitmap(
                    Utils.upscale(Utils.toGrayscale(SaveManager.load_image(this, imgID)), widthInPixels)
            );
        }

        //CONTINUE
        dialog_continueImage.findViewById(R.id.menu_dialog_image_menu_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
                gameIntent.putExtra("isSandbox", false);
                gameIntent.putExtra("imgID", imgID );
                startActivityForResult(gameIntent, GAMEACTIVITY_BACKCODE);

                dialog_continueImage.cancel();
            }
        });
        //START OVER
        dialog_continueImage.findViewById(R.id.menu_dialog_image_menu_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog_startOverConfirmation;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.menu_dialog_image_confirmation_start_over_title)
                    .setPositiveButton(R.string.menu_dialog_image_confirmation_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SaveManager.destroyAutosave(getBaseContext(), imgID);

                            Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
                            gameIntent.putExtra("isSandbox", false);
                            gameIntent.putExtra("imgID", imgID );
                            startActivityForResult(gameIntent, GAMEACTIVITY_BACKCODE);

                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(R.string.menu_dialog_image_confirmation_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    })
                ;
                dialog_startOverConfirmation = builder.create();
                dialog_continueImage.cancel();
                dialog_startOverConfirmation.show();
            }
        });
        //DELETE
        dialog_continueImage.findViewById(R.id.menu_dialog_image_menu_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog_startOverConfirmation;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.menu_dialog_image_confirmation_delete_title)
                        .setMessage(R.string.menu_dialog_image_confirmation_delete_msg)
                        .setPositiveButton(R.string.menu_dialog_image_confirmation_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                SaveManager.destroyImage(getBaseContext(), imgID);

                                dialog.cancel();
                                generateSubMenu();
                            }
                        })
                        .setNegativeButton(R.string.menu_dialog_image_confirmation_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();
                            }
                        })
                ;
                dialog_startOverConfirmation = builder.create();
                dialog_continueImage.cancel();
                dialog_startOverConfirmation.show();
            }
        });
    }
    private void showSandboxMenuDialog(final long imgID){
        Log.d("MA", "Creatinfg dialog with imgID=" + imgID);
        final Context context = this;
        final AlertDialog dialog_sandboxMenu;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_menu_sandbox_menu, null))
        ;
        dialog_sandboxMenu = builder.create();
        dialog_sandboxMenu.show();

        //BAJERY
        ((TextView)dialog_sandboxMenu.findViewById(R.id.menu_dialog_sandbox_menu_title)).setText(
                SaveManager.load_db_sandbox(getBaseContext(), imgID).title
        );
        ((ImageView)dialog_sandboxMenu.findViewById(R.id.menu_dialog_sandbox_menu_img)).setImageBitmap(
                Utils.upscale(SaveManager.load_sandbox(getBaseContext(), imgID), widthInPixels)
                );

        //OPEN
        dialog_sandboxMenu.findViewById(R.id.menu_dialog_sandbox_menu_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
                gameIntent.putExtra("isSandbox", true);
                gameIntent.putExtra("imgID", imgID );
                //startActivity(gameIntent);
                startActivityForResult(gameIntent, GAMEACTIVITY_BACKCODE);


                dialog_sandboxMenu.cancel();
            }
        });
        //ADD TO PROFILE
        dialog_sandboxMenu.findViewById(R.id.menu_dialog_sandbox_menu_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                networking.sendMasterpiece(imgID);

                dialog_sandboxMenu.cancel();
            }
        });
        //SAVE AS IMAGE
        dialog_sandboxMenu.findViewById(R.id.menu_dialog_sandbox_menu_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveManager.share_sandbox_toStorage(getBaseContext(), imgID);

                dialog_sandboxMenu.cancel();
            }
        });
        //SHARE
        dialog_sandboxMenu.findViewById(R.id.menu_dialog_sandbox_menu_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveManager.share_sandbox(getBaseContext(), imgID);

                dialog_sandboxMenu.cancel();
            }
        });
        //DELETE
        dialog_sandboxMenu.findViewById(R.id.menu_dialog_sandbox_menu_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog_startOverConfirmation;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.menu_dialog_sandbox_confirmation_delete_title)
                        .setPositiveButton(R.string.menu_dialog_sandbox_confirmation_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                SaveManager.destroySandbox(getBaseContext(), imgID);

                                dialog_sandboxMenu.cancel();
                                generateSubMenu();
                            }
                        })
                        .setNegativeButton(R.string.menu_dialog_sandbox_confirmation_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();
                            }
                        })
                ;
                dialog_startOverConfirmation = builder.create();
                dialog_sandboxMenu.cancel();
                dialog_startOverConfirmation.show();
            }
        });
    }
    private void showLocalProfileMenuDialog(final long imgID, final String imageTitle, final Bitmap imageBitmap){
        Log.d("MA", "Creatinfg dialog with imgID=" + imgID);
        final Context context = this;
        final AlertDialog dialog_profileImageMenu;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_menu_profile_image_menu, null))
        ;
        dialog_profileImageMenu = builder.create();
        dialog_profileImageMenu.show();

        //BAJERY
        ((TextView)dialog_profileImageMenu.findViewById(R.id.menu_dialog_profile_image_menu_title)).setText(
                //SaveManager.load_db_image(getBaseContext(), imgID).title
                imageTitle
        );
        ((ImageView)dialog_profileImageMenu.findViewById(R.id.menu_dialog_profile_image_menu_img)).setImageBitmap(
                //Utils.upscale(SaveManager.load_image(getBaseContext(), imgID), widthInPixels)
                Utils.upscale(imageBitmap, widthInPixels)
        );

        //DELETE
        dialog_profileImageMenu.findViewById(R.id.menu_dialog_profile_image_menu_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog_startOverConfirmation;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.menu_dialog_profile_image_confirmation_delete_title)
                        .setPositiveButton(R.string.menu_dialog_profile_image_confirmation_delete_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                networking.removeMasterpiece(imgID);

                                dialog_profileImageMenu.cancel();
                                generateSubMenu();
                            }
                        })
                        .setNegativeButton(R.string.menu_dialog_profile_image_confirmation_delete_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();
                            }
                        })
                ;
                dialog_startOverConfirmation = builder.create();
                dialog_profileImageMenu.cancel();
                dialog_startOverConfirmation.show();
            }
        });
    }

    private void generateMenuMultiplayerCreateGameDialog()
    {
        final AlertDialog dialog_multiplayerCreateGame;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        final View createGameLayout = inflater.inflate(R.layout.menu_multiplayer_create_game_dialog, null);
        builder.setView(createGameLayout);
        dialog_multiplayerCreateGame = builder.create();
        dialog_multiplayerCreateGame.show();

        createGameLayout.findViewById(R.id.menu_multiplayer_create_createButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String opponentsName = ((EditText)createGameLayout.findViewById(R.id.menu_multiplayer_create_opponentsName)).getText().toString();
                long imageID = (long)createGameLayout.findViewById(R.id.menu_multiplayer_create_createButton).getTag();
                onMenuMultiplayerCreate(opponentsName, imageID);
                dialog_multiplayerCreateGame.cancel();
                generateSubMenu();
            }
        });

        final LinearLayout scrollList = (LinearLayout)createGameLayout.findViewById(R.id.menu_multiplayer_create_imagesScrollList);
        scrollList.removeAllViews();
        final int imgsPerRow = 4;
        final int padding = 15;
        Map<String, Vector<Long>> menuEntries = new HashMap<>();
        for(String category : SaveManager.getAllSavedCategories_images(this)){
            for(DatabaseEntry dbEntry : SaveManager.getSavedEntries_images(this, category)){
                if(!menuEntries.containsKey(category)){
                    menuEntries.put(category, new Vector<Long>());
                }
                menuEntries.get(category).add(dbEntry.id);
            }
        }

        for(String category : menuEntries.keySet()) {
            TextView category_tv = new TextView(this);
            category_tv.setText(category);
            category_tv.setTextSize(24.f);
            category_tv.setTypeface(null, Typeface.BOLD);
            scrollList.addView(category_tv);

            for (int i = 0; i < menuEntries.get(category).size(); i += imgsPerRow) {
                LinearLayout newRow = new LinearLayout(this);
                newRow.setOrientation(LinearLayout.HORIZONTAL);
                newRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                for (int j = 0; j < imgsPerRow; ++j) {
                    ImageView imgv = new ImageView(this);

                    imgv.setAdjustViewBounds(true);
                    imgv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imgv.setCropToPadding(true);
                    imgv.setPadding(padding, padding, padding, padding);
                    imgv.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.f / imgsPerRow
                    ));

                    if(menuEntries.get(category).size() <= i+j){
                        imgv.setImageBitmap(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
                    }else{
                        long imgID =  menuEntries.get(category).get(i+j);
                        imgv.setBackground(getResources().getDrawable(R.drawable.background));
                        imgv.setTag(imgID);
                        imgv.setImageBitmap(Utils.upscale(Utils.toGrayscale( SaveManager.load_image(this, imgID)) , (widthInPixels/imgsPerRow) ));
                        imgv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    //long prevID = (long);
                                    createGameLayout.findViewWithTag(
                                            createGameLayout.findViewById(R.id.menu_multiplayer_create_createButton).getTag()
                                    ).setBackgroundResource(R.drawable.background);
                                }catch (NullPointerException e){
                                    e.printStackTrace();
                                }

                                /*try {
                                    ((LinearLayout) scrollList.getChildAt(prevID / imgsPerRow)).getChildAt(prevID % imgsPerRow)
                                            .setBackgroundResource(R.drawable.background);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }*/

                                createGameLayout.findViewById(R.id.menu_multiplayer_create_createButton).setTag(view.getTag());
                                view.setBackgroundResource(R.drawable.background_selected);
                            }
                        });
                    }
                    newRow.addView(imgv);
                }
                scrollList.addView(newRow);
            }
        }
        Space space = new Space(scrollList.getContext());
        space.setMinimumHeight(20);
        scrollList.addView(space);
    }
    /*private void shotMultiplayerMenuEntryClickedDialog(LinearLayout gameEntry)
    {
        final AlertDialog dialog_multiplayerMenuEntryClicked;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        LinearLayout gameEntryCpu = gameEntry;
        builder.setView(gameEntry);
        dialog_multiplayerMenuEntryClicked = builder.create();
        dialog_multiplayerMenuEntryClicked.show();

        gameEntry.findViewById(R.id.menu_multiplayer_game_entry_button_start).setVisibility(View.VISIBLE);
        //setting onclicks etc.
        //networking.multiplayer_start(gameID);
    }*/
    /*private void showMultiplayerGameFinishedDialog()
    {
        final AlertDialog dialog_multiplayerFinishedGame;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.multiplayer_dialog_finished_title)
                .setMessage(R.string.multiplayer_dialog_finished_mesg)
                .setPositiveButton(R.string.multiplayer_dialog_finished_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
        ;
        dialog_multiplayerFinishedGame = builder.create();
        dialog_multiplayerFinishedGame.show();
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result OK.d.
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("MA", "Returning: " + resultCode);
        //Log.d("MA", "OK: " + Activity.RESULT_OK);
        if (requestCode == GAMEACTIVITY_BACKCODE) {
            // do something good
            /*Log.d("MA", "Gen...");
            generateSubMenu();
            Log.d("MA", "DONE");*/
            reloadHandler.post(reloadRunnable);
        }else if(requestCode == GAMEACTIVITY_MULTIPLAYER_BACKCODE) {
            networking.multiplayer_finish();
        }else if(requestCode == CAMERAATIVITY_BACKCODE) {
            reloadHandler.post(reloadRunnable);
        }
    }









    public void onCameraButtonClick(View v)
    {
        Intent gameIntent = new Intent(this ,CameraActivity.class);
        startActivity(gameIntent);

        /*
        Game g = new Game(this);
        setContentView(g);
        g.resume();

        Bitmap img1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.bob);
        g.screen.addGraphableToProcess(new Entity(new Vec2(10, 10), img1));
        Bitmap img3 = Utils.toGrayscale(BitmapFactory.decodeResource(this.getResources(), R.drawable.bob));
        g.screen.addGraphableToProcess(new Entity(new Vec2(10, 150), img3));
        */

    }

}