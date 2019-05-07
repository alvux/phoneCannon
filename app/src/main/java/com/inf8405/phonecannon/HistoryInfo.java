package com.inf8405.phonecannon;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class HistoryInfo {

    private Activity mainActivity;
    public HistorySQLRepository historySQLRepository;
    private ImageButton historyBtn;
    private Dialog historyDialog;
    private static  final int MAX_ENTRIES = 16;

   public HistoryInfo(Activity activity){
        this.mainActivity = activity;
        this.historySQLRepository = new HistorySQLRepository(this.mainActivity);
        this.historyDialog = new Dialog(activity);
        this.historyBtn = mainActivity.findViewById(R.id.button_history);


        this.historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryInfo.this.historyDialog.setContentView(R.layout.history);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(   HistoryInfo.this.historyDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;


                HistoryInfo.this.historyDialog.show();
                HistoryInfo.this.historyDialog.getWindow().setAttributes(lp);


                ImageView closeHistoryImg =  HistoryInfo.this.historyDialog.findViewById(R.id.closeHistoryImg);
                closeHistoryImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HistoryInfo.this.historyDialog.dismiss();
                    }
                });

               HistoryInfo.this.displayHistory();

            }
        });
    }

    private void displayHistory(){
        TableLayout tableLayout =   HistoryInfo.this.historyDialog.findViewById(R.id.historyTableLayout);
        TableRow tableRowHeaders = HistoryInfo.this.historyDialog.findViewById(R.id.historyTableHeaders);


        //Display history

        ArrayList<History> histories=  HistoryInfo.this.historySQLRepository.getHistory();

        int entries = 0;

        for(History history: histories){
            if(entries <= 16) {
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                params.weight = 1;
                TextView dateView = new TextView(HistoryInfo.this.historyDialog.getContext());
                TextView opponentView = new TextView(HistoryInfo.this.historyDialog.getContext());
                TextView resultView = new TextView(HistoryInfo.this.historyDialog.getContext());

                dateView.setGravity(Gravity.CENTER);
                opponentView.setGravity(Gravity.CENTER);
                resultView.setGravity(Gravity.CENTER);

                dateView.setLayoutParams(params);
                opponentView.setLayoutParams(params);
                resultView.setLayoutParams(params);

                //Set value in views
                Format formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = formatter.format(history.gameDate);

                dateView.setText(dateString);
                dateView.setTextSize(14);
                opponentView.setText(history.opponent);
                opponentView.setTextSize(14);
                resultView.setText(history.result);
                resultView.setTextSize(14);

                TableRow newRow = new TableRow(HistoryInfo.this.historyDialog.getContext());

                TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                tableParams.setMargins(0, 24, 0, 0);
                newRow.setLayoutParams(tableParams);

                newRow.addView(dateView);
                newRow.addView(opponentView);
                newRow.addView(resultView);

                //add Row
                tableLayout.addView(newRow);

                entries++;
            }
            else{
                break;
            }
        }
    }

}
