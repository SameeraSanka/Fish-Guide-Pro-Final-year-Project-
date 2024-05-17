package com.example.fishguidepro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import com.example.fishguidepro.ml.FinalModelNew;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.Nullable;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity {

    Button logout;

    TextView result, confidenceRate;
    ImageView imageView;
    Button camera,gallery,seeMore;
    int imageSize = 224;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkBlue)));

        seeMore=findViewById(R.id.seeMoreButton);
        camera=findViewById(R.id.btn_camera);
        confidenceRate = findViewById(R.id.confidence);
//        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
//        confidenceRate.startAnimation(fadeInAnimation);
        gallery=findViewById(R.id.btn_gallery);
        result=findViewById(R.id.result);
        imageView=findViewById(R.id.imageView);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launch camera if we have permission
                if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    //request camera permission if we don't have
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent,1);
            }
        });
        result.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String fishName = charSequence.toString();
                if(fishName.isEmpty() || "Invalid object".equals(fishName) ){
                    camera.setVisibility(View.VISIBLE);
                    gallery.setVisibility(View.VISIBLE);
                    seeMore.setVisibility(View.GONE);

                }
                else {

                    camera.setVisibility(View.VISIBLE);
                    gallery.setVisibility(View.VISIBLE);
                    seeMore.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        seeMore.setOnClickListener(new View.OnClickListener() {
           @Override

           public void onClick(View view) {
               String fishNamForPass = result.getText().toString();
               Intent intent=new Intent(MainActivity.this,details.class);
               intent.putExtra("fishNamForPass",fishNamForPass);
               startActivity(intent);
    }
    });



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        logout = findViewById(R.id.logout);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), login.class);
        startActivity(intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void classifyImage(Bitmap image){
        try {
            FinalModelNew model =    FinalModelNew.newInstance(getApplicationContext());
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            // get 1D array of 224 * 224 pixels in image
            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }
            //newly add missing code
            inputFeature0.loadBuffer(byteBuffer);
            // Runs model inference and gets result.
            FinalModelNew.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes={"bulath hapaya","damkolapethiya","galpadiya","kawaiya", "others","thalkossa (belontia signat )"};
            String maxConfidenceString = String.format("%s: %.1f%%", classes[maxPos], maxConfidence * 100);
            //float confidenceValue = Float.parseFloat(maxConfidenceString.split(":")[1].trim().replace("%", ""));
            if (classes[maxPos].equals("others")) {
                result.setText("Invalid object");
                confidenceRate.setText(maxConfidenceString);
            } else {
                result.setText(classes[maxPos]);
                confidenceRate.setText(maxConfidenceString);
            }
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode == 3){//when user take picture from camera
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension=Math.min(image.getWidth(),image.getHeight());
                image= ThumbnailUtils.extractThumbnail(image,dimension,dimension);
                imageView.setImageBitmap(image);

                image=Bitmap.createScaledBitmap(image,imageSize,imageSize,false);//resize image
                classifyImage(image);
            }else{ //when user select picture from gallery
                Uri dat=data.getData();
                Bitmap image=null;
                try{
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                }catch (IOException e){
                    e.printStackTrace();
                }

                imageView.setImageBitmap(image);
                image=Bitmap.createScaledBitmap(image,imageSize,imageSize,false);//resize image
                classifyImage(image);

            }

        }
        super.onActivityResult(requestCode, resultCode,data);
    }
}