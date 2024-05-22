package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapplication.databinding.ActivitySignUpBinding;
import com.example.chatapplication.model.User;
import com.hbb20.CountryCodePicker;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText name = binding.editSignUpName;
        EditText email = binding.editSignUpEmail;
        CountryCodePicker countryCodePicker = binding.countryCodeSignUp;
        EditText phone = binding.editSignUpPhone;

        countryCodePicker.registerCarrierNumberEditText(phone);

        binding.btnSignUp.setOnClickListener(v -> {
            if (name.getText().toString().isEmpty()) {
                name.setError("First name is required");
                return;
            }
            if (email.getText().toString().isEmpty()) {
                email.setError("Last name is required");
                return;
            }
            if (!countryCodePicker.isValidFullNumber()) {
                phone.setError("Invalid phone number");
                return;
            }

            String phoneNum = countryCodePicker.getFullNumberWithPlus();

            User user = new User(name.getText().toString(), email.getText().toString(), phoneNum);

            Intent intent = new Intent(SignUpActivity.this, VerifyActivity.class);
            intent.putExtra("new_user", user);
            startActivity(intent);
        });

        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}