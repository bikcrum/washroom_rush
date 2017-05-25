package com.android.washroomrush;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@IgnoreExtraProperties
public class MarkerDetails {

    private String createdByName = "";
    private String createdByEmail = "";
    private long lastUpdatedTime;

    private double latitude;
    private double longitude;

    private String image;

    @Exclude
    private int distance;

    private ArrayList<Integer> western = new ArrayList<>(Collections.nCopies(2, 0));

    private ArrayList<Integer> attachedBathroom = new ArrayList<>(Collections.nCopies(2, 0));

    private ArrayList<Integer> free = new ArrayList<>(Collections.nCopies(2, 0));

    private ArrayList<Integer> _public = new ArrayList<>(Collections.nCopies(2, 0));

    private ArrayList<Integer> clean = new ArrayList<>(Collections.nCopies(3, 0));

    private ArrayList<Integer> waterSupply = new ArrayList<>(Collections.nCopies(2, 0));

    private ArrayList<Integer> gender = new ArrayList<>(Collections.nCopies(2, 0));

    private ArrayList<Integer> bagCounter = new ArrayList<>(Collections.nCopies(2, 0));

    private int totalRooms = 1;

    private ArrayList<Integer> locksProperly = new ArrayList<>(Collections.nCopies(2, 0));

    private float ratingIn5;

    private List<String> comments = new ArrayList<>();

    public MarkerDetails() {
    }

    //initialization
    public MarkerDetails(String createdByName, String createdByEmail, double latitude, double longitude) {
        this.createdByName = createdByName;
        this.createdByEmail = createdByEmail;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //setters
    @Exclude
    public void setImage(String url) {
        image = url;
    }

    @Exclude void removeImage(){
        image = null;
    }

    @Exclude
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    @Exclude
    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    @Exclude
    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    @Exclude
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Exclude
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Exclude
    public void setWestern(boolean isWestern) {
        if (isWestern) western.set(0,western.get(0)+1);
        else western.set(1,western.get(1)+1);
    }

    @Exclude
    public void hasAttachedBathroom(boolean has) {
        if (has) attachedBathroom.set(0,attachedBathroom.get(0)+1);
        else attachedBathroom.set(1,attachedBathroom.get(1)+1);
    }

    @Exclude
    public void setFree(boolean isFree) {
        if (isFree) free.set(0,free.get(0)+1);
        else free.set(1,free.get(1)+1);
    }

    @Exclude
    public void isPublic(boolean yes) {
        if (yes) _public.set(0,_public.get(0)+1);
        else _public.set(1,_public.get(1)+1);
    }

    @Exclude
    public void setCleanliness(String choice) {
        switch (choice) {
            case "C":
                clean.set(0,clean.get(0)+1);
                break;
            case "M":
                clean.set(1,clean.get(1)+1);
                break;
            case "U":
                clean.set(2,clean.get(2)+1);
        }
    }

    @Exclude
    public void hasWaterSupply(boolean yes) {
        if (yes) waterSupply.set(0,waterSupply.get(0)+1);
        else waterSupply.set(1,waterSupply.get(1)+1);
    }

    @Exclude
    public void setGenderMale() {
        gender.set(0,gender.get(0)+1);
    }

    @Exclude
    public void setGenderFemale() {
        gender.set(1,gender.get(1)+1);
    }

    @Exclude
    public void setDistance(int dist){
        distance = dist;
    }

    @Exclude
    public void containsBagCounter(boolean yes) {
        if (yes) bagCounter.set(0,bagCounter.get(0)+1);
            else bagCounter.set(1,bagCounter.get(1)+1);
    }

    @Exclude
    public void setTotalRooms(int rooms) {
        totalRooms = Math.round((totalRooms * (getTotalReviews() - 1) + rooms) / getTotalReviews());
    }

    @Exclude
    public void setLocksProperly(boolean yes) {
        if (yes) locksProperly.set(0, locksProperly.get(0) + 1);
        else locksProperly.set(1, locksProperly.get(1) + 1);
    }

    @Exclude
    public void setRatingIn5(float rating) {
        ratingIn5 = (ratingIn5 * (getTotalReviews() - 1) + rating) / (getTotalReviews());
    }

    @Exclude
    public void addComment(String comment) {
        //ensuring if other info is appended not deleted
        if (comment.isEmpty()) return;
        comments.add(comment);
    }

    //getters for app, as per voting by users
    @Exclude
    public boolean isWestern() {
        return western.get(0) >= western.get(1);
    }

    @Exclude
    public boolean hasAttachedBathroom() {
        return attachedBathroom.get(0) >= attachedBathroom.get(1);
    }

    @Exclude
    public boolean isFree() {
        return free.get(0) >= free.get(1);
    }

    @Exclude
    public boolean isPublic() {
        return _public.get(0) >= _public.get(1);
    }

    @Exclude
    public char getCleanliness() {
        if (clean.get(0) >= clean.get(1) && clean.get(0) >= clean.get(2)) return 'C';
        else if (clean.get(1) >= clean.get(0) && clean.get(1) >= clean.get(2)) return 'M';
        else return 'U';
    }

    @Exclude
    public int getDistance(){
        return distance;
    }

    @Exclude
    public boolean hasWaterSupply() {
        return waterSupply.get(0) >= waterSupply.get(1);
    }

    @Exclude
    public char getGenderInCHAR() {
        if(gender.get(0) > gender.get(1) * 2) return 'M';
        else if(gender.get(1) > gender.get(1) * 2) return  'F';
        else return 'B';
    }

    @Exclude
    public boolean hasBagCounter() {
        return bagCounter.get(0) >= bagCounter.get(1);
    }

    @Exclude
    public boolean locksProperly() {
        return locksProperly.get(0) >= locksProperly.get(1);
    }

    //getters for json parsing for firebase
    public String getCreatedByName() {
        return createdByName;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getImage() {
        return image;
    }

    @Exclude
    public boolean containsImage() {
        return image != null;
    }

    @Exclude
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    public List<Integer> getWestern() {
        return western;
    }

    public List<Integer> getAttachedBathroom() {
        return attachedBathroom;
    }

    public List<Integer> getFree() {
        return free;
    }

    public List<Integer> get_public() {
        return _public;
    }

    public List<Integer> getClean() {
        return clean;
    }

    public List<Integer> getWaterSupply() {
        return waterSupply;
    }

    public ArrayList<Integer> getGender() {
        return gender;
    }

    public List<Integer> getBagCounter() {
        return bagCounter;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public List<Integer> getLocksProperly() {
        return locksProperly;
    }

    public float getRatingIn5() {
        return ratingIn5;
    }

    public List<String> getComments() {
        return comments;
    }

    @Exclude
    public int getTotalReviews() {
        return western.get(0) + western.get(1);
    }
}


