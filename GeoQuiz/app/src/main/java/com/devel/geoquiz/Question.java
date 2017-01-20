package com.devel.geoquiz;


public class Question {

    private int mTextResID;  // текст вопроса
    private boolean mAnswerTrue;  //правильный ответ

    public Question(int mTextResID, boolean mAnswerTrue) {
        this.mTextResID = mTextResID;
        this.mAnswerTrue = mAnswerTrue;
    }

    public int getTextResID() {
        return mTextResID;
    }

    public void setTextResID(int textResID) {
        mTextResID = textResID;
    }

    public boolean isAnswerTrue() {
        return mAnswerTrue;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswerTrue = answerTrue;
    }
}
