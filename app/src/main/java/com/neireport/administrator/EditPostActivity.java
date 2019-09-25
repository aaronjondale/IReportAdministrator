package com.neireport.administrator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class EditPostActivity extends AppCompatActivity {

    private ImageView img_post, img_cancel;
    private EditText field_desc;
    private ProgressBar progressBar;
    private TextView txt_update;

    private String document_id;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        document_id = getIntent().getStringExtra("document");

        field_desc = findViewById(R.id.field_postDescription);
        img_cancel = findViewById(R.id.image_cancel);
        img_post = findViewById(R.id.image_post);
        progressBar = findViewById(R.id.progressBar);
        txt_update = findViewById(R.id.text_update);

        firestore = FirebaseFirestore.getInstance();

        setUpData();
    }

    public void setUpData() {
        firestore.collection("Reports").document(document_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String img_url = task.getResult().getString("imageURL");
                    String desc = task.getResult().getString("description");
                    Picasso.get()
                            .load(img_url)
                            .into(img_post);
                    field_desc.setText(desc);
                } else {
                    String error = "Error getting report informations!";
                    Toast.makeText(EditPostActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cancelButton(View view) {
        startMainActivity();
    }

    public void updateButton(View view) {
        updateData();
    }

    private void updateData() {
        showProgressBar();
        String update_text = field_desc.getText().toString();
        firestore.collection("Reports").document(document_id).update("description", update_text).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressBar();
                if (task.isSuccessful()) {
                    String successful = "Successfully Updated!";
                    Toast.makeText(EditPostActivity.this, successful, Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    String error = "Error, Try Again!";
                    Toast.makeText(EditPostActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        txt_update.setEnabled(false);
        field_desc.setEnabled(false);
        img_cancel.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        txt_update.setEnabled(true);
        field_desc.setEnabled(true);
        img_cancel.setEnabled(true);
    }

    public void startMainActivity() {
        Intent intent_mainActivity = new Intent(this, MainActivity.class);
        startActivity(intent_mainActivity);
        finish();
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
