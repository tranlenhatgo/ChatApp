package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapplication.databinding.ActivitySignUpBinding;
import com.example.chatapplication.databinding.ActivityVerifyBinding;
import com.example.chatapplication.model.User;
import com.example.chatapplication.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class VerifyActivity extends AppCompatActivity {
    private ActivityVerifyBinding binding;

    private User user;
    private String verifyCode;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent.hasExtra("new_user")) {
            user = (User) intent.getSerializableExtra("new_user");
        }

        sendOtp(user.getPhone_number(),false);

        binding.btnVerify.setOnClickListener(v -> {
            String code = binding.editVerify.getText().toString();
            if (code.isEmpty()){
                binding.editVerify.setError("Code is required");
                return;
            }
            signIn(verifyCode,code);
        });

        binding.tvResend.setOnClickListener(v -> {
            sendOtp(user.getPhone_number(),true);
        });
    }
    void sendOtp(String phone,boolean isResend){
        startResendTimer();
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        Toast.makeText(VerifyActivity.this, "Send OTP to "+phone, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(VerifyActivity.this, "Send OTP failed", Toast.LENGTH_SHORT).show();
                        Log.e("VerifyActivity", "Verification failed", e);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verifyCode = s;
                        mResendToken = forceResendingToken;
                        if(isResend){
                            Toast.makeText(VerifyActivity.this, "OTP Resent", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        if (isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(mResendToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }
    void startResendTimer(){
        binding.tvResend.setEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new java.util.TimerTask() {
            int i = 60;
            public void run() {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        binding.tvCount.setText("After "+ i +"s");
                        i--;
                        if (i <= 0){
                            binding.tvResend.setEnabled(true);
                            timer.cancel();
                            binding.tvCount.setText("After 60s");
                        }
                    }
                });
            }
        }, 0, 1000);
    }
    void signIn(String verifyCode,String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifyCode, code);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                user.setPassword(binding.editPasswordVerify.getText().toString());
                saveUser(user);
                Intent intent = new Intent(VerifyActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Verify failed", Toast.LENGTH_SHORT).show();
                Log.e("VerifyActivity", "Verify failed", task.getException());
            }
        });
    }
    void saveUser(User user){
        user.setUserId(FirebaseUtil.currentUserUid());
        FirebaseUtil.currentUserDetails().set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d("VerifyActivity", "User save success");
                } else {
                    Toast.makeText(VerifyActivity.this, "User save failed", Toast.LENGTH_SHORT).show();
                    Log.e("VerifyActivity", "User save failed", task.getException());
                }
            }
        });
    }
}


