package com.android.washroomrush;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import android.Manifest;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.android.washroomrush.R.id.map;

public class MapsActivity extends FragmentActivity implements
        DialogMarkerDetails.DialogListener,
        DialogMapType.SetMapTypeDialogListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location lastLocation;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;

    public Map<String, MarkerDetails> markerDetailsMap = new HashMap<>();
    public ArrayList<Marker> markers = new ArrayList<>();
    private FirebaseAuth mAuth;

    private static final String TAG = "biky";
    private static final int RC_SIGN_IN = 1;
    private static final float ZOOM_LEVEL_DEFAULT = 16;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 3;
    private static final int MODE_ADD_MARKER = 10;
    private static final int MODE_SEARCH = 11;
    private static final int MODE_ROUTE = 12;
    private static final String SHOWCASE_ID = "1.0biky";

    public Marker sharedMarker;
    private int mywashroomCount;
    private boolean keepUpdatingCurrentLocation = true;

    private ProgressDialog progressDialog;
    private DrawerLayout drawer;

    private LocationRequest locationRequest;

    private GoogleSignInOptions gso;

    private FloatingActionButton fabMyLocation;
    private NavigationView navView;
    private MyAdapter myAdapter;
    LinearLayoutManager linearLayoutManager;

    private int MODE_PREVIOUS;
    private float zoomLevel = ZOOM_LEVEL_DEFAULT;

    private ImageView profilePic;
    private TextView profileName;
    private TextView emailId;
    private EditText text1, text2;
    private BottomSheetBehavior bottomSheetBehavior;

    private Place source, destination;
    private Marker markerSource;
    private Marker markerDest;
    private Polyline polyline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FirebaseApp.initializeApp(this);

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateVisibility(findViewById(R.id.relative_layout_overlay),View.GONE,300);
            }
        });

        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null) {
                    requestSignIn();
                    return;
                }
                LatLng latLng = mMap.getCameraPosition().target;
                if (latLng == null) {
                    return;
                }
                DialogFragment dialogFragment = new DialogMarkerDetails();
                Bundle bundle = new Bundle();
                bundle.putString("current_user_name", mAuth.getCurrentUser().getDisplayName());
                bundle.putString("current_user_email", mAuth.getCurrentUser().getEmail());
                bundle.putBoolean("create_new", true);
                bundle.putBoolean("editable", true);
                bundle.putDouble("curr_lat", latLng.latitude);
                bundle.putDouble("curr_lon", latLng.longitude);

                bundle.putBoolean("last_location", true);
                bundle.putDouble("last_lat", lastLocation.getLatitude());
                bundle.putDouble("last_lon", lastLocation.getLongitude());

                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "dialog");
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);

        mapFragment.getMapAsync(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navView.getHeaderView(0);

        profilePic = (ImageView) headerView.findViewById(R.id.profile_pic);
        profileName = (TextView) headerView.findViewById(R.id.full_name);
        emailId = (TextView) headerView.findViewById(R.id.email_id);
        final ImageButton dropDown = (ImageButton) headerView.findViewById(R.id.drop_down_image_button);

        setUserDetails();

        dropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MapsActivity.this, dropDown);
                popupMenu.getMenuInflater().inflate(R.menu.sign, popupMenu.getMenu());

                if (mAuth.getCurrentUser() == null) {
                    popupMenu.getMenu().findItem(R.id.sign_in_out).setTitle("Sign in");
                    popupMenu.getMenu().findItem(R.id.my_profile).setVisible(false);
                } else {
                    popupMenu.getMenu().findItem(R.id.sign_in_out).setTitle("Sign out");
                    popupMenu.getMenu().findItem(R.id.my_profile).setVisible(true);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sign_in_out:
                                if (mAuth.getCurrentUser() == null) {
                                    closeDrawer();
                                    signIn();
                                } else {
                                    mAuth.signOut();
                                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                                }
                                break;
                            case R.id.my_profile:
                                if (mAuth.getCurrentUser() == null) {
                                    break;
                                }
                                DialogFragment myProfileDialog = new MyProfileDialog();
                                Bundle bundle = new Bundle();
                                if (mAuth.getCurrentUser() != null) {
                                    bundle.putString("my_name", mAuth.getCurrentUser().getDisplayName());
                                    bundle.putString("my_email_id", mAuth.getCurrentUser().getEmail());
                                    bundle.putInt("my_washroom_count", mywashroomCount);
                                    myProfileDialog.setArguments(bundle);
                                    myProfileDialog.show(getSupportFragmentManager(), "my_profile_dialog");
                                }
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_washroom:
                        if (mAuth.getCurrentUser() == null) {
                            closeDrawer();
                            requestSignIn();
                            break;
                        }
                        if (lastLocation == null) {
                            closeDrawer();
                            Snackbar.make(findViewById(R.id.coordinate_layout), "Your location can't be recognised", Snackbar.LENGTH_LONG).show();
                            break;
                        }/*
                        String node = mAuth.getCurrentUser().getEmail().split("@")[0].replace(".", "DOT") + " " + DateFormat.
                                getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH).
                                format(System.currentTimeMillis());
                        if (markerDetailsMap.containsKey(node)) {
                            Snackbar.make(findViewById(R.id.coordinate_layout), Html.fromHtml("<font color=\"#ffffff\">You have already placed new location today</font>"), Snackbar.LENGTH_SHORT)
                                    .setAction("Cancel", null).show();
                            break;
                        }*/
                        setMode(MODE_ADD_MARKER);
                        closeDrawer();
                        break;
                    case R.id.search_route:
                        if (MODE_PREVIOUS == MODE_SEARCH) {
                            setMode(MODE_ROUTE);
                        } else {
                            setMode(MODE_SEARCH);
                        }
                        closeDrawer();
                        break;
                    case R.id.map_type:
                        if (mMap == null) break;
                        DialogFragment dialog = new DialogMapType();
                        Bundle bundle = new Bundle();
                        bundle.putInt("map_type", mMap.getMapType());
                        dialog.setArguments(bundle);
                        dialog.show(getSupportFragmentManager(), "dialog_map_type");
                        break;
                    case R.id.nav_share:
                        break;
                }
                return true;
            }
        });

        fabMyLocation = (FloatingActionButton) findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!keepUpdatingCurrentLocation) {
                    requestLocationUpdates();
                    keepUpdatingCurrentLocation = true;
                } else {
                    removeLocationUpdates();
                    keepUpdatingCurrentLocation = false;
                }
            }
        });

        fabMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mMap != null && lastLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, ZOOM_LEVEL_DEFAULT));
                }
                return true;
            }
        });


        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    animateVisibility(findViewById(R.id.refresh_fab),View.GONE,200);
                } else if (BottomSheetBehavior.STATE_DRAGGING == newState) {
                    animateVisibility(fabMyLocation,View.GONE,200);
                } else if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    animateVisibility(fabMyLocation,View.VISIBLE,200);
                    animateVisibility(findViewById(R.id.refresh_fab),View.VISIBLE,200);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        linearLayoutManager = new LinearLayoutManager(this);

        RecyclerView recyclerView = (RecyclerView) bottomSheet;
        recyclerView.setLayoutManager(linearLayoutManager);

        MyAdapter.AdapterListener adapterListener = new MyAdapter.AdapterListener() {
            @Override
            public void onItemClick(int position) {
                if (mMap == null || position < 0) return;

                markers.get(position).showInfoWindow();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(position).getPosition(), ZOOM_LEVEL_DEFAULT));

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                linearLayoutManager.scrollToPositionWithOffset(0, 0);

                if (position != 0) {
                    try {
                        markers.add(0, markers.remove(position));
                        myAdapter.changePosition(position, 0);
                    }catch (Exception e){
                        initmRef();
                    }
                }

                removeLocationUpdates();
                keepUpdatingCurrentLocation = false;
            }
        };
        myAdapter = new MyAdapter(this, adapterListener);
        recyclerView.setAdapter(myAdapter);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                setUserDetails();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    //Toast.makeText(MapsActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_search);

        PlaceAutocompleteFragment autocompleteFragmentSrc = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_source);

        PlaceAutocompleteFragment autocompleteFragmentDest = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_dest);


        try {
            EditText text;
            text = ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input));
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            text.setHint("Type any place here");

            text1 = ((EditText) autocompleteFragmentSrc.getView().findViewById(R.id.place_autocomplete_search_input));
            text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            text1.setHint("Your Location");
            text1.setText(null);

            text2 = ((EditText) autocompleteFragmentDest.getView().findViewById(R.id.place_autocomplete_search_input));
            text2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            text2.setHint("Destination");
            if (destination != null) text1.setText(destination.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final ImageView searchIcon = (ImageView) ((LinearLayout) autocompleteFragment.getView()).getChildAt(0);
            searchIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_option));
            searchIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("biky", "toggle drawer clicked");
                    toggleDrawer();
                }
            });

            final ImageView searchIcon1 = (ImageView) ((LinearLayout) autocompleteFragmentSrc.getView()).getChildAt(0);
            searchIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_source));
            searchIcon1.setOnClickListener(null);

            final ImageView searchIcon2 = (ImageView) ((LinearLayout) autocompleteFragmentDest.getView()).getChildAt(0);
            searchIcon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_dest));
            searchIcon2.setOnClickListener(null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            autocompleteFragmentSrc.getView().findViewById(R.id.place_autocomplete_clear_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("biky", "src clear button on clicked");
                            if (source == null) return;
                            source = null;
                            text1.setText(null);
                            findRoute();
                        }
                    });
            autocompleteFragmentDest.getView().findViewById(R.id.place_autocomplete_clear_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("biky", "dest clear button on clicked");
                            if (destination == null) return;
                            destination = null;
                            text2.setText(null);
                            removeRoute();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getAddress());
                if (mMap != null) {
                    removeLocationUpdates();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), ZOOM_LEVEL_DEFAULT));
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        autocompleteFragmentSrc.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                source = place;
                if (destination != null) {
                    findRoute();
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        autocompleteFragmentDest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination = place;
                findRoute();
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        findViewById(R.id.cancel_src_dest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(MODE_SEARCH);
            }
        });

        presentShowcaseSequence();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth.addAuthStateListener(mAuthListener);
    }

    Place prevsource, prevdestination;

    private void findRoute() {
        if (mMap == null || destination == null) return;
        if (source == prevsource && destination == prevdestination) return;
        removeRoute();
        Log.i("biky", "find routes source,dest=" + source + "," + destination);
        LatLng src, dest = destination.getLatLng();
        if (source == null) {
            if (lastLocation == null) return;
            src = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        } else {
            src = source.getLatLng();
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(src).include(dest);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

        if (source != null) {
            markerSource = mMap.addMarker(new MarkerOptions()
                    .title("Source")
                    .position(src)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        }
        markerDest = mMap.addMarker(new MarkerOptions()
                .title("Destination")
                .position(dest)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        );
        markerDest.setTag(null);

        String url = getDirectionsUrl(src, dest);

        DownloadTask downloadTask = new DownloadTask();

        downloadTask.execute(url);

        prevsource = source;
        prevdestination = destination;
    }

    private void removeRoute() {
        Log.i("biky", "remove routes source,dest=" + source + "," + destination);

        if (markerSource != null) {
            markerSource.remove();
        }
        if (markerDest != null) {
            markerDest.remove();
        }
        if (polyline != null) {
            polyline.remove();
        }
    }

    private void toggleDrawer() {
        if (drawer == null) return;
        Log.i("biky", "toggling drawer");
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void openDrawer() {
        if (drawer == null) return;
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    private void closeDrawer() {
        if (drawer == null) return;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void setUserDetails() {
        //user signed in
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            try {
                Picasso.with(this)
                        .load(mAuth.getCurrentUser().getPhotoUrl())
                        .into(profilePic);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                emailId.setText(mAuth.getCurrentUser().getEmail());
            } catch (Exception e) {
                emailId.setText("User not signed in");
            }
            try {
                profileName.setText(mAuth.getCurrentUser().getDisplayName());
                if(mAuth.getCurrentUser().getEmail().equals(getString(R.string.admin))){
                    profileName.append(" (Admin)");
                }
            } catch (Exception e) {
                profileName.setText(null);
            }
            //user not signed in
        } else {
            profilePic.setImageBitmap(null);
            profileName.setText(null);
            emailId.setText("User not signed in");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU) {

            if (drawer == null) return false;

            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            } else {
                drawer.closeDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MapsActivity.this, "Something isn't right", Toast.LENGTH_SHORT).show();
                    }
                })
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    private void requestSignIn() {
        Snackbar.make(findViewById(R.id.coordinate_layout), Html.fromHtml("<font color=\"#ffffff\">You must sign in to Add washroom</font>"), Snackbar.LENGTH_LONG)
                .setAction("SIGN IN", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn();
                    }
                }).show();
    }

    private void requestReSignIn() {
        Snackbar.make(findViewById(R.id.coordinate_layout), Html.fromHtml("<font color=\"#ffffff\">Sign in failed</font>"), Snackbar.LENGTH_LONG)
                .setAction("TRY AGAIN", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn();
                    }
                }).show();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("biky", "on connected");

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (keepUpdatingCurrentLocation) {
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                fabMyLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location_enabled));
            }
        }
    }

    private void removeLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            fabMyLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location_disabled));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onPause() {
        Log.i("biky", "on pause");
        super.onPause();
        removeLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("biky","on map ready");
        mMap = googleMap;
        setMode(MODE_SEARCH);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() == null) return;
                if (lastLocation == null) {
                    Snackbar.make(findViewById(R.id.coordinate_layout), "Your location can't be recognised", Snackbar.LENGTH_LONG).show();
                    return;
                }
                sharedMarker = marker;
                DialogFragment dialogFragment = new DialogMarkerDetails();
                Bundle bundle = new Bundle();
                if (mAuth.getCurrentUser() != null) {
                    bundle.putString("current_user_email", mAuth.getCurrentUser().getEmail());
                }
                bundle.putBoolean("create_new", false);
                bundle.putBoolean("editable", mAuth.getCurrentUser() != null);
                bundle.putString("marker_tag", (String) marker.getTag());

                bundle.putBoolean("last_location", true);
                bundle.putDouble("last_lat", lastLocation.getLatitude());
                bundle.putDouble("last_lon", lastLocation.getLongitude());

                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "dialog");
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (lastLocation == null) return;
                zoomLevel = mMap.getCameraPosition().zoom;
                if (findViewById(R.id.relative_layout_overlay).getVisibility() == View.VISIBLE) {
                    LatLng latlng = mMap.getCameraPosition().target;
                    int dist = (int) SphericalUtil.computeDistanceBetween(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), latlng);
                    String distance;
                    if (dist < 1000) {
                        distance = String.format(Locale.ENGLISH,
                                "%d m away", Math.round(dist));
                    } else {
                        distance = String.format(Locale.ENGLISH,
                                "%.1f km away", dist / 1000.0);
                    }
                    ((TextView) findViewById(R.id.instruction)).setText(String.format(Locale.ENGLISH,
                            "Move map to set target location\n%s\nLat: %f\nLong: %f",
                            distance, latlng.latitude, latlng.longitude));
                }
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    removeLocationUpdates();
                    keepUpdatingCurrentLocation = false;
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag() == null) return false;

                int i = markers.indexOf(marker);

                if (i < 0) return false;

                if (i != 0) {
                    try {
                        markers.add(0, markers.remove(i));
                        myAdapter.changePosition(i, 0);
                    }catch (Exception e){
                        initmRef();
                    }
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                linearLayoutManager.scrollToPositionWithOffset(0, 0);

                removeLocationUpdates();
                keepUpdatingCurrentLocation = false;

                return false;
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setPadding(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130, getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()));

        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false);
    }

    @Override
    public void setOnMapType(int i) {
        if (mMap != null) mMap.setMapType(i);
        closeDrawer();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("biky", "on location changed");
        if (lastLocation == null) {

            lastLocation = location;

            initmRef();

            findViewById(R.id.refresh_fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initmRef();
                }
            });
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));
        } else {
            lastLocation = location;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void signIn() {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void setMode(int mode) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        linearLayoutManager.scrollToPositionWithOffset(0,0);
        switch (mode) {
            case MODE_ADD_MARKER:
                LatLng latlng = mMap.getCameraPosition().target;
                int dist = (int) SphericalUtil.computeDistanceBetween(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), latlng);
                String distance;
                if (dist < 1000) {
                    distance = String.format(Locale.ENGLISH,
                            "%d m away", Math.round(dist));
                } else {
                    distance = String.format(Locale.ENGLISH,
                            "%.1f km away", dist / 1000.0);
                }
                ((TextView) findViewById(R.id.instruction)).setText(String.format(Locale.ENGLISH,
                        "Move map to set target location\n%s\nLat: %f\nLong: %f",
                        distance, latlng.latitude, latlng.longitude));
                animateVisibility(findViewById(R.id.relative_layout_overlay),View.VISIBLE,300);
                break;
            case MODE_SEARCH:

                animateVisibility(findViewById(R.id.make_my_trip_autocomplete_overlay),View.GONE,300);
                animateVisibility(findViewById(R.id.cancel_src_dest),View.GONE,300);

                navView.getMenu().findItem(R.id.search_route)
                        .setTitle("Search route")
                        .setIcon(getResources().getDrawable(R.drawable.ic_my_trip));
                removeRoute();

                animateVisibility(findViewById(R.id.card_layout_search),View.VISIBLE,300);

                MODE_PREVIOUS = mode;
                break;
            case MODE_ROUTE:

                animateVisibility(findViewById(R.id.make_my_trip_autocomplete_overlay),View.VISIBLE,300);
                animateVisibility(findViewById(R.id.cancel_src_dest),View.VISIBLE,300);

                navView.getMenu().findItem(R.id.search_route)
                        .setTitle("Search place")
                        .setIcon(getResources().getDrawable(R.drawable.ic_search));

                animateVisibility(findViewById(R.id.card_layout_search),View.GONE,300);

                MODE_PREVIOUS = mode;
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        } /*else if (requestCode == RC_GPS) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                requestLocationUpdates();
            }
        }*/
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        setUserDetails();
                        progressDialog.dismiss();
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            requestReSignIn();
                        } else {
                            Toast.makeText(MapsActivity.this, "Sign in successful",
                                    Toast.LENGTH_SHORT).show();
                            if (mAuth != null && mAuth.getCurrentUser().getEmail().equals(getString(R.string.admin))) {
                                new AlertDialog.Builder(MapsActivity.this)
                                        .setTitle("Welcome back admin")
                                        .setMessage("You have full privilege over this app. Enjoy!")
                                        .setPositiveButton("DISMISS", null)
                                        .create()
                                        .show();
                            }
                        }
                    }
                });
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
            return;
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        if (findViewById(R.id.relative_layout_overlay).getVisibility() == View.VISIBLE) {
            animateVisibility(findViewById(R.id.relative_layout_overlay),View.GONE,300);
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onSaveMarkerDetails(MarkerDetails markerDetails, String key) {
        animateVisibility(findViewById(R.id.relative_layout_overlay),View.GONE,300);
        long timestamp = System.currentTimeMillis();
        markerDetails.setLastUpdatedTime(timestamp);
        //new creation
        if (key == null) {
            databaseReference.push().setValue(markerDetails);
        } else {
            //updation only
            if (sharedMarker != null) {
                sharedMarker.hideInfoWindow();
                switch (markerDetails.getTotalReviews()) {
                    case 1:
                        sharedMarker.setTitle(String.format(Locale.ENGLISH, "Reviewed %d time", markerDetails.getTotalReviews()));
                        break;
                    default:
                        sharedMarker.setTitle(String.format(Locale.ENGLISH, "Reviewed %d times", markerDetails.getTotalReviews()));
                }
                sharedMarker.showInfoWindow();
            }

            for (int i = 0; i < markers.size(); i++) {
                if (markers.get(i).getTag() == key) {
                    myAdapter.updateItem(markerDetails, i);
                    break;
                }
            }

            Map<String, Object> m = new HashMap<>();
            m.put(key, markerDetails);
            databaseReference.updateChildren(m);
        }
    }

    @Override
    public void onCancelSaveMarkerDetails() {
        animateVisibility(findViewById(R.id.relative_layout_overlay),View.GONE,300);
        Toast.makeText(getApplicationContext(), "Not saved", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void requestRemove(String key) {
        databaseReference.child(key).removeValue();
    }

    @Override
    protected void onDestroy() {
        Log.i("biky", "on destroy");
        super.onDestroy();
        removeLocationUpdates();
        if (mGoogleApiClient != null) {
            try {
                mGoogleApiClient.stopAutoManage(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mAuth != null) {
            try {
                mAuth.removeAuthStateListener(mAuthListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void initmRef() {
        Log.i("biky", "init Ref");
        markerDetailsMap.clear();
        markers.clear();
        if (mMap != null) mMap.clear();
        myAdapter.clear();
        mywashroomCount = 0;

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(MapsActivity.this, "data load complete", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i("biky", "on child added ");
                if( s == null) Toast.makeText(MapsActivity.this, "data load started", Toast.LENGTH_SHORT).show();
                //getting items from firebase database
                MarkerDetails markerDetails = dataSnapshot.getValue(MarkerDetails.class);

                if (markerDetailsMap.containsKey(dataSnapshot.getKey())) {
                    return;
                }

                //how far is toilet from current position, it get updated as long as initmRef is called
                markerDetails.setDistance((int)
                        SphericalUtil.computeDistanceBetween(
                                new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                                markerDetails.getPosition()));

                markerDetailsMap.put(dataSnapshot.getKey(), markerDetails);

                Bitmap bitmap = HelperClass.getMarkerIconAccordingToRating(getResources(), markerDetails.getRatingIn5());
                Bitmap resultBitmap;

                if (mAuth.getCurrentUser() != null) {
                    if (markerDetails.getCreatedByEmail().equals(mAuth.getCurrentUser().getEmail())) {
                        Bitmap bitmap1 = ((BitmapDrawable) getResources().getDrawable(R.drawable.my_marker_overlay))
                                .getBitmap();
                        /*resultBitmap = Bitmap.createScaledBitmap(
                                HelperClass.overlay(bitmap, bitmap1),
                                50, 50, false
                        );*/
                        resultBitmap = HelperClass.overlay(bitmap,bitmap1);
                        mywashroomCount++;
                    } else {
                        /*
                        resultBitmap = Bitmap.createScaledBitmap(
                                bitmap,
                                50, 50, false);*/
                        resultBitmap = bitmap;
                    }
                } else {
                  /*  resultBitmap = Bitmap.createScaledBitmap(
                            bitmap,
                            50, 50, false);*/
                    resultBitmap = bitmap;
                }

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(markerDetails.getLatitude(), markerDetails.getLongitude()))
                        .anchor(0.5f, 0.5f)
                        .alpha(0.9f)
                        .icon(BitmapDescriptorFactory.fromBitmap(resultBitmap))
                        .snippet("Tap here to review")
                );

                switch (markerDetails.getTotalReviews()) {
                    case 1:
                        marker.setTitle(String.format(Locale.ENGLISH, "Reviewed %d time", markerDetails.getTotalReviews()));
                        break;
                    default:
                        marker.setTitle(String.format(Locale.ENGLISH, "Reviewed %d times", markerDetails.getTotalReviews()));
                }

                //setting marker tag as its key to retreive information from key
                marker.setTag(dataSnapshot.getKey());

                //adding marker at right place in list meaning nearest marker first
                int i = myAdapter.addItem(markerDetails);
                //returing position where marker was added and adding marker in marker list so that when list item is clicked correctly marker is shown
                markers.add(i, marker);

                //s is null on first call
                Log.i("biky", "key=" + s);
                if (s == null) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                linearLayoutManager.scrollToPositionWithOffset(0, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i("biky", "on child changed ");

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                final String tag = dataSnapshot.getKey();

                if (!markerDetailsMap.containsKey(tag)) {
                    return;
                }

                Log.i("biky", "on child removed ");

                if (mAuth.getCurrentUser() != null &&
                        dataSnapshot.getValue(MarkerDetails.class).getCreatedByEmail()
                                .equals(mAuth.getCurrentUser().getEmail())) {
                    mywashroomCount--;
                }
                try {
                    markerDetailsMap.remove(tag);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("biky", "already removed");
                }

                for (int i = 0; i < markers.size(); i++) {
                    if (markers.get(i).getTag() == tag) {
                        markers.get(i).remove();
                        markers.remove(i);
                        myAdapter.removeItem(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

            iStream.close();
            urlConnection.disconnect();
        } catch (Exception e) {
            Log.d("biky", e.toString());
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(MapsActivity.this);
                progressDialog.setCancelable(false);
            }
            progressDialog.setMessage("Searching for best route...");
            progressDialog.show();
        }

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected void onPreExecute() {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(MapsActivity.this);
                progressDialog.setCancelable(false);
            }
            progressDialog.setMessage("Fetching route...");
            if (!progressDialog.isShowing()) progressDialog.show();
        }

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                JSONParser parser = new JSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
                lineOptions.startCap(new RoundCap());
                lineOptions.endCap(new RoundCap());
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions == null) {
                Snackbar.make(findViewById(R.id.coordinate_layout), Html.fromHtml("<font color=\"#ffffff\">No route found. Try again</font>"), Snackbar.LENGTH_SHORT)
                        .setAction("Cancel", null).show();
            } else {
                polyline = mMap.addPolyline(lineOptions);
            }



            if (source != null) {
                markerSource.setTag(null);
                markerSource.showInfoWindow();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        markerDest.showInfoWindow();
                    }
                }, 2000);
            }else{
                markerDest.showInfoWindow();
            }

            if (progressDialog != null) progressDialog.dismiss();
        }
    }

    private void presentShowcaseSequence() {

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.place_autocomplete_fragment_search))
                        .setTitleText("TUTORIAL 1")
                        .setDismissText("GOT IT")
                        .setContentText("You can search any place here")
                        .withRectangleShape()
                        .setMaskColour(getResources().getColor(R.color.mask_color))
                        .setDismissOnTouch(true)
                        .build()
        );


        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.fab_my_location))
                        .setTitleText("TUTORIAL 2")
                        .setDismissText("GOT IT")
                        .setContentText("Keeps updating current location\nManually enable GPS for high-accuracy location")
                        .setMaskColour(getResources().getColor(R.color.mask_color))
                        .withCircleShape()
                        .setDismissOnTouch(true)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.bottom_sheet))
                        .setTitleText("TUTORIAL 3")
                        .setDismissText("GOT IT")
                        .setContentText("Pull up to view details")
                        .setMaskColour(getResources().getColor(R.color.mask_color))
                        .withCircleShape()
                        .setDismissOnTouch(true)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.refresh_fab))
                        .setTitleText("TUTORIAL 4")
                        .setDismissText("GOT IT")
                        .setContentText("Refresh to list nearer washroom first")
                        .setMaskColour(getResources().getColor(R.color.mask_color))
                        .withCircleShape()
                        .setDismissOnTouch(true)
                        .build()
        );


        sequence.start();
    }

    private void animateVisibility(final View view , int flag, int duration){
           if(flag == View.GONE){
               view.animate().scaleX(0).scaleY(0).setDuration(duration).start();
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       view.setVisibility(View.GONE);
                   }
               },duration);
           }else{
               view.setVisibility(View.VISIBLE);
               view.animate().scaleX(1).scaleY(1).setDuration(duration).start();
           }
    }
}
