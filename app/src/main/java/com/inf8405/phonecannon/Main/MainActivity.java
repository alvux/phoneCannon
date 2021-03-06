package com.inf8405.phonecannon.Main;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.inf8405.phonecannon.DeviceInfo.DeviceInfo;
import com.inf8405.phonecannon.History.HistoryInfo;
import com.inf8405.phonecannon.History.HistorySQLRepository;
import com.inf8405.phonecannon.Login.LoginActivity;
import com.inf8405.phonecannon.Utils.PermissionManager;
import com.inf8405.phonecannon.R;
import com.inf8405.phonecannon.Connection.WiFiDirectBroadcastReceiver;

import java.util.Date;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    private LocationManager locationManager;
    private Marker marker = null;
    private SensorHandler sensorHandler;
    MediaPlayer mediaPlayer;
    ScoreHandler scoreHandler;
    ExchangeService exchangeService;

    private Button throwBtn;

    private Button cancelThrowBtn;
    private Button findOpponent;

    private TextView turnTextView;
    public boolean isMyTurn = false;
    private boolean isGameOver = false;

    private HistoryInfo historyInfo;

    private LatLng landingPosition = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create view
        setContentView(R.layout.activity_main);
        // Initialize device info
        new DeviceInfo(this);
        // Chek permissions again (for map mostly)
        PermissionManager.checkAllPermissions(this);
        // Prepare sensors
        sensorHandler = new SensorHandler(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        this.historyInfo = new HistoryInfo(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorHandler.registerListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorHandler.unregisterListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.historyInfo.historySQLRepository.dbHelper.close();
        WiFiDirectBroadcastReceiver.getInstance().disconnect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Set Camera to mtl by default
        LatLng mtl = new LatLng(45, -73);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mtl));
        if (PermissionManager.checkAllPermissions(this)){
            mMap.setMyLocationEnabled(true);
        } else {
            toast("Missing Location Permissions!");
        }
        setUp();
    }

    private void setUp(){
        // Setup handlers and helpers
        locationManager = (LocationManager)getSystemService(this.LOCATION_SERVICE);
        scoreHandler = new ScoreHandler(this);

        // Subscribe buttons
        subscribeButtons();

        // Decide first turn order and start game
        decideInitialTurn();
    }

    public void toast(String msg) {
        Toast.makeText(this, msg,
                Toast.LENGTH_LONG).show();
    }

    //region Throw related    ======================================================================

    public void startThrow() {
        toast("FIRE!");
        cancelThrowBtn.setEnabled(true);
        mediaPlayer = MediaPlayer.create(this, R.raw.fietfieuwwistle);
        mediaPlayer.start();
    }

    public void makeLanding() {
        throwBtn.setEnabled(true);
        toast("ON TARGET!");
        mediaPlayer = MediaPlayer.create(this, R.raw.explosion);
        mediaPlayer.start();
        try {
            landingPosition = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
        } catch (Exception e) {};
    }

    public void finishShot(long beginTime, long endTime, float acc, boolean isFacingUp) {
        scoreHandler.inputThrow(beginTime, endTime, acc, isFacingUp, true);
        endMyTurn();
    }

    //endregion

    //region Turn related    ======================================================================

    private void decideInitialTurn(){
        turnTextView = findViewById(R.id.text_turn);
        isMyTurn = WiFiDirectBroadcastReceiver.getInstance().isOwner;
        beginTurn();
        // Start exchange service and let know opponent you are ready
        exchangeService = new ExchangeService(this);
    }

    public void beginTurn() {
        runOnUiThread(() -> {
            if (isMyTurn) {
                // it is our turn, prepare interface
                turnTextView.setText(getResources().getString(R.string.your_turn));
                turnTextView.setTextColor(getResources().getColor(R.color.colorYou));
                throwBtn.setEnabled(true);
                findOpponent.setEnabled(true);
                cancelThrowBtn.setEnabled(false);
            } else {
                // it is opponents turn, diable interface
                turnTextView.setText(getResources().getString(R.string.opponent_turn));
                turnTextView.setTextColor(getResources().getColor(R.color.colorOpponent));
                throwBtn.setEnabled(false);
                findOpponent.setEnabled(false);
                cancelThrowBtn.setEnabled(false);
            }
            // Disable localization for first throw
            if (scoreHandler.throwsList.size() == 0)
                findOpponent.setEnabled(false);
        });
    }

    public void endMyTurn() {
        // signal end of turn and adjust interface accordingly
        isMyTurn = false;
        exchangeService.sendFinishTrun(scoreHandler.throwsList.get(scoreHandler.throwsList.size()-1));
        if (!checkForGameEnd())
            beginTurn();
    }

    public void opponentTurnResult(Score score) {
        // safe opponent result and begin our turn
        isMyTurn = true;
        scoreHandler.inputCompleteThrow(score);
        if (!checkForGameEnd())
            beginTurn();
    }

    //endregion

    // region End Game related =====================================================================

    private boolean checkForGameEnd() {
        if (scoreHandler.throwsList.size() == scoreHandler.GAME_LENGHT){
            // Confirm game is over to oponent and tell you nema
            exchangeService.handshakeForEndGame();
            // Change Throw button action
            isGameOver = true;
            runOnUiThread(() -> {
                cancelThrowBtn.setEnabled(false);
                throwBtn.setEnabled(true);
                findOpponent.setEnabled(true);
                throwBtn.setText(R.string.close_game_btn);
                throwBtn.setBackgroundColor(Color.GREEN);
            });
            return true;
        }
        return false;
    }

    public void saveGameData(String opponentUsername) {
        HistorySQLRepository historySQLRepository = new HistorySQLRepository(this);
        historySQLRepository.addHistory(new Date(), opponentUsername, scoreHandler.winOrLoss());
        historySQLRepository.dbHelper.close();
    }

    private void restartGame(){
        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    // endregion

    //region Button Listeners ======================================================================

    private void subscribeButtons() {
        throwBtn = findViewById(R.id.button_throw);
        cancelThrowBtn = findViewById(R.id.button_cancel_throw);
        findOpponent = findViewById(R.id.button_find_opponent);
        subscribeThrows();
        subscribeLocalization();
    }

    private void subscribeThrows(){
        throwBtn.setOnClickListener((View v) -> {
            if (isGameOver){
                // close game
                restartGame();
            } else {
                // normal function
                sensorHandler.listenForThrow();
                throwBtn.setEnabled(false);
            }
        });

        cancelThrowBtn.setOnClickListener((View v) -> {
            sensorHandler.cancelThrow();
            throwBtn.setEnabled(true);
        });
    }

    private void subscribeLocalization(){
        findOpponent.setOnClickListener((View v) -> {
            exchangeService.askOpponentToLocalizeHimself();
        });
    }

    //endregion

    // region Localization ========================================================================

    @SuppressLint("MissingPermission")
    public LatLng giveLocationAndSignal() {
        // make sound
        mediaPlayer = MediaPlayer.create(this, R.raw.comeoverhere);
        mediaPlayer.start();
        // get my position
        return landingPosition;
    }

    public void displayOpponentLocationOnMap(LatLng pos) {
        runOnUiThread(() -> {
            if (marker != null) marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(pos).title(getResources().getString(R.string.opponent_map_tag)));
        });
    }

    // endregion
}
