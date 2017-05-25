package com.android.washroomrush;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by confided on 027, Feb 27, 2017.
 */

public class MyProfileDialog extends DialogFragment {
    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.my_profile_layout, null);
        TextView myname,myemailid, mywashroomCount;
        myname = ((TextView) view.findViewById(R.id.my_name));
        myemailid =  ((TextView) view.findViewById(R.id.my_email));
        mywashroomCount = ((TextView) view.findViewById(R.id.total_washroom_created));
        try {
            myname.setText(String.format(Locale.ENGLISH, "User: %s", getArguments().getString("my_name")));
            myemailid.setText(String.format(Locale.ENGLISH, "Email id: %s", getArguments().getString("my_email_id")));
            if(getArguments().getString("my_email_id").equals(getString(R.string.admin))){
                myname.append(" (Admin)");
            }
            mywashroomCount.setText(String.format(Locale.ENGLISH, "You have created %d washroom", getArguments().getInt("my_washroom_count")));
        }catch (Exception e){
            myname.setText("Error occured. Restarting app may fix");
            myemailid.setVisibility(View.GONE);
            mywashroomCount.setVisibility(View.GONE);
            view.findViewById(R.id.my_profile_info).setVisibility(View.GONE);
            e.printStackTrace();
        }
        builder.setView(view);
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }
}
