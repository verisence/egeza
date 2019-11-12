package com.verisence.zoackadventures.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.verisence.zoackadventures.Constants;
import com.verisence.zoackadventures.R;
import com.verisence.zoackadventures.adapters.FirebaseHotelViewHolder;
import com.verisence.zoackadventures.models.Hotel;
import com.verisence.zoackadventures.zoack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HotelsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private DrawerLayout drawer;
    private DatabaseReference hotelsReference;
    private FirebaseRecyclerAdapter<Hotel, FirebaseHotelViewHolder> firebaseAdapter;

    @BindView(R.id.hotelsRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.contactDrawer)
    TextView contactDrawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotels);
        ButterKnife.bind(this);

        hotelsReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_HOTELS);


        setUpFireBaseAdapter();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_main);
        }
    }

    private void setUpFireBaseAdapter() {
        Bundle bundle = getIntent().getExtras();
        String location = null;
        if (bundle != null) {
            location = bundle.getString("current location");
        }else{
            location = zoack.currentLoc;
        }
        Log.d("HOTELS ACTIVITY", "setUpFireBaseAdapter: "+location);
        Query query = hotelsReference.orderByChild("location").equalTo(location);
        FirebaseRecyclerOptions<Hotel> options = new FirebaseRecyclerOptions.Builder<Hotel>()
                .setQuery(query, Hotel.class)
                .build();
        firebaseAdapter = new FirebaseRecyclerAdapter<Hotel, FirebaseHotelViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FirebaseHotelViewHolder holder, int position, @NonNull Hotel hotel) {
                holder.bindHotel(hotel);
            }

            @NonNull
            @Override
            public FirebaseHotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_list_item, parent, false);
                return new FirebaseHotelViewHolder(view);
            }
        };
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(firebaseAdapter);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_main:
                startActivity(new Intent(HotelsActivity.this, MainActivity.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(HotelsActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_logout:
                logout();
                break;
//            case R.id.nav_contact:
//                startActivity(new Intent(HotelsActivity.this, ContactsActivity.class));
//                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HotelsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAdapter != null){
            firebaseAdapter.stopListening();
        }
    }

    @Override
    public void onClick(View v) {
        if (v==contactDrawer){
            startActivity(new Intent(HotelsActivity.this, MainActivity.class));
        }
    }
}

