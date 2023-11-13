package com.example.project_7;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.project_7.adapter.AddNoteCardAdapter;
import com.example.project_7.model.NoteModel;
import com.example.project_7.model.PermissionModel;
import com.example.project_7.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class NewNote extends AppCompatActivity {
    String content;
    String title;
    EditText contentView;
    EditText titleView;
    NoteModel note;
    UserModel owner;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Notes");
    DatabaseReference usersRef = database.getReference("Users");


    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        ImageView save = findViewById(R.id.saveIcon);

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String activeUserEmail = preferences.getString("email", "");

        usersRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(NewNote.this, "Error Getting Data!", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> registeredUsers = (Map<String, Object>) task.getResult().getValue();
                    if(registeredUsers != null) {
                        registeredUsers.forEach((key, userData) -> {
                            Optional.ofNullable((Map<String, Object>) userData)
                                    .map(userMap -> {
                                        String emailId = (String) userMap.get("email");
                                        String name = (String) userMap.get("name");

                                        if (emailId != null && emailId.equals(activeUserEmail)) {
                                            owner = new UserModel(name, emailId);
                                            return false;
                                        }
                                        return true; // Indicate that the iteration should continue
                                    })
                                    .orElseGet(() -> {
                                        // Handle the case where userData is not a Map
                                        Toast.makeText(NewNote.this, "Invalid User Data!", Toast.LENGTH_SHORT).show();
                                        return false; // Indicate that the iteration should continue
                                    });
                        });
                    } else {
                        Toast.makeText(NewNote.this, "Email Not Registered!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                titleView = findViewById(R.id.title);
                contentView = findViewById(R.id.content);
                title = titleView.getText().toString();
                content = contentView.getText().toString();

                if(note!= null && note.getId()!=null) {
                    // update the note
                    String id = note.getId();
//                    note.setContent(content);
//                    note.setTitle(title);
                    myRef.child(id).child("title").setValue(title);
                    myRef.child(id).child("content").setValue(content);
                    Toast.makeText(NewNote.this, "Data saved to DB!", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<PermissionModel> sharedWith = new ArrayList<>();
                    sharedWith.add(new PermissionModel(PermissionModel.OWNER, owner));
                    note = new NoteModel(title, content, sharedWith, owner);
                    myRef.push().setValue(note);
                    Toast.makeText(NewNote.this, "New note created!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(AddNoteCardAdapter.NOTE_KEY)) {
            note = (NoteModel) intent.getParcelableExtra(AddNoteCardAdapter.NOTE_KEY);

            titleView = findViewById(R.id.title);
            contentView = findViewById(R.id.content);

            titleView.setText(note.getTitle());
            contentView.setText(note.getContent());


            //checking permissions
            ArrayList<PermissionModel> userPermissionModelList = note.getUserPermissionModelList();
            userPermissionModelList.stream()
                    .filter(element -> activeUserEmail.equals(element.getUser().getEmail()))
                    .forEach(element -> {
                        String permission = element.getPermission();
                        if(permission.equals(PermissionModel.VIEW)) {
                            titleView.setEnabled(false);
                            titleView.setFocusable(false);
                            titleView.setFocusableInTouchMode(false);

                            contentView.setEnabled(false);
                            contentView.setFocusable(false);
                            contentView.setFocusableInTouchMode(false);

                            save.setEnabled(false);
                            save.setFocusable(false);
                            save.setFocusableInTouchMode(false);
                        }
                    });

        }
    }
}

