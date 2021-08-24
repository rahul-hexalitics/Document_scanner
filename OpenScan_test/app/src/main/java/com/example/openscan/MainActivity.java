package com.example.openscan;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.monscanner.ScanActivity;
import com.example.monscanner.ScanConstants;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 7;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView= findViewById(R.id.finalImageView);

        checkCamera2Support();
    }


    public void openGalerie(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else {
            startScan(ScanConstants.OPEN_GALERIE);
        }
    }

    public void openCamera(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        else {
            startScan(ScanConstants.OPEN_CAMERA);
        }
    }

    private void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                try {
                    assert data != null;
                    Uri imageUri = Objects.requireNonNull(data.getExtras()).getParcelable(ScanActivity.SCAN_RESULT);
                    assert imageUri != null;
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap scannedImage = BitmapFactory.decodeStream(imageStream);
                    getContentResolver().delete(imageUri, null, null);
                    imageView.setImageBitmap(scannedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean allowCamera2Support(int cameraId) {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraIdS = manager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIdS);
            int support = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

            if( support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY )
                Log.d("TAG", "Camera " + cameraId + " has LEGACY Camera2 support");
            else if( support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED )
                Log.d("TAG", "Camera " + cameraId + " has LIMITED Camera2 support");
            else if( support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL )
                Log.d("TAG", "Camera " + cameraId + " has FULL Camera2 support");
            else
                Log.d("TAG", "Camera " + cameraId + " has unknown Camera2 support?!");

            return support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED || support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void checkCamera2Support() {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            if( getNumberOfCameras() == 0 ) {
                Log.d("TAG", "0 cameras");
            }else {
                for (int i = 0; i < getNumberOfCameras(); i++) {
                    if (!allowCamera2Support(i)) {
                        Log.d("TAG", "camera " + i + " doesn't have limited or full support for Camera2 API");
                    }else{
                        // here you can get ids of cameras that have limited or full support for Camera2 API
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public int getNumberOfCameras() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            return manager.getCameraIdList().length;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch(AssertionError e) {
            e.printStackTrace();
        }
        return 0;
    }
}
