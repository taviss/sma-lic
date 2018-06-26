package com.sma.smartfinder;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sma.smartfinder.db.ObjectContract;
import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import sma.com.smartfinder.R;

public class UserRecognitionActivity extends AppCompatActivity {
    private static final String TAG = UserRecognitionActivity.class.getName();

    private Canvas canvas;
    private Paint paint = new Paint();
    private Bitmap bitmap;
    private Bitmap originalBitmap;
    private ImageView imageView;
    private Rect rect = new Rect();
    
    private int mColorRectangle;
    private int mColorAccent;

    private TextView instructions;
    
    private Button acceptButton;

    //xmin, xmax, ymin, ymax
    private float[] coords = new float[4];
    private int step = 0;

    private LinearLayout linearLayout;

    private Button objectLabelButton = null;

    private EditText objectLabel = null;

    private String label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        mColorRectangle = ResourcesCompat.getColor(getResources(),
                R.color.colorRectangle, null);
        mColorAccent = ResourcesCompat.getColor(getResources(),
                R.color.colorAccent, null);

        imageView = (ImageView) findViewById(R.id.draw_view);
        instructions = findViewById(R.id.draw_instruction);
        
        acceptButton = findViewById(R.id.accept_drawing);

        objectLabel = (EditText) findViewById(R.id.object_label_text);
        objectLabelButton = (Button) findViewById(R.id.object_label_button);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(coords[0] > 0 && coords[1] > 0 && coords[2] > 0 && coords[3] > 0) {
                    showLabelInput();
                } else {
                    Toast.makeText(getApplicationContext(), "You haven't delimited your object!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Load the image
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            originalBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        linearLayout = (LinearLayout)findViewById(R.id.draw_layout);

        ViewTreeObserver vto = linearLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                linearLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width  = imageView.getMeasuredWidth();
                int height = imageView.getMeasuredHeight();
                bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                bitmap = Bitmap.createScaledBitmap(bitmap, (int)width, (int)height, false);
                imageView.setImageBitmap(bitmap);
            }
        });


        // the purpose of the touch listener is just to store the touch X,Y coordinates
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float height = imageView.getHeight();
                final float width = imageView.getWidth();

                System.out.println("height, width = " + height + " " + width);
                // save the X,Y coordinates
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    switch(step) {
                        case 0: {
                            // Reset the drawing
                            bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            bitmap = Bitmap.createScaledBitmap(bitmap, (int)width, (int)height, false);
                            imageView.setImageBitmap(bitmap);
                            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            canvas = new Canvas(bitmap);
                            imageView.invalidate();

                            System.out.println("x,y = " + event.getX() + " " + event.getY());
                            coords[0] = event.getX() / width;
                            coords[1] = event.getY() / height;
                            instructions.setText("Click the bottom right corner");
                            instructions.invalidate();
                            step++;
                            System.out.println("Clicked top left: " + coords[0] + " " + coords[2]);
                            break;
                        }
                        case 1: {
                            System.out.println("x,y = " + event.getX() + " " + event.getY());
                            coords[2] = event.getX() / width;
                            coords[3] = event.getY() / height;
                            System.out.println("Clicked bottom right: " + coords[1] + " " + coords[3]);
                            // Change the color by subtracting an integer.
                            paint.setColor(mColorAccent);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(5f);
                            System.out.format("Print at (%d %d %d %d)",
                                    (int)(coords[0] * imageView.getWidth()),(int)(coords[1]*imageView.getHeight()), (int)(coords[2]*imageView.getWidth()), (int)(coords[3]*imageView.getHeight()));
                            System.out.println();
                            rect.set((int)(coords[0] * imageView.getWidth()), (int)(coords[1]*imageView.getHeight()), (int)(coords[2]*imageView.getWidth()), (int)(coords[3]*imageView.getHeight()));
                            canvas.drawRect(rect, paint);
                            imageView.invalidate();
                            step = 0;
                            break;
                        }
                    }
                }

                // let the touch event pass on to whoever needs it
                return false;
            }
        };

        imageView.setOnTouchListener(touchListener);

    }

    /**
     * Updates activity so the user can input the label for an object
     */
    private void showLabelInput() {
        imageView.setVisibility(View.GONE);
        acceptButton.setVisibility(View.GONE);
        objectLabel.setVisibility(View.VISIBLE);
        objectLabelButton.setVisibility(View.VISIBLE);

        objectLabelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                label = objectLabel.getText().toString();
                if(label.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Object label cannot be empty", Toast.LENGTH_LONG).show();
                }


                if(originalBitmap != null) {
                    // Get the class from the user and the current image
                    HashMap<String, String> extras = new HashMap<>();
                    extras.put("imageClass", label);
                    extras.put("trainable", "true");
                    extras.put("boxLeft", String.valueOf(coords[0]));
                    extras.put("boxTop", String.valueOf(coords[1]));
                    extras.put("boxRight", String.valueOf(coords[2]));
                    extras.put("boxBottom", String.valueOf(coords[3]));

                    String user = SmartFinderApplicationHolder.getApplication().getUser();
                    String password = SmartFinderApplicationHolder.getApplication().getPass();
                    String camerasAddress = SmartFinderApplicationHolder.getApplication().getCameraAddress();

                    if(camerasAddress.isEmpty()) {
                        Log.i(TAG, "No camera address!");
                        startActivity(new Intent(UserRecognitionActivity.this, SettingsActivity.class));
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "No camera address!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    // Try uploading the image using the provided data
                    try {
                        Future<Boolean> logged = HTTPUtility.login(camerasAddress + "/login/submit", "userName", user, "userPass", password);
                        if(logged.get()) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            //Bitmap cropped = Bitmap.createBitmap
                            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] byteImage = stream.toByteArray();
                            Future<byte[]> response = HTTPUtility.postImage(camerasAddress + "/images", byteImage, extras);
                            handleResponse(new String(response.get()), byteImage, byteImage);
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    } catch(IOException |JSONException |InterruptedException|ExecutionException e) {
                        Log.i(TAG, "Exception!");
                        startActivity(new Intent(UserRecognitionActivity.this, SettingsActivity.class));
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Something went wrong! Check credentials!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        finish();
                    }
                }
            }
        });
    }

    /**
     * Handles the response from the server and inserts the newly recognized object into the local database if successful
     * @param response
     */
    public void handleResponse(String response, byte[] image, byte[] boundedObject) {
        if(response != null) {
            try {
                // Get the inserted id
                JSONObject jsonObject = new JSONObject(response);
                int id = (int)jsonObject.get("id");

                // Save the PNG image as byte array(BLOB)
                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                //byte[] byteArray = stream.toByteArray();

                ContentValues values = new ContentValues();
                values.put(ObjectContract.Column.ID, id);
                values.put(ObjectContract.Column.OWNER, SmartFinderApplicationHolder.getApplication().getUser());
                values.put(ObjectContract.Column.OBJECT_NAME, label);
                values.put(ObjectContract.Column.IMG, image);
                values.put(ObjectContract.Column.CROPPED_IMG, boundedObject);
                values.put(ObjectContract.Column.CREATED_AT, new Date().getTime());
                Uri uri = getContentResolver().insert(ObjectContract.CONTENT_URI, values);

                if(uri != null) {
                    Log.i(TAG, String.format("Inserted: %s", label));
                }

                return;
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
            }
        }
    }
}
