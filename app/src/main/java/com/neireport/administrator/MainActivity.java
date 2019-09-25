package com.neireport.administrator;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.location.aravind.getlocation.GeoLocator;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import pendingReports.PendingReportsFragment;
import reports.ReportsFragment;
import reportsChatPager.ReportsPagerFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private final Fragment fragment_account = new AccountFragment();
    private final Fragment fragment_home = new ReportsFragment();
    private final Fragment fragment_inbox = new ReportsPagerFragment();

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private Fragment activeFragment = fragment_home;

    private static final int CAMERA_PHOTO = 111;
    private Uri imageToUploadUri;

    private GeoLocator geoLocator;
    private LocationManager locationManager;

    private FirebaseAuth authentication;
    private FirebaseFirestore firestore;

    public static String accountType, city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authentication = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        getAdminInformation();
        fragmentManager.beginTransaction().add(R.id.frameLayout, fragment_account, "3").hide(fragment_account).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, fragment_inbox, "2").hide(fragment_inbox).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, fragment_home, "1").commit();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigationIcon_home :
                        fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment_home).commit();
                        fragmentManager.beginTransaction().hide(activeFragment).show(fragment_home).commit();
                        activeFragment = fragment_home;
                        return true;
                    case R.id.navigationIcon_inbox :
                        fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment_inbox).commit();
                        fragmentManager.beginTransaction().hide(activeFragment).show(fragment_inbox).commit();
                        activeFragment = fragment_inbox;
                        return true;
                    case R.id.navigationIcon_account :
                        fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment_account).commit();
                        fragmentManager.beginTransaction().hide(activeFragment).show(fragment_account).commit();
                        activeFragment = fragment_account;
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationManager = (LocationManager) MainActivity.this.getSystemService(MainActivity.this.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGPSPromptialog();
        } else {
            geoLocator = new GeoLocator(getApplicationContext(),MainActivity.this);
        }
    }

    private void getAdminInformation() {
        String userID = authentication.getUid();
        CollectionReference collection_adminAccounts = firestore.collection("Admin Accounts");
        collection_adminAccounts.document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    accountType = document.getString("Account Type");
                    city = document.getString("City");
                } else {
                    String error = task.getException().getLocalizedMessage();
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    public void cameraButton(View view) {
        dispatchTakePictureIntent();
    }

    private void startPostActivity(Uri uri_capturedImage) {
        startActivity(new Intent(this, PostActivity.class)
                .putExtra("uri_capturedImage", uri_capturedImage.toString()));
        finish();
    }

    private void dispatchTakePictureIntent() {
        Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg");
        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        imageToUploadUri = Uri.fromFile(f);
        startActivityForResult(chooserIntent, CAMERA_PHOTO);
    }

    private void cropActivity(Uri uri) {
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PHOTO && resultCode == Activity.RESULT_OK) {
            if(imageToUploadUri != null){
                Uri selectedImage = imageToUploadUri;
                getContentResolver().notifyChange(selectedImage, null);
                Bitmap reducedSizeBitmap = getBitmap(imageToUploadUri.getPath());
                if(reducedSizeBitmap != null){
                    cropActivity(selectedImage);
                }else{
                    Toast.makeText(this,"Error while capturing Image",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this,"Error while capturing Image",Toast.LENGTH_LONG).show();
            }
        } else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                getContentResolver().notifyChange(resultUri, null);
                Bitmap reducedSizeBitmap = getBitmap(imageToUploadUri.getPath());
                if(reducedSizeBitmap != null){
                    startPostActivity(resultUri);
                }else{
                    Toast.makeText(this,"Error while capturing Image",Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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
}
