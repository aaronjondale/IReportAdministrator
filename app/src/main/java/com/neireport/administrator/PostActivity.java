package com.neireport.administrator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.location.aravind.getlocation.GeoLocator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private Uri uri_postImage;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private FirebaseAuth authentication;

    private GeoLocator geoLocator;

    private LocationManager locationManager;

    private EditText field_description;
    private TextView text_share;
    private ImageView image_cancel;
    private ProgressBar progressBar;
    
    private String account_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        field_description = findViewById(R.id.field_postDescription);
        text_share = findViewById(R.id.text_share);
        image_cancel = findViewById(R.id.image_cancel);
        progressBar = findViewById(R.id.progressBar);

        authentication = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        getPostCategory();

        setPostImage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationManager = (LocationManager) PostActivity.this.getSystemService(PostActivity.this.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGPSPromptialog();
        } else {
            geoLocator = new GeoLocator(getApplicationContext(),PostActivity.this);
        }
    }

    private void showGPSPromptialog() {
        String gpsPrompt = "GPS should be turned on";
        new AlertDialog.Builder(this)
                .setMessage(gpsPrompt)
                .setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startLocationSettingsActivity();
                    }
                })
                .show();

    }

    public void startLocationSettingsActivity() {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public void setPostImage() {
        String imageUrl = getIntent().getStringExtra("uri_capturedImage");
        uri_postImage = Uri.parse(imageUrl);
        Bitmap bitmap_postImageReducedSize = getBitmap(uri_postImage.getPath());
        ImageView image_post = findViewById(R.id.image_post);
        image_post.setImageBitmap(bitmap_postImageReducedSize);
    }

    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " +
                    b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("", e.getMessage(), e);
            return null;
        }
    }

    public void getPostCategory() {
        String user_id = authentication.getUid();
        CollectionReference admin_accounts = firestore.collection("Admin Accounts");
        admin_accounts.document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    account_type = task.getResult().getString("Account Type");
                    Log.i("mylogs", account_type);
                } else {
                    String error = "Error getting Admin Information!";
                    Toast.makeText(PostActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        
    }

    public String getPostDescription() {
        TextView field_postDescription = findViewById(R.id.field_postDescription);
        String postDescription = field_postDescription.getText().toString();
        return postDescription;
    }

    private void uploadToStorage() {
        showProgressBar();
        final String randomNumber = randomString(123);
        final StorageReference storageReference_postImage = storage.getReference().child("Reports").child(randomNumber + ".jpg");
        storageReference_postImage.putFile(uri_postImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference_postImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        uploadToFirestore(uri, randomNumber);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                Toast.makeText(PostActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    String randomString( int len ){
        SecureRandom rnd = new SecureRandom();
        final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder stringBuilder = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            stringBuilder.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return stringBuilder.toString();
    }

    private void startMainActivity(String documentID) {
        Intent intent_mainActivity = new Intent(this, MainActivity.class);
        startActivity(intent_mainActivity);
        finish();
    }

    public void cancelButton(View view) {
        startMainActivity(null);
    }

    public void shareButton(View view) {
        uploadToStorage();
    }

    private void uploadToFirestore(final Uri uri, final String documentID) {
        String userID = authentication.getCurrentUser().getUid();
        String imageURL = uri.toString();
        Map<String, Object> postInformation;
        postInformation = new HashMap<>();
        postInformation.put("category", account_type);
        postInformation.put("city", geoLocator.getCity());
        postInformation.put("description", getPostDescription());
        postInformation.put("documentID", documentID);
        postInformation.put("imageURL", imageURL);
        postInformation.put("latitude", geoLocator.getLattitude());
        postInformation.put("location", geoLocator.getAddress());
        postInformation.put("longitude", geoLocator.getLongitude());
        postInformation.put("timestamp", FieldValue.serverTimestamp());
        postInformation.put("userID", userID);
        CollectionReference collection = firestore.collection("Reports");
        collection.document(documentID).set(postInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressBar();
                if (task.isSuccessful()) {
                    Toast.makeText(PostActivity.this, "Successful!", Toast.LENGTH_LONG).show();
                    startMainActivity(documentID);
                } else {
                    Toast.makeText(PostActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        text_share.setEnabled(false);
        field_description.setEnabled(false);
        image_cancel.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        text_share.setEnabled(true);
        field_description.setEnabled(true);
        image_cancel.setEnabled(true);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }


}
