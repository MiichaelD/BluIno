package com.webs.itmexicali.bluino.ads;

import android.app.Activity;
import android.view.View;

public abstract class Advertising {
	
	//ADS Constants	
	public final static boolean SHOW_ADS = true;
	
	
    /** AdMob Advertising. Your ad unit id. Replace with your actual ad unit id. */
    public static final String
    	ADVIEW_AD_UNIT_ID 		= "ca-app-pub-4741238402050454/9514283406",
    	
    	//TODO REPLACE - this iD used for interstitial is from package name: com.itmexicali.webs.bluino
    	INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4741238402050454/3079300200";
    
    public static final String APIKEY = "1409523218201390512";
    public static final int APPID = 237342;

    public final static int ADS_ADMOB = 0, ADS_AIRPUSH_STANDARD = 1;
	
	public final static int AD_SERVICE = ADS_AIRPUSH_STANDARD;
	
	
	
	
	protected Activity pAct = null;
	
	/** Create a new Ads service object and initialize it*/
	public Advertising(Activity ctx){
		pAct = ctx;
		initAds();
	}
	/** Init ads if they are enabled, depending on the ads service using load
	 * the corresponding one AirPush, AdMob, etc*/
	public abstract void initAds();
	
	/** Get the banner view to add it to the layout*/
	public abstract View getBanner();
	
	/** Destroy the banner after detaching it from the layout to create and load a new one*/
	public abstract void destroyBanner();
	
	/** Beging the loading of your Interstitial ad and save it to cache*/
	public abstract void loadInterstitial();
	
	/** Show the Interstitial saved on cache*/
	public abstract void showInterstitial();
	
	public void onResume(){}
	public void onPause(){}
}
