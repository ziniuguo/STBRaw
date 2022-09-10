package com.example.stbraw.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.net.URL;

public class DownloadFromInternet extends AsyncTask<String, Void, Bitmap> {

    public AsyncResponse delegate = null;

    @Override
    protected Bitmap doInBackground(String... params) {
        URL url;
        Bitmap bitmap = null;
        try {
            url = new URL("https://i.stack.imgur.com/7ZFuD.jpg");
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }



    @Override
    protected void onPostExecute(Bitmap result) {
        delegate.processFinish(result);
    }
}
