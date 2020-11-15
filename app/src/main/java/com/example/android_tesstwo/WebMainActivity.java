package com.example.android_tesstwo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.Manifest;
import static android.widget.Toast.*;
import static android.widget.Toast.LENGTH_SHORT;
public class WebMainActivity extends AppCompatActivity {


    public static Context mContext;

    private WebView mWebView;
    private ImageView imageView;
    private Handler mHandler;
    private boolean mFlag = false;
    private String registurl;
    private String tmpurl ="";
    private String mCameraPhotoPath;

    dbHelper helper;
    SQLiteDatabase db;


    String notconnect = "<html>"
            + "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi' />"
            + "<style type='text/css'>#hl{width:width:320px;}</style>"
            + "<body>데이터 통신을 사용 할 수 없습니다.<br> 기기에서  3G & 4G 통신 또는  Wi-Fi.<br> 연결상태를 확인하여 주세요. <br> 확인 후 다시 시도하여 주시기 바랍니다. </body>"
            + "</html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_main);

        appinit();
        intiweb();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        imageView = (ImageView) findViewById(R.id.imageView_intro);
        imageView.setMinimumWidth(width);
        imageView.setMinimumHeight(height);
        imageView.setMaxWidth(width);
        imageView.setMaxHeight(height);
    }

    public void appinit() {

    }
    public void intiweb() {
        mWebView = (WebView) findViewById(R.id.webview1);
        mWebView.setWebViewClient(new WebViewClient());

        WebSettings webset = mWebView.getSettings();
        webset.setJavaScriptEnabled(true);
        webset.setSupportZoom(true);
        webset.setUseWideViewPort(false);
        webset.setBuiltInZoomControls(true);
        webset.setJavaScriptCanOpenWindowsAutomatically(true);
        webset.setSupportMultipleWindows(true);
        String userAgent = webset.getUserAgentString();
        userAgent = webset.getUserAgentString() ;
        webset.setUserAgentString(userAgent);

        mWebView.setWebChromeClient(new CustomWebChromeClient());
        mWebView.setWebViewClient(new AxaWebViewClient());


        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String pushurl = pref.getString("pushurl", "");

        if(pushurl.length() >0 ){
            mWebView.loadUrl(pushurl);

            SharedPreferences.Editor editor = pref.edit();
            editor.putString("", pushurl);
            editor.commit();

        }else {
            mWebView.loadUrl(StaticClass.domain);
        }

    }

    private class CustomWebChromeClient extends WebChromeClient {
        private static final int INPUT_FILE_REQUEST_CODE = 1;
        private static final String TYPE_IMAGE = "image/*";

        // For Android Version 5.0+
        // Ref: https://github.com/GoogleChrome/chromium-webview-samples/blob/master/input-file-example/app/src/main/java/inputfilesample/android/chrome/google/com/inputfilesample/MainFragment.java
        @RequiresApi(api = Build.VERSION_CODES.N)
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            System.out.println("WebViewActivity A>5, OS Version : " + Build.VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3");
            ValueCallback<Uri[]> mFilePathCallback = null;
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;
            imageChooser();
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private void imageChooser() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
//                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(getClass().getName(), "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:"+photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType(TYPE_IMAGE);

            Intent[] intentArray;
            if(takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "사진 선택");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            return imageFile;
        }



    }

    private class AxaWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            //mWebView.loadData(notconnect, "text/html", "utf-8");
            super.onReceivedError(view, request, error);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String fallingUrl) {

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            tmpurl = mWebView.getUrl();

            Log.i("url", url);
            String decourl = null;


            mFlag = false;

            if(url.contains("app:login")) {
                String[] id0 = url.split( "id=" );
                String id = id0[1];

                if (id.length() > 2) {  //로그인

                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("id", id);
                    editor.commit();
                    //id 저장 완료
                    return true;
                }
            }

            //회원가입할때
            if(url.contains("app:join")) {
                String[] id0 = url.split( "id=" );
                String ID = id0[1];



                helper = new dbHelper(WebMainActivity.this);

                try {
                    db = helper.getWritableDatabase();
                    //데이터베이스 객체를 얻기 위하여 getWritableDatabse()를 호출

                } catch (SQLiteException e) {
                    db = helper.getReadableDatabase();
                }


                String name = ID;
                String id = ID;
                String pw = "1234";
                String checkPw = "1234";
                String phone = "01011112222";

                UserVO userInfo = new UserVO();
                userInfo.setName(name);
                userInfo.setId(id);
                userInfo.setPasswd(pw);
                userInfo.setPhone(phone);

                StringBuffer sb = new StringBuffer();
                sb.append("INSERT INTO user (");
                sb.append(" id, name, passwd, phone )");
                sb.append(" VALUES (?, ?, ?, ?)");

                String aa =userInfo.getId();
                String bb =userInfo.getName();
                String cc =userInfo.getPasswd();
                String dd =userInfo.getPhone();
                db.execSQL(sb.toString(), new Object[]{ userInfo.getId(), userInfo.getName(), userInfo.getPasswd(), userInfo.getPhone()});
                db.close();

                return true;
            }

            // 웹에서 클릭시 도메인을 통해 연결
            if(url.contains("app://callocr")) {
                Intent intent = new Intent(WebMainActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            //캘린더
            if(url.contains("app://cal")) {
//                Intent cal = new Intent(WebMainActivity.this, MainActivity2.class);
//                startActivity(cal);
//                return true;

                SharedPreferences pref = getSharedPreferences( "pref", MODE_PRIVATE );
                String Id = pref.getString("id", "");



                Intent intent = new Intent(WebMainActivity.this, MainpageActivity.class);
                intent.putExtra("id", Id);
                intent.putExtra("name", Id);
                intent.putExtra("phone", "1234");
                startActivity(intent);
                return true;


            }

            //외부도메인일 경우 새창 띄우기
            if (!(url.contains(StaticClass.domain))) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        // SSL 예외처리
        // 본 샘플에서는 ssl을 이용하는 https 프로토콜을 이용하는 웹페이지에 대해서,
        // 서버인증서가 검증되지 않은 사설 서버인증서를 이용할 경우에 에라로 처리하지 아니하고
        // 진행하기 위해 아래와 같이 onReceivedSslError메소드를 오버라이드 하였음
        @SuppressWarnings("deprecation")
        /*
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (error.getPrimaryError() == SslError.SSL_IDMISMATCH) {
                handler.proceed();
            } else if (error.getPrimaryError() == SslError.SSL_EXPIRED) {
                handler.proceed();
            } else if (error.getPrimaryError() == SslError.SSL_MAX_ERROR) {
                handler.proceed();
            } else if (error.getPrimaryError() == SslError.SSL_NOTYETVALID) {
                handler.proceed();
            } else if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
                handler.proceed();
            } else {
                handler.proceed();
            }
        }

         */

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //imageView.setVisibility(View.GONE); // 로딩이미지 시간으로 하기 위하여 주석처리



            //타입과 같이 처리
            //if(url.equals(StaticClass.domain)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    imageView.setVisibility(View.GONE);
                }
            },2000);

            //}
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        tmpurl ="";
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //String tmpurl ="";
        String aaaurl = mWebView.getUrl();
        String aaaurlaaaaaa =tmpurl;

        // 백 키를 터치한 경우
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mWebView.canGoBack()) {
                if((aaaurl.contains("#"))){
                    //if(aaaurlaaaaaa.length() > 0){
                    mWebView.goBack();
                    //}
                    //return false;
                }else{
                    //if(aaaurlaaaaaa.equals(aaaurl)){
                    //    Log.w("TAG","tmpurl: " +  aaaurlaaaaaa);
                    //    Log.w("TAG","url: " +  aaaurl);
                    //}else{
                    mWebView.goBack();
                    //}
                }
                return false;

            }else {
                if (!mFlag) {
                    Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", LENGTH_SHORT).show();
                    mFlag = true;
                    //mHandler.sendEmptyMessageDelayed(0, 2000); // 2초 내로 터치시
                    return false;
                } else {
                    finish();
                }
                return false;
            }

        }
        return super.onKeyDown( keyCode, event );
    }
    void sendRegistrationToServer(String token_id_reg_url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        //request post
        /*
        RequestBody body = new FormBody.Builder()
                .add("Token", token)
                .build();
        Request request = new Request.Builder()
                .url("http://api.apopy.com/api/push/v1/device/update/")
                .post(body)
                .build();
        esponse response = client.newCall(request).execute();
        return response.body().string();
         */
        //request get

        String sendurl = token_id_reg_url;
        Log.w("TAG","sendurl: " +  sendurl);
        try {
            Request(sendurl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tokensendToServer(String sendurl) throws IOException {
        Request(sendurl);
    }

    private void sendToServer(String userid, String sendurl) throws IOException {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String gcm_token = pref.getString("token", "");
        String pref_housenum =  pref.getString("houseNum","");
        Log.w("TAG","gcm_token: " +  gcm_token);
        //request get
        registurl = StaticClass.domain;
        registurl = registurl + gcm_token;

        registurl = registurl + "&userid=" + userid;

        //registurl = registurl + "&usage=on";
        Log.w("TAG","sendurl: " +  registurl);
        //String response = httpClass.runOnUiThread(registurl);
        Request(registurl);
    }

    void Request(String registurl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        //RequestBody body = RequestBody.create(JSON, postBody);
        Request request = new Request.Builder()
                .url(registurl)
                //       .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.d("TAG", response.toString());
            }
        });
    }

    private void startMain2(String id) {

        Intent intent = new Intent(WebMainActivity.this, MainpageActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", id);
        intent.putExtra("phone", "1234");
        startActivity(intent);

    }

}

