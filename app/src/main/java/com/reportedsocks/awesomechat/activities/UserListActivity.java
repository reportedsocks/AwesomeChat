package com.reportedsocks.awesomechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.reportedsocks.awesomechat.R;
import com.reportedsocks.awesomechat.model.User;
import com.reportedsocks.awesomechat.data.UserAdapter;

import java.util.ArrayList;
import java.util.UUID;

public class UserListActivity extends AppCompatActivity {

    private static final int  RC_AVATAR_PICKER = 111;

    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;
    private ArrayList<User> userArrayList;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager userLayoutManager;

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference userAvatarsStorageReference;

    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userArrayList = new ArrayList<>();
        auth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        userAvatarsStorageReference = storage.getReference().child("user_avatars");

        attachUserDatabaseReferenceListener();
        buildRecyclerView();
    }

    private void attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if(usersChildEventListener == null){
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    try {
                        User user = dataSnapshot.getValue(User.class);
                        if(!user.getId().equals(auth.getCurrentUser().getUid())){
                            user.setAvatarMockUpResource(R.drawable.ic_person_black_50dp);
                            userArrayList.add(user);
                            userAdapter.notifyDataSetChanged();
                        } else {
                            userKey = dataSnapshot.getKey();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        Log.d("UserListActivity", e.getMessage());
                        Toast.makeText(UserListActivity.this, "Error with retrieving users", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            usersDatabaseReference.addChildEventListener(usersChildEventListener);
        }
    }

    private void buildRecyclerView() {
        userRecyclerView = findViewById(R.id.userListRecyclerView);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.addItemDecoration(new DividerItemDecoration(
                userRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        userLayoutManager = new LinearLayoutManager(this);
        userAdapter = new UserAdapter(userArrayList, UserListActivity.this);
        userRecyclerView.setLayoutManager(userLayoutManager);
        userRecyclerView.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(new UserAdapter.onUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToChat(position);
            }
        });

    }

    private void goToChat(int position) {
        Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
        intent.putExtra("recipientUserId", userArrayList.get(position).getId());
        intent.putExtra("recipientUserName", userArrayList.get(position).getName());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.signOut:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserListActivity.this, SignInActivity.class));
                return true;
            case R.id.changeAvatar:
                changeAvatar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void changeAvatar() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Choose an avatar"), RC_AVATAR_PICKER);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RC_AVATAR_PICKER:
                saveAvatar(resultCode, data);
                break;
        }
    }
    private void saveAvatar(int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            final StorageReference avatarReference = userAvatarsStorageReference
                    .child(UUID.randomUUID().toString());

            UploadTask uploadTask = avatarReference.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return avatarReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        usersDatabaseReference.child(userKey).child("avatarUrl").setValue(downloadUri.toString());
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }
}
