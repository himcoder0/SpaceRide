package com.example.newapp.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.newapp.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;


import java.io.IOException;
import java.util.UUID;

public class UserProfileActivity extends AppCompatActivity {

    private TextView name_tv;
    private TextView email_tv;
    private TextView mode_tv;
    private ImageView imgProfile;
    private ProgressBar progressBar;
    private String userPic;
    private Boolean updateFromAllList;
    private Uri imagePath;   // global variable to store the image from gallery and then show it on profile photo icon
    private String loginMode;

    ActivityResultLauncher<Intent> imgPickLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        // associating variables with views using corresponding id(s)
        imgProfile = findViewById(R.id.uploadImage_b);
        progressBar = findViewById(R.id.progressBar_profile);
        mode_tv = findViewById(R.id.mode_info1_user_profile);

        name_tv = findViewById(R.id.name_profile_et);
        email_tv = findViewById(R.id.email_profile_et);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        loginMode = intent.getStringExtra("loginMode");
        updateFromAllList = intent.getBooleanExtra("update_from_allList", false);

        if (loginMode.equals("user")) {
            mode_tv.setText("User");
        } else if (loginMode.equals("admin")) {
            mode_tv.setText("admin");
        }

        if (updateFromAllList) {
            userPic = intent.getStringExtra("sender_pic");
            name_tv.setText(intent.getStringExtra("sender_name"));
            email_tv.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        CircularProgressDrawable circularProgressDrawable =
                new CircularProgressDrawable(UserProfileActivity.this);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        Glide.with(getApplicationContext()).load(userPic).error(R.drawable.account_img)
                .placeholder(circularProgressDrawable)
                .into(imgProfile);

        progressBar.setVisibility(View.GONE);

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(UserProfileActivity.this)
                        .crop()
                        .compress(512)
                        .maxResultSize(512, 512)	//Final image resolution
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Get the image selected in gallery on icon
        if (resultCode == RESULT_OK && data != null) {

            // Store image file in form of Uri type data
            imagePath = data.getData();
            try {
                getImageInImageView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Transform Uri (path) to Bitmap and put image in imageView.
    private void getImageInImageView() throws IOException {

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Surrounding the code with try catch block to handle exception (in case image not found!!!)
        imgProfile.setImageBitmap(bitmap);
        UploadImage();
    }

    private void UploadImage() {

        // Progress dialog to show the percentage of image uploaded while uploading image
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        FirebaseStorage.getInstance().getReference("images/" + UUID.randomUUID().toString())
                .putFile(imagePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

            /*To upload photo to firebaseStorage and generating random Unique ID so that no two images have same ID
             .putFile associates the image having unique ID to our user */

                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) { //When uploading of photo is complete

                        //If successfully uploaded then show image uploaded
                        if (task.isSuccessful()) {
                            task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                //if task is successful download url of image from storage

                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {  //Downloading takes time

                                    //If url downloaded successfully then call method having url as argument
                                    if (task.isSuccessful()) {
                                        uploadProfilePicture(task.getResult().toString());
                                    }
                                }
                            });
                            Toast.makeText(UserProfileActivity.this, "Image Uploaded",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss(); //on completion in either case dismiss process dialog

                    }

                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        //Calculating the percentage of image uploaded
                        double progress = 100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount();

                        //Setting up %age of image uploaded on progress dialogue
                        progressDialog.setMessage(" Uploaded " + (int) progress + "%");
                    }
                });
    }

    //Uploading profile picture of user.
    private void uploadProfilePicture(String url) {
        /* After downloading image url from database we update it in realtime database by referencing using user UID(path).
         Path will be : user/Unique UID associated with user/profilePic */
        try {
            if(loginMode.equals("user")) {
                FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getCurrentUser()
                        .getUid() + "/profilePic").setValue(url)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            } else if(loginMode.equals("admin")){
                FirebaseDatabase.getInstance().getReference("admin/" + FirebaseAuth.getInstance().getCurrentUser()
                        .getUid() + "/profilePic").setValue(url)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Slow Internet Connection", Toast.LENGTH_SHORT).show();
        }
        userPic = url;
    }

}