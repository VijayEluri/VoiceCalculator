/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wolink.app.voicecalc;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Config;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class Calculator extends Activity {
    EventListener mListener = new EventListener();
    private CalculatorDisplay mDisplay;
    private Persist mPersist;
    private History mHistory;
    private Logic mLogic;
    private PanelSwitcher mPanelSwitcher;

//    private static final int CMD_CLEAR_HISTORY  = 1;
//    private static final int CMD_BASIC_PANEL    = 2;
//    private static final int CMD_ADVANCED_PANEL = 3;
    private static final int CMD_SETTINGS		  = 4;
    private static final int CMD_ABOUT			  = 5;
    private static final int CMD_CAPITAL		  = 6;

    private static final int HVGA_WIDTH_PIXELS  = 320;

    static final int BASIC_PANEL    = 0;
    static final int ADVANCED_PANEL = 1;

    private static final String LOG_TAG = "Calculator";
    private static final boolean LOG_ENABLED = Config.LOGD;
    private static final String STATE_CURRENT_VIEW = "state-current-view";

    private SoundManager sm;
    private String mVoicePkg;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    	SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);

    	mVoicePkg = "";
        
        sm = SoundManager.getInstance();
        sm.initSounds(this);
              
        setContentView(R.layout.main);

        mPersist = new Persist(this);
        mHistory = mPersist.history;

        mDisplay = (CalculatorDisplay) findViewById(R.id.display);

        boolean history_on = prefs.getBoolean("history_on", false);
        if (!history_on) {
        	mHistory.clear();
        }
        mLogic = new Logic(this, mHistory, mDisplay, (Button) findViewById(R.id.equal));
        HistoryAdapter historyAdapter = new HistoryAdapter(this, mHistory, mLogic);
        mHistory.setObserver(historyAdapter);

        mPanelSwitcher = (PanelSwitcher) findViewById(R.id.panelswitch);
        mPanelSwitcher.setCurrentIndex(state==null ? 0 : state.getInt(STATE_CURRENT_VIEW, 0));

        mListener.setHandler(mLogic, mPanelSwitcher);

        //mDisplay.setOnKeyListener(mListener);

//        View view;
//        if ((view = findViewById(R.id.del)) != null) {
//            view.setOnClickListener(mListener);
//            view.setOnLongClickListener(mListener);
//        }
        /*
        if ((view = findViewById(R.id.clear)) != null) {
            view.setOnClickListener(mListener);
        }
        */        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item;
        
//        item = menu.add(0, CMD_CLEAR_HISTORY, 0, R.string.clear_history);
//        item.setIcon(R.drawable.clear_history);
//        
//        item = menu.add(0, CMD_ADVANCED_PANEL, 0, R.string.advanced);
//        item.setIcon(R.drawable.advanced);
//        
//        item = menu.add(0, CMD_BASIC_PANEL, 0, R.string.basic);
//        item.setIcon(R.drawable.simple);

      item = menu.add(0, CMD_CAPITAL, 0, R.string.convert_capital);
      item.setIcon(R.drawable.currency_black_yuan);

      item = menu.add(0, CMD_SETTINGS, 0, R.string.setting);
      item.setIcon(R.drawable.setting);
      item.setIntent(new Intent(this, Settings.class));
      
      item = menu.add(0, CMD_ABOUT, 0, R.string.about);
      item.setIcon(R.drawable.about);
      item.setIntent(new Intent(this, About.class));

      return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
