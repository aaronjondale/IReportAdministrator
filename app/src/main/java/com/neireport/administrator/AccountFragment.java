package com.neireport.administrator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.location.aravind.getlocation.GeoLocator;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {
    private FirebaseAuth authentication;
    private FirebaseFirestore firestore;

    private Button button_signOut;
    private CircleImageView image_userProfile;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView text_currentLocation, text_userName;

    private String name, url_userProfileImage, user_loc;

    private GeoLocator geoLocator;
    private LocationManager locationManager;

    public AccountFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGPSPromptialog();
        }
        getUserProfileInformation(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);


        geoLocator = new GeoLocator(getContext(),getActivity());


        button_signOut = view.findViewById(R.id.button_signOut);
        text_currentLocation = view.findViewById(R.id.text_currentLocation);
        text_userName = view.findViewById(R.id.text_username);
        image_userProfile = view.findViewById(R.id.image_user);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        authentication = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            final String userID = bundle.getString("userID", "null");
            getUserProfileInformation(userID);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.i("mylog", userID);
                    delayAccountUpdate(userID);
                }
            });
        } else {

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.i("mylog", "null");
                    delayAccountUpdate(null);
                }
            });
            button_signOut.setVisibility(View.VISIBLE);
            button_signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOut();
                }
            });
        }








        return view;
    }


    private void showGPSPromptialog() {
        String gpsPrompt = "GPS should be turned on";
        new AlertDialog.Builder(getActivity())
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


    public void delayAccountUpdate(final String userID) {
        int splashTime = 3000;
        Log.i("mylog", "delay: " + userID);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getUserProfileInformation(userID);
            }
        }, splashTime); //Timeout
    }

    public void getUserProfileInformation(String report_account_id) {
        final String userID;
        CollectionReference collection_users;
        if (report_account_id != null) {
            userID = report_account_id;
            collection_users = firestore.collection("Users");
        } else {
            userID = authentication.getCurrentUser().getUid();
            collection_users = firestore.collection("Admin Accounts");
        }
        collection_users.document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    swipeRefreshLayout.setRefreshing(false);
                    DocumentSnapshot document = task.getResult();
                    name = document.getString("Name");
                    url_userProfileImage = document.getString("Profile Image Link");
                    setText(name, url_userProfileImage);
                    if (userID != authentication.getCurrentUser().getUid()) {
                        user_loc = document.getString("City");
                    } else {
                        if (geoLocator != null) {
                            user_loc = geoLocator.getAddress();
                        } else {
                            user_loc = null;
                            String error = "Error in finding your location!";
                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    text_currentLocation.setText(user_loc);
                } else {
                    Toast.makeText(getActivity(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setText(String name, String url_userProfileImage){
        text_userName.setText(name);
        Picasso.get()
                .load(url_userProfileImage)
                .placeholder(R.drawable.user_profile_image)
                .into(image_userProfile);
    }

    public void signOut() {
        String current_id = authentication.getCurrentUser().getUid();
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token_id", FieldValue.delete());
        firestore.collection("Users").document(current_id).update(tokenMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                authentication.signOut();
                startHomeActivity();
            }
        });
    }

    public void startHomeActivity() {
        startActivity(new Intent(getActivity(), HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
