package com.example.project_7;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project_7.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RegisterUser extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        Button register = findViewById(R.id.registerButton);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText usernameEditText = findViewById(R.id.usernameEditText);
                String name = usernameEditText.getText().toString();

                EditText emailEditText = findViewById(R.id.emailEditText);
                String email = emailEditText.getText().toString();

                EditText passwordEditText = findViewById(R.id.passwordEditText);
                String password = passwordEditText.getText().toString();

                EditText verifyPasswordEditText = findViewById(R.id.verifyPasswordEditText);
                String verifyPassword = verifyPasswordEditText.getText().toString();
                if(password.equals(verifyPassword) == false) {
                    Toast.makeText(RegisterUser.this, "Password Does Not Match!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    usersRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @TargetApi(Build.VERSION_CODES.N)
                        @Override
                        public void onComplete(@NotNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegisterUser.this, "Error Getting Data!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Map<String, Object> registeredUsers = (Map<String, Object>) task.getResult().getValue();
                                AtomicBoolean alreadyRegistered = new AtomicBoolean(false);
                                if(registeredUsers != null) {
                                    registeredUsers.forEach((key, userData) -> {
                                        String emailId = (String) ((Map<String, Object>) userData).get("email");
                                        if(emailId.equals(email)) {
                                            alreadyRegistered.set(true);
                                            Toast.makeText(RegisterUser.this, "Already Registered -"+email, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    });
                                }
                                if(!alreadyRegistered.get()){
                                    registerUser(name, email, password);
                                }
                            }
                        }

                        private void registerUser(String name, String email, String password) {
                            UserModel user = new UserModel(name, email, password);
                            usersRef.push().setValue(user);
                            Toast.makeText(RegisterUser.this, "User Registered!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterUser.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });


    }
}