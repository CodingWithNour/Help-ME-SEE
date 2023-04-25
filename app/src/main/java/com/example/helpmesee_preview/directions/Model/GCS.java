package com.example.helpmesee_preview.directions.Model;


import static com.example.helpmesee_preview.app_logic.Constants.EARTH_RADIUS_MILES;

import com.google.android.gms.maps.model.LatLng;

//This GCS is a system of coordinates,
// events and events used to be able to determine the location of the Earth's surface
//It would have two basic needs:
//1- The angle between the two latungs
//2- The first meridian, right and left
public class GCS {

    //This is the primary longitude line that has two directions, right and left
    public static final int RIGHT = -1;
    public static final int LEFT = 1;

    private double latitude;
    private double longitude;

    public GCS(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    //Any location we will determine, there will be 3 possibilities
    //1- On the equator it will be 0
    //2- Above the equator it will be +1
    //3- Below the equator it will be -1

    // XLat
    // This will be the latitude
    // which is at the first point
    // yLat
    //This will be the latitude
    // The one at the second point

    // Ibrahim                         // Start Point (Lat,Long)
    public static int detectPointSide(double xLat, double yLat, GCSPoint startPoint, GCSPoint endPoint) {

        Point3D start2d = Point3D.convertTo2d(startPoint);
        Point3D end2d = Point3D.convertTo2d(endPoint);
        Point3D x2d = Point3D.convertTo2d(xLat, yLat);

        double determinant =
                (end2d.x - start2d.x) * (x2d.y - start2d.y) - (end2d.y - start2d.y) * (x2d.x - start2d.x);

        return (int) Math.signum(determinant);

    }


    public static int detectPointSide(GCSPoint xLocation, GCSPoint startPoint,GCSPoint endPoint) {

        Point3D x2d = Point3D.convertTo2d(xLocation);
        Point3D start2d = Point3D.convertTo2d(startPoint);
        Point3D end2d = Point3D.convertTo2d(endPoint);

        double determinant =
                (end2d.x - start2d.x) * (x2d.y - start2d.y) - (end2d.y - start2d.y) * (x2d.x - start2d.x);

        return (int) Math.signum(determinant);
    }

/*
    // Mohammed
    public static double distanceBetweenPoints(GCS startPoint,GCS endPoint) {

    }
*/

    // Anas
    // Compute bearing of two points
  /*  public static double computeBearingSegment(GCS startPoint, GCS endPoint) {

    }*/
    public static GCSPoint predictFutureLocation(LatLng startPoint, Float bearing,
                                                 Float speedMph, Float deltaT) {
        double bearingRadians = Math.toRadians(bearing);
        double x = speedMph * Math.sin(bearingRadians) * deltaT / 3600;
        double y = speedMph * Math.cos(bearingRadians) * deltaT / 3600;

        double yDegrees = Math.toDegrees(y);
        double endLat = startPoint.latitude + yDegrees / EARTH_RADIUS_MILES;

        double startLatRadians = Math.toRadians(startPoint.latitude);
        double endLong = startPoint.longitude + 180 / Math.PI / Math.sin(startLatRadians) * x
                / EARTH_RADIUS_MILES;

        return new GCSPoint(endLat, endLong);
    }

}
