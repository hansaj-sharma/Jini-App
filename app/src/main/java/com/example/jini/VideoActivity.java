package com.example.jini;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
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

public class VideoActivity extends Activity
    implements SessionListener, PublisherKit.PublisherListener

{
    private static final String TAG = "";
    private static String API_Key="46746182";
    //private static String SESSION_ID="1_MX40Njc0NjE4Mn5-MTU5Mzk0MDg2MjU4MX5GRWE5dmhCV2RzVE9aTk9lR3E4SFdUam9-fg";
   // private static String TOKEN= "T1==cGFydG5lcl9pZD00Njc0NjE4MiZzaWc9Mjg1NDc5NTllNjA2YjE0N2NjMzUzZTg3NGIwY2VmOWQ4N2FiNGJlZTpzZXNzaW9uX2lkPTFfTVg0ME5qYzBOakU0TW41LU1UVTVNemswTURnMk1qVTRNWDVHUldFNWRtaENWMlJ6VkU5YVRrOWxSM0U0U0ZkVWFtOS1mZyZjcmVhdGVfdGltZT0xNTkzOTQwOTQ2Jm5vbmNlPTAuNDA4MTU4OTAxNzcyOTY3NDMmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5NjUzMjk0NyZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private ImageView closeVideoBtn;
    private DatabaseReference usersRef,tokenRef, sessionRef;
    private String userId="",Token="",Session="";
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
        tokenRef= FirebaseDatabase.getInstance().getReference().child("Token");
        sessionRef= FirebaseDatabase.getInstance().getReference().child("Session");
        closeVideoBtn= findViewById(R.id.close_video_btn);

//this is for Firebase Token nd session update
        tokenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               final String Token = dataSnapshot.getValue(String.class);


                sessionRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Session = dataSnapshot.getValue().toString();
                        requestPermission(Token ,Session);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        closeVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               cancelCallButton();
            }
        });
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions,grantResults, VideoActivity.this);

    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission(String token, String session_key)
    {

        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)){

        mPublisherView=findViewById(R.id.publisher_container);
        mSubscriberView = findViewById(R.id.subscriber_container);

            //1 intil nd connecting to yhr session
            mSession= new Session.Builder(this, API_Key ,session_key).build();
            mSession.setSessionListener(VideoActivity.this);
            mSession.connect(token);


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
            session.unpublish(mPublisher);
            mPublisher.destroy();

            cancelCallButton();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        Log.i(LOG_TAG,"ERROR");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);

        builder.setMessage("Please end the call first").setPositiveButton("ok",null );

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStop(){
        super.onStop();
        finishAffinity();
        Toast.makeText(this, "videoActivity_khatam", Toast.LENGTH_SHORT).show();
    }


    private void cancelCallButton() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(userId).hasChild("Ringing") || dataSnapshot.child(userId).hasChild("Calling"))
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
                        mSession.unpublish(mPublisher);
                        mPublisher.destroy();
                        Intent i = new Intent(VideoActivity.this, RefreshActivity.class);
                        finish();
                        startActivity(i);
                    }
                   else  if (mSubscriber!=null)
                    {

                        mSession.unsubscribe(mSubscriber);
                        mSubscriber.destroy();
                        Intent i = new Intent(VideoActivity.this, RefreshActivity.class);
                        finish();
                        startActivity(i);
                    }
                    else {
                        Intent i = new Intent(VideoActivity.this, RefreshActivity.class);
                        finishAffinity();
                        startActivity(i);
                    }



                }
                else {
                    Intent i = new Intent(VideoActivity.this, RefreshActivity.class);
                    finish();
                    startActivity(i);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
