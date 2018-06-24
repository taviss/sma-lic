package com.sma.smartfinder;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.sma.smartfinder.services.ObjectRecoginzerService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sma.com.smartfinder.R;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

/**
 * Activity for recognizing a new object
 */
public class DetectActivity extends BaseActivity {

    private static final String TAG = DetectActivity.class.getSimpleName();

    private static final int INPUT_SIZE = 600;

    private Button btnDetectObject, btnToggleCamera;
    /**
     * The captured image
     */
    private ImageView imageViewResult;

    /**
     * View for camera
     */
    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        cameraView = (CameraView) findViewById(R.id.cameraView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        imageViewResult.setVisibility(View.GONE);

        btnToggleCamera = (Button) findViewById(R.id.btnToggleCamera);
        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);

        // When taking a picture, send it for recognition
        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);

                Toast.makeText(getApplicationContext(), "Analyzing image...", Toast.LENGTH_LONG).show();

                // Decode the picture to a bitmap and display it
                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                cameraView.setVisibility(View.GONE);
                imageViewResult.setVisibility(View.VISIBLE);
                imageViewResult.setScaleType(ImageView.ScaleType.FIT_XY);
                imageViewResult.setImageBitmap(bitmap);
                Intent requestRecognizeIntent = new Intent(DetectActivity.this, ObjectRecoginzerService.class);

                // Save the image to file for later usage by recognition service
                try {
                    String filename = "img_locate.jpeg";
                    FileOutputStream stream = openFileOutput(filename, Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    stream.close();

                    requestRecognizeIntent.putExtra("locate_image", filename);
                    startService(requestRecognizeIntent);
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "There was an error with sending the image to the ObjectRecognizerService!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFacing();
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume() DetectActivity");
        imageViewResult.setVisibility(View.GONE);
        cameraView.setVisibility(View.VISIBLE);
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Terminate activity if permission for camera is not granted
        int i = 0;
        for(String permission : permissions) {
            if(permission.equals(Manifest.permission.CAMERA) || permission.equals(Manifest.permission.CAPTURE_AUDIO_OUTPUT)) {
                if(grantResults[i] == PERMISSION_DENIED) {
                    finish();
                    break;
                }
            }
            i++;
        }
    }
}
