package jp.chau2chaun2.mannerstimer;
abc

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {  

	public static final String ID_KEY = "id";
	
    @Override  
    public void onReceive(Context context, Intent intent) {
    	
    	String action = intent.getAction();
    	if (action != null) {
	    	if (action.equals(Intent.ACTION_BOOT_COMPLETED)
	    			|| action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
	    		
	    		/* ログ出力 */
	    		if (PreferenceControl.isHistoryDebugMode(context)) {
	    			FileEditor fe = new FileEditor();
	    			if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
	    				fe.writeText("--- BOOT COMPLETED!");	
	    			} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
	    				fe.writeText("--- PACKAGE UPDATED!");
	    			}
	    			
	    		}
	    		
	    		/* システム起動時 と アプリアップデート時 */
	    		DataControl dc = new DataControl(context);
	    		Cursor cr = dc.getCursor();
	    		if (cr.moveToFirst()) {
	    			/*** ※※もうちょっと軽い動作が可能。パワーONだけで絞ればおｋ */
	    			int length = cr.getCount();
	    			ListData listData;
	    			for (int i = 0; i < length; i++) {
	    				listData = new ListData(context, cr.getInt(0));
	    				/* 機能させるか判断して切り替える */
	    				if (listData.isPowerON()) {
	    					AlarmController.sendAlarmManager(context, listData.getID());
	    					
	    				}
	
	    				cr.moveToNext();
	    			}
	    		}
	    		
	    		DBGLog.d("AlarmReceiver:auto");
	    		
	    	} else if (action.equals(Intent.ACTION_TIME_CHANGED)) {
	    		/* ログ出力 */
	    		if (PreferenceControl.isHistoryDebugMode(context)) {
	    			FileEditor fe = new FileEditor();
	    			fe.writeText("--- TIME_CHANGE! reset Timer!");	
	    			
	    		}
	    		
	    		/* システム起動時 と アプリアップデート時 */
	    		DataControl dc = new DataControl(context);
	    		Cursor cr = dc.getCursor();
	    		if (cr.moveToFirst()) {
	    			/*** ※※もうちょっと軽い動作が可能。パワーONだけで絞ればおｋ */
	    			int length = cr.getCount();
	    			ListData listData;
	    			for (int i = 0; i < length; i++) {
	    				listData = new ListData(context, cr.getInt(0));
	    				/* 機能させるか判断して切り替える */
	    				if (listData.isPowerON()) {
	    					AlarmController.stopAlarmManager(context, listData.getID());
	    					AlarmController.sendAlarmManager(context, listData.getID());
	    					
	    				}
	
	    				cr.moveToNext();
	    			}
	    		}
	    		
	    		
	    	}
    	} else {
    		
    		/* 手動で投げられた場合 */
    		Bundle bundle = intent.getExtras();
    		int id = bundle.getInt(ID_KEY);
    		ListData listData = new ListData(context, id);
    		
    		/* ログ出力 */
    		if (PreferenceControl.isHistoryDebugMode(context)) {
    			FileEditor fe = new FileEditor();
    			fe.writeText("--- TIMER RECEIVED! ListID [" + listData.getListID() + "]");    			
    		}
    		
    		/* 機能させるか判断して切り替える */
    		if (listData.isChangeOk()) {
    			EtcController.changePhoneStatus(context, listData);
    			if (PreferenceControl.isHistoryDebugMode(context)) {
        			FileEditor fe = new FileEditor();
        			fe.writeText("- changed " + listData.toString());    			
        		}
    		}
    		
    		if (listData.getMode() == DatabaseInfo.Timer.MODE_ONECE) {
    			listData.setPower(Boolean.FALSE);
    			listData.updateAllColumnValueToDB();
    			
    			/* ホームウィジェットを更新 */
    			Intent broadcast = new Intent();
    			broadcast.setAction(HomeSwitchWidget.ACTION_EDITED);
    			broadcast.putExtra(HomeSwitchWidget.KEY_DATASID, id);
    	    	DBGLog.i("sendBroadcast ACTION_EDITED:id:" + id);
    	    	context.sendBroadcast(broadcast);
    		}
    		
    		DBGLog.d("AlarmReceiver:syudo:" + id);
    	}
		
    }
    
    
}


