package com.verisence.egeza.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.verisence.egeza.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.profileImage)
    CircleImageView profileImage;
    @BindView(R.id.profileName)
    TextView profileName;
    @BindView(R.id.profilePhone)
    TextView profilePhone;
    @BindView(R.id.profileEmail)
    TextView profileEmail;
    @BindView(R.id.toolbarName)
    TextView toolbarName;
    @BindView(R.id.toolbarEmail)
    TextView toolbarEmail;

    FirebaseUser user;
    String name, email, phone;
    Uri photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user!=null) {
            name = user.getDisplayName();
            email = user.getEmail();
            phone = user.getPhoneNumber();
            photoUrl = user.getPhotoUrl();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        if (!Uri.EMPTY.equals(photoUrl)) {
            Picasso.get().load(photoUrl).into(profileImage);
        }

        if (!name.isEmpty()){
            toolBarLayout.setTitle(name);
            toolbarName.setText(name);
            profileName.setText(name);
        } else {
            toolBarLayout.setTitle(getTitle());
        }

        if (!phone.isEmpty()){
            profilePhone.setText(phone);
        } else {
            profilePhone.setText("Phone number not set");
        }

        if (!email.isEmpty()){
            toolbarEmail.setText(email);
            profileEmail.setText(email);
        } else {
            toolbarEmail.setText("Email not set");
            profileEmail.setText("Email not set");
        }

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorAccent));
        toggle.syncState();

        navigationView.setCheckedItem(R.id.nav_profile);

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_logout:
                logout();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void logout() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(ProfileActivity.this,AccountActivity.class));
                Toast.makeText(ProfileActivity.this, "Successfully Logged Out", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
}