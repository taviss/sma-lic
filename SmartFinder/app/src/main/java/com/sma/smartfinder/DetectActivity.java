package com.sma.smartfinder;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import com.sma.smartfinder.db.ObjectContract;
import com.sma.smartfinder.object.recognition.Classifier;
import com.sma.smartfinder.object.recognition.TensorFlowImageClassifier;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sma.com.smartfinder.R;

public class DetectActivity extends BaseActivity {

    private static final String TAG = DetectActivity.class.getSimpleName();

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnToggleCamera, btnAcceptObject;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private Pair<String, byte[]> currentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        cameraView = (CameraView) findViewById(R.id.cameraView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        btnToggleCamera = (Button) findViewById(R.id.btnToggleCamera);
        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);
        btnAcceptObject = (Button) findViewById(R.id.btnAcceptObject);

        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);

                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                imageViewResult.setImageBitmap(bitmap);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                if(results != null && !results.isEmpty()) {
                    textViewResult.setText(results.toString());

                    currentImage = new Pair<>(results.get(0).getTitle(), picture);

                    btnAcceptObject.setVisibility(View.VISIBLE);
                } else {
                    imageViewResult.setImageBitmap(null);
                    textViewResult.setText("");
                    btnAcceptObject.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "No object detected, try again!", Toast.LENGTH_SHORT).show();
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

        btnAcceptObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentImage != null) {
                    ContentValues values = new ContentValues();
                    values.put(ObjectContract.Column.OBJECT_NAME, currentImage.first);
                    values.put(ObjectContract.Column.IMG, currentImage.second);
                    values.put(ObjectContract.Column.CREATED_AT, new Date().getTime());
                    Uri uri = getContentResolver().insert(ObjectContract.CONTENT_URI, values);

                    if(uri != null) {
                        Log.i(TAG, String.format("Inserted: %s", currentImage.first));
                    }
                }
                imageViewResult.setImageBitmap(null);
                textViewResult.setText("");
                btnAcceptObject.setVisibility(View.INVISIBLE);
            }
        });

        initTensorFlowAndLoadModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }
}
