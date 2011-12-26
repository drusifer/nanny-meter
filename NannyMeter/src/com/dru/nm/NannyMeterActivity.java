package com.dru.nm;

import android.app.Activity;
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
import android.widget.ToggleButton;

public class NannyMeterActivity extends Activity {
	private long startTime;

	private TextView timeLabel;

	private TextView startTimeLabel;

	private TextView youOweLabel;

	private EditText tipEditText;

	private EditText rateEditText;

	private double rate = 0.0;
	private double tip = 0.0;

	private ToggleButton startStop;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		startStop = (ToggleButton) findViewById(R.id.startstop);
		timeLabel = (TextView) findViewById(R.id.time);
		startTimeLabel = (TextView) findViewById(R.id.starttime);
		youOweLabel = (TextView) findViewById(R.id.youowe);
		rateEditText = (EditText) findViewById(R.id.rate);
		tipEditText = (EditText) findViewById(R.id.tip);


		rateEditText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				parseAndValidateFields();
				return false;
			}
		});

		tipEditText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				parseAndValidateFields();
				return false;
			}
		});


		startStop.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					startTimer();
				} else {
					stopTimer();
				}
			} });
	}

	private void parseAndValidateFields() {
		boolean rateValid = false, tipValid = false;

		final String rateString = rateEditText.getText().toString();
		try {
			rate = Double.parseDouble(rateString);
			rateEditText.setBackgroundColor(0x000000);
			rateValid = true;
		} catch (NumberFormatException e) {
			rateEditText.setBackgroundColor(0xffaaaa);

		}

		final String tipString = tipEditText.getText().toString();
		try {
			tip = Integer.parseInt(tipString);
			tipEditText.setBackgroundColor(0x000000);
			tipValid = true;
		} catch (NumberFormatException e) {
			tipEditText.setBackgroundColor(0xffaaaa);
		}

		startStop.setEnabled( rateValid && tipValid);
	}

	private Handler mHandler = new Handler();

	private Runnable mUpdateTimeTask = new Runnable() {

		public void run() {
			final long start = startTime;
			final long elapsed = SystemClock.uptimeMillis() - start;
			int seconds = (int) (elapsed / 1000);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes = minutes % 60;
			seconds  = seconds % 60;

			final double owed = (rate/(60*60)) * (elapsed/1000);
			final double tipCalc = owed * (tip/100.0);
			youOweLabel.setText(String.format("%0.2f", owed + tipCalc ));

			timeLabel.setText(String.format("%d:%02d:02d", hours, minutes, seconds));

			mHandler.postAtTime(this,
					start + (((minutes * 60) + seconds + 1) * 1000));
		}
	};

	protected void stopTimer() {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	protected void startTimer() {
		startTime = System.currentTimeMillis();	
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 100);
		Time t = new Time();
		t.set(startTime);
		startTimeLabel.setText(t.format("%I:%M:%S %p"));
	}
}