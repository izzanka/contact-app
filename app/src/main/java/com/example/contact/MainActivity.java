package com.example.contact;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import adapter.ContactAdapter;
import db.DbHelper;
import model.Contact;

public class MainActivity extends AppCompatActivity{
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private ArrayList<Contact> contactArrayList;
    private DbHelper dbHelper;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.rview);
        contactAdapter = new ContactAdapter(this);
        dbHelper = new DbHelper(this);
        contactArrayList = dbHelper.getAll();
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        SearchView searchView = findViewById(R.id.searchView);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);

        contactAdapter.setContactArrayList(contactArrayList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contactAdapter);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
//                searchContact(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchContact(s);
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                startActivity(intent);
            }
        });
    }


    private void searchContact(String keyword)
    {
        ArrayList<Contact> contacts = dbHelper.search(keyword);
        if(contacts.size() > 0)
        {
            contactAdapter.setContactArrayList(contacts);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(contactAdapter);

        }else{
            Toast.makeText(getApplicationContext(), "Contact with name " + keyword + " not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        contactArrayList = dbHelper.getAll();
        contactAdapter.setContactArrayList(contactArrayList);
        contactAdapter.notifyDataSetChanged();
    }

}