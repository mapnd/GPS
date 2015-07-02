package nd.edu.mapresearch;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JoaoGuilherme on 6/21/2015.
 */
public class GPSNavigator {
    public static final String DRIVING_MODE = "driving";
    public static final String WALKING_MODE = "walking";

    public static final int MIN_DISTANCE = 50;

    String status;

    private boolean isReady = false;

    private MainActivity activity;

    ArrayList<LatLng> coords = new ArrayList<LatLng>();
    ArrayList<String> directs = new ArrayList<String>();
    ArrayList<String> polylines = new ArrayList<String>();
    ArrayList<String> distances = new ArrayList<String>();
    ArrayList<String> durations = new ArrayList<String>();

    String totalDistance;
    String totalDuration;



    private LatLng nextDestination;
    private int currentPositionInList;

    public LatLng getNextDestination() {
        return nextDestination;
    }

    public boolean isFinalDestination() {
        if (currentPositionInList == coords.size()-1) {
            return true;
        } else {
            return false;
        }
    }

    public void advanceDestination() {
        currentPositionInList++;
        nextDestination = coords.get(currentPositionInList);
    }

    public String getCurrentDirection() {
        return directs.get(currentPositionInList);
    }

    public String getCurrentPolyline() {
        return polylines.get(currentPositionInList);
    }

    public String getCurrentDistance() {
        return distances.get(currentPositionInList);
    }

    public String getCurrentDurations() {
        return durations.get(currentPositionInList);
    }

    public GPSNavigator(LatLng start, LatLng end, String mode, MainActivity act){
        activity = act;
        isReady = false;
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+String.valueOf(start.latitude)+","+String.valueOf(start.longitude)+
                "&destination="+String.valueOf(end.latitude)+","+String.valueOf(end.longitude)+ "&sensor=false&key=AIzaSyCh0IztU40zYvay3rZzbITFFfIPVfQN3gM&language=en&mode="+mode;
        Log.d("GPSNavigator", "Downlaoding: "+ url);
        new DownloadTask().execute(url);
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;

        public DownloadTask() {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... url) {
            String response = "";
            try{
                response = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("RESLT:", result);
            try {
                JSONObject jObject = new JSONObject(result);

                status = jObject.getString("status");
                if (!status.equals("OK")) {
                    //There is an error!
                    return;
                }
                Log.d("GPS", "Result OK");

                JSONArray routes = jObject.getJSONArray("routes");
                JSONObject route = routes.getJSONObject(0);
                JSONArray legs = route.getJSONArray("legs");
                JSONObject leg = legs.getJSONObject(0);
                totalDistance = leg.getJSONObject("distance").getString("text");
                totalDuration = leg.getJSONObject("duration").getString("text");

                JSONArray steps = leg.getJSONArray("steps");
                for (int i =0; i<steps.length(); i++) {
                    JSONObject step = steps.getJSONObject(i);
                    distances.add(step.getJSONObject("distance").getString("text"));
                    durations.add(step.getJSONObject("duration").getString("text"));

                    JSONObject endLoc = step.getJSONObject("end_location");
                    coords.add(new LatLng(endLoc.getDouble("lat"), endLoc.getDouble("lng")));

                    directs.add(step.getString("html_instructions"));

                    polylines.add(step.getJSONObject("polyline").getString("points"));
                }

                nextDestination = coords.get(0); //Setting up!
                currentPositionInList = 0;
                activity.directions.setText("Next step: " + Html.fromHtml(getCurrentDirection()));
                activity.distanceDuration.setText("Distance: " + getCurrentDistance() + ", Duration: " + getCurrentDurations());
                activity.mPolyline = activity.mMap.addPolyline(getNextPolyline());
                Log.d("GPS", "Directions");
                for (String a : directs) {
                    Log.d("GPS", a);
                }
                isReady = true;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("GPS", "MERDA");
            }

        }
    }

    /** Followed tutorial on
     * http://wptrafficanalyzer.in/blog/driving-distance-and-travel-time-duration-between-two-locations-in-google-map-android-api-v2/ */
    private String downloadUrl(String stringURL) throws IOException {
        String response = "";
        InputStream content = null;
        HttpURLConnection htURLCon = null;
        try{
            URL url = new URL(stringURL);
            htURLCon = (HttpURLConnection) url.openConnection();
            htURLCon.connect();
            content = htURLCon.getInputStream();
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(content));
            String s = "";
            while( ( s = buffRead.readLine())  != null){
                response += s;
            }
            buffRead.close();
        }catch(Exception e){
            Log.d("Exception downloadURL", e.toString());
        }finally{
            content.close();
            htURLCon.disconnect();
        }
        return response;
    }

    public boolean isGPSReady() {
        return isReady;
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public PolylineOptions getNextPolyline() {
        String hash = getCurrentPolyline();
        List<LatLng> list = decodePoly(hash);
        PolylineOptions line = new PolylineOptions();
        line.width(5);
        line.color(Color.RED);

        for (LatLng latlng : list) {
            line.add(latlng);
        }
        return line;
    }

    public void stopGPS() {
        activity.directions.setText("");
        activity.distanceDuration.setText("");
        activity.mPolyline.remove();
        isReady = false;
    }
}