//        menu.findItem(CMD_BASIC_PANEL).setVisible(mPanelSwitcher != null && 
//                          mPanelSwitcher.getCurrentIndex() == ADVANCED_PANEL);
//        
//        menu.findItem(CMD_ADVANCED_PANEL).setVisible(mPanelSwitcher != null && 
//                          mPanelSwitcher.getCurrentIndex() == BASIC_PANEL);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//        case CMD_CLEAR_HISTORY:
//            mHistory.clear();
//            break;
//
//        case CMD_BASIC_PANEL:
//            if (mPanelSwitcher != null && 
//                mPanelSwitcher.getCurrentIndex() == ADVANCED_PANEL) {
//                mPanelSwitcher.moveRight();
//            }
//            break;
//
//        case CMD_ADVANCED_PANEL:
//            if (mPanelSwitcher != null && 
//                mPanelSwitcher.getCurrentIndex() == BASIC_PANEL) {
//                mPanelSwitcher.moveLeft();
//            }
//            break;
        case CMD_SETTINGS:
        	break;
        case CMD_ABOUT:
        	break;
        case CMD_CAPITAL:
        	String chineseDigit = "";
        	try {
        		chineseDigit = ChineseDigit.toChineseDigit(mDisplay.getText().toString());
        	} catch (Exception e) {
        	}
        	
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.RMB_CHINESE);
    		if (chineseDigit.equals("")) {
    			builder.setIcon(android.R.drawable.ic_dialog_alert);
    			builder.setMessage(R.string.error_number);
    		} else {
        		builder.setMessage(chineseDigit);	
    		}

    		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dlg, int sumthin) {
    				dlg.dismiss();
    			}
    		});
    		
    		builder.setCancelable(true);
    		builder.show();
        	break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(STATE_CURRENT_VIEW, mPanelSwitcher.getCurrentIndex());
    }

    @Override
    public void onPause() {
        super.onPause();
        mLogic.updateHistory();
        mPersist.save();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    	SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	boolean bVoiceOn = prefs.getBoolean("voice_on", true);
    	boolean bHapticOn = prefs.getBoolean("haptic_on", true);
    	String pkg = prefs.getString("voice_pkg", "default");
    	mListener.mbVoice = bVoiceOn;
    	mListener.mbHaptic = bHapticOn;
    	if (bVoiceOn && !pkg.equals(mVoicePkg)) {
    		mVoicePkg = pkg;
    		new SoundLoadTask(this).execute(sm);
    	}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK 
            && mPanelSwitcher.getCurrentIndex() == ADVANCED_PANEL) {
            mPanelSwitcher.moveRight();
            return true;
        } else {
            return super.onKeyDown(keyCode, keyEvent);
        }
    }

    static void log(String message) {
        if (LOG_ENABLED) {
            Log.v(LOG_TAG, message);
        }
    }

    /**
     * The font sizes in the layout files are specified for a HVGA display.
     * Adjust the font sizes accordingly if we are running on a different
     * display.
     */
    public void adjustFontSize(TextView view) {
        float fontPixelSize = view.getTextSize();
        Display display = getWindowManager().getDefaultDisplay();
        int h = Math.min(display.getWidth(), display.getHeight());
        float ratio = (float)h/HVGA_WIDTH_PIXELS;
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontPixelSize*ratio);
    }
    
	class SoundLoadTask extends AsyncTask<SoundManager, Void, Void> {  
		ProgressDialog dialog;
		Calculator context;
		
		SoundLoadTask(Context context) {
			super();
			this.context = (Calculator)context;
		}
		
		@Override  
		protected void onPreExecute() {  
			dialog = ProgressDialog.show(context, "", 
					context.getString(R.string.loadingvoice), true);
			dialog.setCancelable(false);
		}  		
		
		@Override
		protected Void doInBackground(SoundManager... sm) { 
			sm[0].unloadAll();
	        PackageManager pm = getPackageManager();
	        List<ProviderInfo> list = null;
	        if (!context.mVoicePkg.equals("default")) { 
	        	list = pm.queryContentProviders("org.wolink.app.voicecalc", 
	        		context.getApplicationInfo().uid, 0);
	        }
	        
	        Cursor cursor = null;
	        String pkgName = null;
	        if (list != null) {        
		        String authority = null;
		        
		        for (int i = 0; i < list.size(); i++) {
		        	ProviderInfo info = list.get(i);
		        	if (context.mVoicePkg.equals(info.authority)) {
		        		pkgName = info.packageName;
		        		authority = info.authority;
		        	}
		        }
		        
		        if (authority != null) {
		        	cursor = ((Activity)context).managedQuery(
						Uri.parse("content://" + authority + "/voices"), 
						null, null, null, null);
		        }
	        }
			
			if (cursor != null && cursor.moveToFirst()) {
				int keyColumn = cursor.getColumnIndex("key");
				int resIdColumn = cursor.getColumnIndex("resId");
				int timeColumn = cursor.getColumnIndex("time");
				String key;
				int resId;
				int time;
				do {
					key = cursor.getString(keyColumn);
					resId = cursor.getInt(resIdColumn);
					time = cursor.getInt(timeColumn);
					try {
						AssetFileDescriptor afd = context.getContentResolver().openAssetFileDescriptor(
							Uri.parse("android.resource://" + pkgName + "/" + resId),
							"r"
							);
						sm[0].addSound(key, afd, time);
					}
					catch (Throwable t) {
						// Nothing
					}
				} while (cursor.moveToNext());
			} else {
		        sm[0].addSound("1", R.raw.one, 320);
		        sm[0].addSound("2", R.raw.two, 274);
		        sm[0].addSound("3", R.raw.three, 304);
		        sm[0].addSound("4", R.raw.four, 215);
		        sm[0].addSound("5", R.raw.five, 388);
		        sm[0].addSound("6", R.raw.six, 277);
		        sm[0].addSound("7", R.raw.seven, 447);
		        sm[0].addSound("8", R.raw.eight, 274);
		        sm[0].addSound("9", R.raw.nine, 451);
		        sm[0].addSound("0", R.raw.zero, 404);
		        sm[0].addSound("AC", R.raw.ac, 696);
		        sm[0].addSound("DEL", R.raw.del, 442);
		        sm[0].addSound("+", R.raw.plus, 399);
		        sm[0].addSound(getString(R.string.minus), R.raw.minus, 530);
		        sm[0].addSound(getString(R.string.mul), R.raw.mul, 350);
		        sm[0].addSound(getString(R.string.div), R.raw.div, 350);
		        sm[0].addSound("=", R.raw.equal, 480);
		        sm[0].addSound(".", R.raw.dot, 454);
			}
	        return null;
		}  
		
		@Override  
		protected void onPostExecute(Void n) {  
			dialog.dismiss();
		} 
	}  
}
