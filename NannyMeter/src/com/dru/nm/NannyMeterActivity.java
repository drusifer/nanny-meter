package com.dru.nm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
	
	private boolean mIsRunning = false;

	private long mStartTime;

	private TextView mTimeLabel;

	private TextView mStartTimeLabel;

	private TextView mYouOweLabel;

	private EditText mTipEditText;

	private EditText mRateEditText;

	private double mRate = 0.0;
	private double mTip = 0.0;

	private ToggleButton mStartStop;


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
		boolean rateValid = false, tipValid = false;

		final String rateString = mRateEditText.getText().toString();
		try {
			mRate = Double.parseDouble(rateString);
			mRateEditText.setBackgroundColor(0x000000);
			rateValid = true;
		} catch (NumberFormatException e) {
			mRateEditText.setBackgroundColor(0xffaaaa);

		}

		final String tipString = mTipEditText.getText().toString();
		try {
			mTip = Integer.parseInt(tipString);
			mTipEditText.setBackgroundColor(0x000000);
			tipValid = true;
		} catch (NumberFormatException e) {
			mTipEditText.setBackgroundColor(0xffaaaa);
		}

		mStartStop.setEnabled( rateValid && tipValid);
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
			mYouOweLabel.setText(String.format("%0.2f", owed + tipCalc ));

			mTimeLabel.setText(String.format("%d:%02d:02d", hours, minutes, seconds));

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
		Time t = new Time();
		t.set(mStartTime);
		mStartTimeLabel.setText(t.format("%I:%M:%S %p"));
		mIsRunning = true;
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
					"Failed to write state!", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Sets the initial state of the spinner when the application is first run.
	 */
	public void setInitialState() {

	}

	public boolean readInstanceState(Context c) {


		SharedPreferences p = c.getSharedPreferences(PREFERENCES_FILE, MODE_WORLD_READABLE);
		mStartTime = p.getLong(STARTTIME_KEY, 0);
		mIsRunning = p.getBoolean(ISRUNNING_KEY, false);
		return (p.contains(STARTTIME_KEY));

	}

	public boolean writeInstanceState(Context c) {

		SharedPreferences p =
				c.getSharedPreferences(NannyMeterActivity.PREFERENCES_FILE, MODE_WORLD_READABLE);
		SharedPreferences.Editor e = p.edit();
		e.putLong(STARTTIME_KEY, mStartTime);
		e.putBoolean(ISRUNNING_KEY, mIsRunning);
		return (e.commit());

	}

}