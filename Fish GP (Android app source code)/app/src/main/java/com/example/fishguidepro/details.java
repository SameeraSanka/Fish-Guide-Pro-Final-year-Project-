package com.example.fishguidepro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class details extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent intent = getIntent();
        String fishNameForDetails = intent.getStringExtra("fishNamForPass");
        FragmentManager fragmentManager = getSupportFragmentManager();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkBlue)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if ("damkolapethiya".equals(fishNameForDetails)) {
            fragmentManager.beginTransaction().replace(R.id.fragmentMain, DankolaPethiyaDetails.class, null).
                    setReorderingAllowed(true).addToBackStack("damkolapethiya").commit();
        }
        else if ("bulath hapaya".equals(fishNameForDetails)) {
            fragmentManager.beginTransaction().replace(R.id.fragmentMain, BulathHapayaDetails.class, null).
                    setReorderingAllowed(true).addToBackStack("bulath hapaya").commit();
        }
        else if ("kawaiya".equals(fishNameForDetails)) {
            fragmentManager.beginTransaction().replace(R.id.fragmentMain, KawaiyaDetail.class, null).
                    setReorderingAllowed(true).addToBackStack("kawaiya").commit();
        }
        else if ("galpadiya".equals(fishNameForDetails)) {
            fragmentManager.beginTransaction().replace(R.id.fragmentMain, GalpadiyaDetails.class, null).
                    setReorderingAllowed(true).addToBackStack("galpadiya").commit();
        }
        else if ("thalkossa (belontia signat )".equals(fishNameForDetails)) {
            fragmentManager.beginTransaction().replace(R.id.fragmentMain, ThalkossaDetails.class, null).
                    setReorderingAllowed(true).addToBackStack("thalkossa (belontia signat )").commit();
        }
    }
}