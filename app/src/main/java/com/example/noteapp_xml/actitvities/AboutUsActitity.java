package com.example.noteapp_xml.actitvities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noteapp_xml.R;

public class AboutUsActitity extends AppCompatActivity {

    ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us_actitity);
        backBtn = findViewById(R.id.backToMain);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AboutUsActitity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}