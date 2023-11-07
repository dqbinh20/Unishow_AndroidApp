package com.example.scanqrdevmtdn;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.scanqrdevmtdn.databinding.ActivityMainBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import okhttp3.*;
//import okhttp3.FormBody;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer failMediaPlayer = new MediaPlayer();
    private MediaPlayer trueMediaPlayer = new MediaPlayer();
    private ActivityMainBinding binding;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
               if(isGranted){
                   showCamera();
               }else {
                   // show why user need this permission
               }
            });
    private ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(), result -> {
       if(result.getContents() == null){
           Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
       } else {
           if (isValidQRCode(result.getContents())){
               trueMediaPlayer.start();
           }else {
               failMediaPlayer.start();
           }
       }
    });

    private boolean isValidQRCode(String contents) {
//        String url = "http://192.168.1.13:3000/check-valid-qr-code";
        String url = "h3000/check-valid-qr-code";
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");

        String name = "Duong QUang Binhf";
        RequestBody body = RequestBody.create(mediaType, "{\"key\":\"" + name + "\"}");
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            setResult("result.getContents()");
            String responseBody = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        return true;



//        String url = "http://api.qrserver.com/v1/create-qr-code/?size=400x400&data=" + contents;
//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .addHeader("Content-Type", "application/json")
//                .build();
//        try {
//            Response response = client.newCall(request).execute();
//            String responseBody = response.body().string();
//            // Xử lý dữ liệu phản hồi tại đây
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return true;





//        OkHttpClient unishowClient = new OkHttpClient();
//
//        RequestBody requestBody = new FormBody.Builder()
//                .add("content", contents)
//                .build();
//        Request request = new Request.Builder()
////                .url("http://ec2-3-27-195-167.ap-southeast-2.compute.amazonaws.com/check-valid-qr-code")
//                .url("http://192.168.1.24:3000/check-valid-qr-code")
//                .post(requestBody)
//                .build();
//
//        try{
//            Response response = unishowClient.newCall(request).execute();
//            if (response.isSuccessful()){
//                String responseData = response.body().string();
//                setResult(responseData);
//                return true;
//            }else {
//                Toast.makeText(this, "Error send to server", Toast.LENGTH_SHORT).show();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return false;
    }

    private void playMedia(String contents) {
        String data;
        String hashCode;
        String privateKey = "privateKey";
        try {
            int indexHashCode = contents.indexOf("HashCode: ");
            if (indexHashCode != -1){
                data = contents.substring(0,indexHashCode-1);
                hashCode = contents.substring(indexHashCode+10,indexHashCode+10+64);
                if(Objects.equals(hashCode,sha256Hash(data + "\n" + privateKey))){
                    trueMediaPlayer.start();
                    setResult(data);
                    return;
                }
            }
            failMediaPlayer.start();
        }catch (Exception e){
            failMediaPlayer.start();
        }
    }
    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] enCodedHash = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : enCodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setResult(String contents) {
        binding.textResult.setText(contents);
    }

    private void showCamera() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan QR code");
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(false);

        qrCodeLauncher.launch(options);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBinding();
        initView();

        try {
            String url = "https://cms-public-artifacts.artlist.io/Y29udGVudC9zZngvYWFjLzU2MzEyXzQ2NDY0MF80NjQ2MzlfQmVlcF8wNF9kb3duX25vcm1hbC5hYWM="; // your URL here
            failMediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            failMediaPlayer.setDataSource(url);
            failMediaPlayer.prepare(); // might take long! (for buffering, etc)
        }catch (Exception e){
            Toast.makeText(this, "Error media", Toast.LENGTH_SHORT).show();
        }
        try {
            String url = "https://cms-public-artifacts.artlist.io/Y29udGVudC9zZngvYWFjLzI1NTI0XzMzNjAxOV8zMzYwMTlfOGJpdF9zdGF0dXNfcG9pbnRfMjlfbm9ybWFsLmFhYw=="; // your URL here
            trueMediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            trueMediaPlayer.setDataSource(url);
            trueMediaPlayer.prepare(); // might take long! (for buffering, etc)
        }catch (Exception e){
            Toast.makeText(this, "Error media", Toast.LENGTH_SHORT).show();
        }

        String url = "http://192.168.1.13:3000/check-valid-qr-code";
//        String url = "h3000/check-valid-qr-code";
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");

        String name = "Duong QUang Binhf";
        RequestBody body = RequestBody.create(mediaType, "{\"key\":\"" + name + "\"}");
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            setResult("result.getContents()");
            String responseBody = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        binding.fab.setOnClickListener(view -> {
            checkPermissionAndShowActivity(this);
        });
    }

    private void checkPermissionAndShowActivity(Context context) {
        if(ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED){
            showCamera();
        }else if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show();
        }else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void initBinding() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}