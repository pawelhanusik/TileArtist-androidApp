package PEngine;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import pl.com.gemstones.tileartist.MainActivity;
import pl.com.gemstones.tileartist.R;

public class SaveManager {
    private static Database m_Database;
    private static Context m_Context;

    public static final String externalDirName = "TileArtist";
    public static final String externalTmpFileName = "shareTmp";

    public static final String IMAGE = Database.TABLE_NAME_IMAGES;
    public static final String SANDBOX = Database.TABLE_NAME_SANDBOX;
    public static final String autosave_prefix = "autosave";
    //public static final String saveDir_sandbox = "sandbox;";//replaced by Database's table's name
    //public static final String saveDir_image = "auto;";     //replaced by Database's table's name
    public static final String extension = ".png";

    private static long share_imageID;

    private static void init(Context context){
        m_Context = context;
        m_Database = new Database(context);
    }
    public static void syncImage(MainActivity mainActivity){
        if(m_Database == null)
            init(mainActivity.getBaseContext());

        Networking networking = new Networking(mainActivity);
        networking.syncImage(m_Database.getAllIDs(Database.TABLE_NAME_IMAGES));
    }
    public static void syncImage(Context context, Networking networking){
        if(m_Database == null)
            init(context);

        networking.syncImage(m_Database.getAllIDs(Database.TABLE_NAME_IMAGES));
    }

    /*public static void saveImage(Context context, String title, Bitmap toSave){
        saveImage(context, getDefaultMetadata(), title, toSave);
    }
    public static void saveSandbox(Context context, String title, Bitmap toSave){
        saveSandbox(context, getDefaultMetadata(), title, toSave);
    }
    private static String[] getDefaultMetadata(){
        return new String[] {
                "uncategorized",
                "anonymous"
        };
    }*/
    public static void save_image(String noContextErrMsg, DatabaseEntry metadata, Bitmap toSave)
    {
        if(m_Context == null)
            Log.e("SaveManager", noContextErrMsg);
        else
            save(m_Context, Database.TABLE_NAME_IMAGES, metadata, toSave);
    }
    public static long save_image(Context context, DatabaseEntry metadata, Bitmap toSave)
    {
        return save(context, Database.TABLE_NAME_IMAGES, metadata, toSave);
    }
    public static long save_sandbox(Context context, DatabaseEntry metadata, Bitmap toSave)
    {
        return save(context, Database.TABLE_NAME_SANDBOX, metadata, toSave);
    }
    public static void save_autosave_image(Context context, long imageID, Bitmap toSave) {
        save_autosave(context, autosave_prefix, imageID, toSave);
    }
    public static void save_autosave_sandbox(Context context, long imageID, Bitmap toSave) {
        save_autosave(context, SANDBOX, imageID, toSave);
    }
    private static void save_autosave(Context context, String databaseTable, long imageID, Bitmap toSave)
    {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(databaseTable + ";" + String.valueOf(imageID) + extension, Context.MODE_PRIVATE);
            toSave.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static long save(Context context, String databaseTable ,DatabaseEntry metadata, Bitmap toSave)
    {
        if(m_Database == null)
            init(context);
        /*File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/" + saveDir);
        dir.mkdir();
        File file = new File(dir, title + extension);*/
        long ID = m_Database.put(databaseTable, metadata);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(databaseTable + ";" + String.valueOf(ID) + extension, Context.MODE_PRIVATE);
            toSave.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ID;
    }

    ///=============================================================================================
    public static long[] getAllSaved_images(Context context){
        if(m_Database == null)
            init(context);
        return m_Database.getAllIDs(Database.TABLE_NAME_IMAGES);
    }
    public static long[] getAllSaved_sandboxes(Context context){
        if(m_Database == null)
            init(context);
        return m_Database.getAllIDs(Database.TABLE_NAME_SANDBOX);
    }
    public static String[] getAllSavedCategories_images(Context context){
        if(m_Database == null)
            init(context);
        return m_Database.getAllCategories(Database.TABLE_NAME_IMAGES);
    }
    public static String[] getAllSavedCategories_sandbox(Context context){
        if(m_Database == null)
            init(context);
        return m_Database.getAllCategories(Database.TABLE_NAME_SANDBOX);
    }
    public static Vector<DatabaseEntry> getSavedEntries_images(Context context, String category){
        if(m_Database == null)
            init(context);
        return m_Database.getCategory(Database.TABLE_NAME_IMAGES, category);
    }
    public static Vector<DatabaseEntry> getSavedEntries_sandbox(Context context, String category){
        if(m_Database == null)
            init(context);
        return m_Database.getCategory(Database.TABLE_NAME_SANDBOX, category);
    }

    /*public static Bitmap load(Context context, String title){
        return load(context, "uncategorized", title);
    }*/
    public static boolean was_autosaved(Context context, long imageID) {
        return new File(context.getFilesDir() + "/" + autosave_prefix + ";" + String.valueOf(imageID) + extension).exists();
    }
    public static Bitmap load_image(Context context, long imageID) {
        return load(context, Database.TABLE_NAME_IMAGES, imageID);
    }
    public static Bitmap load_imageAutosave(Context context, long imageID) {
        Bitmap ret = load(context, autosave_prefix, imageID);
        if(ret == null)
            ret = load(context, Database.TABLE_NAME_IMAGES, imageID);
        return ret;
    }
    public static Bitmap load_sandbox(Context context, long imageID){
        return load(context, Database.TABLE_NAME_SANDBOX, imageID);
    }
    public static DatabaseEntry load_db_image(Context context, long imageID){
        return m_Database.get(Database.TABLE_NAME_IMAGES, imageID).elementAt(0);
    }
    public static DatabaseEntry load_db_sandbox(Context context, long imageID){
        return m_Database.get(Database.TABLE_NAME_SANDBOX, imageID).elementAt(0);
    }
    public static String get_sandbox_path(Context context, long imageID) {
        return context.getFilesDir() + "/" + Database.TABLE_NAME_SANDBOX + ";" + String.valueOf(imageID) + extension;
    }
    private static Bitmap load(Context context, String databeseTable, long imageID)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeFile(context.getFilesDir() + "/" + databeseTable + ";" + String.valueOf(imageID) + extension, options);
        /*
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(saveDir + title + extension);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
        */
    }
    ///=============================================================================================
    public static boolean destroyAutosave(Context context, long imageID)
    {
        return new File(context.getFilesDir() + "/" + autosave_prefix + ";" + String.valueOf(imageID) + extension).delete();
    }
    public static boolean destroySandbox(Context context, long imageID)
    {
        if(!m_Database.deleteID(Database.TABLE_NAME_SANDBOX, imageID))
            return false;
        return new File(context.getFilesDir() + "/" + SANDBOX + ";" + String.valueOf(imageID) + extension).delete();
    }
    public static boolean destroyImage(Context context, long imageID)
    {
        if(!m_Database.deleteID(Database.TABLE_NAME_IMAGES, imageID))
            return false;
        return (
                destroyAutosave(context, imageID)
                && new File(context.getFilesDir() + "/" + IMAGE + ";" + String.valueOf(imageID) + extension).delete()
        );
    }

