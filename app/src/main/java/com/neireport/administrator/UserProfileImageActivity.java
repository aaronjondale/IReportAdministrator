package com.neireport.administrator;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileImageActivity extends AppCompatActivity {

    private Button button_upload;
    private CircleImageView image_userProfile;
    private ImageButton image_add;
    private ProgressBar progressBar;
    private TextView text_emailAddress;

    private Uri uri_userProfileImage;

    private final static String SUCCESSFUL = "Successful";
    private final static String PICTURE_REQUIRED = "Picture is required!";

    private FirebaseAuth authentication;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_image);

        button_upload = findViewById(R.id.button_upload);

        image_userProfile = findViewById(R.id.image_user);

        image_add = findViewById(R.id.imageButton_add);

        progressBar = findViewById(R.id.progressBar);

        text_emailAddress = findViewById(R.id.text_applicationName);

        authentication = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void uploadButton(View view) {
        if (validateImage() == true) {
            progressBarDelay();
        }
    }

    public void addButton(View view) {
        cropActivity();
    }

    public void emailAddressButton(View view) {
        sendEmail();
    }

    private void sendEmail() {
        String emailAddress = getString(R.string.emailAddress);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{emailAddress});
        i.putExtra(Intent.EXTRA_SUBJECT, "");
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email application installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateImage() {
        if (uri_userProfileImage != null && !uri_userProfileImage.equals(Uri.EMPTY)) {
            return true;
        } else {
            hideProgressBar();
            Toast.makeText(this, PICTURE_REQUIRED, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void progressBarDelay() {
        int delayTime = 2000;
        showProgressBar();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadToStorage();
            }
        }, delayTime);
    }

    private void uploadToStorage() {
        final String userID = authentication.getCurrentUser().getUid();
        final StorageReference filePath = storage.getReference().child("Admin Accounts").child("Profile").child(userID + ".jpg");
        UploadTask uploadTask = filePath.putFile(uri_userProfileImage);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri_download = task.getResult();
                    uploadToFirestore(uri_download, userID);
                } else {
                    hideProgressBar();
                    String error = task.getException().getLocalizedMessage();
                    Toast.makeText(UserProfileImageActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadToFirestore(Uri uri_image, String userID) {
        String url_file = uri_image.toString();
        firestore.collection("Admin Accounts").document(userID).update("Profile Image Link", url_file).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressBar();
                if (task.isSuccessful()) {
                    Toast.makeText(UserProfileImageActivity.this, SUCCESSFUL, Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    String error = task.getException().getLocalizedMessage();
                    Toast.makeText(UserProfileImageActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void cropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri_userProfileImage = result.getUri();
                image_userProfile.setImageURI(uri_userProfileImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                String error = result.getError().getLocalizedMessage();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        button_upload.setEnabled(false);
        image_add.setEnabled(false);
        text_emailAddress.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        button_upload.setEnabled(true);
        image_add.setEnabled(true);
        text_emailAddress.setEnabled(true);
    }
}
