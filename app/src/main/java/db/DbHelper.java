package db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

import model.Contact;

public class DbHelper extends SQLiteOpenHelper {
    public static String DB_NAME = "contact_app";
    private static final int DB_VERSION = 1;
    private static final String TBL_NAME = "contacts";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_BIRTH_DATE = "birth_date";
    private static final String KEY_SOCIAL_MEDIA = "social_media";
    private static final String KEY_USERNAME = "username";

    private static final String CREATE_TABLE_CONTACTS =
            "CREATE TABLE " + TBL_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_NAME + " TEXT NOT NULL,"
                    + KEY_IMAGE + " BLOB,"
                    + KEY_PHONE + " TEXT NOT NULL,"
                    + KEY_EMAIL + " TEXT,"
                    + KEY_STATUS + " TEXT,"
                    + KEY_ADDRESS + " TEXT,"
                    + KEY_BIRTH_DATE + " TEXT,"
                    + KEY_SOCIAL_MEDIA + " TEXT,"
                    + KEY_USERNAME + " TEXT);";

    private static final String SEEDING =
            "INSERT INTO " + TBL_NAME + " (" + KEY_NAME + "," +  KEY_IMAGE
                    + "," + KEY_PHONE + "," + KEY_EMAIL + "," + KEY_STATUS + "," + KEY_ADDRESS
                    + "," + KEY_BIRTH_DATE + "," + KEY_SOCIAL_MEDIA + ") "
                    + "VALUES('izzan',null,'6281382635503',null,null,null,null,null);";

    public DbHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(CREATE_TABLE_CONTACTS);
//        sqLiteDatabase.execSQL(SEEDING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS'" + TBL_NAME + "'");
        onCreate(sqLiteDatabase);
    }

    @SuppressLint("Range")
    public ArrayList<Contact> getAll()
    {
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        String q = "SELECT * FROM " + TBL_NAME + " ORDER BY name ASC";
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(q, null);

        if(cursor.moveToFirst())
        {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                contact.setPhone_number(cursor.getString(cursor.getColumnIndex(KEY_PHONE)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
                contact.setImage(cursor.getBlob(cursor.getColumnIndex(KEY_IMAGE)));
                contact.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
                contact.setAddress(cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)));
                contact.setBirth_date(cursor.getString(cursor.getColumnIndex(KEY_BIRTH_DATE)));
                contact.setSocial_media(cursor.getString(cursor.getColumnIndex(KEY_SOCIAL_MEDIA)));
                contact.setUsername(cursor.getString(cursor.getColumnIndex(KEY_USERNAME)));
                contactArrayList.add(contact);
            } while(cursor.moveToNext());
        }

        return contactArrayList;
    }

    @SuppressLint("Range")
    public ArrayList<Contact> search(String keyword)
    {
        ArrayList<Contact> contactArrayList = new ArrayList<>();

        try{
            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TBL_NAME + " where " + KEY_NAME + " like ?", new String[] { "%" + keyword + "%" });
            if(cursor.moveToFirst()){
                do{
                    Contact contact = new Contact();
                    contact.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                    contact.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    contact.setPhone_number(cursor.getString(cursor.getColumnIndex(KEY_PHONE)));
                    contactArrayList.add(contact);
                } while(cursor.moveToNext());
            }
        }catch (Exception e){
            contactArrayList = null;
        }

        return contactArrayList;
    }

    public long store(String name, String phone_number, String email, byte[] image,
          String status, String address, String birth_date, String social_media, String username)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, name);
        values.put(KEY_PHONE, phone_number);
        values.put(KEY_EMAIL, email);
        values.put(KEY_IMAGE, image);
        values.put(KEY_STATUS, status);
        values.put(KEY_ADDRESS, address);
        values.put(KEY_BIRTH_DATE, birth_date);
        values.put(KEY_SOCIAL_MEDIA, social_media);
        values.put(KEY_USERNAME, username);

        long insert = sqLiteDatabase.insert(TBL_NAME, null, values);
        return insert;
    }

    public int update(int id, String name, String phone_number, String email, byte[] image,
                      String status, String address, String birth_date, String social_media, String username)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, name);
        values.put(KEY_PHONE, phone_number);
        values.put(KEY_EMAIL, email);
        values.put(KEY_IMAGE, image);
        values.put(KEY_STATUS, status);
        values.put(KEY_ADDRESS, address);
        values.put(KEY_BIRTH_DATE, birth_date);
        values.put(KEY_SOCIAL_MEDIA, social_media);
        values.put(KEY_USERNAME, username);

        return sqLiteDatabase.update(TBL_NAME, values, KEY_ID + "= ?",
                new String[]{String.valueOf(id)});
    }

    public void delete(int id, Uri uri)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if(uri != null){
            String path = uri.getEncodedPath();
            File file = new File(path);
            if(file.exists()) {
                file.delete();
            }
        }
        sqLiteDatabase.delete(TBL_NAME, KEY_ID + "= ?", new String[]{String.valueOf(id)});
    }

}