    // SHARING METHODS (cant work with permissions)
    public static void share_sandbox_toStorage(Context context, long imageID) {
        if( GetPermission.storage(GetPermission.STORAGE_SAVE_REQUEST_BACKCODE) ){
            share_sandbox_toStorage_withoutPermission(context, imageID);
        }else{
            if(m_Context == null) {
                m_Context = context;
            }
            share_imageID = imageID;
        }
    }
    public static void share_sandbox_toStorage_withoutPermission(Context context, long imageID) {
        String tmpPath = Environment.getExternalStorageDirectory() + "/" + externalDirName;
        new File(tmpPath).mkdir();
        tmpPath += "/" + SANDBOX + ";" + String.valueOf(imageID) + extension;
        Bitmap bitmap = load_sandbox(context, imageID);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(tmpPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(context, context.getResources().getString(R.string.menu_dialog_sandbox_savedMsg), Toast.LENGTH_SHORT).show();
    }
    public static void share_sandbox(Context context, long imageID) {
        if( GetPermission.storage(GetPermission.STORAGE_SHARE_REQUEST_BACKCODE) ) {
            share_sandbox_withoutPermission(context, imageID);
        }else{
            if(m_Context == null) {
                m_Context = context;
            }
            share_imageID = imageID;
        }
    }
    public static void share_sandbox_withoutPermission(Context context, long imageID) {
        String tmpPath = Environment.getExternalStorageDirectory() + "/" + externalDirName;
        new File(tmpPath).mkdir();
        tmpPath += "/" + externalTmpFileName + extension;
        Bitmap bitmap = load_sandbox(context, imageID);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(tmpPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(tmpPath));
        context.startActivity(Intent.createChooser(share, context.getResources().getString(R.string.menu_dialog_sandbox_share_title)));
    }
    public static void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GetPermission.STORAGE_SAVE_REQUEST_BACKCODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted) {
                        share_sandbox_toStorage_withoutPermission(m_Context, share_imageID);
                    } else {
                        Toast.makeText(m_Context, R.string.storage_permission_denied, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case GetPermission.STORAGE_SHARE_REQUEST_BACKCODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted) {
                        share_sandbox_withoutPermission(m_Context, share_imageID);
                    } else {
                        Toast.makeText(m_Context, R.string.storage_permission_denied, Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    ///END OF SHARING METHODS

}
