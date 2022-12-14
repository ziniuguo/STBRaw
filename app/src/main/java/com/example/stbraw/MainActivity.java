package com.example.stbraw;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stbraw.async.AsyncResponse;
import com.example.stbraw.async.DownloadFromInternet;
import com.example.stbraw.ml.BagModel;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse, IPickResult {
    DownloadFromInternet dd = new DownloadFromInternet();
    TextView predictionView1;
    TextView predictionView2;
    TextView predictionView3;
    Button upload_img_btn;
    ImageView uploaded_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        predictionView1 = findViewById(R.id.prediction1);
        predictionView2 = findViewById(R.id.prediction2);
        predictionView3 = findViewById(R.id.prediction3);

//        predictionView1.setVisibility(View.INVISIBLE);
//        predictionView2.setVisibility(View.INVISIBLE);
//        predictionView3.setVisibility(View.INVISIBLE);

        upload_img_btn = findViewById(R.id.upload_img_btn);
        uploaded_img = findViewById(R.id.uploaded_img);

        dd.delegate = this;
        dd.execute();

        upload_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                PickImageDialog.build(new PickSetup()).show(MainActivity.this);
//                launchSomeActivity.launch(i);
            }
        });
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            Toast.makeText(this, "Image uploaded", Toast.LENGTH_LONG).show();

            processFinish(r.getBitmap());
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    ActivityResultLauncher<Intent> launchSomeActivity
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null
                            && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap;

                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    selectedImageUri);
                            uploaded_img.setImageBitmap(selectedImageBitmap);
                            processFinish(selectedImageBitmap);
                            Toast.makeText(MainActivity.this, "Details Saved Successfully.", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    public void processFinish(Bitmap output) {
        ArrayList<Recognition> result = new ArrayList<>();

        try {
            BagModel model = BagModel.newInstance(this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(output);

            // Runs model inference and gets result.
            BagModel.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            int count = 0;
            for (Category prob : probability) {
                if (count > 2){
                    break;
                }
                count+=1;
                result.add(new Recognition(prob.getLabel(), prob.getScore()));
            }
            // Releases model resources if no longer used.
            model.close();

            predictionView1.setText(result.get(0).toString());
            predictionView2.setText(result.get(1).toString());
            predictionView3.setText(result.get(2).toString());
            predictionView1.setVisibility(View.VISIBLE);
            predictionView2.setVisibility(View.VISIBLE);
            predictionView3.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // should use Records
    static class Recognition {
        private final String label;
        private final Float confidence;

        Recognition(String label, Float confidence) {
            this.label = label;
            this.confidence = confidence;
        }

        @NonNull
        @Override
        public String toString() {
            return label + "/" + String.format("%.1f%%", confidence);
        }
    }
}