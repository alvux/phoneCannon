package com.inf8405.phonecannon.MainActivity;

public class Score {
    public boolean isForMe;
    public float acc;
    public long time;
    public boolean isFaceUp;

    public Score(boolean isForMe, float acc, long time, boolean isFaceUp) {
        this.isForMe = isForMe;
        this.acc = acc;
        this.time = time;
        this.isFaceUp = isFaceUp;
    }
}
