package com.example.project_7;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_7.model.UserModel;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;

    GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN = 100;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        TextView register = findViewById(R.id.registerTextView);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterUser.class);
                startActivity(intent);
            }
        });


        Button signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText emailEditText = findViewById(R.id.emailEditText);
                EditText passwordField = findViewById(R.id.passwordEditText);
                String email = emailEditText.getText().toString();
                String password = passwordField.getText().toString();

                usersRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @TargetApi(Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NotNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Error Getting Data!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Map<String, Object> registeredUsers = (Map<String, Object>) task.getResult().getValue();
                            if(registeredUsers != null) {
                                registeredUsers.forEach((key, userData) -> {
                                    Optional.ofNullable((Map<String, Object>) userData)
                                            .map(userMap -> {
                                                String emailId = (String) userMap.get("email");
                                                String pass = Optional.ofNullable((String) userMap.get("password")).orElse(null);

                                                if (emailId != null && emailId.equals(email)) {
                                                    if (pass!= null && !pass.isEmpty() && pass.equals(password)) {
                                                        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.putString("email", email);
                                                        editor.apply();

                                                        Intent intent = new Intent(MainActivity.this, MyNotes.class);
                                                        startActivity(intent);
//                                                        Toast.makeText(MainActivity.this, "Signed In!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                        return false;
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                                                        return false;
                                                    }
                                                }
                                                return true; // Indicate that the iteration should continue
                                            })
                                            .orElseGet(() -> {
                                                // Handle the case where userData is not a Map
                                                Toast.makeText(MainActivity.this, "Invalid User Data!", Toast.LENGTH_SHORT).show();
                                                return false; // Indicate that the iteration should continue
                                            });
                                });

                            } else {
                                Toast.makeText(MainActivity.this, "Email Not Registered!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                });
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            if (acct != null) {
                SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("email", acct.getEmail());
                editor.apply();
                registerUserFromGoogleSignIn(acct.getGivenName(), acct.getEmail());
//                Toast.makeText(MainActivity.this, "Signed In!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MyNotes.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Not Signed In!", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("Sign In Error :", e.getMessage());
        }
    }

    private void registerUserFromGoogleSignIn(String name, String email) {
        usersRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NotNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Error Getting Data!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Map<String, Object> registeredUsers = (Map<String, Object>) task.getResult().getValue();
                    AtomicBoolean alreadyRegistered = new AtomicBoolean(false);
                    if(registeredUsers != null) {
                        registeredUsers.forEach((key, userData) -> {
                            String emailId = (String) ((Map<String, Object>) userData).get("email");
                            if(emailId.equals(email)) {
                                alreadyRegistered.set(true);
//                                Toast.makeText(MainActivity.this, "Already Registered -"+email, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                    }
                    if(!alreadyRegistered.get()){
                        registerUser(name, email);
                    }
                }
            }

            private void registerUser(String name, String email) {
                UserModel user = new UserModel(name, email);
                usersRef.push().setValue(user);
            }
        });
    }

}
//
//oneTapClient = Identity.getSignInClient(this);
//        signUpRequest = BeginSignInRequest.builder()
//        .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//        .setSupported(true)
//        // Your server's client ID, not your Android client ID.
//        .setServerClientId(getString(R.string.client_id))
//        // Show all accounts on the device.
//        .setFilterByAuthorizedAccounts(false)
//        .build())
//        .build();
//
//
//        ActivityResultLauncher<IntentSenderRequest> activityResultLauncher =
//        registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
//        new ActivityResultCallback<ActivityResult>() {
//@Override
//public void onActivityResult(ActivityResult result) {
//        if(result.getResultCode() == Activity.RESULT_OK) {
//        try {
//        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
//        String idToken = credential.getGoogleIdToken();
//        if (idToken !=  null) {
//        // Got an ID token from Google. Use it to authenticate
//        // with your backend.
//
//        Log.d("TAG", "Got ID token.");
//
//        String email = credential.getId();
//        Toast.makeText(getApplicationContext(), "Email: "+email, Toast.LENGTH_SHORT).show();
//

//        }
//        } catch (ApiException e) {
//        // ...
//        //Caller has been temporarily blocked due to too many canceled sign-in prompts.
//        // dial this in ur android device ->  *#*#66382723#*#*
//        e.printStackTrace();
//        }
//        }
//        }
//        });
//
//        Button button = findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View view) {
//        oneTapClient.beginSignIn(signUpRequest)
//        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<BeginSignInResult>() {
//@Override
//public void onSuccess(BeginSignInResult result) {
//        IntentSenderRequest intentSenderRequest =
//        new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
//        activityResultLauncher.launch(intentSenderRequest);
//        }
//        })
//        .addOnFailureListener(MainActivity.this, new OnFailureListener() {
//@Override
//public void onFailure( Exception e) {
//        // No Google Accounts found. Just continue presenting the signed-out UI.
//        Log.d("TAG", e.getLocalizedMessage());
//        }
//        });
//        }
//        });