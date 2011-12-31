package com.dru.nm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NannyMeterActivity extends Activity {
  private static final String PREFERENCES_FILE = "NannyMeterActivity";

  private static final String STARTTIME_KEY = "STARTTIME";

  private static final String ISRUNNING_KEY = "ISRUNNING";

  private static final String RATE_KEY = "RATE";

  private static final String TIP_KEY = "TIP";

  public static final int ERROR_COLOR = 0xffaaaa;

  private boolean mIsRunning = false;

  private long mStartTime;

  private TextView mTimeLabel;

  private TextView mStartTimeLabel;

  private TextView mYouOweLabel;

  private EditText mTipEditText;

  private EditText mRateEditText;

  private float mRate = 0.0f;
  private int mTip = 0;

  private ToggleButton mStartStop;

  public boolean mRateValid = false;
  public boolean mTipValid = false;


  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mStartStop = (ToggleButton) findViewById(R.id.startstop);
    mTimeLabel = (TextView) findViewById(R.id.time);
    mStartTimeLabel = (TextView) findViewById(R.id.starttime);
    mYouOweLabel = (TextView) findViewById(R.id.youowe);
    mRateEditText = (EditText) findViewById(R.id.rate);
    mTipEditText = (EditText) findViewById(R.id.tip);

    mRateEditText.setOnEditorActionListener(new OnEditorActionListener() {
      public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        parseAndValidateFields();
        return false;
      }
    });

    mRateEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
      public void onFocusChange(View v, boolean hasFocus) {
        parseAndValidateFields();
      }
    });

    mTipEditText.setOnEditorActionListener(new OnEditorActionListener() {
      public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        parseAndValidateFields();
        return false;
      }
    });

    mTipEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
      public void onFocusChange(View v, boolean hasFocus) {
        parseAndValidateFields();
      }
    });


    mStartStop.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      public void onCheckedChanged(CompoundButton buttonView,
          boolean isChecked) {
        if (isChecked) {
          startTimer();
        } else {
          stopTimer();
        }
      } });
  }

  private void parseAndValidateFields() {
    mRateValid = false; 
    mTipValid = false;

    final String rateString = mRateEditText.getText().toString();
    try {
      mRate = Float.parseFloat(rateString);
      mRateValid = true;
      mRateEditText.setError(null);
    } catch (NumberFormatException e) {
      mRateEditText.setError("You must specify a rate");
    }

    final String tipString = mTipEditText.getText().toString();
    try {
      mTip = Integer.parseInt(tipString);
      mTipValid = true;
      mTipEditText.setError(null);
    } catch (NumberFormatException e) {
      mTipEditText.setError("You must specify a tip (or 0 to exclude a tip)");
    }

    mStartStop.setEnabled(mRateValid && mTipValid);
  }

  private Handler mHandler = new Handler();

  public String getElapsedTimeString(long start, long end) {
    final long elapsed = end - start;
    int seconds = (int) (elapsed / 1000);
    int minutes = seconds / 60;
    int hours = minutes / 60;
    minutes = minutes % 60;
    seconds  = seconds % 60;
    return String.format("%d:%02d:%02d", hours, minutes, seconds);
  }

  public String getOwedString(long start, long end) {
    final long elapsed = end - start;
    final double owed = (mRate/(60*60)) * (elapsed/1000);
    final double tipCalc = owed * (mTip/100.0);
    return String.format("%5.2f", owed + tipCalc );

  }

  private Runnable mUpdateTimeTask = new Runnable() {

    public void run() {
      final long start = mStartTime;
      final long end = System.currentTimeMillis();

      mYouOweLabel.setText(getOwedString(start, end));
      mTimeLabel.setText(getElapsedTimeString(start,end));
      mHandler.postDelayed(this, 200);
    }
  };

  private boolean inOnResume = false;

  protected void stopTimer() {
    mHandler.removeCallbacks(mUpdateTimeTask);
    mIsRunning = false;
  }

  protected void startTimer() {
    if(!inOnResume) {
      mStartTime = System.currentTimeMillis();	
      mHandler.removeCallbacks(mUpdateTimeTask);
      mHandler.postDelayed(mUpdateTimeTask, 100);
      mStartTimeLabel.setText(getTimeString( mStartTime ));
      mIsRunning = true;
    }
  }

  private String getTimeString( long timems ) {
    Time t = new Time();
    t.set(mStartTime);
    return(t.format("%I:%M:%S %p"));
  }

  @Override
  public void onResume() {
    super.onResume();

    inOnResume = true;
    if (!readInstanceState(this)) setInitialState();
    setWidgetState();
    if (mIsRunning ) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.post(mUpdateTimeTask);
    }
    inOnResume = false;
  }

  @Override
  public void onPause() {
    super.onPause();

    if (!writeInstanceState(this)) {
      Toast.makeText(this,
          "NannyMeter Failed to write state!", Toast.LENGTH_LONG).show();
    }
  }

  public void setInitialState() {
    mStartTimeLabel.setText("");
    mRateEditText.setText("15.00");
    mTipEditText.setText("10");
    mStartStop.setEnabled(true);
    mStartStop.setChecked(false);
    mTimeLabel.setText("");
    mYouOweLabel.setText("");
  }

  public void setWidgetState() {
    mStartTimeLabel.setText(getTimeString(mStartTime));
    mRateEditText.setText(String.format("%5.2f", mRate));
    mTipEditText.setText(String.format("%d", mTip));
    mStartStop.setEnabled(true);
    mStartStop.setChecked(mIsRunning);
    parseAndValidateFields();
  }

  public void resetSavedPreferences()
  {
    setInitialState();
    writeInstanceState(this);
  }

  public boolean readInstanceState(Context c) {
    try {
      SharedPreferences p = c.getSharedPreferences(PREFERENCES_FILE, MODE_WORLD_READABLE);
      mStartTime = p.getLong(STARTTIME_KEY, 0);
      mIsRunning = p.getBoolean(ISRUNNING_KEY, false);
      mRate = p.getFloat(RATE_KEY, 15.00f);
      mTip = p.getInt(TIP_KEY, 10);
      return (p.contains(STARTTIME_KEY));
    } catch (Exception e) {
      e.printStackTrace();
      Toast.makeText(this, "NannyMeter Failed to read state! " + e.getMessage(),
          Toast.LENGTH_LONG).show();
      return(false);
    }
  }

  public boolean writeInstanceState(Context c) {
    SharedPreferences p =
      c.getSharedPreferences(NannyMeterActivity.PREFERENCES_FILE, MODE_WORLD_READABLE);
    SharedPreferences.Editor e = p.edit();
    e.putLong(STARTTIME_KEY, mStartTime);
    e.putBoolean(ISRUNNING_KEY, mIsRunning);
    e.putFloat(RATE_KEY, mRate);
    e.putInt(TIP_KEY, mTip);
    return (e.commit());

  }

}