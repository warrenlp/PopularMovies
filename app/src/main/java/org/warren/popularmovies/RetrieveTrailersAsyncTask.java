package org.warren.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
public class RetrieveTrailersAsyncTask extends AsyncTask<Integer, Void, Map<String, List<String>>> {

    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String END_URL = "/videos";
    private static final String API_STRING_EXTRA = "api_key";

    private WeakReference<DetailsFragment> mDetailsFragmentWeakReference;
    private String mApiKey;

    public RetrieveTrailersAsyncTask(DetailsFragment detailsFragment, String apiKey) {
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
                String iso_639_1 = resultsObj.getString("iso_639_1");
                String key = resultsObj.getString("key");
                String name = resultsObj.getString("name");
                String site = resultsObj.getString("site");
                String size = resultsObj.getString("size");
                String type = resultsObj.getString("type");
                if (type.equals("Trailer")) {
                    List<String> resultsList = new ArrayList<>();
                    resultsList.add(iso_639_1);
                    resultsList.add(key);
                    resultsList.add(name);
                    resultsList.add(site);
                    resultsList.add(size);
                    results.put(id, resultsList);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    protected void onPostExecute(Map<String, List<String>> results) {
        super.onPostExecute(results);
        final DetailsFragment detailsFragment = mDetailsFragmentWeakReference.get();
        if (detailsFragment != null) {
            ViewSwitcher trailersViewSwitcher = detailsFragment.getTrailersViewSwitcher();
            LinearLayout trailersLinearLayout = (LinearLayout) trailersViewSwitcher.findViewById(R.id.trailersLinearLayout);
            if (results.isEmpty()) {
                TextView noResultsText = new TextView(detailsFragment.getActivity());
                noResultsText.setText("There are no trailers for this movie.");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                noResultsText.setLayoutParams(params);
                trailersLinearLayout.addView(noResultsText);
            } else {
                LayoutInflater inflater = detailsFragment.getActivity().getLayoutInflater();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.LEFT;
                for (String key : results.keySet()) {
                    List<String> resultsList = results.get(key);
                    LinearLayout trailerLayout = (LinearLayout) inflater.inflate(R.layout.trailer_layout, null);
                    trailerLayout.setLayoutParams(params);
                    trailerLayout.setTag(resultsList);
                    TextView trailerText = (TextView) trailerLayout.findViewById(R.id.trailerText);
                    trailerText.setText(resultsList.get(2));
                    trailerLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v instanceof LinearLayout) {
                                LinearLayout trailerLayout = (LinearLayout) v;
                                List<String> resultsList = (List<String>) trailerLayout.getTag();
                                Intent youtubeIntent = new Intent();
                                Uri youtubeUri = Uri.parse("https://www.youtube.com/watch")
                                        .buildUpon()
                                        .appendQueryParameter("v", resultsList.get(1))
                                        .build();
                                youtubeIntent.setData(youtubeUri);
                                youtubeIntent.setAction("android.intent.action.VIEW");
                                youtubeIntent.addCategory("android.intent.category.DEFAULT");
                                youtubeIntent.addCategory("android.intent.category.BROWSABLE");
                                detailsFragment.getActivity().startActivity(youtubeIntent);
                            }
                        }
                    });
                    trailersLinearLayout.addView(trailerLayout);
                }
            }

            trailersViewSwitcher.showNext();
        }
    }
}
