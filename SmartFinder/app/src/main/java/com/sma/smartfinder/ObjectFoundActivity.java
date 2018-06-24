package com.sma.smartfinder;

import android.content.Intent;
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

        String json = getIntent().getStringExtra("recognitions");
        recognitions = customGson.fromJson(json, new TypeToken<Collection<Recognition>>(){}.getType());

        List<String> recognitionNames = new ArrayList<>();

        for(Recognition recognition : recognitions) {
            recognitionNames.add(recognition.getTitle());
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                recognitionNames);
        findings.setAdapter(adapter);

        findings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(ObjectFoundActivity.this, ObjectFoundDetailsActivity.class).putExtra("image", recognitions.get(i).getSource()).putExtra("name", recognitions.get(i).getTitle()));
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
