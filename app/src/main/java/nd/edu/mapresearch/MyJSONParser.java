package nd.edu.mapresearch;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
  Followed tutorial on
  http://wptrafficanalyzer.in/blog/driving-distance-and-travel-time-duration-between-two-locations-in-google-map-android-api-v2/
 */

public class MyJSONParser{
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        JSONArray jsonRoutes = null;
        JSONArray jsonLegs = null;
        JSONArray jsonSteps = null;
        JSONObject jsonDistance = null;
        JSONObject jsonDuration = null;

        try {

            jsonRoutes = jObject.getJSONArray("routes");

            for(int i=0;i<jsonRoutes.length();i++){
                jsonLegs = ( (JSONObject)jsonRoutes.get(i)).getJSONArray("legs");

                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                for(int j=0;j<jsonLegs.length();j++){

                    jsonDistance = ((JSONObject) jsonLegs.get(j)).getJSONObject("distance");
                    HashMap<String, String> hashMapDistance = new HashMap<String, String>();
                    hashMapDistance.put("distance", jsonDistance.getString("text"));

                    jsonDuration = ((JSONObject) jsonLegs.get(j)).getJSONObject("duration");
                    HashMap<String, String> hmDuration = new HashMap<String, String>();
                    hmDuration.put("duration", jsonDuration.getString("text"));

                    path.add(hashMapDistance);

                    path.add(hmDuration);

                    jsonSteps = ( (JSONObject)jsonLegs.get(j)).getJSONArray("steps");

                    for(int k=0;k<jsonSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jsonSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                }
                routes.add(path);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return routes;
    }

    /**
     * Method to decode polyline points
     * jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
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
}