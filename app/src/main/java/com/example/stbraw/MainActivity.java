package com.example.stbraw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;

import com.example.stbraw.async.AsyncResponse;
import com.example.stbraw.async.DownloadFromInternet;
import com.example.stbraw.ml.BagModel;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
    DownloadFromInternet dd = new DownloadFromInternet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dd.delegate = this;
        dd.execute();


        System.out.println(2);



    }

    @Override
    public void processFinish(Bitmap output) {
        System.out.println(output);
        ArrayList<Recognition> result = new ArrayList<>();

        try {
            BagModel model = BagModel.newInstance(this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(output);

            // Runs model inference and gets result.
            BagModel.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            for (Category prob : probability) {
                result.add(new Recognition(prob.getLabel(), prob.getScore()));
            }
            // Releases model resources if no longer used.
            model.close();


            TextView predictionView = findViewById(R.id.prediction);

            predictionView.setText(result.toString());

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