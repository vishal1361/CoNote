package com.example.project_7.model;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class PermissionModel implements Parcelable {
    /*
    1. read (default)
    2. read + write
    3. owner permissions ( delete users, change permissions for individual users, add users to note, delete note);
     */
    public static String OWNER = "com.example.project_7.model.Permission.Creator";
    public static String VIEW = "com.example.project_7.model.Permission.Viewer";
    public static String EDIT = "com.example.project_7.model.Permission.Editor";

    private String permission;
    private UserModel user;

    public PermissionModel() {
    }

    public PermissionModel(String permission, UserModel user) {
        this.permission = permission;
        this.user = user;
    }

    protected PermissionModel(Parcel in) {
        permission = in.readString();
        user = in.readParcelable(UserModel.class.getClassLoader());
    }

    public static final Creator<PermissionModel> CREATOR = new Creator<PermissionModel>() {
        @Override
        public PermissionModel createFromParcel(Parcel in) {
            return new PermissionModel(in);
        }

        @Override
        public PermissionModel[] newArray(int size) {
            return new PermissionModel[size];
        }
    };

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(permission);
        parcel.writeParcelable(user, i);
    }
}
