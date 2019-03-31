package PEngine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import pl.com.gemstones.tileartist.MainActivity;
import pl.com.gemstones.tileartist.R;

public class Networking {
    private static final String URL = "http://gemstones.hanusik.pl/tileartist/";
    private static final String URL_donwload = URL + "download.php?app=1";
    private static final String URL_register = URL + "register.php?app=1";
    private static final String URL_login = URL + "login.php?app=1";
    private static final String URL_logout = URL + "logout.php?app=1";
    private static final String URL_multiplayer = URL + "multiplayer.php?app=1";
    private static final String URL_addfriends = URL + "addFriend.php?app=1";
    private static final String URL_profile = URL + "profile.php?app=1";
    private static final String URL_panel = URL + "panel.php?app=1";
    private static final String URL_getfollows = URL + "getFollows.php?app=1";
    private static final String URL_upload = URL + "upload.php?app=1";
    private static final String URL_update_profile = URL + "updateProfile.php?app=1";
    private static final String URL_ads = URL + "showAds.php?app=1";

    public  static final String URL_privacy_policy = URL + "privacy.php";

    static String default_error = "Error";

    MainActivity mainActivity;
    public boolean autologgedin = true;

    private Vector<Long> remoteIDs;
    private long[] localIDs;
    private long multiplayer_started_gameID = -1;

    private CookieManager cookieManager = null;

    public Networking(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
         default_error = this.mainActivity.getResources().getString(R.string.networking_default_error);
        /*try {
            credentials += URLEncoder.encode("login", "UTF-8")
                    + "=" + URLEncoder.encode(user, "UTF-8");

            credentials += "&" + URLEncoder.encode("passwd", "UTF-8")
                    + "=" + URLEncoder.encode(passwd, "UTF-8");
            credentials += "&ed_pass_keydown=&ed_pass_keyup=&captcha=&czy_js=1&loguj_synergia=";
        }catch (UnsupportedEncodingException e){
            credentials = "login=" + user + "&passwd=" + passwd + "&ed_pass_keydown=&ed_pass_keyup=&captcha=&czy_js=1&loguj_synergia=\n";
            e.printStackTrace();
        }*/

        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }
    /*
    * Syncing images should be used by SaveManeger
    * */
    public void syncImage(long[] localIDs)
    {
        remoteIDs = new Vector<>();
        this.localIDs = localIDs;

        Get get = new Get();
        get.execute(URL_donwload, "getIndieces");
    }
    public void getProfileImageBitmap(long imageID)
    {
        Get get = new Get();
        get.execute(URL_donwload, "getImageBitmap", String.valueOf(imageID));
    }

    public void register(String username, String password)
    {
        Get get = new Get();
        get.execute(URL_register, "register", username, password);
    }
    public void login(String username, String password)
    {
        Get get = new Get();
        get.execute(URL_login, "login", username, password);
    }
    public void logout()
    {
        Get get = new Get();
        get.execute(URL_logout, "logout");
    }
    public void autologin(String username, String password)
    {
        Get get = new Get();
        get.execute(URL_login, "autologin", username, password);
    }
    public void updateAccountInfo(String email, String password)
    {
        if(email != null && email.length() > 0){
            Get get = new Get();
            get.execute(URL_update_profile, "accountUpdate", "email", email);
        }
        if(password != null && password.length() > 0){
            Get get = new Get();
            get.execute(URL_update_profile, "accountUpdate", "password", password);
        }
    }
    public void checkAdsVisibility()
    {
        Get get = new Get();
        get.execute(URL_ads, "adsVisibility");
    }

