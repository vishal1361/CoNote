package com.example.project_7.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.recyclerview.widget.RecyclerView;

import com.example.project_7.NewNote;
import com.example.project_7.R;
import com.example.project_7.model.NoteModel;
import com.example.project_7.model.PermissionModel;
import com.example.project_7.model.UserModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Permission;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class AddNoteCardAdapter extends  RecyclerView.Adapter<AddNoteCardAdapter.NoteCardViewHolder> {
    public static final String NOTE_KEY = "com.example.project_7.adapter.AddNoteCardAdapter.note";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Notes");
    SharedPreferences preferences;
    String currentEmail;

    private ArrayList<NoteModel> myNotes;

    public AddNoteCardAdapter(ArrayList<NoteModel> myNotes) {
        this.myNotes = myNotes;
    }


    @Override
    public NoteCardViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_card_layout, parent, false);
        return new NoteCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteCardViewHolder holder, int position) {
        NoteModel note = myNotes.get(position);
        // profile image left
        holder.title.setText(note.getTitle());
        holder.content.setText(getSummary(note.getContent()));
        if(note!= null && note.getCreatedBy().getEmail().equals(currentEmail)) {
            holder.ownerOrSharedNoteIcon.setImageResource(R.drawable.ic_crown_svgrepo_com);
        }
        if(note!= null && !note.getCreatedBy().getEmail().equals(currentEmail)) {
            holder.deleteNoteButton.setVisibility(View.INVISIBLE);
            holder.addUser.setVisibility(View.INVISIBLE);
        }
    }

    private String getSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        int summaryLength = Math.min(content.length(), 100);
        return content.substring(0, summaryLength);
    }


    @Override
    public int getItemCount() {
        return myNotes.size();
    }

    public class NoteCardViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;
        ImageView manageUsers;
        ImageView addUser;
        ImageView deleteNoteButton;
        ImageView ownerOrSharedNoteIcon;
        Dialog dialog; // Declare the dialog variable outside the method

        public NoteCardViewHolder( View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            content = itemView.findViewById(R.id.taskContent);
            manageUsers = itemView.findViewById(R.id.manageUserIcon);
            addUser = itemView.findViewById(R.id.addUserIcon);
            deleteNoteButton = itemView.findViewById(R.id.deleteNoteIcon);
            ownerOrSharedNoteIcon = itemView.findViewById(R.id.ownerOrSharedNoteIcon);

            preferences = itemView.getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
            currentEmail = preferences.getString("email", "");


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        NoteModel note = myNotes.get(position);
                        Intent intent = new Intent(itemView.getContext(), NewNote.class);
                        intent.putExtra(AddNoteCardAdapter.NOTE_KEY, note);
                        itemView.getContext().startActivity(intent);
                    }
                }
            });


            manageUsers.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        NoteModel note = myNotes.get(position);

                        // Use the context to get the LayoutInflater
                        LayoutInflater inflater = LayoutInflater.from(view.getContext());

                        Dialog dialog = new Dialog(view.getContext());
                        dialog.setContentView(R.layout.popup_layout);

                        Window window = dialog.getWindow();
                        if (window != null) {
                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                            layoutParams.copyFrom(window.getAttributes());

                            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Replace with your desired width
                            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Replace with your desired height

                            window.setAttributes(layoutParams);
                        }

                        // Adding cards
                        LinearLayout cardContainer = dialog.findViewById(R.id.popupLayout);
                        ArrayList<PermissionModel> userPermissionModelList = note.getUserPermissionModelList();
                        if(userPermissionModelList != null) {
                            userPermissionModelList.forEach((userPermissionModel) -> {
                                String permission = userPermissionModel.getPermission();
                                UserModel user = userPermissionModel.getUser();

                                View cardView = inflater.inflate(R.layout.manage_user_layout, null);

                                TextView nameTextView = cardView.findViewById(R.id.nameTextView);
                                nameTextView.setText(user.getName());

                                TextView emailTextView = cardView.findViewById(R.id.emailTextView);
                                emailTextView.setText(user.getEmail());

                                ImageView permissionIcon = cardView.findViewById(R.id.permissionIcon);
                                ImageView togglePermission = cardView.findViewById(R.id.setPermissionIcon);



                                if(permission.equals(PermissionModel.OWNER)) {
                                    permissionIcon.setImageResource(R.drawable.ic_owner_svgrepo_com);
                                    togglePermission.setImageResource(R.drawable.ic_owner_svgrepo_com);
                                    cardView.findViewById(R.id.permissionIcon).setOnClickListener(null);
                                } else if(permission.equals(PermissionModel.VIEW)) {
                                    permissionIcon.setImageResource(R.drawable.ic_view_svgrepo_com);
                                    togglePermission.setImageResource(R.drawable.ic_write_pen_pencil_svgrepo_com);
                                } else {
                                    permissionIcon.setImageResource(R.drawable.ic_write_pen_pencil_svgrepo_com);
                                    togglePermission.setImageResource(R.drawable.ic_view_svgrepo_com);
                                }

                                if(!permission.equals(PermissionModel.OWNER)) {
                                    togglePermission.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String permission = userPermissionModel.getPermission();

                                            if (permission.equals(PermissionModel.OWNER)) {
                                                permissionIcon.setImageResource(R.drawable.ic_owner_svgrepo_com);
                                                togglePermission.setImageResource(R.drawable.ic_owner_svgrepo_com);
                                            } else if (permission.equals(PermissionModel.VIEW)) {
                                                setPermission(PermissionModel.EDIT);
                                                permissionIcon.setImageResource(R.drawable.ic_write_pen_pencil_svgrepo_com);
                                                togglePermission.setImageResource(R.drawable.ic_view_svgrepo_com);
                                            } else {
                                                setPermission(PermissionModel.VIEW);
                                                permissionIcon.setImageResource(R.drawable.ic_view_svgrepo_com);
                                                togglePermission.setImageResource(R.drawable.ic_write_pen_pencil_svgrepo_com);
                                            }
                                        }

                                        private void setPermission(String newPermission) {
                                            userPermissionModelList.stream()
                                                    .filter(element -> user.getEmail().equals(element.getUser().getEmail()))
                                                    .forEach(element -> element.setPermission(newPermission));

                                            userPermissionModel.setPermission(newPermission);
                                            note.setUserPermissionModelList(userPermissionModelList);
                                            myRef.child(note.getId()).child("userPermissionModelList").setValue(userPermissionModelList);
                                        }
                                    });
                                }




                                ImageView deleteUser = cardView.findViewById(R.id.deleteImageView);
                                deleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cardContainer.removeView(cardView);
                                        ArrayList<PermissionModel> updatedPermissionModelList = userPermissionModelList.stream()
                                                .filter(element -> !user.getEmail().equals(element.getUser().getEmail()))
                                                .collect(Collectors.toCollection(ArrayList::new));

                                        myRef.child(note.getId()).child("userPermissionModelList").setValue(updatedPermissionModelList);
                                        Toast.makeText(view.getContext(), "User removed!" + note.getId(), Toast.LENGTH_SHORT).show();
                                    }
                                });



                                ImageView ownerNoteIcon = cardView.findViewById(R.id.ownerIcon);
                                if(user.getEmail().equals(note.getCreatedBy().getEmail())) {
                                    ownerNoteIcon.setImageResource(R.drawable.ic_crown_svgrepo_com);
                                } else {
                                    ((ViewGroup) ownerNoteIcon.getParent()).removeView(ownerNoteIcon);
                                }

                                ImageView liveIcon = cardView.findViewById(R.id.liveIcon);
                                if(user.getEmail().equals(currentEmail)) {
                                    liveIcon.setImageResource(R.drawable.ic_triangle_svgrepo_com);
                                } else {
                                    ((ViewGroup) liveIcon.getParent()).removeView(liveIcon);
                                }


                                if(note!= null && note.getCreatedBy().getEmail().equals(user.getEmail())) {
                                    cardView.findViewById(R.id.deleteImageView).setVisibility(View.INVISIBLE);
                                }

                                if(note!= null && !note.getCreatedBy().getEmail().equals(currentEmail)) {
                                    cardView.findViewById(R.id.deleteImageView).setVisibility(View.INVISIBLE);
                                    cardView.findViewById(R.id.setPermissionIcon).setVisibility(View.INVISIBLE);
                                }
                                cardContainer.addView(cardView);
                            });
                        }

                        dialog.show();
                        Toast.makeText(view.getContext(), "Manage Users!", Toast.LENGTH_SHORT).show();
                    }
                }


            });



            addUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        NoteModel note = myNotes.get(position);
                        System.out.println(note.getTitle());
                        // Use the context to get the LayoutInflater
                        LayoutInflater inflater = LayoutInflater.from(view.getContext());

                        // Inflate the add_user_layout
                        View dialogView = inflater.inflate(R.layout.add_user_layout, null);

                        // Initialize the "Add User" button inside the add_user_layout
                        Button addUserButtonInDialog = dialogView.findViewById(R.id.addUserButton);

                        // Set a click listener for the "Add User" button
                        addUserButtonInDialog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addUserToTask(note);
                            }

                            private void addUserToTask(NoteModel note) {
                                EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
                                EditText nickNameEditText = dialogView.findViewById(R.id.nicknameEditText);

                                if (emailEditText != null && nickNameEditText != null) {
                                    String email = emailEditText.getText() != null ? emailEditText.getText().toString() : null;
                                    String nickName = nickNameEditText.getText() != null ? nickNameEditText.getText().toString() : null;
                                    if(email == null || email.equals(note.getCreatedBy().getEmail())) {
                                        Toast.makeText(dialogView.getContext(), "Wrong Email!", Toast.LENGTH_SHORT).show();
                                    } else if (checkUserExists(note, email)) {
                                        // User with the same email already exists
                                        Toast.makeText(dialogView.getContext(), "User with the same email already exists", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // User with the same email does not exist, proceed to add
                                        UserModel user = new UserModel(nickName, email);
                                        ArrayList<PermissionModel> newUserPermissionModelList;
                                        if(note.getUserPermissionModelList() == null) {
                                            newUserPermissionModelList = new ArrayList<>();
                                        } else {
                                            newUserPermissionModelList = note.getUserPermissionModelList();
                                        }
                                        newUserPermissionModelList.add(new PermissionModel(PermissionModel.VIEW, user));
                                        myRef.child(note.getId()).child("userPermissionModelList").setValue(newUserPermissionModelList);
                                        Toast.makeText(view.getContext(), "User Added!", Toast.LENGTH_SHORT).show();
                                        if (dialog != null && dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                    }
                                } else {
                                    Log.e("AddNoteCardAdapter", "emailEditText or nickNameEditText is null");
                                }
                            }

                            private boolean checkUserExists(NoteModel note, String email) {
                                if (note != null && note.getUserPermissionModelList() != null) {
                                    // Iterate through the user list to check if the email exists
                                    for (PermissionModel permissionModel : note.getUserPermissionModelList()) {
                                        if (permissionModel.getUser().getEmail().equals(email)) {
                                            return true; // User with the same email already exists
                                        }
                                    }
                                }
                                return false; // User with the same email does not exist
                            }
                        });

                        // Create the dialog
                        dialog = new Dialog(view.getContext());
                        dialog.setContentView(dialogView);

                        Window window = dialog.getWindow();
                        if (window != null) {
                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                            layoutParams.copyFrom(window.getAttributes());

                            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Replace with your desired width
                            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Replace with your desired height

                            window.setAttributes(layoutParams);
                        }

                        // Show the dialog
                        dialog.show();
                    }
                }
            });



            deleteNoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        NoteModel note = myNotes.get(position);
                        myRef.child(note.getId()).removeValue();
                        Toast.makeText(view.getContext(), "Note deleted!", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }
}

