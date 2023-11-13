package com.example.project_7.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class NoteModel implements Parcelable {
    private String id;
    private String title;
    private String content;
    private ArrayList<PermissionModel> userPermissionModelList;
    private UserModel createdBy;


    public NoteModel() {

    }

    public NoteModel(String title, String content, ArrayList<PermissionModel> userPermissionModelList, UserModel createdBy) {
        this.title = title;
        this.content = content;
        this.userPermissionModelList = userPermissionModelList;
        this.createdBy = createdBy;
    }


    protected NoteModel(Parcel in) {
        id = in.readString();
        title = in.readString();
        content = in.readString();
        userPermissionModelList = in.createTypedArrayList(PermissionModel.CREATOR);
        createdBy = in.readParcelable(UserModel.class.getClassLoader());
    }

    public static final Creator<NoteModel> CREATOR = new Creator<NoteModel>() {
        @Override
        public NoteModel createFromParcel(Parcel in) {
            return new NoteModel(in);
        }

        @Override
        public NoteModel[] newArray(int size) {
            return new NoteModel[size];
        }
    };

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<PermissionModel> getUserPermissionModelList() {
        return userPermissionModelList;
    }

    public void setUserPermissionModelList(ArrayList<PermissionModel> userPermissionModelList) {
        this.userPermissionModelList = userPermissionModelList;
    }

    public UserModel getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserModel createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeTypedList(userPermissionModelList);
        parcel.writeParcelable(createdBy, i);
    }
}
