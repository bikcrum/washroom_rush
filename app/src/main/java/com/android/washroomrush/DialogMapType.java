package com.android.washroomrush;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by confided on 018, Mar 18, 2017.
 */

public class DialogMapType extends DialogFragment {


    private SetMapTypeDialogListener listener;

    public interface SetMapTypeDialogListener{
        void setOnMapType(int i);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (SetMapTypeDialogListener) context;
    }

    RadioGroup mapType;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_map_type, null);

        //initialization
        mapType = (RadioGroup) view.findViewById(R.id.radiogroup_map_type);
        switch (getArguments().getInt("map_type")){
            case GoogleMap.MAP_TYPE_NORMAL:
                mapType.check(R.id.map_type_normal);
                break;
            case GoogleMap.MAP_TYPE_SATELLITE:
                mapType.check(R.id.map_type_satellite);
                break;
            case GoogleMap.MAP_TYPE_TERRAIN:
                mapType.check(R.id.map_type_terrain);
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                mapType.check(R.id.map_type_hybrid);
                break;
            default:
                dismiss();
        }
        builder.setView(view);

        builder.setMessage("Set map type")
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (mapType.getCheckedRadioButtonId()){
                            case R.id.map_type_normal:
                                listener.setOnMapType(GoogleMap.MAP_TYPE_NORMAL);
                                break;
                            case R.id.map_type_satellite:
                                listener.setOnMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case R.id.map_type_terrain:
                                listener.setOnMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case R.id.map_type_hybrid:
                                listener.setOnMapType(GoogleMap.MAP_TYPE_HYBRID);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
