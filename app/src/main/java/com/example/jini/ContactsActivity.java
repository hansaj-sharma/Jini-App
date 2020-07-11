package com.example.jini;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity {
    BottomNavigationView navView;
    RecyclerView myContactsList;
    ImageView findPeoples;
    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId, username="",profileImage="";

    private String callBy="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_contacts);
        navView =findViewById(R.id.nav_view);



        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        myContactsList= findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        findPeoples= findViewById(R.id.find_peoples);

        findPeoples.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findPeopleIntent = new Intent(ContactsActivity.this , FindPeopleActivity.class);
                startActivity(findPeopleIntent);
            }
        });

    }



    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.navigation_home:
                    Intent mainIntent = new Intent(ContactsActivity.this , ContactsActivity.class);
                    startActivity(mainIntent);
                    break;

                case R.id.navigation_notifications:
                    Intent notificationsIntent = new Intent(ContactsActivity.this , NotificationsActivity.class);
                    startActivity(notificationsIntent);
                    break;

                case R.id.navigation_settings:
                    Intent settingsIntent = new Intent(ContactsActivity.this , SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;

                case R.id.navigation_logout:

                    AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);

                    builder.setMessage("Are you sure to logout").setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Intent logoutIntent = new Intent(ContactsActivity.this , RegistrationActivity.class);
                            startActivity(logoutIntent);
                            finish();
                            Toast.makeText(ContactsActivity.this, "logged out", Toast.LENGTH_SHORT).show();
                        }
                    })
                            .setNegativeButton("Cancel",null);

                    AlertDialog alert = builder.create();
                    alert.show();
                    break;

            }
            return true;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        checkForReceivingCall();

        validateUser();

        FirebaseRecyclerOptions<Contacts> options
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserId), Contacts.class).build();

        final FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull final Contacts contacts) {

                final String listUserId = getRef(i).getKey();
                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                      if (dataSnapshot.exists())
                      {
                          profileImage= dataSnapshot.child("image").getValue().toString();

                          username= dataSnapshot.child("name").getValue().toString();
                          holder.userNameTxt.setText(username);
                          Picasso.get().load(profileImage).into(holder.profileImageView);
                      }

                      holder.callBtn.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {

                                  Toast.makeText(ContactsActivity.this, "Calling", Toast.LENGTH_SHORT).show();
                                  Intent callingIntent = new Intent(ContactsActivity.this, CallingActivity.class);
                                  callingIntent.putExtra("visit_user_id", listUserId);
                                  startActivity(callingIntent);
                                  finish();

                          }
                      });

                      holder.deleteBtn.setOnClickListener(new View.OnClickListener() {

                          @Override
                          public void onClick(View v)
                          {


                              AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(ContactsActivity.this);

                              deleteBuilder.setTitle("Delete Contact");
                              deleteBuilder.setMessage("After Deleting, you cant make calls ").setPositiveButton("Delete", new DialogInterface.OnClickListener()
                              {
                                  @Override
                                  public void onClick(DialogInterface dialog, int which) {
                                      contactsRef.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                          @Override
                                          public void onComplete(@NonNull Task<Void> task) {
                                              if (task.isSuccessful())
                                              {
                                                  contactsRef.child(listUserId).child(currentUserId).removeValue();
                                                  Toast.makeText(ContactsActivity.this, "Contact Deleted", Toast.LENGTH_SHORT).show();
                                              }
                                          }
                                      });

                                  }

                              }).setNegativeButton("Cancel",null);

                              AlertDialog alert = deleteBuilder.create();
                              alert.show();
                          }
                      });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
                ContactsViewHolder  viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void checkForReceivingCall()
    {
        userRef.child(currentUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("ringing")) {
                            callBy = dataSnapshot.child("ringing").getValue().toString();

                            Intent callingIntent = new Intent(ContactsActivity.this, CallingActivity.class);
                            callingIntent.putExtra("visit_user_id", callBy);
                            startActivity(callingIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void validateUser()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {
                    Intent settingIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(settingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTxt;
        Button callBtn, deleteBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt= itemView.findViewById(R.id.username_contact);
            callBtn = itemView.findViewById(R.id.video_call_btn);
            profileImageView= itemView.findViewById(R.id.image_contact);
            cardView= itemView.findViewById(R.id.card_view1);
            deleteBtn= itemView.findViewById(R.id.delete_btn);
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
