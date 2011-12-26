package com.dru.nm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.Time;
import android.view.KeyEvent;
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

	private static final String RATE_KEY = null;

	private static final String TIP_KEY = null;

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

	private TextView mRateLabel;

	private TextView mTipLabel;
    
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
        mRateLabel = (TextView) findViewById(R.id.rateLabel);
        mTipLabel = (TextView) findViewById(R.id.tipLabel);

		mRateEditText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				parseAndValidateFields();
				return false;
			}
		});

		mTipEditText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				parseAndValidateFields();
				return false;
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
			mRateLabel.setBackgroundColor(Color.BLACK);
			mRateValid = true;
		} catch (NumberFormatException e) {
			mRateLabel.setBackgroundColor(ERROR_COLOR);
		}

		final String tipString = mTipEditText.getText().toString();
		try {
			mTip = Integer.parseInt(tipString);
			mTipLabel.setBackgroundColor(Color.BLACK);
			mTipValid = true;
		} catch (NumberFormatException e) {
			mTipLabel.setBackgroundColor(ERROR_COLOR);
		}

		mStartStop.setEnabled(mRateValid && mTipValid);
	}

	private Handler mHandler = new Handler();

	private Runnable mUpdateTimeTask = new Runnable() {

		public void run() {
			final long start = mStartTime;
			final long elapsed = SystemClock.uptimeMillis() - start;
			int seconds = (int) (elapsed / 1000);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes = minutes % 60;
			seconds  = seconds % 60;

			final double owed = (mRate/(60*60)) * (elapsed/1000);
			final double tipCalc = owed * (mTip/100.0);
			mYouOweLabel.setText(String.format("%05.2f", owed + tipCalc ));

			mTimeLabel.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));

			mHandler.postAtTime(this,
					start + (((minutes * 60) + seconds + 1) * 1000));
		}
	};

	protected void stopTimer() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		mIsRunning = false;
	}

	protected void startTimer() {
		mStartTime = System.currentTimeMillis();	
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 100);
		mStartTimeLabel.setText(getTimeString( mStartTime ));
		mIsRunning = true;
	}
	
	private String getTimeString( long timems ) {
		Time t = new Time();
		t.set(mStartTime);
		return(t.format("%I:%M:%S %p"));
	}

	@Override
	public void onResume() {
		super.onResume();
        
		if (!readInstanceState(this)) setInitialState();
		
		if (mIsRunning ) {
			
		}
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
		mRateEditText.setText(String.format("%0.2f", mRate));
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