package com.devel.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_CHEATER = "cheater";
    private static final int REQUEST_CODE_CHEAT  = 0;

    private Button mTrueBtn;
    private Button mFalseBtn;
    private Button mNextBtn;
    private Button mPrevBtn;
    private Button mCheatBtn;
    private TextView mQuestionTextView;

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_ocean, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true)
    };

    private int mCurrentIndex = 0;
    private boolean mIsCheater;

    private void updateQuestion(){
        //Log.d(TAG, "Updating question text for question #" + mCurrentIndex, new Exception());
        int question = mQuestionBank[mCurrentIndex].getTextResID();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedTrue){

        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResID = 0;
    // защита от читеров, можно обойти, если повернуть экран или иначе перезапустить activity
        if(mIsCheater) messageResID = R.string.judgment_toast;
        else {
            if (userPressedTrue == answerIsTrue) {
                messageResID = R.string.correct_toast;
            } else {
                messageResID = R.string.incorrect_toast;
            }
        }
        Toast.makeText(this, messageResID, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "method onCteate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        mQuestionTextView = (TextView)findViewById(R.id.question_text_view);

        //#1
        mQuestionTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex+1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });

        //#2
        mPrevBtn = (Button) findViewById(R.id.prev_btn);
        mPrevBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCurrentIndex--;
                if (mCurrentIndex == -1)mCurrentIndex = mQuestionBank.length-1;
                updateQuestion();
            }
        });


        mTrueBtn = (Button) findViewById(R.id.true_btn);
        mTrueBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //просто this в toast означает View.OnClickListener
              //  Toast.makeText(QuizActivity.this, R.string.incorrect_toast, Toast.LENGTH_SHORT).show();
                checkAnswer(true);
            }
        });

        mFalseBtn = (Button) findViewById(R.id.false_btn);
        mFalseBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        mNextBtn = (Button) findViewById(R.id.next_btn);
        mNextBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex+1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mCheatBtn = (Button) findViewById(R.id.cheat_btn);
        mCheatBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean ansIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent i = CheatActivity.newIntent(QuizActivity.this, ansIsTrue);
                startActivityForResult(i, REQUEST_CODE_CHEAT);
            }
        });

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mIsCheater = savedInstanceState.getBoolean(KEY_CHEATER, false);
        }

        updateQuestion();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_CODE_CHEAT){
            if (data == null) return;
            mIsCheater = CheatActivity.wasAnswerShown(data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
        outState.putInt(KEY_INDEX, mCurrentIndex);
        outState.putBoolean(KEY_CHEATER, mIsCheater);  // запрет обхода защиты от читерства
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "method onStart() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "method onPause() called");
    }

    // onResume() is absent

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "method onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "method onDestroy() called");
    }
}
