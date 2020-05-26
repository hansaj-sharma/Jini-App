package com.example.jini;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueNextButton;
    private RelativeLayout relativeLayout;
    private  String checker="" , phoneNumber="";

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallbacks;
    private FirebaseAuth mAuth;
    private String  mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        loadingBar= new ProgressDialog(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        phoneText= findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueNextButton = findViewById(R.id.continueNextButton);
        relativeLayout= findViewById(R.id.phoneAuth);
        ccp= (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        continueNextButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (continueNextButton.getText().equals("Submit") || checker.equals("Code Sent")) {
                    String verificationCode = codeText.getText().toString();
                    if(verificationCode.equals("")) {
                        Toast.makeText(RegistrationActivity.this, "ENTER THE CODE FIRST", Toast.LENGTH_SHORT).show();

                    }
                    else {
                        loadingBar.setTitle("CODE VERIFICATION");
                        loadingBar.setMessage("please wait , while verifying the code ");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId ,verificationCode);
                        signInWithPhoneAuthCredential(credential);

                    }

                } else {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals("")) {
                        loadingBar.setTitle("phone number verification");
                        loadingBar.setMessage("please Wait , do not click any button");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60,TimeUnit.SECONDS,RegistrationActivity.this,mcallbacks);


                    }

                    else {
                        Toast.makeText(RegistrationActivity.this, "please enter valid phone number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            });


        mcallbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(RegistrationActivity.this, "InValid Phone Number  ", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);

                continueNextButton.setText("Continue");
                codeText.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                relativeLayout.setVisibility(View.GONE);
                checker="Code Sent" ;

                mVerificationId=s;
                mResendToken= forceResendingToken;
                continueNextButton.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(RegistrationActivity.this, "CODE SENT", Toast.LENGTH_SHORT).show();
            }
        };

        }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!= null ){
            //Intent homeIntend = new Intent(RegistrationActivity.this, ContactsActivity.class);
            //startActivity(homeIntend);
            startActivity(new Intent(RegistrationActivity.this, ContactsActivity.class));
            finish();
        }



    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Resister Successfully ", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();

                        }
                        else {
                                loadingBar.dismiss();
                                String e = task.getException().toString();
                            Toast.makeText(RegistrationActivity.this,"ERROR: "+e , Toast.LENGTH_SHORT).show();
                        }

                        }

                });
    }

    private void sendUserToMainActivity(){

        Intent intent= new Intent(RegistrationActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();

    }
}
