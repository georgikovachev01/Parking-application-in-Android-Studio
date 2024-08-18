package com.parking.app.models;
import java.util.List; 

// Retrofit response model
public class GeocodingResponse {
  private List<Result> results;

  public List<Result> getResults() {
      return results;
  }

  public void setResults(List<Result> results) {
      this.results = results;
  }

  public static class Result {
      private Geometry geometry;

      public Geometry getGeometry() {
          return geometry;
      }

      public void setGeometry(Geometry geometry) {
          this.geometry = geometry;
      }
  }

  public static class Geometry {
      private Location location;

      public Location getLocation() {
          return location;
      }

      public void setLocation(Location location) {
          this.location = location;
      }
  }

  public static class Location {
      private double lat;
      private double lng;

      public double getLat() {
          return lat;
      }

      public void setLat(double lat) {
          this.lat = lat;
      }

      public double getLng() {
          return lng;
      }

      public void setLng(double lng) {
          this.lng = lng;
      }
  }
}
