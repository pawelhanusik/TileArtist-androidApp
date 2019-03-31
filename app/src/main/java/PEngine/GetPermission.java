package PEngine;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Vector;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission_group.CAMERA;

///TODO: make this class - DONE (probably xD)
public class GetPermission {
    public static final int CAMERA_REQUEST_BACKCODE = 0;
    public static final int STORAGE_SAVE_REQUEST_BACKCODE = 1;
    public static final int STORAGE_SHARE_REQUEST_BACKCODE = 2;
    private static Activity m_mainActiviry = null;

    private static boolean isAskingForPermissionNeeded() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        return (currentapiVersion >= android.os.Build.VERSION_CODES.M);
    }

    public static void init(Activity activity)
    {
        m_mainActiviry = activity;
    }
    public static boolean storage(int request_code)
    {
        if(m_mainActiviry != null){
            return storage(m_mainActiviry, request_code);
        }
        Log.d("GetPermission", "m_mainActivity is NULL");
        return false;
    }
    public static boolean storage(Activity activity, int request_code)
    {
        if(m_mainActiviry == null){
            m_mainActiviry = activity;
        }

        if(isAskingForPermissionNeeded()){
            Log.d("GetPermission", "STORAGE: checking for permission...");
            return askForPermission(activity,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE }
                    , request_code);
        }else{
            return true;
        }
    }
    public static boolean camera(Activity activity)
    {
        if(m_mainActiviry == null){
            m_mainActiviry = activity;
        }

        if(isAskingForPermissionNeeded()){
            Log.d("GetPermission", "CAMERA: checking for permission...");
            return askForPermission(activity, Manifest.permission.CAMERA, CAMERA_REQUEST_BACKCODE);
        }else{
            return true;
        }
    }

    private static boolean askForPermission(Activity activity, String permissionString, int permission_request_code) {
        return askForPermission(activity, new String[] {permissionString}, permission_request_code );
    }
    private static boolean askForPermission(Activity activity, String[] permissionStrings, int permission_request_code)
    {
        // Here, thisActivity is the current activity
        Vector<String> permissionsDeniedVec = new Vector<>();
        for(String s : permissionStrings){
            if(ContextCompat.checkSelfPermission(activity, s) != PackageManager.PERMISSION_GRANTED) {
                permissionsDeniedVec.add(s);
            }
        }
        String[] permissionDenied = permissionsDeniedVec.toArray(new String[permissionsDeniedVec.size()]);
        if (permissionDenied.length > 0) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(activity,
                    permissionDenied,
                    permission_request_code);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }


    /*private boolean askForCameraPermissionOLD()
    {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            Log.d("Camera", "checking for permissions...");
            if (checkPermissionOLD()) {
                Toast.makeText(getApplicationContext(), "Permission already granted", Toast.LENGTH_LONG).show();
                return true;
            } else {
                Log.d("Camera", "requesting for permissions...");
                requestPermissionOLD();
                return false;
            }
        }
        return true;
    }*/
    /*private boolean checkPermissionOLD() {
        //int result = ContextCompat.checkSelfPermission(getApplicationContext(), GET_ACCOUNTS);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return *//*result == PackageManager.PERMISSION_GRANTED &&*//* result1 == PackageManager.PERMISSION_GRANTED;
    }*/
    /*private void requestPermissionOLD() {
        ActivityCompat.requestPermissions(this, new String[]{GET_ACCOUNTS, CAMERA}, REQUEST_GET_ACCOUNT);
        //ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }*/

}
