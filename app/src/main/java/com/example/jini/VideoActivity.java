package com.example.jini;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Session.SessionListener;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoActivity extends AppCompatActivity
    implements SessionListener, PublisherKit.PublisherListener

{
    private static String API_Key="46746182";
    private static String SESSION_ID= "2_MX40Njc0NjE4Mn5-MTU4OTg2OTU0Mzg3MH5yM2QxNVdaTGlqeFZyRVd3YTR5djZQY2h-fg";
    private static String TOKEN= "T1==cGFydG5lcl9pZD00Njc0NjE4MiZzaWc9MjUyYWQ1MTQ3ZWQ5OWMxYmU2YjFhODUwYjQyYzk3ZmVlNGRmZTBiYzpzZXNzaW9uX2lkPTJfTVg0ME5qYzBOakU0TW41LU1UVTRPVGcyT1RVME16ZzNNSDV5TTJReE5WZGFUR2xxZUZaeVJWZDNZVFI1ZGpaUVkyaC1mZyZjcmVhdGVfdGltZT0xNTg5ODY5NjM5Jm5vbmNlPTAuMDYxODMzMzI0OTcyNjMzMjc1JnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE1OTI0NjE2NDAmaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";
    private static final String LOG_TAG = VideoActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private ImageView closeVideoBtn;
    private DatabaseReference usersRef;
    private String userId="";
    private FrameLayout mPublisherView, mSubscriberView;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("users");
        closeVideoBtn= findViewById(R.id.close_video_btn);


        closeVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot) {
                        /* if (dataSnapshot.child(userId).hasChild("Ringing"))
                         {


                             usersRef.child(userId).child("Ringing").removeValue();

                             if (mPublisher!= null)
                             {
                                 mPublisher.destroy();
                             }
                             if (mSubscriber!=null)
                             {
                                 mSubscriber.destroy();
                             }
                             Toast.makeText(VideoActivity.this, "ringing ne nakh diya", Toast.LENGTH_SHORT).show();

                             Intent i = new Intent(VideoActivity.this, RegistrationActivity.class);
                             startActivity(i);
                             finish();
                             Toast.makeText(VideoActivity.this, "video activity finish", Toast.LENGTH_SHORT).show();
                         }

                         if (dataSnapshot.child(userId).hasChild("Calling"))
                         {
                             usersRef.child(userId).child("Calling").removeValue();
                             if (mPublisher!= null)
                             {
                                 mPublisher.destroy();
                             }
                             if (mSubscriber!=null)
                             {
                                 mSubscriber.destroy();
                             }
                             Toast.makeText(VideoActivity.this, "calling ne nakh diya", Toast.LENGTH_SHORT).show();
                             Intent i = new Intent(VideoActivity.this, RegistrationActivity.class);
                             startActivity(i);
                             finish();
                         } */

                         if (dataSnapshot.child(userId).hasChild("Ringing") ||dataSnapshot.child(userId).hasChild("Calling"))
                         {

                             usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(DataSnapshot dataSnapshot) {
                                     if (dataSnapshot.hasChild("Ringing"))
                                         usersRef.child(userId).child("Ringing").removeValue();
                                     else if (dataSnapshot.hasChild("Calling"))
                                         usersRef.child(userId).child("Calling").removeValue();
                                 }

                                 @Override
                                 public void onCancelled(DatabaseError databaseError) {

                                 }
                             });


                             if (mPublisher!= null)
                             {
                                 mPublisher.destroy();
                             }
                             if (mSubscriber!=null)
                             {

                                 mSubscriber.destroy();
                             }

                             Intent i = new Intent(VideoActivity.this, RegistrationActivity.class);
                             startActivity(i);
                             finish();
                             Toast.makeText(VideoActivity.this, "Video Chat Ended", Toast.LENGTH_SHORT).show();
                         }

                         /*else
                         {
                             if (mPublisher!= null)
                             {
                                 mPublisher.destroy();
                             }
                             if (mSubscriber!=null)
                             {
                                 mSubscriber.destroy();
                             }
                             Toast.makeText(VideoActivity.this, "Samne wala rakh diya h", Toast.LENGTH_SHORT).show();
                             Intent i = new Intent(VideoActivity.this, RegistrationActivity.class);
                             startActivity(i);
                             finish();

                         }*/
                     }

                     @Override
                     public void onCancelled(DatabaseError databaseError) {

                     }
                 });
            }
        });

        requestPermission();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions,grantResults, VideoActivity.this);

    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission()
    {

        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)){

        mPublisherView=findViewById(R.id.publisher_container);
        mSubscriberView = findViewById(R.id.subscriber_container);

            //1 intil nd connecting to yhr session
            mSession= new Session.Builder(this, API_Key ,SESSION_ID).build();
            mSession.setSessionListener(VideoActivity.this);
            mSession.connect(TOKEN);


        }
        else
        {
            EasyPermissions.requestPermissions(this,"Please Allow All Permissions, To make video Chat",
                    RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream)
    {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream)
    {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError)
    {

    }
//2. PublishingStream To the Session
    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG,"Session Connected");
        mPublisher= new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoActivity.this);

        mPublisherView.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");

    }
//3. subscribing to the Stream which has been publised
    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        Log.i(LOG_TAG,"Stream Received");
        if (mSubscriber == null)
        {
            mSubscriber = new  Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberView.addView(mSubscriber.getView());

        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG,"User Disconnected");
        if (mSubscriber!= null)
        {
            Toast.makeText(VideoActivity.this, "User Disconnected", Toast.LENGTH_SHORT).show();

            mSubscriber = null;
            mSubscriberView.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        Log.i(LOG_TAG,"ERROR");

    }
}
