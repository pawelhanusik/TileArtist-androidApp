package pl.com.gemstones.tileartist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Vector;

import PEngine.ColorDB;
import PEngine.DatabaseEntry;
import PEngine.Entities.Tile;
import PEngine.Game;
import PEngine.Graphics.ScreenSandboxExtension;
import PEngine.Math.Vec2;
import PEngine.SaveManager;
import PEngine.Utils;

public class GameActivity extends Activity {

    Game game;

    Handler multiplayerFinishCheckerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Settings.getSetting(Settings.Name.FULLSCREEN)) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        final Intent gameIntent = getIntent();
        boolean isSandbox = gameIntent.getBooleanExtra("isSandbox", false);
        boolean isMultiplayer = gameIntent.getBooleanExtra("isMultiplayer", false);
        long imgID = gameIntent.getLongExtra("imgID", 1);

        if(isMultiplayer){
            multiplayerFinishCheckerHandler = new Handler();
            Runnable multiplayerFinishCheckerRunnable = new Runnable() {
                @Override
                public void run() {
                    if(Tile.tiles_left == 0){
                        Log.d("GameActivity", "Multiplayer Finished!!!");
                        gameIntent.putExtra("finished", "true");
                        finish();
                    }else{
                        multiplayerFinishCheckerHandler.postDelayed(this, 1);
                    }
                }
            };
            multiplayerFinishCheckerHandler.postDelayed(multiplayerFinishCheckerRunnable, 100);
        }

        startGame(imgID, isSandbox, isMultiplayer);

        if(MainActivity.showAds) {
            AdRequest adRequest = new AdRequest.Builder().build();
            ((AdView) findViewById(R.id.game_adView)).loadAd(adRequest);
        }
    }
    @Override
    protected void onDestroy()
    {
        Log.i("GameActivity", "Saving...");

        if(!game.isMultiplayer) {
            if (!game.isSandbox) {
                SaveManager.save_autosave_image(this, game.imageID, Tile.getBitmapFromAllTiles());
            } else if(Settings.getSetting(Settings.Name.AUTOSAVE_SANDBOX)) {
                if (game.imageID >= 0) {
                    SaveManager.save_autosave_sandbox(this, game.imageID, Tile.getBitmapFromAllTiles(true));
                } else {
                    SaveManager.save_sandbox(this, new DatabaseEntry(), Tile.getBitmapFromAllTiles(true));
                }
            }
        }

        MainActivity.reload = true;

        super.onDestroy();
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

    private void saveSandboxDialog()
    {
        AlertDialog dialog_savesandbox;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_save_sandbox, null))
                .setTitle(R.string.game_dialog_save_sandbox_head_text)
                // Add action buttons
                .setPositiveButton(R.string.game_dialog_save_sandbox_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseEntry newEntry = new DatabaseEntry();
                        newEntry.title = ((EditText)((AlertDialog)dialog).findViewById(R.id.game_dialog_save_sandbox_title)).getText().toString();
                        game.imageID = SaveManager.save_sandbox(getBaseContext(), newEntry, Tile.getBitmapFromAllTiles(true));
                    }
                })
                .setNegativeButton(R.string.game_dialog_save_sandbox_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })

        ;
        dialog_savesandbox = builder.create();
        dialog_savesandbox.show();
    }

    void startGame(long imgID, boolean isSandbox, boolean isMultiplayer)
    {
        game = new Game(this, imgID, isSandbox, isMultiplayer);

        setContentView(R.layout.game_layout);
        ((RelativeLayout)findViewById(R.id.game_relativeLayout)).addView(game, 0);


        short colors_count = 100;
        if(isMultiplayer)
        {
            colors_count = loadMultiplayer(imgID);
        }
        else if(imgID >= 0 && !isSandbox)
        {
            colors_count = loadImg(imgID);
            /*if(img >= 0)
                colors_count = loadImg(img);*/
            //colors_count = loadImg(SaveManager.load(this, game.title), 1.f);
        }
        else if(imgID >= 0 && isSandbox)
        {
            short colors_added = loadSandbox(imgID);
            for(short i = colors_added; i < colors_count; ++i){
                //ColorDB.setColor(i, r.nextInt(255), r.nextInt(255), r.nextInt(255));
                ColorDB.addColor(255, 255, 255);
            }
        }
        else
        {
            int imgWidth = ScreenSandboxExtension.sandboxSize;
            ColorDB.init(colors_count);
            for(int lol = 1; lol < 2; ++lol) {
                for (int i = 0; i < 2 * imgWidth - 1; ++i) {
                    for (int x = (i < imgWidth) ? i : imgWidth - 1;
                         x >= ((i < imgWidth) ? 0 : i - imgWidth + 1);
                         --x) {
                        int y = (i - x);
                        //Log.d("pos", x + ", " + y);
                        game.screen.addGraphableToProcess(new Tile(new Vec2(x * Tile.m_size.x, y * Tile.m_size.y), (short) -1)); //(short) r.nextInt(colors_count)));
                    }
                }
            }
            game.screen.setImageDimensions(new Vec2(imgWidth * Tile.m_size.x, imgWidth * Tile.m_size.y));
            for(short i = 0; i < colors_count; ++i){
                //ColorDB.setColor(i, r.nextInt(255), r.nextInt(255), r.nextInt(255));
                ColorDB.setColor(i, 255, 255, 255);
            }
        }


        //ColorDB.init(colors_count);
        int dp_50 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        for(short i = 0; i < colors_count; ++i) {
            //ColorDB.setColor(i, r.nextInt(255), r.nextInt(255), r.nextInt(255));

            Space space = new Space(this);
            space.setMinimumWidth(dp_50/5);
            ((LinearLayout)findViewById(R.id.game_linearLayout)).addView(space);

            Button b = new Button(this);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onColorButtonClick(view);
                }
            });
            b.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onColorButtonLongClick(view);
                    return false;
                }
            });
            b.setLayoutParams(new LinearLayout.LayoutParams(dp_50, dp_50));

            b.setText(String.valueOf(i));
            b.setTag(i);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(ColorDB.getColor(i).getColor());
            gd.setCornerRadius(10);
            if(i == 0) gd.setStroke(5, 0xFF000000);
            b.setBackground(gd);


            ((LinearLayout)findViewById(R.id.game_linearLayout)).addView(b);
        }
        Space space = new Space(this);
        space.setMinimumWidth(dp_50/5);
        ((LinearLayout)findViewById(R.id.game_linearLayout)).addView(space);

        if(isSandbox)
            prepareToolbar();
    }

    private short loadMultiplayer(long imageID)
    {
        Bitmap bitmap = SaveManager.load_image(this, imageID);
        return loadImg(bitmap, 3.f);
    }
    private short loadSandbox(long imageID)
    {
        Bitmap bitmap = SaveManager.load_sandbox(this, imageID);
        return loadImg(bitmap, 3.f);
    }
    private short loadImg(long imageID)
    {
        //Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), imgResID);
        Bitmap bitmap = SaveManager.load_imageAutosave(this, imageID);
        return loadImg(bitmap, 3.f);
    }
    private short loadImg(Bitmap bitmap, float whiteningFactor)
    {
        boolean wasAutosaved = SaveManager.was_autosaved(this, game.imageID);
        Bitmap imageOriginal;
        if(!game.isSandbox && !game.isMultiplayer && wasAutosaved){
            imageOriginal = SaveManager.load_image(this, game.imageID);
        }else{
            imageOriginal = bitmap;
        }

        Vector<Integer> allBitmapColors = new Vector<>();
        //short[][] colorIds = new short[bitmap.getWidth()][bitmap.getHeight()];
        for(int y = 0; y < imageOriginal.getHeight(); ++y) {
            for (int x = 0; x < imageOriginal.getWidth(); ++x) {
                int pix_color = imageOriginal.getPixel(x, y);
                //continue if met 100% alpha pixel
                if(pix_color >>> 24 == 0) {
                    continue;
                }else if(pix_color >>> 24 != 0xff){
                    pix_color = Color.argb(255, Color.red(pix_color), Color.green(pix_color), Color.blue(pix_color));
                }
                if(!allBitmapColors.contains(pix_color)){
                    allBitmapColors.add(pix_color);
                }
                //game.screen.addGraphableToProcess(new Tile(new Vec2(x * Tile.m_size.x, y * Tile.m_size.y), (byte)(allBitmapColors.size()-1) ));///(short) r.nextInt(colors_count)));
            }
        }
        short colors_count = (short)allBitmapColors.size();
        ColorDB.init((short)allBitmapColors.size());
        for(short i = 0; i < allBitmapColors.size(); ++i){
            int col = allBitmapColors.elementAt(i); //TODO: crashes if tries to load to much colors (more than SHORT_MAX)
            //Log.d("Col", ""+col);
            ColorDB.setColor(i, col >>> 16, col >>> 8, col >>> 0);
        }

        Bitmap grey_bitmap = Utils.toGrayscale(Utils.whitening(bitmap, whiteningFactor));
        //lol for loop is required to reduce ugly glich while moving tiles around
        for(int lol = 1; lol < 2; ++lol) {
            for (int i = 0; i < 2 * bitmap.getWidth() - 1; ++i) {
                for (int x = (i < bitmap.getWidth()) ? i : bitmap.getWidth() - 1;
                     x >= ((i < bitmap.getWidth()) ? 0 : i - bitmap.getWidth() + 1);
                     --x) {
                    int y = (i - x);
                    //Log.d("pos", x + ", " + y);
                    int pix_color;
                    try {
                        pix_color = bitmap.getPixel(x, y);
                    }catch (IllegalArgumentException e){
                        Log.w("GameActivity", "IllegalArgException whan tried to load bitmap. " + x + ">=" + bitmap.getWidth() + " or " + y + " >= " + bitmap.getHeight());
                        continue;
                    }
                    int tileColor;
                    boolean isWellColored = false;
                    //continue if met 100% alpha pixel
                    if(pix_color >>> 24 == 0){
                        if(!game.isSandbox) {
                            continue;
                        }else /*if(pix_color == 0)*/{
                            tileColor = 0;
                        }
                    }else if (pix_color >>> 24 != 0xff || !wasAutosaved || game.isMultiplayer) {
                        pix_color = Color.argb(255, Color.red(pix_color), Color.green(pix_color), Color.blue(pix_color));
                        tileColor = Utils.setFullAlpha(grey_bitmap.getPixel(x, y));
                    }else if(!game.isSandbox){
                        tileColor =  Utils.setFullAlpha(bitmap.getPixel(x, y));
                        isWellColored = true;
                    }else{
                        tileColor = bitmap.getPixel(x, y);
                    }
                    short ID = -1;
                    for (short id = 0; id < allBitmapColors.size(); ++id) {
                        if (allBitmapColors.elementAt(id) == imageOriginal.getPixel(x, y)) {
                            ID = id;
                            break;
                        }
                    }

                    //TODO: title color shouldnt matter now
                    if(!game.isSandbox) {
                        game.screen.addGraphableToProcess(new Tile(new Vec2(x * Tile.m_size.x, y * Tile.m_size.y), lol == 1 ? ID : -2, tileColor, isWellColored)); //(short) r.nextInt(colors_count)));
                    }else{
                        game.screen.addGraphableToProcess(new Tile(new Vec2(x * Tile.m_size.x, y * Tile.m_size.y), (tileColor != 0) ? ID : (short)-1, false, true));
                    }
                }
            }
        }
        game.screen.setImageDimensions(new Vec2( bitmap.getWidth() * Tile.m_size.x,  bitmap.getWidth() * Tile.m_size.y));
        return colors_count;
    }

    private void prepareToolbar()
    {
        int dp_50 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

        Space space = new Space(this);
        space.setMinimumWidth(dp_50/5);
        findViewById(R.id.view2).setVisibility(View.VISIBLE);
        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(space);

        Button toolbar_plus = new Button(this);
        toolbar_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getTag().equals(true)){
                    //ScreenSandboxExtension.isWorking = false;
                    view.setBackgroundResource(R.drawable.toolbar_plus0);
                    view.setTag(false);

                    findViewById(R.id.game_toolbar_arrow_0).setVisibility(View.GONE);
                    findViewById(R.id.game_toolbar_arrow_1).setVisibility(View.GONE);
                    findViewById(R.id.game_toolbar_arrow_2).setVisibility(View.GONE);
                    findViewById(R.id.game_toolbar_arrow_3).setVisibility(View.GONE);
                }else{
                    //ScreenSandboxExtension.isWorking = true;
                    view.setBackgroundResource(R.drawable.toolbar_plus1);
                    view.setTag(true);

                    findViewById(R.id.game_toolbar_arrow_0).setVisibility(View.VISIBLE);
                    findViewById(R.id.game_toolbar_arrow_1).setVisibility(View.VISIBLE);
                    findViewById(R.id.game_toolbar_arrow_2).setVisibility(View.VISIBLE);
                    findViewById(R.id.game_toolbar_arrow_3).setVisibility(View.VISIBLE);
                }
            }
        });
        toolbar_plus.setLayoutParams(new LinearLayout.LayoutParams(dp_50, dp_50));
        toolbar_plus.setBackgroundResource(R.drawable.toolbar_plus0);
        toolbar_plus.setTag(false);

        Button toolbar_save = new Button(this);
        toolbar_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //SaveManager.save_autosave(getBaseContext(), game.imageID, Tile.getBitmapFromAllTiles() );
                saveSandboxDialog();
            }
        });
        toolbar_save.setLayoutParams(new LinearLayout.LayoutParams(dp_50, dp_50));
        toolbar_save.setBackgroundResource(R.drawable.toolbar_save);
        toolbar_save.setTag(false);

        Button toolbar_eraser = new Button(this);
        toolbar_eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getTag().equals(true)){
                    Tile.isInEraserMode = false;
                    view.setBackgroundResource(R.drawable.toolbar_eraser0);
                    view.setTag(false);
                }else{
                    Tile.isInEraserMode = true;
                    view.setBackgroundResource(R.drawable.toolbar_eraser1);
                    view.setTag(true);
                }
            }
        });
        toolbar_eraser.setLayoutParams(new LinearLayout.LayoutParams(dp_50, dp_50));
        toolbar_eraser.setBackgroundResource(R.drawable.toolbar_eraser0);
        toolbar_eraser.setTag(false);

        /*Button toolbar_camera = new Button(this);
        toolbar_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameIntent = new Intent(getBaseContext() ,CameraActivity.class);
                startActivity(gameIntent);
            }
        });
        toolbar_camera.setLayoutParams(new LinearLayout.LayoutParams(dp_50, dp_50));
        toolbar_camera.setBackgroundResource(R.drawable.toolbar_camera);
        toolbar_camera.setTag(false);*/

        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(toolbar_plus);
        space = new Space(this);
        space.setMinimumWidth(dp_50/5);
        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(space);
        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(toolbar_save);
        space = new Space(this);
        space.setMinimumWidth(dp_50/5);
        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(space);
        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(toolbar_eraser);
        /*space = new Space(this);
        space.setMinimumWidth(dp_50/5);
        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(space);
        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(toolbar_camera);*/

        /*Space space2 = new Space(this);
        space.setMinimumWidth(dp_50/5);
        ((LinearLayout)findViewById(R.id.game_linearLayout2)).addView(space);*/
    }

    public void onColorButtonClick(View v)
    {
        View prev = ((LinearLayout)findViewById(R.id.game_linearLayout)).getChildAt(Tile.selectedColorIndex*2 +1);
        if(prev instanceof Button){
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(ColorDB.getColor(Tile.selectedColorIndex).getColor());
            gd.setCornerRadius(10);
            //((Button) prev).setText(String.valueOf(prev.getTag()));
            prev.setBackground(gd);
        }


        Button b = (Button)v;
        Tile.selectedColorIndex = (short)b.getTag();

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(10);
        gd.setColor(ColorDB.getColor(Tile.selectedColorIndex).getColor());
        gd.setStroke(5, 0xFF000000);
        //b.setText("| " + b.getText() + " |");
        b.setBackground(gd);
    }


    public void onColorButtonLongClick(View v)
    {
        short ID = (short)v.getTag();
        showPaletteDialog(ID);
    }
    public void showPaletteDialog(final short ID)
    {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle(R.string.game_palette_title)
                .initialColor(ColorDB.getColor(ID).getColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .lightnessSliderOnly()
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        //Toast.makeText(this, "onColorSelected: 0x"+Integer.toHexString(selectedColor), Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton(R.string.game_palette_ok, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        //changeBackgroundColor(selectedColor);
                        ColorDB.setColor(ID, selectedColor >>> 16, selectedColor >>> 8, selectedColor >>> 0);
                        View prev = ((LinearLayout)findViewById(R.id.game_linearLayout)).getChildAt(ID*2 +1);
                        if(prev instanceof Button){
                            //prev.setBackgroundColor(ColorDB.getColor(ID).getColor());
                            GradientDrawable gd = new GradientDrawable();
                            gd.setCornerRadius(10);
                            gd.setColor(ColorDB.getColor(ID).getColor());
                            if(Tile.selectedColorIndex == ID)
                                gd.setStroke(5, 0xFF000000);
                            prev.setBackground(gd);
                        }
                    }
                })
                .setNegativeButton(R.string.game_palette_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }



    public void onSandboxToolbarAddTilesArrowButtonClick(View view){
        switch ((String)view.getTag())
        {
            case "UP":
                for(int i = 0; i < game.screen.imageSizeInPixels.x / Tile.m_size.x ; ++i ) {
                    game.screen.addGraphableToProcess(new Tile(new Vec2(game.screen.imageStartPos.x + Tile.m_size.x * i, game.screen.imageStartPos.y - Tile.m_size.y ), (short)-1));
                }
                game.screen.imageStartPos.y -= Tile.m_size.y;
                game.screen.imageSizeInPixels.y += Tile.m_size.y;
                break;
            case "RIGHT":
                for(int i = 0; i < game.screen.imageSizeInPixels.y / Tile.m_size.y ; ++i ) {
                    game.screen.addGraphableToProcess(new Tile(new Vec2(game.screen.imageStartPos.x + game.screen.imageSizeInPixels.x, game.screen.imageStartPos.y + Tile.m_size.y * i), (short)-1));
                }
                game.screen.imageSizeInPixels.x += Tile.m_size.x;
                break;
            case "DOWN":
                for(int i = 0; i < game.screen.imageSizeInPixels.x / Tile.m_size.x ; ++i ) {
                    game.screen.addGraphableToProcess(new Tile(new Vec2(game.screen.imageStartPos.x + Tile.m_size.x * i, game.screen.imageStartPos.y + game.screen.imageSizeInPixels.y), (short)-1));
                }
                game.screen.imageSizeInPixels.y += Tile.m_size.y;
                break;
            case "LEFT":
                for(int i = 0; i < game.screen.imageSizeInPixels.y / Tile.m_size.y ; ++i ) {
                    game.screen.addGraphableToProcess(new Tile(new Vec2(game.screen.imageStartPos.x - Tile.m_size.x, game.screen.imageStartPos.y + Tile.m_size.y * i), (short)-1));
                }
                game.screen.imageStartPos.x -= Tile.m_size.x;
                game.screen.imageSizeInPixels.x += Tile.m_size.x;
                break;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        game.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        game.pause();
    }
}
