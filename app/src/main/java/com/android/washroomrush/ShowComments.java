package com.android.washroomrush;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by confided on 006, Mar 6, 2017.
 */

public class ShowComments  extends DialogFragment{

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.show_comments_layout, null);
        List<String> comments;
        try {
            comments = getArguments().getStringArrayList("comments");
        }catch (Exception e){
            comments = new ArrayList<>();
            comments.add("No Comments");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,comments);

        ListView listView = (ListView) view.findViewById(R.id.comments_listview);
        listView.setAdapter(adapter);

        builder.setView(view);
        builder.setPositiveButton("Dismiss",null);

        return builder.create();

    }
}
