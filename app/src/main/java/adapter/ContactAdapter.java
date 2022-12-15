package adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.contact.R;
import com.example.contact.EditActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.io.Serializable;
import java.util.ArrayList;

import db.DbHelper;
import model.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>
{
    private ArrayList<Contact> contactArrayList = new ArrayList<>();
    private Activity activity;
    private DbHelper dbHelper;

    public ContactAdapter(Activity activity)
    {
        this.activity = activity;
        dbHelper = new DbHelper(activity);
    }

    public ArrayList<Contact> getContactArrayList() {
        return contactArrayList;
    }

    public void setContactArrayList(ArrayList<Contact> contactArrayList)
    {
        if(contactArrayList.size() > 0)
        {
            this.contactArrayList.clear();
        }

        this.contactArrayList.addAll(contactArrayList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position)
    {
        String phone_number = contactArrayList.get(position).getPhone_number();
        String email = contactArrayList.get(position).getEmail();
        byte[] bytes = contactArrayList.get(position).getImage();

        if(bytes != null)
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.imageView.setImageBitmap(bitmap);
        }

        holder.tvName.setText(contactArrayList.get(position).getName());
        holder.tvPhone.setText(phone_number);

        holder.layout.setOnClickListener((View view) -> {
            Intent intent = new Intent(activity, EditActivity.class);
            intent.putExtra("contact", (Serializable) contactArrayList.get(position));
            activity.startActivity(intent);
        });

        holder.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dexter.withContext(activity)
                        .withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                makeCall(phone_number);
                            }
                            @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                showSettingsDialog();
                            }
                            @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        holder.btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dexter.withContext(activity)
                        .withPermission(Manifest.permission.SEND_SMS)
                        .withListener(new PermissionListener() {
                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                sendMessage(phone_number);
                            }
                            @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                showSettingsDialog();
                            }
                            @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();

            }
        });

        holder.btnWa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://api.whatsapp.com/send?phone=" + phone_number + "&text=" +
                        Uri.parse("");
                Intent waIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                waIntent.setPackage("com.whatsapp");
                waIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try{
                    activity.startActivity(waIntent);
                }catch (android.content.ActivityNotFoundException e){
                    Toast.makeText(activity, "Whatsapp is not installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.isEmpty()){
                    String[] TO = {email};
                    String[] CC = {""};
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);

                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                    emailIntent.putExtra(Intent.EXTRA_CC, CC);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");

                    try{
                        activity.startActivity(Intent.createChooser(emailIntent,""));
                    }catch (android.content.ActivityNotFoundException e){
                        Toast.makeText(activity, "Email client is not installed.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(activity, "This contact does not have an email.", Toast.LENGTH_SHORT).show();
                }

            }
        });



    }

    public void sendMessage(String phone_number)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone_number));
        activity.startActivity(intent);
    }

    public void makeCall(String phone_number)
    {
        String phone = phone_number.replace("62","0");
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phone));
        activity.startActivity(intent);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Need Permissions");

        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return contactArrayList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvName, tvPhone;
        RelativeLayout layout;
        Button btnCall, btnMsg, btnWa, btnEmail;
        ImageView imageView;

        public ContactViewHolder(View view)
        {
            super(view);
            tvName = view.findViewById(R.id.tv_name);
            tvPhone = view.findViewById(R.id.tv_phone_number);
            imageView = view.findViewById(R.id.img_photo);
            layout = view.findViewById(R.id.box);
            btnCall = view.findViewById(R.id.btn_call);
            btnMsg = view.findViewById(R.id.btn_msg);
            btnWa = view.findViewById(R.id.btn_wa);
            btnEmail = view.findViewById(R.id.btn_mail);
        }
    }



}