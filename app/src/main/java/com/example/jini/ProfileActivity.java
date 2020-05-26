package com.example.jini;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID="",receiverUserImage="",receiverUserName="";
    private ImageView background_profile_view;
    private TextView name_profile;
    private Button add_friend, decline_friend_request;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentStatus= "new";
    private DatabaseReference contactsRef, friendRequestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID=  getIntent().getExtras().get("visit_user_name").toString();
        receiverUserName= getIntent().getExtras().get("profile_name").toString();
        receiverUserImage= getIntent().getExtras().get("profile_image").toString();

        mAuth= FirebaseAuth.getInstance();
        senderUserId= mAuth.getCurrentUser().getUid();

        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

        background_profile_view= findViewById(R.id.background_profile_view);
        name_profile= findViewById(R.id.name_profile);
        add_friend= findViewById(R.id.add_friend);
        decline_friend_request= findViewById(R.id.decline_friend_request);

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profile.setText(receiverUserName);

        manageClickEvent();

    }

    private void manageClickEvent() {

        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild( receiverUserID))
                        {
                             String requestType = dataSnapshot.child(receiverUserID)
                                     .child("request_type").getValue().toString();
                             if (requestType.equals("sent"))
                             {
                                 currentStatus= "request_sent";
                                 add_friend .setText("Cancel Request");

                             }
                             else if (requestType.equals("received"))
                             {
                                 currentStatus= "request_received";
                                 add_friend.setText("Accept Request");
                                 decline_friend_request.setVisibility(View.VISIBLE);

                                 decline_friend_request.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         CancelFriendRequest();
                                     }
                                 });

                             }
                             else
                             {
                                 contactsRef.child(senderUserId)
                                         .addListenerForSingleValueEvent(new ValueEventListener() {
                                             @Override
                                             public void onDataChange(DataSnapshot dataSnapshot)
                                             {

                                                 if (dataSnapshot.hasChild(receiverUserID))
                                                 {
                                                     currentStatus="friends";
                                                     add_friend.setText("delete_contact");
                                                 }
                                                 else
                                                 {
                                                     currentStatus="new";
                                                 }
                                             }

                                             @Override
                                             public void onCancelled(DatabaseError databaseError) {

                                             }
                                         });
                             }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        if (senderUserId.equals(receiverUserID))
            add_friend.setVisibility(View.GONE);
        else
        {
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (currentStatus.equals("new"))
                    {
                        SendFriendRequest();
                    }
                    if(currentStatus.equals("request_sent"))
                    {
                        CancelFriendRequest();

                    }
                    if(currentStatus.equals("request_received"))
                    {
                        AcceptFriendRequest();

                    }
                    if(currentStatus.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }


                }
            });
        }

     }

    private void AcceptFriendRequest()
    {
        contactsRef.child(senderUserId).child(receiverUserID).child("contact").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(receiverUserID).child(senderUserId).child("contact").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendRequestRef.child(senderUserId).child(receiverUserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(receiverUserID).child(senderUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        add_friend.setText("Delete Contact");
                                                                                        currentStatus= "friends";
                                                                                        decline_friend_request.setVisibility(View.GONE);
                                                                                        Toast.makeText(ProfileActivity.this, "Friend Added", Toast.LENGTH_SHORT).show();

                                                                                    }

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelFriendRequest()
    {
        friendRequestRef.child(senderUserId).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendRequestRef.child(receiverUserID).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                add_friend.setText("Add Friend");
                                                currentStatus= "new";
                                                Toast.makeText(ProfileActivity.this, "Friend Request Cancelled", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });
                        }

                    }
                });
    }


    private void SendFriendRequest(){
        friendRequestRef.child(senderUserId).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            friendRequestRef.child(receiverUserID).child(senderUserId).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                currentStatus= "request_sent";
                                                add_friend.setText("Cancel Request");
                                                Toast.makeText(ProfileActivity.this, "Friend Request Send Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }




                    }
                });


     }
}
