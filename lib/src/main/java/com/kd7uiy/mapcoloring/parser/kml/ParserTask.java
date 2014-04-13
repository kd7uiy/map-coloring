package com.kd7uiy.mapcoloring.parser.kml;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ParserTask extends AsyncTask<InputStream, Integer, ArrayList<Country>> {

    private static final String TAG = ParserTask.class.getName();

    private static final int PROGRESS_MAX = 250;

    private final MainActivity activity;

    private final KmlParser parser = new KmlParser(this);

    private ProgressBar progress;

    public ParserTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        progress = (ProgressBar) activity.findViewById(R.id.progress);
        progress.setMax(PROGRESS_MAX);
        progress.setVisibility(View.VISIBLE);
        progress.setProgress(0);
    }

    @Override
    protected ArrayList<Country> doInBackground(InputStream... inStreams) {
        try {
            return parser.parse(inStreams[0]);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                inStreams[0].close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Country> countries) {
        activity.setCountries(countries);
        progress.setProgress(PROGRESS_MAX);
        progress.setVisibility(View.GONE);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progress.setProgress(values[0]);
    }

    void publishProgress(int progress) {
        super.publishProgress(progress);
    }

}
