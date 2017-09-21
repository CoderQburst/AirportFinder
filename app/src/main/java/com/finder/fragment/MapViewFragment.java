package com.finder.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airport.finder.R;
import com.finder.activity.AirportDetailsActivity;
import com.finder.activity.BaseActivity;
import com.finder.values.Airports;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.finder.values.Constants.AIRPORT;
import static com.finder.values.Constants.ID;
import static com.finder.values.Constants.INITIAL_ZOOM;
import static com.finder.values.Constants.MAP_LOCATION_UPDATE_DELAY;
import static com.finder.values.Constants.MAP_MINIMUM_ZOOM;
import static com.finder.values.Constants.MARKER_TITLE;
import static com.finder.values.Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION;


public class MapViewFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    public final static String sTag = "map_frag";
    private ArrayList<String> mId = new ArrayList<>();
    private ArrayList<String> mAirportNames = new ArrayList<>();
    private ArrayList<String> mLatitudes = new ArrayList<>();
    private ArrayList<String> mLongitudes = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Marker> mMarkers = new ArrayList<>();
    private String searchMarkerTitle;
    private GoogleMap mMap;
    private MapFragmentInteractionListener mListener;

    private boolean firstTimeLocationRequest = true;

    public MapViewFragment() {
        // Required empty public constructor
    }

    public static MapViewFragment newInstance(ArrayList<Airports> airports, String markerTitle) {
        MapViewFragment fragment = new MapViewFragment();
        Bundle data = new Bundle();
        data.putParcelableArrayList(AIRPORT, airports);
        data.putString(MARKER_TITLE, markerTitle);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ArrayList<Airports> airports
                    = getArguments().getParcelableArrayList(AIRPORT);
            if (airports != null) {
                for (Airports object : airports) {
                    mId.add(object.getId());
                    mAirportNames.add(object.getAirport());
                    mLatitudes.add(object.getLatitude());
                    mLongitudes.add(object.getLongitude());
                }
            }
            searchMarkerTitle = getArguments().getString(MARKER_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_google_map, container, false);

        // Adding maps
        MapView map = rootView.findViewById(R.id.mapview_map);
        map.onCreate(savedInstanceState);
        map.onResume();
        map.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        setCurrentLocation();
        if (mAirportNames != null && mLatitudes != null && mLongitudes != null) {
            setAirportLocation();
            setCameraMoveListener();
            mMap.setOnMarkerClickListener((Marker marker) -> {
                goToAirportDetailsActivity(marker);
                return true;
            });
            if (searchMarkerTitle != null) {
                searchForMarker(searchMarkerTitle);
            }
        } else {
            ((BaseActivity) getActivity()).mGoingToExitApp = true;
            displayDataMissingAlert(getResources().getString(R.string.dataError));
        }
    }

    private void goToAirportDetailsActivity(Marker marker) {
        for (int counter = 0; counter < mAirportNames.size(); counter++) {
            if (mAirportNames.get(counter).equals(marker.getTitle()) &&
                    mId.get(counter) != null) {
                Intent intent = new Intent(getActivity()
                        , AirportDetailsActivity.class);
                intent.putExtra(ID, mId.get(counter));
                startActivity(intent);
                break;
            } else if (counter == mAirportNames.size() - 1) {
                displayDataMissingAlert(getResources().getString(R.string.dataError));
            }
        }
    }

    private void setCameraMoveListener() {
        if (mMarkers != null) {
            mMap.setOnCameraMoveListener(() -> {
                if (mMap.getCameraPosition().zoom < MAP_MINIMUM_ZOOM) {
                    for (Marker m : mMarkers) m.setVisible(false);
                } else {
                    for (Marker m : mMarkers)
                        m.setVisible(true);
                }
            });
        }
    }

    private void setCurrentLocation() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                if (firstTimeLocationRequest) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CALL_PHONE)) {
                        ((BaseActivity) getActivity())
                                .createAlert(getResources()
                                        .getString(R.string.requestLocationPermission));
                    }
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    firstTimeLocationRequest = false;
                }
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setCurrentLocation();
                }
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MAP_LOCATION_UPDATE_DELAY);
        mLocationRequest.setFastestInterval(MAP_LOCATION_UPDATE_DELAY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        //Move map to current location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (searchMarkerTitle == null) moveMapCamera(latLng);
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void moveMapCamera(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(INITIAL_ZOOM));
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    // Set markers at each airport location from api
    private void setAirportLocation() {
        for (int counter = 0; counter < mAirportNames.size(); counter++) {
            if (mAirportNames.get(counter) != null && mLatitudes.get(counter) != null
                    && mLongitudes.get(counter) != null) {
                LatLng location = new LatLng(Double.parseDouble(mLatitudes.get(counter)),
                        Double.parseDouble(mLongitudes.get(counter)));
                mMarkers.add(mMap.addMarker(new MarkerOptions().position(location)
                        .title(mAirportNames.get(counter))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane_icon))));
            } else {
                displayDataMissingAlert(getResources().getString(R.string.dataMissing));
            }
        }
    }

    // Searches for marker and moves map camera to that position
    public void searchForMarker(String MarkerTitle) {
        if (mMarkers != null) {
            for (int counter = 0; counter < mMarkers.size(); counter++) {
                if (MarkerTitle.equals(mMarkers.get(counter).getTitle())) {
                    LatLng position = mMarkers.get(counter).getPosition();
                    moveMapCamera(position);
                    break;
                } else if (counter == mMarkers.size() - 1) {
                    ((BaseActivity) getActivity()).displaySnackBar(this.getView(),
                            getResources().getString(R.string.dataError));
                }
            }
        }
    }

    private void displayDataMissingAlert(String string) {
        ((BaseActivity) getActivity())
                .createAlert(string);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MapFragmentInteractionListener) {
            mListener = (MapFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MapFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // TODO: Need for future use
    public interface MapFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}