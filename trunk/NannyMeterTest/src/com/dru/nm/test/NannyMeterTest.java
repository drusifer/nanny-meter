package com.dru.nm.test;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.dru.nm.NannyMeterActivity;


public class NannyMeterTest extends
ActivityInstrumentationTestCase2<NannyMeterActivity> {

	private NannyMeterActivity mActivity;
	private TextView mStartTimeView;
	private String resourceString;
	private EditText mRateView;
	private EditText mTipView;
	private TextView mRateLabel;
	private TextView mTipLabel;

	public NannyMeterTest() {
		super("com.dru.nm", NannyMeterActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
		mActivity.runOnUiThread( new Runnable() {
			public void run() {
				mActivity.resetSavedPreferences();
			} } ); 
		mStartTimeView = (TextView) mActivity.findViewById(com.dru.nm.R.id.starttime);
		mRateView = (EditText) mActivity.findViewById(com.dru.nm.R.id.rate);
		mRateLabel = (TextView) mActivity.findViewById(com.dru.nm.R.id.rateLabel);
		mTipView   = (EditText) mActivity.findViewById(com.dru.nm.R.id.tip);
		mTipLabel   = (TextView) mActivity.findViewById(com.dru.nm.R.id.tipLabel);
		resourceString = mActivity.getString(com.dru.nm.R.string.nannymeter);
	}

	public void testPreconditions() {
		assertNotNull(mStartTimeView);
	}

	public void testText() {
		assertEquals("",(String)mStartTimeView.getText());
	}

	public void testFieldUI() {
		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						mRateView.requestFocus();
						mRateView.setSelection(0,10);
						sendKeys(KeyEvent.KEYCODE_DEL); // delete everything
						sendKeys(KeyEvent.KEYCODE_ENTER);
					} 
				} 
				); 
		assertTrue("a rate error was detected", mActivity.mRateValid == false );
		assertTrue("startStop is disabled", mStartTimeView.isEnabled() == false);

		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						mRateView.requestFocus();
						mRateView.setSelection(0);
						sendKeys("12.00"); // delete everything

						mTipView.requestFocus();
						mTipView.setSelection(0,10);
						sendKeys(KeyEvent.KEYCODE_DEL);
						mRateView.requestFocus();
					} 
				} 
				); 
		assertTrue("rate is okay", mActivity.mRateValid == true );
		assertTrue("tip is invalid", mActivity.mTipValid == false );
		assertTrue("startStop is disabled", mStartTimeView.isEnabled() == false);

		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						mTipView.requestFocus();
						mTipView.setSelection(0);
						sendKeys("8");
						sendKeys(KeyEvent.KEYCODE_BACK);
					} 
				} 
				); 

		assertTrue("tip is okay", mActivity.mTipValid == true );
		assertTrue("startStop is enabled", mStartTimeView.isEnabled() == true);
	}


}
