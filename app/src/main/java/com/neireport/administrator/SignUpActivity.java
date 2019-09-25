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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private Button button_createAccount;
    private EditText field_accountType, field_city, field_confirmPassword, field_emailAddress, field_name, field_password;
    private TextView text_termsOfUse, text_alreadyHaveAnAccount;
    private ProgressBar progressBar;


    private final static String SUCCESSFUL = "Successful";
    private final static String REQUIRED_FIELD = "Required Field";
    private final static String PASSWORD_LENGTH_ERROR = "Password must be 8 characters above!";
    private final static String PASSWORD_MISMATCH_ERROR = "Password does not match!";

    private FirebaseAuth authentication;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        button_createAccount = findViewById(R.id.button_createAccount);

        field_accountType = findViewById(R.id.field_accountType);
        field_city = findViewById(R.id.field_city);
        field_confirmPassword = findViewById(R.id.field_confirmPassword);
        field_emailAddress = findViewById(R.id.field_emailAddress);
        field_name = findViewById(R.id.field_name);
        field_password = findViewById(R.id.field_password);

        text_termsOfUse = findViewById(R.id.text_termsOfUse);
        text_alreadyHaveAnAccount = findViewById(R.id.text_alreadyHaveAnAccount);

        progressBar = findViewById(R.id.progressBar);

        authentication = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void createAccountButton(View view) {
        String accountType = field_accountType.getText().toString();
        String city = field_city.getText().toString();
        String name = field_name.getText().toString();
        String emailAddress = field_emailAddress.getText().toString();
        String password = field_password.getText().toString();
        String confirmPassword = field_confirmPassword.getText().toString();
        if (validateUserInputs(accountType, city, confirmPassword, emailAddress, name, password)) {
            createFirebaseAccount(accountType, city, emailAddress, name, password);
        }
    }

    private void createFirebaseAccount(final String accountType, final String city, final String emailAddress, final String name, String password) {
        showProgressBar();
        authentication.createUserWithEmailAndPassword(emailAddress, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    uploadUserInformationToFirestore(accountType, city, emailAddress, name);
                } else {
                    hideProgressBar();
                    String error = task.getException().getLocalizedMessage();
                    Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadUserInformationToFirestore(String accountType, String city, String emailAddress, String name) {
        String userID = authentication.getUid();
        CollectionReference collection_users = firestore.collection("Admin Accounts");
        Map<String, Object> map_adminInfo = new HashMap<>();
        map_adminInfo.put("Account Type", accountType);
        map_adminInfo.put("City", city);
        map_adminInfo.put("Email Address", emailAddress);
        map_adminInfo.put("Date Created", FieldValue.serverTimestamp());
        map_adminInfo.put("Name", name);
        map_adminInfo.put("Profile Image Link", null);
        collection_users.document(userID).set(map_adminInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressBar();
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, SUCCESSFUL, Toast.LENGTH_SHORT).show();
                    startUploadUserProfileImageActivity();
                } else {
                    String error = task.getException().getLocalizedMessage();
                    Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startUploadUserProfileImageActivity() {
        startActivity(new Intent(this, UserProfileImageActivity.class));
        finish();
    }

    private boolean validateUserInputs(String accountType, String city, String confirmPassword, String emailAddress, String name, String password) {
        int error = 0;
        if (city.isEmpty()) {
            error++;
            field_city.setError(REQUIRED_FIELD);
            field_city.requestFocus();
        }
        if (accountType.isEmpty()) {
            error++;
            field_accountType.setError(REQUIRED_FIELD);
            field_accountType.requestFocus();
        }
        if (confirmPassword.isEmpty()) {
            error++;
            field_confirmPassword.setError(REQUIRED_FIELD);
            field_confirmPassword.requestFocus();
        }
        if (password.isEmpty()) {
            error++;
            field_password.setError(REQUIRED_FIELD);
            field_password.requestFocus();
        }
        if (emailAddress.isEmpty()) {
            error++;
            field_emailAddress.setError(REQUIRED_FIELD);
            field_emailAddress.requestFocus();
        }
        if (name.isEmpty()) {
            error++;
            field_name.setError(REQUIRED_FIELD);
            field_name.requestFocus();
        }
        if (error > 0) {
            return false;
        } else {
            boolean check = validatePasswordFields(password, confirmPassword);
            if (check) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean validatePasswordFields(String password, String confirmPassword) {
        int password_length = 8;
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            if (password.isEmpty() && confirmPassword.isEmpty()) {
                field_password.requestFocus();
                field_password.setError(REQUIRED_FIELD);
                field_confirmPassword.setError(REQUIRED_FIELD);
            } else if (confirmPassword.isEmpty()) {
                field_confirmPassword.requestFocus();
                field_confirmPassword.setError(REQUIRED_FIELD);
            } else {
                field_password.requestFocus();
                field_password.setError(REQUIRED_FIELD);
            }
            return false;
        }
        if (password.length() < password_length || confirmPassword.length() < password_length) {
            field_password.requestFocus();
            field_password.setError(PASSWORD_LENGTH_ERROR);
            field_confirmPassword.setError(PASSWORD_LENGTH_ERROR);
            return false;
        } else {
            if (!password.equals(confirmPassword)) {
                field_password.requestFocus();
                field_password.setError(PASSWORD_MISMATCH_ERROR);
                field_confirmPassword.setError(PASSWORD_MISMATCH_ERROR);
                return false;
            }
        }
        return true;
    }

    public void termsOfUseButton(View view) {
        //Empty
    }

    public void alreadyHaveAnAccountButton(View view) {
        startSignInActivity();
    }

    private void startSignInActivity() {
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        button_createAccount.setEnabled(false);
        field_accountType.setEnabled(false);
        field_city.setEnabled(false);
        field_confirmPassword.setEnabled(false);
        field_emailAddress.setEnabled(false);
        field_name.setEnabled(false);
        field_password.setEnabled(false);
        text_termsOfUse.setEnabled(false);
        text_alreadyHaveAnAccount.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        button_createAccount.setEnabled(true);
        field_accountType.setEnabled(true);
        field_city.setEnabled(true);
        field_confirmPassword.setEnabled(true);
        field_emailAddress.setEnabled(true);
        field_name.setEnabled(true);
        field_password.setEnabled(true);
        text_termsOfUse.setEnabled(true);
        text_alreadyHaveAnAccount.setEnabled(true);
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
