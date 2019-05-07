package com.inf8405.phonecannon.Main;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.inf8405.phonecannon.Connection.CommunicationListener;
import com.inf8405.phonecannon.Login.LoginActivity;
import com.inf8405.phonecannon.Connection.WiFiDirectBroadcastReceiver;
import com.inf8405.phonecannon.Utils.MessageType;

public class ExchangeService implements CommunicationListener {

    public MainActivity mActivity;
    private Gson gson = new Gson();

    public ExchangeService (MainActivity activity){
        mActivity = activity;
        WiFiDirectBroadcastReceiver.getInstance().communicationService.register(this);
        WiFiDirectBroadcastReceiver.getInstance().communicationService.sendMessage(MessageType.READY_TO_PLAY,"");
    }

    @Override
    public void onConnection() {}

    @Override
    public void onNewMessage(int type, String message) {
        // Accept messages and forward to method
        if (type == MessageType.READY_TO_PLAY) {
            handleReady();
        } else if (type == MessageType.MY_TURN_IS_FINISHED) {
            receiveTurn(message);
        } else if (type == MessageType.MY_NAME_IS) {
            mActivity.saveGameData(message);
        } else if (type == MessageType.WHERE_ARE_YOU) {
            localizeMeForOpponent();
        } else if (type == MessageType.HERE_IS_MY_LOCATION) {
            opponentIsLocalized(message);
        }
    }

    public void sendFinishTrun(Score scoreOfThrow){
        String scoreAsString = gson.toJson(scoreOfThrow);
        WiFiDirectBroadcastReceiver.getInstance().communicationService.sendMessage(MessageType.MY_TURN_IS_FINISHED,scoreAsString);
    }

    public void handshakeForEndGame() {
        WiFiDirectBroadcastReceiver.getInstance().communicationService.sendMessage(MessageType.MY_NAME_IS, LoginActivity.username);
    }

    private void handleReady(){
        // if it is my turn play else let opponent know it is his turn and he can procedde
        if (mActivity.isMyTurn){
            mActivity.beginTurn();
        } else {
            WiFiDirectBroadcastReceiver.getInstance().communicationService.sendMessage(MessageType.READY_TO_PLAY,"");
        }
    }

    private void opponentIsLocalized(String message){
        // deserialize location and let activily know
        Log.d("TEST", "opponent localized:" + message);
        LatLng pos = gson.fromJson(message, LatLng.class);
        mActivity.displayOpponentLocationOnMap(pos);
    }

    private void localizeMeForOpponent(){
        LatLng position = mActivity.giveLocationAndSignal();
        if (position != null) {
            String posAsString = gson.toJson(position);
            WiFiDirectBroadcastReceiver.getInstance().communicationService.sendMessage(MessageType.HERE_IS_MY_LOCATION, posAsString);
            Log.d("TEST", "sent to opponent:" + posAsString);
        }
    }

    public void askOpponentToLocalizeHimself(){
        WiFiDirectBroadcastReceiver.getInstance().communicationService.sendMessage(MessageType.WHERE_ARE_YOU, "");
    }

    private void receiveTurn(String message){
        // deserialize score of turn and give data to main acctivity
        Score score = gson.fromJson(message, Score.class);
        mActivity.opponentTurnResult(score);
    }

}
