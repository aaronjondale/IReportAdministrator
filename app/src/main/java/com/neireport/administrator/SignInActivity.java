package com.neireport.administrator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private Button button_signIn;
    private EditText field_emailAddress, field_password;
    private ProgressBar progressBar;
    private TextView text_dontHaveAnAccount, text_forgotPassword;

    private final static String REQUIRED_FIELD = "Required Field!";
    private final static String ERROR_INVALID_CREDENTIALS = "The email or password you entered doesn't match. Please check your email or password and try again.";
    private final static String SUCCESSFUL = "Successful";

    private FirebaseAuth authentication;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        button_signIn = findViewById(R.id.button_signIn);

        field_emailAddress = findViewById(R.id.field_emailAddress);
        field_password = findViewById(R.id.field_password);

        progressBar = findViewById(R.id.progressBar);

        text_dontHaveAnAccount = findViewById(R.id.text_dontHaveAnAccount);
        text_forgotPassword = findViewById(R.id.text_forgotPassword);

        authentication = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void signInButton(View view) {
        String email_address = field_emailAddress.getText().toString();
        String password = field_password.getText().toString();
        validateInputs(email_address, password);
    }

    public void forgotPasswordButton(View view) {
        Toast.makeText(this, "Test: Forgot Password", Toast.LENGTH_SHORT).show();
    }

    public void dontHaveAnAccountButton(View view) {
        startSignUpActivity();
    }

    public void validateInputs(String email_address, String password) {
        if (TextUtils.isEmpty(email_address) || TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(email_address) && TextUtils.isEmpty(password)) {
                field_password.setError(REQUIRED_FIELD);
                field_emailAddress.setError(REQUIRED_FIELD);
                field_emailAddress.requestFocus();
                return;
            } else if (TextUtils.isEmpty(password)) {
                field_password.setError(REQUIRED_FIELD);
                field_password.requestFocus();
                return;
            } else {
                field_emailAddress.setError(REQUIRED_FIELD);
                field_emailAddress.requestFocus();
                return;
            }
        } else {
            progressBarDelay(email_address, password);
        }
    }

    public void progressBarDelay(final String emailAddress, final String password) {
        int delayTime = 2000;
        showProgressBar();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkFirebaseAuthenticationCredentials(emailAddress, password);
            }
        }, delayTime); //Timeout
    }

    public void checkFirebaseAuthenticationCredentials(String email_address, String password) {
        authentication.signInWithEmailAndPassword(email_address, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignInActivity.this, SUCCESSFUL, Toast.LENGTH_SHORT).show();
                    startHomeFeedActivity();
                } else {
                    hideProgressBar();
                    try {
                        throw task.getException();
                    } catch(FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(SignInActivity.this, ERROR_INVALID_CREDENTIALS, Toast.LENGTH_SHORT).show();
                    } catch(Exception e) {
                        Log.e("mylog", e.getLocalizedMessage());
                    }
                    Toast.makeText(SignInActivity.this, ERROR_INVALID_CREDENTIALS, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        button_signIn.setEnabled(false);
        text_forgotPassword.setEnabled(false);
        text_dontHaveAnAccount.setEnabled(false);

        field_emailAddress.setEnabled(false);
        field_password.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        button_signIn.setEnabled(true);
        text_forgotPassword.setEnabled(true);
        text_dontHaveAnAccount.setEnabled(true);

        field_emailAddress.setEnabled(true);
        field_password.setEnabled(true);
    }

    private void startHomeFeedActivity() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void startSignUpActivity() {
        finish();
        startActivity(new Intent(this, SignUpActivity.class));
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
