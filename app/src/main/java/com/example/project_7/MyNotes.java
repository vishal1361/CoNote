package com.example.project_7;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.project_7.adapter.AddNoteCardAdapter;
import com.example.project_7.model.NoteModel;
import com.example.project_7.model.PermissionModel;
import com.example.project_7.model.UserModel;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MyNotes extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AddNoteCardAdapter adapter;
    private ArrayList<NoteModel> myNotes = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Notes");

    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String activeUserEmail = preferences.getString("email", "");

        // get all the notes created by me and shared with me
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddNoteCardAdapter(myNotes);
        recyclerView.setAdapter(adapter);

        ImageView addNote = findViewById(R.id.newNoteIcon);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Write a message to the database
                Intent intent = new Intent(getApplicationContext(), NewNote.class);
                startActivity(intent);
            }
        });

        ImageView logout = findViewById(R.id.logoutIcon);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });


        myRef.addValueEventListener(new ValueEventListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> allNotes = (Map<String, Object>) snapshot.getValue();

                if (allNotes != null) {
                    myNotes.clear();
                    allNotes.forEach((id, value) -> {
                        NoteModel note = new NoteModel();
                        note.setId(id);

                        Optional.ofNullable(value)
                                .ifPresent(noteValue -> {
                                    note.setTitle((String) ((Map<String, Object>) noteValue).get("title"));
                                    note.setContent((String) ((Map<String, Object>) noteValue).get("content"));

                                    Optional.ofNullable(((Map<String, Object>) noteValue).get("createdBy"))
                                            .ifPresent(createdByMap -> {
                                                UserModel createdBy = new UserModel((String) ((Map<String, Object>) createdByMap).get("name"),
                                                        (String) ((Map<String, Object>) createdByMap).get("email"));
                                                note.setCreatedBy(createdBy);
                                            });

                                    Optional.ofNullable(((Map<String, Object>) noteValue).get("userPermissionModelList"))
                                            .ifPresent(userPermissionModelListTemp -> {

                                                ArrayList<PermissionModel> userPermissionModelList = new ArrayList<>();

                                                ((ArrayList<Map>) userPermissionModelListTemp).forEach(element -> {
                                                    Map<String, Object> permissionModelMap = (Map<String, Object>) element;

                                                    Map<String, Object> userMap = (Map<String, Object>) permissionModelMap.get("user");
                                                    UserModel user = new UserModel((String) userMap.get("name"), (String) userMap.get("email"));

                                                    PermissionModel permissionModel = new PermissionModel((String) permissionModelMap.get("permission"), user);
                                                    userPermissionModelList.add(permissionModel);
                                                });

                                                note.setUserPermissionModelList(userPermissionModelList);
                                            });
                                });

                        // if created by active user
                        if(note.getCreatedBy().getEmail().equals(activeUserEmail)) {
                            myNotes.add(note);
                        } else {
                            // shared with active user
                            note.getUserPermissionModelList().forEach((permissionModel) -> {
                                if(permissionModel.getUser().getEmail().equals(activeUserEmail)) {
                                    myNotes.add(note);
                                }
                            });
                        }
                    });
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });

    }
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NotNull Task<Void> task) {
                        Toast.makeText(MyNotes.this, "SignOut!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MyNotes.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

}