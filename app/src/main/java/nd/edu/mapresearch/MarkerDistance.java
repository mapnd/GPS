package nd.edu.mapresearch;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by JoaoGuilherme on 7/16/2015.
 */
public class MarkerDistance implements Comparable<MarkerDistance>  {

    private final LatLng origin;
    private final double distance;

    public MarkerDistance(final LatLng origin, final double distance) {
        this.origin = origin;
        this.distance = distance;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(MarkerDistance other) {
        return distance < other.distance ? -1 : 1;
    }
}
