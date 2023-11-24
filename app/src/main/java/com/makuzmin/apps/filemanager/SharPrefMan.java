package com.makuzmin.apps.filemanager;

import android.content.*;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.*;

public class SharPrefMan
{
	
	static final String SAVED_PATH = "saved_path_sd";
	static final String SAVED_SMB = "saved_url_smb";
	static final String SAVED_URL = "saved_url_net";
	static final String SAVED_AUTH = "saved_auth";
	static final String SAVED_NCHECKED = "saved_net_frag_checked";
	static final String SAVED_HOST = "saved_host";
	
	static final String SAVED_LOC_SORT = "saved_loc_sort";
	static final String SAVED_LOC_DIRECT = "saved_loc_sort_direct";
	static final String SAVED_NET_SORT = "saved_net_sort";
	static final String SAVED_NET_DIRECT = "saved_net_sort_direct";
	
	
	
	
	public static void savePrefString(Activity activity, String code, String value){
		SharedPreferences sPref = activity.getPreferences(activity.MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putString(code, value);
		ed.commit();
	}
	
	public static String getPrefString(Activity activity, String code, String defValue){
		SharedPreferences sPref = activity.getPreferences(activity.MODE_PRIVATE);
		String savedText = sPref.getString(code, defValue);
		return savedText;
	}
	
	public static void savePrefInt(Activity activity, String code, int value){
		SharedPreferences sPref = activity.getPreferences(activity.MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putInt(code, value);
		ed.commit();
	}

	public static int getPrefInt(Activity activity, String code, int defValue){
		SharedPreferences sPref = activity.getPreferences(activity.MODE_PRIVATE);
		int savedValue = sPref.getInt(code, defValue);
		return savedValue;
	}
	
	public static void savePrefBool(Activity activity, String code, Boolean value){
		SharedPreferences sPref = activity.getPreferences(activity.MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putBoolean(code, value);
		ed.commit();
	}

	public static Boolean getPrefBool(Activity activity, String code, Boolean defValue){
		SharedPreferences sPref = activity.getPreferences(activity.MODE_PRIVATE);
		Boolean savedText = sPref.getBoolean(code, defValue);
		return savedText;
	}
}