    public void searchFriend(String seachUsername)
    {
        Get get = new Get();
        get.execute(URL_addfriends, "friend_search", seachUsername);
    }
    public void profileFriend(long userID)
    {
        Get get = new Get();
        get.execute(URL_profile, "friend_profile", String.valueOf(userID));
    }
    public void profileMine()
    {
        Get get = new Get();
        get.execute(URL_panel, "friend_profile", "");
    }
    public void addFriend(long friendID)
    {
        Get get = new Get();
        get.execute(URL_addfriends, "friend_add", String.valueOf(friendID));
    }
    public void removeFriend(long friendID)
    {
        Get get = new Get();
        get.execute(URL_addfriends, "friend_remove", String.valueOf(friendID));
    }
    public void getFollows()
    {
        Get get = new Get();
        get.execute(URL_getfollows, "getFollows");
    }
    public void sendMasterpiece(long masterpieceID)
    {
        SendMasterpiece sendMasterpiece = new SendMasterpiece();
        sendMasterpiece.execute(URL_upload, "sendMasterpiece", String.valueOf(masterpieceID));
    }
    public void removeMasterpiece(long masterpieceID)
    {
        Get get = new Get();
        get.execute(URL_upload, "removeMasterpiece", String.valueOf(masterpieceID));
    }

    //MULTIPLAYER
    public void multiplayer_init(String opponentsName, long imageID)
    {
        Get get = new Get();
        get.execute(URL_multiplayer, "multiplayer_init", opponentsName, String.valueOf(imageID));
    }
    public void multiplayer_start(long gameID)
    {
        multiplayer_started_gameID = gameID;
        Get get = new Get();
        get.execute(URL_multiplayer, "multiplayer_start", String.valueOf(gameID));
    }
    public void multiplayer_finish()
    {
        Get get = new Get();
        get.execute(URL_multiplayer, "multiplayer_finish", String.valueOf(multiplayer_started_gameID));
    }
    public void multiplayer_check()
    {
        Get get = new Get();
        get.execute(URL_multiplayer, "multiplayer_check");
    }
    //END


    /*public void handleProgressBar(ProgressBar progressBar)
    {
        this.progressBar = progressBar;
    }*/

