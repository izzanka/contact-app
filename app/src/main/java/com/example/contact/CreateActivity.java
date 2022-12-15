package com.example.contact;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import db.DbHelper;

public class CreateActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    DbHelper dbHelper;
    private EditText edtName, edtPhoneNumber, edtEmail, edtStatus, edtAddress, edtBirthDate, edtSocialMedia;
    private ImageView imageView;
    private Button btnSave;
    private Spinner spinnerStatus, spinnerSosmed;
    byte[] bytes = null;
    Uri imageUri = null;
    String status = null;
    String sosmed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        dbHelper = new DbHelper(this);

        edtName = findViewById(R.id.edt_name);
        edtPhoneNumber = findViewById(R.id.edt_phone);
        edtEmail = findViewById(R.id.edt_email);
//        edtStatus = findViewById(R.id.edt_status);
        edtAddress = findViewById(R.id.edt_address);
//        edtBirthDate = findViewById(R.id.edt_birth_date);
        edtSocialMedia = findViewById(R.id.edt_social_media);

        btnSave = findViewById(R.id.btn_submit);

        imageView = findViewById(R.id.imageView);

        spinnerStatus = findViewById(R.id.spinner_status);
        loadSpinnerStatusData();
        spinnerStatus.setOnItemSelectedListener(this);

        spinnerSosmed = findViewById(R.id.spinner_sosmed);
        loadSpinnerSosmedData();
        spinnerSosmed.setOnItemSelectedListener(new SpinnerSosmed());


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtName.getText().toString();
                String phone_number = edtPhoneNumber.getText().toString();
                String email = edtEmail.getText().toString();
//                String status = edtStatus.getText().toString();
                String address = edtAddress.getText().toString();
//                String birth_date = edtBirthDate.getText().toString();
                String social_media = edtSocialMedia.getText().toString();

                if(!validateName(name) || !validatePhoneNumber(phone_number) || !validateEmail(email)){
                    return;
                }

                try{
                    if(imageUri != null){
                        bytes = getBytes(CreateActivity.this, imageUri);
                    }
                    System.out.println(sosmed);
                    dbHelper.store(name, phone_number, email, bytes, status, address, null, sosmed, social_media);

                    Toast.makeText(getApplicationContext(), "Contact created successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                    startActivity(intent);

                }catch(Exception e){
                    Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                }

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    public boolean validateName(String name)
    {
        if(name.isEmpty()){
            edtName.setError("Name must be filled.");
            return false;
        }else if(!name.matches("[A-Za-z]+( [A-Za-z]+)*$")){
            edtName.setError("Name must be valid.");
            return false;
        }else{
            return true;
        }
    }

    public boolean validatePhoneNumber(String phoneNumber) {

        if (phoneNumber.isEmpty()) {
            edtPhoneNumber.setError("Phone Number must be filled.");
            return false;
        }else if(!phoneNumber.matches("^(62)[0-9]{11}$")){
            edtPhoneNumber.setError("Phone Number must be valid.");
            return false;
        }
        else {
            return true;
        }

    }

    public boolean validateEmail(String email)
    {
        if(!email.isEmpty()){
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edtEmail.setError("Email must be valid.");
                return false;
            }else{
                return true;
            }
        }else{
            return true;
        }

    }


    public void loadSpinnerStatusData()
    {
        ArrayList labels = new ArrayList();
        labels.add("Select status:");
        labels.add("Family");
        labels.add("Friend");
        labels.add("Colleague");
        labels.add("Company");
        labels.add("Other");

        ArrayAdapter<String> arrayAdapterStatus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, labels);

        arrayAdapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerStatus.setAdapter(arrayAdapterStatus);
    }

    public void loadSpinnerSosmedData()
    {
        ArrayList labels = new ArrayList();
        labels.add("Select social media:");
        labels.add("Instagram");
        labels.add("Facebook");
        labels.add("Twitter");

        ArrayAdapter<String> arrayAdapterSosmed = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, labels);

        arrayAdapterSosmed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSosmed.setAdapter(arrayAdapterSosmed);
    }



    public void chooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        activityResultLauncher.launch(intent);
    }

    public static byte[] getBytes(Context context, Uri uri) throws IOException {
        InputStream iStream = context.getContentResolver().openInputStream(uri);
        try {
            return getBytes(iStream);
        } finally {
            // close the stream
            try {
                iStream.close();
            } catch (IOException ignored) { /* do nothing */ }
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {

        byte[] bytesResult = null;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 2048;
        byte[] buffer = new byte[bufferSize];
        try {
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            bytesResult = byteBuffer.toByteArray();
        } finally {
            // close the stream
            try{ byteBuffer.close(); } catch (IOException ignored){ /* do nothing */ }
        }
        return bytesResult;
    }

    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            Intent intent  = result.getData();
                           if(intent != null && intent.getData() != null){
                               imageUri = intent.getData();
                               imageView.setImageURI(imageUri);
                            }
                        }
                    }
            );

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        status = adapterView.getItemAtPosition(i).toString();
        if(status == "Select status:"){
            status = null;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    class SpinnerSosmed implements AdapterView.OnItemSelectedListener
    {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            sosmed = adapterView.getItemAtPosition(i).toString();
            if(sosmed == "Select social media:"){
                sosmed = null;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }
}