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

import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

class EventListener implements View.OnKeyListener, 
                               View.OnClickListener, 
                               View.OnLongClickListener {
    Logic mHandler;
    PanelSwitcher mPanelSwitcher;
    boolean mbVoice;
    boolean mbHaptic;
    
    void setHandler(Logic handler, PanelSwitcher panelSwitcher) {
        mHandler = handler;
        mPanelSwitcher = panelSwitcher;
        mbVoice = true;
        mbHaptic = true;
    }
    
    //@Override
    public void onClick(View view) {
    	if (mbVoice) {
    		SoundManager sm = SoundManager.getInstance();
    		sm.playSound(((Button) view).getText().toString());
    	}
    	if (mbHaptic) {
    		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, 
				HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    	}
    	
        int id = view.getId();        
        switch (id) {
        case R.id.del:
            mHandler.onDelete();
            break;

        case R.id.equal:
            mHandler.onEnter();
            if (mbVoice) {
            	SoundManager sm = SoundManager.getInstance();
            	String result = mHandler.getText().toString();
            	String[] keys = new String[result.length()];
            	for(int i = 0; i < result.length(); i++) {
            		keys[i] = String.valueOf(result.charAt(i));
            	}
            	sm.playSeqSounds(keys);
            }
           break;

        case R.id.clear:
            mHandler.onClear();
            break;

        default:
            if (view instanceof Button) {
                String text = ((Button) view).getText().toString();
                if (text.length() >= 2) {
                    // add paren after sin, cos, ln, etc. from buttons
                    text += '(';
                }
                mHandler.insert(text);
                if (mPanelSwitcher != null && 
                    mPanelSwitcher.getCurrentIndex() == Calculator.ADVANCED_PANEL) {
                    mPanelSwitcher.moveRight();
                }                    
            }
        }
    }

    //@Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        if (id == R.id.del) {
            mHandler.onClear();
            return true;
        }
        return false;
    }
    
    private static final char[] EQUAL = {'='};

    //@Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            //boolean eat = mHandler.eatHorizontalMove(keyCode == KeyEvent.KEYCODE_DPAD_LEFT);
            return false;
        }

        //Work-around for spurious key event from IME, bug #1639445
        if (action == KeyEvent.ACTION_MULTIPLE && keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            return true; // eat it
        }

        //Calculator.log("KEY " + keyCode + "; " + action);
        
        if (keyEvent.getMatch(EQUAL, keyEvent.getMetaState()) == '=') {
            if (action == KeyEvent.ACTION_UP) {
                mHandler.onEnter();
            }
            return true;
        }

        if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER &&
            keyCode != KeyEvent.KEYCODE_DPAD_UP &&
            keyCode != KeyEvent.KEYCODE_DPAD_DOWN &&
            keyCode != KeyEvent.KEYCODE_ENTER) {
            return false;
        }

        /* 
           We should act on KeyEvent.ACTION_DOWN, but strangely
           sometimes the DOWN event isn't received, only the UP.
           So the workaround is to act on UP...
           http://b/issue?id=1022478
         */

        if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {                
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mHandler.onEnter();
                break;
                
            case KeyEvent.KEYCODE_DPAD_UP:
                mHandler.onUp();
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:            
                mHandler.onDown();
                break;
            }
        }
        return true;
    }
}
