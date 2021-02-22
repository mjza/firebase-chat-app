package com.mjzsoft.chatapp;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;


public class Chat extends AppCompatActivity {
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2;
    ProgressDialog pd;

    public static Date dateFromUTC(Date date){
        return new Date(date.getTime() + Calendar.getInstance().getTimeZone().getOffset(new Date().getTime()));
    }

    public static Date dateToUTC(Date date){
        return new Date(date.getTime() - Calendar.getInstance().getTimeZone().getOffset(date.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = findViewById(R.id.layout1);
        layout_2 = findViewById(R.id.layout2);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);

        pd = new ProgressDialog(Chat.this);
        pd.setMessage(getResources().getString(R.string.loading));
        pd.show();

        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://mahdi-chat-app-default-rtdb.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://mahdi-chat-app-default-rtdb.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.username);
                    map.put("date", "" + Chat.dateToUTC(new Date()).getTime());
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    Map map = dataSnapshot.getValue(Map.class);

                    String message = map.get("message").toString();
                    String userName = map.get("user").toString();
                    String timestamp = map.get("date").toString();
                    Long time = Long.parseLong(timestamp);
                    Date date = new Date();
                    date.setTime(time.longValue());
                    SimpleDateFormat formatter=new SimpleDateFormat("dd-MMM-yyyy HH:mm");

                    if (userName.equals(UserDetails.username)) {
                        addMessageBox(message, formatter.format(dateFromUTC(date)), 1);
                    } else {
                        addMessageBox(message, formatter.format(dateFromUTC(date)), 2);
                    }
                } catch(Exception e){
                    System.err.println(e.getStackTrace());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addMessageBox(String message, String date, int type){
        TextView textView = new TextView(Chat.this);
        textView.setMinHeight(25);
        textView.setTextSize(20);
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMargins(10,10,10,10);
        lp2.weight = 7.0f;
        if(type == 1) {
            lp2.gravity = Gravity.RIGHT;
            textView.setText("You @ " +  date + ":\n" + message );
            textView.setBackgroundResource(R.drawable.chat_sender_bubble);
        }
        else{
            lp2.gravity = Gravity.LEFT;
            textView.setText(UserDetails.partner + " @ " + date +  ":\n" + message );
            textView.setBackgroundResource(R.drawable.chat_receiver_bubble);
        }

        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
        pd.dismiss();
    }
}