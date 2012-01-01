package com.dru.nm.test;

import java.util.concurrent.CyclicBarrier;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dru.nm.NannyMeterActivity;


public class NannyMeterTest extends
ActivityInstrumentationTestCase2<NannyMeterActivity> {

	private NannyMeterActivity mActivity;
	private TextView mStartTimeView;
	private EditText mRateView;
	private EditText mTipView;
	private ToggleButton mStartStopView;

	public NannyMeterTest() {
		super("com.dru.nm", NannyMeterActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);
		mActivity = getActivity();
		myRunOnUiThread( new Runnable() {
			public void run() {
				mActivity.resetSavedPreferences();
			} } ); 
		mStartTimeView = (TextView) mActivity.findViewById(com.dru.nm.R.id.starttime);
		mRateView = (EditText) mActivity.findViewById(com.dru.nm.R.id.rate);
		mTipView   = (EditText) mActivity.findViewById(com.dru.nm.R.id.tip);
		mStartStopView = (ToggleButton) mActivity.findViewById(com.dru.nm.R.id.startstop); 
	}

	public void testPreconditions() {
		assertNotNull(mStartTimeView);
	}

	public void testText() {
		assertEquals("",(String)mStartTimeView.getText());
	}

	private void myRunOnUiThread( final Runnable r) {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		mActivity.runOnUiThread(
				new Runnable() {
					public void run() {
						r.run();
						try { barrier.await(); } catch (Exception e) { e.printStackTrace(); }
					} 
				});
		try { barrier.await(); } catch (Exception e) { e.printStackTrace(); }
	}

	public void testFieldUI() {
		myRunOnUiThread(
				new Runnable() {
					public void run() {
						mRateView.requestFocus();
						mRateView.selectAll();
					} 
				} 
		); 
		sendKeys(KeyEvent.KEYCODE_DEL); // delete everything
		assertTrue("rate is deleted", mRateView.getText().length() == 0);
		sendKeys(KeyEvent.KEYCODE_ENTER);

		assertTrue("a rate error was detected", mActivity.mRateValid == false );
    assertNotNull("Error Text is set on Rate", mRateView.getError());
		assertTrue("startStop is disabled", mStartStopView.isEnabled() == false);

		myRunOnUiThread(
				new Runnable() {
					public void run() {
						mRateView.requestFocus();
						mRateView.setSelection(0);
					} 
				} 
		); 
		sendKeys("1 2 PERIOD 0 0"); // delete everything and set it to 12.00
		assertTrue("rate is set to 12.00", mRateView.getText().toString().equals("12.00"));

		myRunOnUiThread( new Runnable() {
			public void run() {
				mTipView.requestFocus();
				mTipView.selectAll();
			}
		} );
		// tip getting focus should trigger the validation logic
		assertTrue("rate is okay", mActivity.mRateValid == true );
    assertNull("Error text is unset on Rate", mRateView.getError());
		sendKeys(KeyEvent.KEYCODE_DEL);
		assertTrue("Tip is deleted", mTipView.getText().length() == 0);
		myRunOnUiThread( new Runnable() {
			public void run() {
				mRateView.requestFocus();
			}
		} );

		assertTrue("tip is invalid", mActivity.mTipValid == false );
    assertNotNull("Error Text is set on Tip", mTipView.getError());
		assertTrue("startStop is disabled", mStartStopView.isEnabled() == false);

		myRunOnUiThread(
				new Runnable() {
					public void run() {
						mTipView.requestFocus();
						mTipView.setSelection(0);
					} 
				} 
		); 

		sendKeys("8");
		sendKeys(KeyEvent.KEYCODE_ENTER);

		assertTrue("tip is okay", mActivity.mTipValid == true );
    assertNull("Error text is unset on Tip", mTipView.getError());
		assertTrue("startStop is enabled", mStartStopView.isEnabled() == true);
	}
    
	public void testCalc() {
	 
	 long start = System.currentTimeMillis();
   long end = start + 1*60*60*1000 + 1*60*1000 + 1*1000;  
	 String elapsed = mActivity.getElapsedTimeString(start, end);
	 assertEquals("elapsed is 1:01:01", elapsed, "1:01:01");
	}
}
