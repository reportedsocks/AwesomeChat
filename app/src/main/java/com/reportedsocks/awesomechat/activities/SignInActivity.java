package com.reportedsocks.awesomechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.reportedsocks.awesomechat.R;
import com.reportedsocks.awesomechat.model.User;
import com.reportedsocks.awesomechat.utils.Util;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordRepeatEditText;
    private EditText nameEditText;
    private TextView toggleLoginSignUpTextView;
    private Button loginSignUpButton;
    private boolean loginModeActive;

    private String userEmail;
    private String userPassword;
    private String userPasswordRepeat;
    private String userName;

    private FirebaseDatabase database;
    private DatabaseReference usersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Util.setupUI(findViewById(R.id.activitySignInParentView), SignInActivity.this);
        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null){
            startActivity(new Intent(SignInActivity.this, UserListActivity.class));
            finish();
        }

        database = FirebaseDatabase.getInstance();
        usersDatabaseReference = database.getReference().child("users");

        emailEditText = findViewById(R.id.emailEditText);
        nameEditText = findViewById(R.id.nameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordRepeatEditText = findViewById(R.id.passwordRepeatEditText);
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView);
        loginSignUpButton = findViewById(R.id.loginSignUpButton);

        loginSignUpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(validateTexts())
                    loginSignUpUser();
            }
        });
    }

    private void loginSignUpUser() {

        if(loginModeActive){
            auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();
                                Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                                startActivity(intent);
                                finish();
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        } else {
            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();
                                createUser(user);
                                Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                                startActivity(intent);
                                finish();
                                // updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        }

    }

    private void createUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(userName);
    }

    private boolean validateTexts(){
        userEmail = emailEditText.getText().toString().trim();
        userPassword = passwordEditText.getText().toString().trim();
        if(loginModeActive){
            if(userEmail.equals("")){
                Toast.makeText(SignInActivity.this, "Fill in email",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else if (userPassword.equals("")){
                Toast.makeText(SignInActivity.this, "Fill in password",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else if (userPassword.length() < 6) {
                Toast.makeText(SignInActivity.this, "Password must be at least 6 characters long",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        } else {
            userPasswordRepeat = passwordRepeatEditText.getText().toString().trim();
            userName = nameEditText.getText().toString().trim();
            if(userEmail.equals("")){
                Toast.makeText(SignInActivity.this, "Fill in email",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else if (userPassword.equals("")){
                Toast.makeText(SignInActivity.this, "Fill in password",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else if (userPassword.length() < 6) {
                Toast.makeText(SignInActivity.this, "Password must be at least 6 characters long",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else if (!userPasswordRepeat.equals(userPassword)){
                Toast.makeText(SignInActivity.this, "Passwords don't match",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else if (userName.equals("")){
                Toast.makeText(SignInActivity.this, "Fill in name",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        }
    }

    public void toggleLoginMode(View view) {

        if(loginModeActive){
            loginModeActive = false;
            loginSignUpButton.setText("Sign Up");
            toggleLoginSignUpTextView.setText("Or, log in");
            passwordRepeatEditText.setVisibility(View.VISIBLE);
            nameEditText.setVisibility(View.VISIBLE);
        } else {
            loginModeActive = true;
            loginSignUpButton.setText("Log In");
            toggleLoginSignUpTextView.setText("Or, sign up");
            passwordRepeatEditText.setVisibility(View.GONE);
            nameEditText.setVisibility(View.GONE);
        }
    }
}
