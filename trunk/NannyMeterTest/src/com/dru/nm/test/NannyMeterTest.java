package com.dru.nm.test;

import android.test.ActivityInstrumentationTestCase2;
import com.dru.nm.NannyMeterActivity;
import android.widget.TextView;


public class NannyMeterTest extends
ActivityInstrumentationTestCase2<NannyMeterActivity> {

	private NannyMeterActivity mActivity;
	private TextView mView;
	private String resourceString;

	public NannyMeterTest() {
		super("com.dru.nm", NannyMeterActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
		mView = (TextView) mActivity.findViewById(com.dru.nm.R.id.starttime);
		resourceString = mActivity.getString(com.dru.nm.R.string.nannymeter);
	}

    public void testPreconditions() {
        assertNotNull(mView);
      }
    
    public void testText() {
        assertEquals("",(String)mView.getText());
      }



}
