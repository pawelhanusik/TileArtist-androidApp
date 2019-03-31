package pl.com.gemstones.tileartist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

import PEngine.DatabaseEntry;
import PEngine.GetPermission;
import PEngine.SaveManager;
import PEngine.Utils;

import static android.content.ContentValues.TAG;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class CameraActivity extends Activity {

    private static final int REQUEST_GET_ACCOUNT = 112;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int FILE_SELECT_BACKCODE = 7173;

    boolean isUsingBackCamera = true;
    Camera mCamera;
    private Camera.PictureCallback mPicture;

    int widthInPixels;
    int dp_50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Settings.getSetting(Settings.Name.FULLSCREEN)) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        setContentView(R.layout.camera_layout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthInPixels = displayMetrics.widthPixels;
        dp_50 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());


        mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                final Bitmap orygImg = BitmapFactory.decodeByteArray(data, 0, data.length);
                postProcessBitmap(orygImg);
            }
        };

        /*if(askForCameraPermission())
            init();*/
        if( GetPermission.camera(this) ){
            init();
        }
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



    private boolean init()
    {
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            Toast.makeText(this, R.string.camera_no_cameras, Toast.LENGTH_SHORT).show();
            return false;
        }

        return openCamera( isUsingBackCamera ? 0 : 1 );
    }
    private boolean openCamera(int cameraID)
    {
        mCamera = null;
        try {
            mCamera = Camera.open(cameraID); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
            Toast.makeText(this, R.string.camera_no_cameras, Toast.LENGTH_SHORT).show();
            return false;
        }
        /*
        //change resolution to the lowest one
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size lowestSize = sizes.get(0);
        for (Camera.Size size : sizes) {
            Log.i(TAG, "Available resolution: "+size.width+" "+size.height);
            if ( size.width < lowestSize.width ) {
                lowestSize = size;
                break;
            }
        }
        Log.i(TAG, "Chosen resolution: "+lowestSize.width+" "+lowestSize.height);
        params.setPictureSize(lowestSize.width, lowestSize.height);
        mCamera.setParameters(params);
        */

        // Create an instance of Camera
        //camera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        if(preview != null) {
            CameraPreview cameraPreview = new CameraPreview(this, mCamera);
            preview.removeAllViews();
            preview.addView(cameraPreview);
        }


        return true;
    }

    ///===============PERMISSION STUFF==============================================================

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("Camera", "processing request result..");
        switch (requestCode) {
            case GetPermission.CAMERA_REQUEST_BACKCODE:
                if (grantResults.length > 0) {
                    //boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);

                    if (/*locationAccepted && */cameraAccepted) {
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access location data and camera", Toast.LENGTH_LONG).show();
                        init();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.camera_permission_denied, Toast.LENGTH_LONG).show();
                        //Try anyway :p
                        //init();
                    }
                }
                break;
        }
    }
    /*private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(CaptureActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }*/
    ///===============END OF PERMISSION STUFF=======================================================

    private void postProcessBitmap(final Bitmap orygImg)
    {
        setContentView(R.layout.camera_post_layout);
        //final Bitmap img = Utils.tilelize(BitmapFactory.decodeByteArray(data, 0, data.length), 100);
        final ImageView imgView = new ImageView(getBaseContext());
        imgView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //imgView.setImageBitmap(Utils.upscale(Utils.tilelize(orygImg, 100), widthInPixels));
        imgView.setImageBitmap(Utils.upscale(Utils.removeFamiliarColors(Utils.tilelize(orygImg, 100), 5), widthInPixels));
        ((LinearLayout)findViewById(R.id.camera_post_layout)).addView(imgView, 0);
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Bitmap tmpImg = orygImg;
                if(i <= 0)
                    return;

                int progress_tilelizeing = ((SeekBar)findViewById(R.id.camera_post_sb_tilelizeAmount)).getProgress();
                int progress_whitening = ((SeekBar)findViewById(R.id.camera_post_sb_whiteningFactor)).getProgress();
                int progress_rotation = ((SeekBar)findViewById(R.id.camera_post_sb_rotation)).getProgress();

                //tmpImg = Utils.whitening(tmpImg, factor);
                //tmpImg = Utils.whitening(tmpImg, 1/factor);

                        /*tmpImg = Utils.tilelize(tmpImg, progress_tilelizeing );
                        //tmpImg = Utils.removeFamiliarColors(tmpImg, i+1);
                        tmpImg = Utils.whitening(tmpImg, progress_whitening );
                        tmpImg = Utils.whitening(tmpImg, 1.f / progress_whitening );
                        ///tmpImg = Utils.whitening(tmpImg, 5.0f);
                        ///tmpImg = Utils.whitening(tmpImg, 0.2f);
                        //imgView.setImageBitmap(Utils.removeFamiliarColors(tmpImg, i));
                        tmpImg = Utils.upscale(tmpImg, widthInPixels);*/
                imgView.setImageBitmap(Utils.upscale(
                        Utils.rotate(
                        Utils.whitening(Utils.whitening(Utils.tilelize(orygImg, progress_tilelizeing), progress_whitening), 1.f/progress_whitening)
                        , 90.f * progress_rotation)
                        , widthInPixels
                        ));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        ((SeekBar)findViewById(R.id.camera_post_sb_tilelizeAmount)).setOnSeekBarChangeListener(onSeekBarChangeListener);
        ((SeekBar)findViewById(R.id.camera_post_sb_whiteningFactor)).setOnSeekBarChangeListener(onSeekBarChangeListener);
        ((SeekBar)findViewById(R.id.camera_post_sb_rotation)).setOnSeekBarChangeListener(onSeekBarChangeListener);
        ((Button)findViewById(R.id.camera_post_b_accept)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MainActivity.bitmap = img;
                int progress_tilelizeing = ((SeekBar)findViewById(R.id.camera_post_sb_tilelizeAmount)).getProgress();
                int progress_whitening = ((SeekBar)findViewById(R.id.camera_post_sb_whiteningFactor)).getProgress();
                int progress_rotation = ((SeekBar)findViewById(R.id.camera_post_sb_rotation)).getProgress();
                DatabaseEntry databaseEntry = new DatabaseEntry();
                databaseEntry.category = "Camera";
                long sandboxID = SaveManager.save_sandbox(getBaseContext(), databaseEntry,
                        Utils.rotate(
                                Utils.whitening(Utils.whitening(Utils.tilelize(orygImg, progress_tilelizeing), progress_whitening), 1.f/progress_whitening)
                                , 90.f * progress_rotation)
                );
                Intent gameIntent = new Intent(getBaseContext(), GameActivity.class);
                gameIntent.putExtra("isSandbox", true);
                gameIntent.putExtra("isMultiplayer", false);
                gameIntent.putExtra("imgID", sandboxID);
                startActivity(gameIntent);

                //Reload MainActivity
                finish();
                MainActivity.reload = true;
            }
        });
    }
    ///===========BUTTON ONCLICKS & BACK HANDLERS===================================================================
    public void onCameraSwapClick(View v)
    {
        isUsingBackCamera = !isUsingBackCamera;
        releaseCamera();
        openCamera( isUsingBackCamera ? 0 : 1 );
    }
    public void onCameraShotClick(View v)
    {
        //                  smth, raw,     jpg
        if(mCamera != null) {
            mCamera.takePicture(null, null, mPicture);
        }else{
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
            GetPermission.camera(this);
        }
    }
    ///===========END===============================================================================


    @Override
    protected void onResume() {
        super.onResume();
        init();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }
    /*private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }*/
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);



        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            if(getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT){
                mCamera.setDisplayOrientation(90);
            }else{
                mCamera.setDisplayOrientation(0);
            }

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }
}