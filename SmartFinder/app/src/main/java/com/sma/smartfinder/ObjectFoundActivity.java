package com.sma.smartfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.sma.object.recognizer.api.Recognition;

import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sma.com.smartfinder.R;

public class ObjectFoundActivity extends BaseActivity {

    /**
     * List of recognitions
     */
    private ListView findings;

    /**
     * Recognitions array
     */
    private List<Recognition> recognitions;

    /**
     * Adapter
     */
    private ArrayAdapter<String> adapter;


    public static final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
            new ObjectFoundActivity.ByteArrayToBase64TypeAdapter()).create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_found2);

        findings = findViewById(R.id.list_findings);

        //FIXME Too much memory
        //String json = getIntent().getStringExtra("recognitions");
        //recognitions = customGson.fromJson(json, new TypeToken<Collection<Recognition>>(){}.getType());

        final List<String> recognitionNames = getIntent().getStringArrayListExtra("recognitions");
        List<String> titles = new ArrayList<>();

        for(String title : recognitionNames) {
            String fixed = title.split(":")[0];
            titles.add(fixed.substring(0, 1).toUpperCase() + fixed.substring(1));
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                titles);
        findings.setAdapter(adapter);

        findings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(ObjectFoundActivity.this, ObjectFoundDetailsActivity.class).putExtra("image", recognitionNames.get(i)));
                System.out.println(i + " ");
            }
        });

    }

    // Using Android's base64 libraries. This can be replaced with any base64 library.
    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decode(json.getAsString(), Base64.NO_WRAP);
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.encodeToString(src, Base64.NO_WRAP));
        }
    }
}
