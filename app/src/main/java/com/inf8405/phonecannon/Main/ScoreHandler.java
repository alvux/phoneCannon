package com.inf8405.phonecannon.Main;

import android.app.Activity;
import android.widget.TextView;

import com.inf8405.phonecannon.R;

import java.util.ArrayList;

public class ScoreHandler {

    public final int GAME_LENGHT = 6;
    public ArrayList<Score> throwsList = new ArrayList<>();
    private ArrayList<ArrayList<TextView>> cells = new ArrayList<>();
    private int friendlyColor;
    private int fopponentColor;
    private String faceUp;
    private String faceDown;
    MainActivity mainActivity;
    private TextView myTopScoreTextView;
    private TextView opponentTopScoreTextView;
    private float myTop = 0f;
    private float opponentTop = 0f;


    public ScoreHandler(MainActivity activity){
        mainActivity = activity;
        initCells(activity);

        // get resources
        friendlyColor = activity.getResources().getColor(R.color.colorYou);
        fopponentColor = activity.getResources().getColor(R.color.colorOpponent);
        faceUp = activity.getResources().getString(R.string.orientation_face_up);
        faceDown = activity.getResources().getString(R.string.orientation_face_down);
        myTopScoreTextView = activity.findViewById(R.id.text_my_score);
        opponentTopScoreTextView = activity.findViewById(R.id.text_opponent_score);
    }

    public void inputThrow(long beginTime, long endTime, float acc, boolean isFacingUp, boolean isForMe){
        Score newScore = new Score(isForMe, acc, endTime - beginTime, isFacingUp);
        throwsList.add(newScore);
        updateCells();
    }

    public void inputCompleteThrow(Score score){
        score.isForMe = false;
        throwsList.add(score);
        updateCells();
    }

    private void updateCells(){
        mainActivity.runOnUiThread(() -> {
            myTop = 0f;
            opponentTop = 0f;
            for (int i = 0; i < GAME_LENGHT && i < throwsList.size(); ++i){
                // Number
                int color = throwsList.get(i).isForMe? friendlyColor : fopponentColor;
                cells.get(i).get(0).setTextColor(color);
                // Acceleration
                cells.get(i).get(1).setText(String.valueOf(throwsList.get(i).acc));
                // Time
                cells.get(i).get(2).setText(String.valueOf(throwsList.get(i).time));
                //Orientation
                String orientation = throwsList.get(i).isFaceUp? faceUp : faceDown;
                cells.get(i).get(3).setText(orientation);

                // Top scores
                if (throwsList.get(i).isForMe && throwsList.get(i).acc > myTop){
                    myTop = throwsList.get(i).acc;
                } else if (!throwsList.get(i).isForMe && throwsList.get(i).acc > opponentTop) {
                    opponentTop = throwsList.get(i).acc;
                }
                myTopScoreTextView.setText(String.valueOf(myTop));
                opponentTopScoreTextView.setText(String.valueOf(opponentTop));
            }
        });
    }

    public String winOrLoss() {
        if (myTop > opponentTop)
            return "WIN";
        return "LOSS";
    }

    private void initCells(Activity activity){
        // Get interface elments
        cells.add(new ArrayList<>());
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell11));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell12));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell13));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell14));
        cells.add(new ArrayList<>());
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell21));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell22));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell23));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell24));
        cells.add(new ArrayList<>());
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell31));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell32));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell33));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell34));
        cells.add(new ArrayList<>());
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell41));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell42));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell43));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell44));
        cells.add(new ArrayList<>());
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell51));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell52));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell53));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell54));
        cells.add(new ArrayList<>());
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell61));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell62));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell63));
        cells.get(cells.size()-1).add(activity.findViewById(R.id.cell64));
    }
}
