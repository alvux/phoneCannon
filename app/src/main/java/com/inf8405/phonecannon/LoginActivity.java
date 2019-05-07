package com.inf8405.phonecannon;

import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    public static String username;
    private  String password;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.db = FirebaseFirestore.getInstance();

        //Get initial battery
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus =  this.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale * 100;
        DeviceInitialBatteryStatus.getInstance().setInitialBatteryPct(batteryPct);
    }

    public void signIn(View button){
        EditText usernameText = findViewById(R.id.usernameText);
        EditText passwordText = findViewById(R.id.passwordText);

        this.username = usernameText.getText().toString();
        this.password = passwordText.getText().toString();


        this.db.collection("users").whereEqualTo("username", this.username).whereEqualTo("password", this.password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            int documentSize = task.getResult().getDocuments().size();

                            if (documentSize > 0) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                Intent myIntent = new Intent(LoginActivity.this, ConnectionActivity.class);
                                startActivity(myIntent);
                            } else {
                                Toast.makeText(LoginActivity.this, "Username/password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("result", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void signUp(View button){
        EditText usernameText = findViewById(R.id.usernameText);
        EditText passwordText = findViewById(R.id.passwordText);

        this.username = usernameText.getText().toString();
        this.password = passwordText.getText().toString();

        if(TextUtils.isEmpty(this.username) || TextUtils.isEmpty(this.password)){
            Toast.makeText(LoginActivity.this, "Username/password fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        this.db.collection("users").whereEqualTo("username", this.username).whereEqualTo("password", this.password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            int documentSize = task.getResult().getDocuments().size();

                            if (documentSize > 0) {
                                Toast.makeText(LoginActivity.this, "Cannnot create account: This username already exist", Toast.LENGTH_SHORT).show();
                            } else {
                                LoginActivity.this.saveUser();
                            }
                        } else {
                            Log.w("result", "Error getting documents.", task.getException());
                        }
                    }
                });
    }


    private void saveUser(){
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", this.username);
        newUser.put("password", this.password);

        db.collection("users").document()
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent myIntent = new Intent(LoginActivity.this, ConnectionActivity.class);
                        startActivity(myIntent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error", "Error writing document", e);
                    }
                });
    }

}
