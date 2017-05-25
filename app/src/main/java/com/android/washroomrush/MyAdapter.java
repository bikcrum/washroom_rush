package com.android.washroomrush;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by confided on 025, Feb 25, 2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    public interface AdapterListener {
        void onItemClick(int position);
    }
    private Context context;

    private List<MarkerDetails> list = new ArrayList<>();

    private AdapterListener adapterListener;

    public MyAdapter(Context context, AdapterListener adapterListener) {
        this.context = context;
        this.adapterListener = adapterListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView distance;
        private RatingBar rating;
        private TextView ratingInfo;
        private TextView toiletInfo;
        private ImageView toiletPhoto;

        public ViewHolder(final View view) {
            super(view);
            distance = (TextView) view.findViewById(R.id.textview_distance);
            rating = (RatingBar) view.findViewById(R.id.rating);
            ratingInfo = (TextView) view.findViewById(R.id.rating_info);
            toiletInfo = (TextView) view.findViewById(R.id.toiletinfo);
            toiletPhoto = (ImageView) view.findViewById(R.id.toiletimage);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapterListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public int addItem(MarkerDetails markerDetails) {
        int i;
        for(i=0;i<list.size();i++){
            if(markerDetails.getDistance() < list.get(i).getDistance()){
                break;
            }
        }
        list.add(i,markerDetails);
        notifyItemInserted(i);
        return i;
    }

    public void removeItem(int index) {
        list.remove(index);
        notifyItemRemoved(index);
    }

    public void updateItem(MarkerDetails newMarkerDetails, int index) {
        list.set(index,newMarkerDetails);
        notifyItemChanged(index);
    }

    public void swap(int i,int j){
        Collections.swap(list,i,j);
        notifyItemChanged(i);
        notifyItemChanged(j);
    }

    public void changePosition(int from,int to){
        MarkerDetails temp = list.get(from);
        list.remove(from);
        notifyItemRemoved(from);
        list.add(to,temp);
        notifyItemInserted(to);
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MarkerDetails markerDetails = list.get(position);
        int dist = markerDetails.getDistance();
        String distance;
        if (dist < 1000) {
            distance = String.format(Locale.ENGLISH,
                    "%d m away", Math.round(dist));
        } else {
            distance = String.format(Locale.ENGLISH,
                    "%.1f km away", dist / 1000.0);
        }
        holder.distance.setText(distance);
        Log.i("biky", "distance=" + distance);

        holder.rating.setRating(markerDetails.getRatingIn5());
        holder.ratingInfo.setText(String.format(Locale.ENGLISH,
                "%.1f (%d)", markerDetails.getRatingIn5(), markerDetails.getTotalReviews()));

        String listItem;
        if (markerDetails.isFree()) listItem = "#  Free";
        else listItem = "#  Pay and use";
        if (markerDetails.isWestern()) listItem += "\n#  Western style";
        else listItem += "\n#  Indian/Squat style";
        if (markerDetails.isPublic()) listItem += "\n#  Public";
        else listItem += "\n#  Private";
        if (markerDetails.hasWaterSupply()) listItem += "\n#  Water supply";
        else listItem += "\n#  No ater supply";
        if (markerDetails.hasAttachedBathroom()) listItem += "\n#  Attached bathroom";
        if (markerDetails.getGenderInCHAR() == 'B') listItem += "\n#  For male ♂\n#  For female ♀";
        else if (markerDetails.getGenderInCHAR() == 'M') listItem += "\n#  Only for male ♂";
        else listItem += "\n#  Only for female ♀";

        holder.toiletInfo.setText(listItem);

        if (markerDetails.containsImage()) {
            holder.toiletPhoto.setImageBitmap(HelperClass.StringToBitMap(markerDetails.getImage()));
        } else {
            holder.toiletPhoto.setImageResource(R.drawable.no_image_marker);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
