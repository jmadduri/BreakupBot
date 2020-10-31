package com.example.textmessageapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Context context = this;
    static final int code = 2003;
    SmsManager smsManager = SmsManager.getDefault();
    Handler handler = new Handler();
    boolean aBoolean = false;
    BroadcastReceiver broadcastReceiver;
    Runnable runnable;
    String message;
    String otherMessage;
    String otherPhoneNumber;
    int count = 0;
    TextView textView;
    Boolean bol = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.id_textview);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, code);
        else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, code);

        runnable = new Runnable() {
            @Override
            public void run() {
                if (count <= 3) {
                    StateMachine stateMachine = new StateMachine(count, otherMessage, bol);

                    if (stateMachine.getState() == 0) {
                        textView.setText("Greeting state");
                    } else if (stateMachine.getState() == 1) {
                        textView.setText("Hinting at Breakup");
                    } else if (stateMachine.getState() == 2) {
                        textView.setText("Starting Breakup");
                    } else if (stateMachine.getState() == 3) {
                        textView.setText("Executing Breakup");
                    }
                    message = stateMachine.executeState();
                    count = stateMachine.getState();
                    bol = stateMachine.getB();

                    smsManager.sendTextMessage(otherPhoneNumber, null, message, null, null);
                }
                else
                {
                    textView.setText("Post-Breakup");
                }
            }
        };

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                SmsMessage[] smsMessages;
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (bundle != null) {
                    smsMessages = new SmsMessage[pdus.length];

                    for (int i = 0; i < pdus.length; i++) {
                        smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                        Log.d("TAG", "onReceive: " + "Num: " + smsMessages[i].getOriginatingAddress() + ", Msg:" + smsMessages[i].getMessageBody());
                        Toast toast = Toast.makeText(context, "Num: " + smsMessages[i].getOriginatingAddress() + ", Msg:" + smsMessages[i].getMessageBody(), Toast.LENGTH_LONG);
                        otherMessage = smsMessages[i].getMessageBody();
                        otherPhoneNumber = smsMessages[i].getOriginatingAddress();
                        toast.show();
                        aBoolean = true;
                    }
                    int rand = (int) (Math.random() * 5000) + 2000;
                    if (aBoolean) {
                        handler.postDelayed(runnable, rand);
                        aBoolean = false;
                    }
                }
            }
        };
    }
        @Override
        protected void onResume() {
            super.onResume();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(broadcastReceiver, filter);
        }

        @Override
        protected void onPause() {
            super.onPause();
            unregisterReceiver(broadcastReceiver);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(requestCode == 123){
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("TAG", "PERMISSION GRANTED");
                }
            }
        }

    public class StateMachine{

        private int state;
        private String userMsg;
        private String message;
        private boolean b;

        public StateMachine(int count, String userMessage, Boolean bo){
            state = count;
            userMsg = userMessage;
            message = "";
            b = bo;
        }
        public String executeState(){
            if(state == 0){
                if(b) {
                    if (userMsg.contains("hi") || userMsg.contains("Hi") || userMsg.contains("hello") || userMsg.contains("hey") || userMsg.contains("wassup") || userMsg.contains("Wassup") || userMsg.contains("up") || userMsg.contains("Up")) {
                        message = randGreeting() + ", how are you today?";
                        state++;
                    }
                    else{
                        message = "That's weird why are you saying " + userMsg + "?";
                        b = false;
                    }
                }
                else if(!b){
                    if(userMsg.contains("mistake") || userMsg.contains("sorry") || userMsg.contains("yea") || userMsg.contains("Yea") || userMsg.contains("is") || userMsg.contains("Is")  || userMsg.contains("yup") || userMsg.contains("Yup")) {
                        message = "Oh ok";
                        b = true;
                        state++;
                    }
                    else {
                        message = "I am assuming that autocorrect is screwing up on you...";
                        b = false;
                    }
                }
            }
            else if(state == 1){
                if(b) {
                    if (userMsg.contains("Ok") || userMsg.contains("ok") || ((userMsg.contains("not") || userMsg.contains("Not")) && (userMsg.contains("Bad") || userMsg.contains("bad"))) || ((userMsg.contains("not") || userMsg.contains("Not")) && (userMsg.contains("Good") || userMsg.contains("good")))) {
                        message = "Oh. I do not mean to make anything worse but I have been waiting to tell you something actually...";
                        state++;
                    }
                    else if(((userMsg.contains("good") || userMsg.contains("Good")) && (!userMsg.contains("not") || !userMsg.contains("Not"))) || userMsg.contains("great") || userMsg.contains("Great")  || userMsg.contains("yea") || userMsg.contains("Yea")) {
                        message = "That's cool, I have been waiting to tell you something actually...";
                        state++;
                    }
                    else {
                        message = "I am assuming that autocorrect is screwing up on you...";
                        b = false;
                    }
                }
                else if(!b){
                    if (userMsg.contains("mistake") || userMsg.contains("sorry") || userMsg.contains("yea") || userMsg.contains("Yea") || userMsg.contains("is") || userMsg.contains("Is")  || userMsg.contains("yup") || userMsg.contains("Yup")){
                        message = "Oh. I do not mean to make anything worse but I have been waiting to tell you something actually...";
                        b = true;
                        state++;
                    }
                    else {
                        message = "I am assuming that autocorrect is screwing up on you again...?";
                        b = false;
                    }
                }
            }
            else if(state == 2){
                if(b) {
                    if (userMsg.contains("Ok") || userMsg.contains("ok") || userMsg.contains("yea") || userMsg.contains("Yea")) {
                        message = "I have been thinking we should go our separate ways.....";
                        state++;
                    }
                    else if (userMsg.contains("what") || userMsg.contains("What") || userMsg.contains("?") || userMsg.contains("why") || userMsg.contains("Why")){
                        message = "I understand you are confused...but, I have been thinking we should go our separate ways.....";
                        state++;
                    }
                    else {
                        message = "I am assuming that autocorrect is screwing up on you...";
                        b = false;
                    }
                }
                else if(!b){
                    if (userMsg.contains("mistake") || userMsg.contains("sorry") || userMsg.contains("yea") || userMsg.contains("Yea") || userMsg.contains("is") || userMsg.contains("Is")  || userMsg.contains("yup") || userMsg.contains("Yup")){
                        message = "Ok...I have been thinking we should go our separate ways.....";
                        b = true;
                        state++;
                    }
                    else {
                        message = "I am assuming that autocorrect is screwing up on you again...?";
                        b = false;
                    }
                }
            }
            else if(state == 3){
                if(b) {
                    if (userMsg.contains("why") || userMsg.contains("huh") || userMsg.contains("what") || userMsg.contains("Why") || userMsg.contains("Huh") || userMsg.contains("What") || userMsg.contains("?")) {
                        message = "I understand you are confused, but I want to break up with you. Goodbye forever";
                        state++;
                    }
                    else if(userMsg.contains("ok") || userMsg.contains("Ok") || userMsg.contains("great") || userMsg.contains("Great")  || userMsg.contains("yea") || userMsg.contains("Yea")) {
                        message = "That's cool, let us break up then. Goodbye forever";
                        state++;
                    }
                    else {
                        message = "I am assuming that autocorrect is screwing up on you...";
                        b = false;
                    }
                }
                else if(!b){
                    if (userMsg.contains("mistake") || userMsg.contains("sorry") || userMsg.contains("yea") || userMsg.contains("Yea") || userMsg.contains("is") || userMsg.contains("Is")  || userMsg.contains("yup") || userMsg.contains("Yup")){
                        message = "Oh. I think you are confused, but I want to break up with you. Goodbye forever";
                        b = true;
                        state++;
                    }
                    else {
                        message = "I am assuming that autocorrect is screwing up on you again...?";
                        b = false;
                    }
                }
            }

            return message;
        }
        public int getState(){
            return state;
        }
        public boolean getB()
        {
            return b;
        }
        public String randGreeting()
        {
            String[] greetings = {"Hi","Hey","Hola","Good day"};
            int rand = (int)(Math.random()*4)+1;
            return greetings[rand];
        }
    }
}
