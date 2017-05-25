package com.android.washroomrush;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class DialogMarkerDetails extends DialogFragment {

    private static final String TAG = "biky";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 4;

    private MarkerDetails markerDetails;

    private RelativeLayout viewAddImage;
    private RadioGroup radioGroupToiletType;
    private ToggleButton toggleButtonAttachedBathroom;
    private ToggleButton toggleButtonIsFree;
    private RadioGroup radioGroupPublic;
    private RadioGroup radioGroupCleanliness;
    private ToggleButton toggleButtonwaterSupply;
    private CheckBox checkBoxMale;
    private CheckBox checkBoxFemale;
    private ToggleButton toggleButtonContainsBagCounter;
    private EditText editTextTotalRooms;
    private ToggleButton toggleButtonLocksProperly;
    private RatingBar ratingBar;
    private TextView textViewRatingInfo;
    private TextView textViewShortInfo;
    private ImageView imageView;
    private EditText editTextComment;
    private CardView removeWashroomButton;
    private CardView showCommentsButton;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
    public final static int PICK_FROM_GALLERY_REQUEST_CODE = 1;


    private List<String> comments = new ArrayList<>();

    public interface DialogListener {
        void onSaveMarkerDetails(MarkerDetails markerDetails, String tag);

        void signIn();

        void onCancelSaveMarkerDetails();

        void requestRemove(String tag);
    }

    private DialogListener dialogListener;
    private String tag = null;
    private Marker marker;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dialogListener = (DialogListener) context;
            if (!getArguments().getBoolean("create_new")) {
                tag = getArguments().getString("marker_tag");
                markerDetails = ((MapsActivity) getActivity()).markerDetailsMap.get(tag);
                marker = ((MapsActivity) getActivity()).sharedMarker;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_marker_details_layout, null);

        //creation of new marker
        if (getArguments().getBoolean("create_new")) {
            markerDetails = new MarkerDetails(
                    getArguments().getString("current_user_name"),
                    getArguments().getString("current_user_email"),
                    getArguments().getDouble("curr_lat"),
                    getArguments().getDouble("curr_lon")
            );
        }
        preloadDialogInterface(view, getArguments().getBoolean("editable"));
        builder.setView(view);

        if (getArguments().getBoolean("editable")) {
            String title = null;
            if (markerDetails.getTotalReviews() == 0) {
                title = "Add your first review";
            } else {
                title = "Reviews from most of the users";
            }
            builder.setPositiveButton("Save as my review", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateMarkerDetails();
                    if (marker != null) {
                        Bitmap bitmap = HelperClass.getMarkerIconAccordingToRating(getResources(), markerDetails.getRatingIn5());
                        Bitmap resultBitmap;
                        if (getArguments().getString("current_user_email") != null) {
                            if (getArguments().getString("current_user_email").equals(markerDetails.getCreatedByEmail())) {
                                Bitmap bitmap1 = ((BitmapDrawable) getResources().getDrawable(R.drawable.my_marker_overlay))
                                        .getBitmap();
                                resultBitmap = Bitmap.createScaledBitmap(
                                        HelperClass.overlay(bitmap, bitmap1),
                                        50, 50, false
                                );
                            } else {
                                resultBitmap = Bitmap.createScaledBitmap(
                                        bitmap,
                                        50, 50, false);
                            }
                        } else {
                            resultBitmap = Bitmap.createScaledBitmap(
                                    bitmap,
                                    50, 50, false);
                        }
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(resultBitmap));
                    }
                    dialogListener.onSaveMarkerDetails(markerDetails, tag);
                }
            }).setTitle(title);
        } else {
            builder.setPositiveButton("Sign In with google", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogListener.signIn();
                }
            }).setTitle("Please sign in to add your review");
        }

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogListener.onCancelSaveMarkerDetails();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogListener.onCancelSaveMarkerDetails();
            }
        });


        return builder.create();
    }

    private void preloadDialogInterface(View view, boolean editable) {
        String listItems;


        LatLng latLng = markerDetails.getPosition();
        LatLng lastLatLng = new LatLng(getArguments().getDouble("last_lat"), getArguments().getDouble("last_lon"));

        double dist = SphericalUtil.computeDistanceBetween(latLng, lastLatLng);

        markerDetails.setDistance((int) dist);

        String distance;
        if (dist < 1000) {
            distance = String.format(Locale.ENGLISH,
                    "%d m away", Math.round(dist));
        } else {
            distance = String.format(Locale.ENGLISH,
                    "%.1f km away", dist / 1000.0);
        }
        listItems = distance;

        listItems += String.format(Locale.ENGLISH,
                "\n\nCreated by: %s\n\nLat: %f\nLong: %f",
                markerDetails.getCreatedByEmail().substring(0, markerDetails.getCreatedByEmail().indexOf('@')),
                markerDetails.getLatitude(),
                markerDetails.getLongitude());

        long time = markerDetails.getLastUpdatedTime();
        if (time != 0) listItems += "\n\nLast update: " + DateFormat.getInstance().format(time);

        textViewShortInfo = (TextView) view.findViewById(R.id.short_toilet_info);
        textViewShortInfo.setText(listItems);

        textViewRatingInfo = (TextView) view.findViewById(R.id.rating_info);
        textViewRatingInfo.setText(String.format(Locale.ENGLISH,
                "Rating %.1f (%d)", markerDetails.getRatingIn5(), markerDetails.getTotalReviews()));

        ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        ratingBar.setRating(markerDetails.getRatingIn5());
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser)
                    textViewRatingInfo.setText(String.format(Locale.ENGLISH,
                            "Rating %.1f (%d)", rating, markerDetails.getTotalReviews()));
            }
        });
        ratingBar.setIsIndicator(!editable);

        imageView = (ImageView) view.findViewById(R.id.toiletimage);
        viewAddImage = (RelativeLayout) view.findViewById(R.id.layout_add_image);

        if (markerDetails.containsImage()) {
            imageView.setImageBitmap(HelperClass.StringToBitMap(markerDetails.getImage()));
            imageView.setVisibility(View.VISIBLE);
            viewAddImage.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.GONE);
            viewAddImage.setVisibility(View.VISIBLE);
        }

        if (editable) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Do you want to delete image?");
                    builder.setPositiveButton("Don't Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            markerDetails.removeImage();
                            imageView.setVisibility(View.GONE);
                            viewAddImage.setVisibility(View.VISIBLE);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        } else {
            imageView.setOnClickListener(null);
        }

        viewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Set Image");
                builder.setPositiveButton("FROM GALLERY", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            checkStoragePermission();
                        } else {
                            onGalleryPick();
                        }
                    }
                });
                builder.setNegativeButton("FROM CAMERA", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onLaunchCamera();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        viewAddImage.setEnabled(editable);


        radioGroupToiletType = (RadioGroup) view.findViewById(R.id.radiogrouptoilettype);
        if (markerDetails.isWestern()) {
            radioGroupToiletType.check(R.id.radiobtn_western);
        } else {
            radioGroupToiletType.check(R.id.radiobtn_squat);
        }
        for (int i = 0; i < radioGroupToiletType.getChildCount(); i++) {
            radioGroupToiletType.getChildAt(i).setEnabled(editable);
        }

        toggleButtonAttachedBathroom = (ToggleButton) view.findViewById(R.id.togglebutton_attachedBathroom);
        toggleButtonAttachedBathroom.setChecked(markerDetails.hasAttachedBathroom());
        toggleButtonAttachedBathroom.setEnabled(editable);

        toggleButtonIsFree = (ToggleButton) view.findViewById(R.id.togglebutton_isFree);
        toggleButtonIsFree.setChecked(markerDetails.isFree());
        toggleButtonIsFree.setEnabled(editable);

        radioGroupPublic = (RadioGroup) view.findViewById(R.id.radiogroup_ispublic);
        if (markerDetails.isPublic()) {
            radioGroupPublic.check(R.id.radiobtn_public);
        } else {
            radioGroupPublic.check(R.id.radiobtn_private);
        }
        for (int i = 0; i < radioGroupPublic.getChildCount(); i++) {
            radioGroupPublic.getChildAt(i).setEnabled(editable);
        }

        radioGroupCleanliness = (RadioGroup) view.findViewById(R.id.radiogroup_cleanliness);
        switch (markerDetails.getCleanliness()) {
            case 'C':
                radioGroupCleanliness.check(R.id.radiobtn_clean);
                break;
            case 'M':
                radioGroupCleanliness.check(R.id.radiobtn_moderate);
                break;
            case 'U':
                radioGroupCleanliness.check(R.id.radiobtn_unclean);
        }
        for (int i = 0; i < radioGroupCleanliness.getChildCount(); i++) {
            radioGroupCleanliness.getChildAt(i).setEnabled(editable);
        }

        toggleButtonwaterSupply = (ToggleButton) view.findViewById(R.id.togglebutton_watersupply);
        toggleButtonwaterSupply.setChecked(markerDetails.hasWaterSupply());
        toggleButtonwaterSupply.setEnabled(editable);

        checkBoxMale = (CheckBox) view.findViewById(R.id.checkbox_male);
        checkBoxFemale = (CheckBox) view.findViewById(R.id.checkbox_female);
        switch (markerDetails.getGenderInCHAR()) {
            case 'M':
                checkBoxMale.setChecked(true);
                checkBoxFemale.setChecked(false);
                break;
            case 'F':
                checkBoxMale.setChecked(false);
                checkBoxFemale.setChecked(true);
                break;
            case 'B':
                checkBoxMale.setChecked(true);
                checkBoxFemale.setChecked(true);
        }
        checkBoxFemale.setEnabled(editable);
        checkBoxMale.setEnabled(editable);

        toggleButtonContainsBagCounter = (ToggleButton) view.findViewById(R.id.togglebutton_containsbagcounter);
        toggleButtonContainsBagCounter.setChecked(markerDetails.hasBagCounter());
        toggleButtonContainsBagCounter.setEnabled(editable);

        editTextTotalRooms = (EditText) view.findViewById(R.id.editText_totalrooms);
        editTextTotalRooms.setText(String.format(getString(R.string.totalrooms_text), markerDetails.getTotalRooms()));
        editTextTotalRooms.setEnabled(editable);

        toggleButtonLocksProperly = (ToggleButton) view.findViewById(R.id.togglebutton_locksproperly);
        toggleButtonLocksProperly.setChecked(markerDetails.locksProperly());
        toggleButtonLocksProperly.setEnabled(editable);

        showCommentsButton = (CardView) view.findViewById(R.id.showcomment_button);
        showCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerDetails.getComments().size() == 0) {
                    Toast.makeText(getContext(), "No comments found", Toast.LENGTH_SHORT).show();
                    return;
                }
                DialogFragment dialogFragment = new ShowComments();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("comments", (ArrayList<String>) markerDetails.getComments());
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getFragmentManager(), "dialog_show_comments");
            }
        });

        editTextComment = (EditText) view.findViewById(R.id.mycomment);
        editTextComment.setEnabled(editable);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm remove?");

        removeWashroomButton = (CardView) view.findViewById(R.id.cardview_request_remove);
        try {
            if (editable && markerDetails.getTotalReviews() > 0) {
                if (getArguments().getString("current_user_email").equals(markerDetails.getCreatedByEmail())){
                    removeWashroomButton.setVisibility(View.VISIBLE);
                    builder.setMessage("This washroom was placed by you. You may remove this but can't undo action");
                } else if(getArguments().getString("current_user_email").equals(getString(R.string.admin))){
                    removeWashroomButton.setVisibility(View.VISIBLE);
                    builder.setMessage("Dear admin, You may remove this but can't undo action");
                }else{
                    removeWashroomButton.setVisibility(View.GONE);
                }
            } else {
                removeWashroomButton.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            removeWashroomButton.setVisibility(View.GONE);
            e.printStackTrace();
        }

        builder.setPositiveButton("Don't Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialogListener.requestRemove(tag);
                dismiss();
            }
        });

        removeWashroomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.create().show();
            }
        });
    }


    private void updateMarkerDetails() {
        markerDetails.setWestern(radioGroupToiletType.getCheckedRadioButtonId() == R.id.radiobtn_western);
        markerDetails.hasAttachedBathroom(toggleButtonAttachedBathroom.isChecked());
        markerDetails.setFree(toggleButtonIsFree.isChecked());
        markerDetails.isPublic(radioGroupPublic.getCheckedRadioButtonId() == R.id.radiobtn_public);
        switch (radioGroupCleanliness.getCheckedRadioButtonId()) {
            case R.id.radiobtn_unclean:
                markerDetails.setCleanliness("U");
                break;
            case R.id.radiobtn_moderate:
                markerDetails.setCleanliness("M");
                break;
            case R.id.radiobtn_clean:
                markerDetails.setCleanliness("C");
        }
        markerDetails.hasWaterSupply(toggleButtonwaterSupply.isChecked());
        if (checkBoxMale.isChecked()) {
            markerDetails.setGenderMale();
        }
        if (checkBoxFemale.isChecked()) {
            markerDetails.setGenderFemale();
        }
        markerDetails.containsBagCounter(toggleButtonContainsBagCounter.isChecked());
        markerDetails.setTotalRooms(Integer.parseInt(editTextTotalRooms.getText().toString()));
        markerDetails.setLocksProperly(toggleButtonLocksProperly.isChecked());
        markerDetails.setRatingIn5(ratingBar.getRating());
        String comment = editTextComment.getText().toString().trim();
        if (!comment.isEmpty()) {
            String username = getArguments().getString("current_user_email");
            try {
                username = username.substring(0, username.indexOf('@'));
            } catch (Exception e) {
                e.printStackTrace();
            }
            comment += "\n\b- " + username;
            markerDetails.addComment(comment);
            comments.add(comment);
        }
    }

    private void setImage(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()),
                false);
        markerDetails.setImage(HelperClass.BitMapToString(bitmap));
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
        viewAddImage.setVisibility(View.GONE);
    }

    public void onLaunchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void onGalleryPick() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_FROM_GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = (Bitmap) data.getExtras().get("data");
                setImage(takenImage);
            }
        } else if (requestCode == PICK_FROM_GALLERY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                try {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    setImage(BitmapFactory.decodeFile(picturePath));
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkStoragePermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Storage Permission Needed")
                        .setMessage("This app needs to read from external storage")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            onGalleryPick();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onGalleryPick();
            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
        }
    }
}