    private String[] readStreamAndSeparate(InputStream is, String separator) {
        Vector<String> ret = new Vector<>();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while (-1 != (len = is.read(buffer))) {
                //bo = new ByteArrayOutputStream();
                //bo.write(buffer, 0, len);

                //String[] curChunk =  bo.toString().split(separator);
                String bufferStr = new String(buffer);
                String[] curChunk =  bufferStr.split(separator);
                if(curChunk.length == 1 ){
                    stringBuilder.append(curChunk[0]);
                }else{
                    stringBuilder.append(curChunk[0]);
                    ret.add(stringBuilder.toString());
                    stringBuilder.setLength(0);

                    for(int i = 1; i < curChunk.length -1; ++i){
                        ret.add(curChunk[i]);
                    }

                    stringBuilder.append(curChunk[curChunk.length-1]);
                }
            }
            return ret.toArray(new String[ret.size()]);
        } catch (IOException e) {
            return new String[0];
        }
    }
    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while (-1 != (len = is.read(buffer))) {
                bo = new ByteArrayOutputStream();
                bo.write(buffer, 0, len);
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    private class Get extends AsyncTask<String, Void, String[]> {
        @Override
        public String[] doInBackground(String... params) {
            long imageID = 0;
            if(params[1] == "getImage") {
                imageID = remoteIDs.get(0);
                remoteIDs.remove(0);
            }else if( params[1].equals("getImageBitmap") || params[1].equals("load_db_sandbox") ) {
                try {
                    imageID = Long.parseLong(params[2]);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("Networking", "specified wrong stirng for imageID");
                }
            }

            URL url = null;
            try {
                if(params[1] == "getImage") {
                    url = new URL(params[0] + "&id=" + imageID);
                }else if(params[1].equals("getImageBitmap")){
                    url = new URL(params[0] + "&img=" + imageID);
                }else if(params[1].startsWith("multiplayer")) {

                    if(params[1].equals("multiplayer_init")) {
                        url = new URL(params[0] + "&mode=0");
                    }else if(params[1].equals("multiplayer_start")){
                        url = new URL(params[0] + "&mode=1"
                            + "&gameID=" + params[2]
                        );
                    }else if(params[1].equals("multiplayer_finish")){
                        url = new URL(params[0] + "&mode=2"
                                + "&gameID=" + params[2]
                        );
                    }else if(params[1].equals("multiplayer_check")){
                        url = new URL(params[0] + "&mode=3");
                    }else{
                        Log.w("Networking", "Used incorrect multiplayer mode.");
                    }
                }else if(params[1].equals("removeMasterpiece")){
                    url = new URL(params[0]
                            + "&del=" + params[2]
                    );
                }else {
                    url = new URL(params[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection urlConnection = null;
            String data = default_error;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setUseCaches(false);

                if(params.length >= 4) {
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");

                    String postMsg = "";
                    if(params[1].equals("register")){
                        postMsg = "username=" + URLEncoder.encode(params[2], "UTF-8")
                                + "&" + "password=" + URLEncoder.encode(params[3], "UTF-8")
                                + "&" + "password2=" + URLEncoder.encode(params[3], "UTF-8");
                    }else if(params[1].equals("login")){
                        postMsg = "username=" + URLEncoder.encode(params[2], "UTF-8")
                                + "&" + "password=" + URLEncoder.encode(params[3], "UTF-8");
                    }else if(params[1].equals("autologin")){
                        postMsg = "username=" + URLEncoder.encode(params[2], "UTF-8")
                                + "&" + "password=" + URLEncoder.encode(params[3], "UTF-8");
                    }else if(params[1].equals("multiplayer_init")){
                        postMsg = "opponent=" + URLEncoder.encode(params[2], "UTF-8")
                                + "&" + "imageID=" + URLEncoder.encode(params[3], "UTF-8");
                    }
                    else if(params[1].equals("accountUpdate")){
                        postMsg = params[2] + "=" + URLEncoder.encode(params[3], "UTF-8");
                    }

                    OutputStream os = urlConnection.getOutputStream();
                    os.write(postMsg.getBytes());
                    os.flush();
                }else if(params.length >= 3) {
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");

                    String postMsg = "";
                    if(params[1].equals("friend_search")){
                        postMsg = "search_username=" + URLEncoder.encode(params[2], "UTF-8");
                    }else if(params[1].equals("friend_profile")){
                        postMsg = "user=" + URLEncoder.encode(params[2], "UTF-8");
                    }else if(params[1].equals("friend_add")){
                        postMsg = "add=" + URLEncoder.encode(params[2], "UTF-8");
                    }else if(params[1].equals("friend_remove")){
                        postMsg = "remove=" + URLEncoder.encode(params[2], "UTF-8");
                    }
                    /*else if(params[1].equals("sendMasterpiece")){
                        DatabaseEntry databaseEntry = SaveManager.load_db_sandbox(mainActivity, imageID);
                        postMsg = "title=" + URLEncoder.encode(databaseEntry.title, "UTF-8")
                                + "category=" + URLEncoder.encode(databaseEntry.category, "UTF-8")
                                + "submit=1"
                        ;

                    }*/

                    OutputStream os = urlConnection.getOutputStream();
                    os.write(postMsg.getBytes());
                    os.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
                data =  mainActivity.getResources().getString(R.string.networking_network_unreachable);
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if(params[1].equals("multiplayer_check")) {
                    mainActivity.onMenuMultiplayerGameNetworkingChecked(readStreamAndSeparate(in, "\n"));
                }else if(params[1].equals("friend_profile")) {
                    mainActivity.onMenuAccountFriendProfileGot(readStreamAndSeparate(in, "\n"));
                }else if(params[1].equals("getImageBitmap")){
                    mainActivity.onMenuAccountProfileBitmapReceived(imageID, BitmapFactory.decodeStream(in));
                }else if(params[1].equals("getFollows")){
                    mainActivity.onMenuAccountFollowsGot(readStreamAndSeparate(in, "\n"));
                }else{
                    data = readStream(in);
                }

                if(params[1].equals("getImage")) {
                    //handle response with img
                    Bitmap imgBitmap = null;
                    String[] metadata = data.split("\n");

                    if (params[1].equals("getImage")) {
                        URL urlImg = null;
                        try {
                            url = new URL(URL_donwload + "&img=" + imageID);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        HttpURLConnection urlConnectionImg = null;
                        try {
                            urlConnectionImg = (HttpURLConnection) url.openConnection();
                            urlConnectionImg.setUseCaches(false);

                            InputStream inImg = new BufferedInputStream(urlConnectionImg.getInputStream());
                            imgBitmap = BitmapFactory.decodeStream(inImg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (imgBitmap != null) {
                        SaveManager.save_image("Error ocured during downloading contents from server.",
                                new DatabaseEntry(imageID, metadata[1], metadata[2], metadata[3]),
                                imgBitmap
                        );
                    }else{
                        Log.wtf("Networking", "Couldn't download id=" + imageID);
                    }

                    Log.d("Networking", "Downloaded image id=" + imageID);

                    //GOT IT!!!
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            if(params.length >= 2)
                return new String[] {data, params[1]};
            else
                return new String[] {data};
        }

        protected void onPostExecute(String... result) {
            if(result.length >= 2){
                switch (result[1]){
                    case "getImage":
                        mainActivity.generateMenu();
                        if(remoteIDs.size() > 0){
                            Get getImage1 = new Get();
                            getImage1.execute(URL_donwload, "getImage");
                        }
                        break;
                    case "getIndieces":
                        Log.d("Networking", result[0]);
                        for(String id : result[0].split(" ")) {
                            try {
                                remoteIDs.add(Long.valueOf(id));
                            }catch (NumberFormatException e){
                                e.printStackTrace();
                            }
                        }
                        for(long id : localIDs){
                            if(remoteIDs.contains(id)) {
                                remoteIDs.remove((Object) id);
                            }else{
                                Log.wtf("Networking", "Client has ID(" + id + ") that doesnt exists on server.");
                            }
                        }

                        //Don't allow to download too much at once
                        while(remoteIDs.size() > 20) {
                            remoteIDs.remove(0);
                        }

                        if(remoteIDs.size() > 0){
                            Get getImage0 = new Get();
                            getImage0.execute(URL_donwload, "getImage");
                        }
                        break;

                    case "register":
                        String msgR = mainActivity.getResources().getString(R.string.account_success_register);
                        if(!result[0].startsWith("Success")){
                            msgR = result[0];
                        }else{
                            //success!
                            mainActivity.onMenuAccountButtonBackClick(null);
                            mainActivity.onMenuAccountButtonSelectionLoginClick(null);
                        }
                        Toast.makeText(mainActivity.getBaseContext(), msgR, Toast.LENGTH_SHORT).show();
                        break;
                    case "login":
                        String msgL = mainActivity.getResources().getString(R.string.account_success_login);
                        if(!result[0].startsWith("Success")){
                            msgL = result[0];
                        }else{
                            //success!
                            mainActivity.generateLoggedInSubmenu();
                        }
                        Toast.makeText(mainActivity.getBaseContext(), msgL, Toast.LENGTH_SHORT).show();
                        break;
                    case "autologin":
                        if(!result[0].startsWith("Success")){
                            //Toast.makeText(mainActivity, "Couldn't autologin to your account!!!", Toast.LENGTH_SHORT).show();
                            autologgedin = false;
                        }else{
                            autologgedin = true;
                            if(result[0].charAt(result[0].length() - 1) == '.'){
                                MainActivity.showAds = false;
                            }else{
                                MainActivity.showAds = true;
                                mainActivity.recreateAds();
                            }
                        }
                        break;
                    case "accountUpdate":
                        mainActivity.onMenuSettingsAccSetCompleted(result[0]);
                        break;
                    case "adsVisibility":
                        if(result[0].contains("true")){
                            MainActivity.showAds = true;
                            mainActivity.recreateAds();
                        }else{
                            MainActivity.showAds = false;
                        }
                        break;

                    case "friend_search":
                        if(result[0].equals( mainActivity.getResources().getString(R.string.networking_network_unreachable) )){
                            Toast.makeText(mainActivity, result[0], Toast.LENGTH_SHORT).show();
                        }
                        mainActivity.onMenuAccountFriendSearchCompleted(result[0]);
                        break;
                    case "friend_profile":
                        //already handled in doInBackground
                        //mainActivity.onMenuAccountFriendProfileGot();
                        break;
                    case "friend_add":
                        if(result[0].startsWith("Success")){
                            mainActivity.onMenuAccountFriendAddSuccessfullyCompleted();
                        }else{
                            Toast.makeText(mainActivity.getBaseContext(), result[0], Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "friend_remove":
                        if(result[0].startsWith("Success")){
                            mainActivity.onMenuAccountFriendRemoveSuccessfullyCompleted();
                        }else{
                            Toast.makeText(mainActivity.getBaseContext(), result[0], Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "multiplayer_init":
                        mainActivity.onMenuMultiplayerGameNetworkingCreated();
                        break;
                    case "multiplayer_start":
                        try {
                            mainActivity.onMenuMultiplayerGameNetworkingStarted(Long.valueOf(result[0]));
                        }catch (NumberFormatException e){
                            e.printStackTrace();
                        }
                        break;
                    case "multiplayer_finish":
                        mainActivity.onMenuMultiplayerGameNetworkingFinished();
                        break;
                        //multiplayer_check is handled in doInBackground

                    case "removeMasterpiece":
                        if(!result[0].startsWith("Success")){
                            Toast.makeText(mainActivity.getBaseContext(), result[0], Toast.LENGTH_SHORT).show();
                        }else{
                            profileMine();
                        }

                        break;
                }
            }
        }

    }
    private class SendMasterpiece extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String... params) {
            if(!params[1].equals("sendMasterpiece")){
                return null;
            }

            long masterpieceID = 0;
            try {
                masterpieceID = Long.parseLong(params[2]);
            }catch (Exception e){
                e.printStackTrace();
                Log.e("Networking", "SendMasterpiece: specified wrong stirng for imageID");
            }

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            DatabaseEntry databaseEntry = SaveManager.load_db_sandbox(mainActivity, masterpieceID);
            String data = default_error;
            try {
                String fileName = SaveManager.get_sandbox_path(mainActivity, masterpieceID);
                File sourceFile = new File(fileName);
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(URL_upload);

                // Open a HTTP  connection to  the URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                //TITLE
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
                dos.writeBytes(lineEnd + databaseEntry.title + lineEnd);
                //CATEGORY
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"category\"" + lineEnd);
                dos.writeBytes(lineEnd + databaseEntry.category + lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + String.valueOf(masterpieceID) + ".png" + "\"" + lineEnd);
                dos.writeBytes("Content-Type: image/png" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                /*if (serverResponseCode == 200) {
                    Toast.makeText(mainActivity, "Response: " + serverResponseMessage, Toast.LENGTH_SHORT).show();
                }*/

                InputStream in = new BufferedInputStream(conn.getInputStream());
                data = readStream(in);

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            }catch (SocketException e){
                //e.printStackTrace();
                data = "NetworkUnreachable";
            }catch (Exception e){
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(final String result) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Net", result);
                    if(result.startsWith("Success")){
                        Toast.makeText(mainActivity, mainActivity.getResources().getString(R.string.menu_dialog_sandbox_success), Toast.LENGTH_SHORT).show();
                    }else if(result.equals("NetworkUnreachable")){
                        Toast.makeText(mainActivity, mainActivity.getResources().getString(R.string.networking_network_unreachable), Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(mainActivity, result, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


    public void logCookies()
    {
        Log.d("Networking", "==========COOKIES:==========");
        List<HttpCookie> allCookies = cookieManager.getCookieStore().getCookies();
        for(HttpCookie c : allCookies)
        {
            Log.d(c.getName(), c.getValue());
        }
        Log.d("Networking", "=============END============");
    }
    private void logHeaders(HttpURLConnection urlConnection)
    {
        Map<String, List<String>> headers = urlConnection.getHeaderFields();
        Log.d("Networking", "=======HEADERS===========");
        for(String key : headers.keySet()){
            List<String> tmp = headers.get(key);
            for(String val : tmp)
                Log.d(key, val);
        }
        Log.d("Networking", "=======HEADERS=END=======");
    }
}
