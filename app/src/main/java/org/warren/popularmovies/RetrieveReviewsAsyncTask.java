package org.warren.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Warren on 8/12/2015.
 */
public class RetrieveReviewsAsyncTask extends AsyncTask<Integer, Void, Map<String, List<String>>> {

    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String END_URL = "/reviews";
    private static final String API_STRING_EXTRA = "api_key";

    private WeakReference<DetailsFragment> mDetailsFragmentWeakReference;
    private String mApiKey;

    public RetrieveReviewsAsyncTask(DetailsFragment detailsFragment, String apiKey) {
        super();
        mDetailsFragmentWeakReference = new WeakReference<>(detailsFragment);
        this.mApiKey = apiKey;
    }

    @Override
    protected Map<String, List<String>> doInBackground(Integer... movieID) {

        Map<String, List<String>> results = new HashMap<>();

        Uri builtUri = null;
        if (mApiKey != null) {
            builtUri = Uri.parse(BASE_URL + movieID[0] + END_URL)
                    .buildUpon()
                    .appendQueryParameter(API_STRING_EXTRA, mApiKey)
                    .build();
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String reviewsJsonStr = null;

        if (builtUri != null) {
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder sb = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return results;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    sb.append(line + "\n");
                }

                if (sb.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return results;
                }
                reviewsJsonStr = sb.toString();
            } catch (IOException e) {
                Log.e("RetrieveTrailers", "Error ", e);
                // If the code didn't successfully get the movies data, there's no point in attempting
                // to parse it.
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("RetrieveTrailers", "Error closing stream", e);
                    }
                }
            }
        }

        try {
            JSONObject jsonObject = new JSONObject(reviewsJsonStr);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for (int i=0; i<jsonArray.length(); ++i) {
                JSONObject resultsObj = (JSONObject) jsonArray.get(i);
                String id = resultsObj.getString("id");
                String author = resultsObj.getString("author");
                String content = resultsObj.getString("content");
                List<String> resultsList = new ArrayList<>();
                resultsList.add(author);
                resultsList.add(content);
                results.put(id, resultsList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    protected void onPostExecute(Map<String, List<String>> results) {
        super.onPostExecute(results);
        DetailsFragment detailsFragment = mDetailsFragmentWeakReference.get();
        if (detailsFragment != null) {
            ViewSwitcher reviewsViewSwitcher = detailsFragment.getReviewsViewSwitcher();
            LinearLayout reviewsLinearLayout = (LinearLayout) reviewsViewSwitcher.findViewById(R.id.reviewsLinearLayout);
            if (results.isEmpty()) {
                TextView noResultsText = new TextView(detailsFragment.getActivity());
                noResultsText.setText("There are no reviews for this movie.");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                noResultsText.setLayoutParams(params);
                reviewsLinearLayout.addView(noResultsText);
            } else {
                for (String key : results.keySet()) {
                    List<String> resultsList = results.get(key);
                    String author = resultsList.get(0);
                    String content = resultsList.get(1);
                    TextView authorTextView = new TextView(detailsFragment.getActivity());
                    authorTextView.setText("Author: " + author);
                    TextView contentTextView = new TextView(detailsFragment.getActivity());
                    contentTextView.setText("Content: " + content);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.LEFT;
                    authorTextView.setLayoutParams(params);
                    contentTextView.setLayoutParams(params);
                    reviewsLinearLayout.addView(authorTextView);
                    reviewsLinearLayout.addView(contentTextView);
                }
            }

            reviewsViewSwitcher.showNext();
        }
    }
}
