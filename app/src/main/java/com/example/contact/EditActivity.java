package com.example.contact;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import model.Contact;

public class EditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private DbHelper dbHelper;
    private EditText etName, etPhone, etEmail, etStatus, etAddress, etBirthDate, etSocialMedia;
    private Button btnUpdate, btnDelete;
    private ImageView imageView;
    private Contact contact;
    private Spinner sStatus, sSosmed;
    Uri imageUri = null;
    byte[] imageBytes = null;
    byte[] bytes = null;
    int id;
    String status;
    String sosmed;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        dbHelper = new DbHelper(this);

        etName = findViewById(R.id.edt_name);
        etPhone = findViewById(R.id.edt_phone);
        etEmail = findViewById(R.id.edt_email);
//        etStatus = findViewById(R.id.edt_status);
        etAddress = findViewById(R.id.edt_address);
//        etBirthDate = findViewById(R.id.edt_birth_date);
        etSocialMedia = findViewById(R.id.edt_social_media);


        btnUpdate = findViewById(R.id.btn_submit);
        btnDelete = findViewById(R.id.btn_delete);

        imageView = findViewById(R.id.edt_image);

        Intent intent = getIntent();
        contact = (Contact) intent.getSerializableExtra("contact");

        bytes = contact.getImage();

        etName.setText(contact.getName());
        etPhone.setText(contact.getPhone_number());
        etEmail.setText(contact.getEmail());
//        etStatus.setText(contact.getStatus());
        etAddress.setText(contact.getAddress());
//        etBirthDate.setText(contact.getBirth_date());
        etSocialMedia.setText(contact.getUsername());

        id = contact.getId();

        sStatus = findViewById(R.id.s_status);
        status = contact.getStatus();
        sStatus.setOnItemSelectedListener(this);
        loadSpinnerStatusData();

        sSosmed = findViewById(R.id.spinner_sosmed);
        sosmed = contact.getSocial_media();
        System.out.println(sosmed);
        sSosmed.setOnItemSelectedListener(new SosmedSpinner());
        loadSpinnerSosmedData();

        if(bytes != null)
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
        }

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String phone_number = etPhone.getText().toString();
                String email = etEmail.getText().toString();
//                String status = etStatus.getText().toString();
                String address = etAddress.getText().toString();
//                String birth_date = etBirthDate.getText().toString();
                String social_media = etSocialMedia.getText().toString();

                if(!validateName(name) || !validatePhoneNumber(phone_number) || !validateEmail(email)){
                    return;
                }

                try{

                    if(imageUri != null){
                        imageBytes = getBytes(EditActivity.this, imageUri);
                    }else{
                        imageBytes = bytes;
                    }

                    dbHelper.update(id, name, phone_number, email, imageBytes, status, address, null, sosmed, social_media);
                    Toast.makeText(getApplicationContext(), "Contact updated successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditActivity.this, MainActivity.class);
                    startActivity(intent);

                }catch(Exception e){
                    Toast.makeText(getApplicationContext(), "error: " + e, Toast.LENGTH_LONG).show();
                }

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmDialog();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    public void chooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        activityResultLauncher.launch(intent);
    }

    public boolean validateName(String name)
    {
        if(name.isEmpty()){
            etName.setError("Name must be filled.");
            return false;
        }else if(!name.matches("[A-Za-z]+( [A-Za-z]+)*$")){
            etName.setError("Name must be valid.");
            return false;
        }else{
            return true;
        }
    }

    public boolean validatePhoneNumber(String phoneNumber) {

        if (phoneNumber.isEmpty()) {
            etPhone.setError("Phone Number must be filled.");
            return false;
        }else if(!phoneNumber.matches("^(62)[0-9]{11}$")){
            etPhone.setError("Phone Number must be valid.");
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
                etEmail.setError("Email must be valid.");
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

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, labels);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sStatus.setAdapter(arrayAdapter);

        if(status != null){

            int spinnerPosition = arrayAdapter.getPosition(status);

            sStatus.setSelection(spinnerPosition);
         }

    }

    public void loadSpinnerSosmedData()
    {
        ArrayList labelsSosmed = new ArrayList();
        labelsSosmed.add("Select social media:");
        labelsSosmed.add("Instagram");
        labelsSosmed.add("Facebook");
        labelsSosmed.add("Twitter");

        ArrayAdapter<String> arrayAdapterSosmed = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, labelsSosmed);

        arrayAdapterSosmed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sSosmed.setAdapter(arrayAdapterSosmed);

        if(sosmed != null){

            int spinnerPosition = arrayAdapterSosmed.getPosition(sosmed);

            sSosmed.setSelection(spinnerPosition);
        }
    }


    public void showConfirmDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        builder.setTitle("Delete confirmation");
        builder.setMessage("This contact will be deleted form your device");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbHelper.delete(id, imageUri);
                Toast.makeText(EditActivity.this, "Contact successfully deleted.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                EditActivity.this.finish();
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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

    class SosmedSpinner implements AdapterView.OnItemSelectedListener
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