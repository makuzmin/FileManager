package com.makuzmin.apps.filemanager;
import android.app.*;
import android.os.*;
import android.view.View.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.content.SharedPreferences.Editor;

public class SortActivity extends Activity implements OnClickListener
{
	
	public static final String PACKAGE_NAME = "com.makuzmin.apps.filemanager";
	
	SharedPreferences sp;
	
	Button btnOK, btnCancel;
	RadioGroup rgLocal, rgLocalDirect, rgNet, rgNetDirect;
	RadioButton rbLocName, rbLocModif, rbLocSize, rbLocAscen, rbLocDescen;
	RadioButton rbNetName, rbNetModif, rbNetSize, rbNetAscen, rbNetDescen;
	

	@Override
	public void onClick(View v)
	{
		if(v.getId()==R.id.sort_btnOK) {
			Editor editor = sp.edit();
			switch(rgLocal.getCheckedRadioButtonId()){
				case R.id.sort_rbLocName:
					editor.putInt(SharPrefMan.SAVED_LOC_SORT, MyCollections.FILE_NAME);					
					break;
				case R.id.sort_rbLocModif:
					editor.putInt(SharPrefMan.SAVED_LOC_SORT, MyCollections.FILE_MODIFIED);
					break;
				case R.id.sort_rbLocSize:
					editor.putInt(SharPrefMan.SAVED_LOC_SORT, MyCollections.FILE_LENGTH);
					break;
			}
			switch(rgLocalDirect.getCheckedRadioButtonId()){
				case R.id.sort_rbLocAscen:
					editor.putBoolean(SharPrefMan.SAVED_LOC_DIRECT, true);
					break;
				case R.id.sort_rbLocDescen:
					editor.putBoolean(SharPrefMan.SAVED_LOC_DIRECT, false);
					break;
			}
			switch(rgNet.getCheckedRadioButtonId()){
				case R.id.sort_rbNetName:
					editor.putInt(SharPrefMan.SAVED_NET_SORT, MyCollections.FILE_NAME);
					break;
				case R.id.sort_rbNetModif:
					editor.putInt(SharPrefMan.SAVED_NET_SORT, MyCollections.FILE_MODIFIED);
					break;
				case R.id.sort_rbNetSize:
					editor.putInt(SharPrefMan.SAVED_NET_SORT, MyCollections.FILE_LENGTH);
					break;
			}
		//	Toast.makeText(this, String.valueOf(SharPrefMan.getPrefInt(this, SharPrefMan.SAVED_NET_SORT,1)), Toast.LENGTH_SHORT).show();
			switch(rgNetDirect.getCheckedRadioButtonId()){
				case R.id.sort_rbNetAscen:
					editor.putBoolean(SharPrefMan.SAVED_NET_DIRECT, true);
					break;
				case R.id.sort_rbNetDescen:
					editor.putBoolean(SharPrefMan.SAVED_NET_DIRECT, false);
					break;
			}
			
			editor.commit();
			
			setResult(300);
			finish();
		}else finish();
		
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.sort);
		
		rgLocal=(RadioGroup)findViewById(R.id.sort_rgLocal);
		rgLocalDirect=(RadioGroup)findViewById(R.id.sort_rgLocalDirect);
		rgNet=(RadioGroup)findViewById(R.id.sort_rgNet);
		rgNetDirect=(RadioGroup)findViewById(R.id.sort_rgNetDirect);
		
		rbLocName=(RadioButton)findViewById(R.id.sort_rbLocName);
		rbLocModif=(RadioButton)findViewById(R.id.sort_rbLocModif);
		rbLocSize=(RadioButton)findViewById(R.id.sort_rbLocSize);
		rbLocAscen=(RadioButton)findViewById(R.id.sort_rbLocAscen);
		rbLocDescen=(RadioButton)findViewById(R.id.sort_rbLocDescen);
		
		rbNetName=(RadioButton)findViewById(R.id.sort_rbNetName);
		rbNetModif=(RadioButton)findViewById(R.id.sort_rbNetModif);
		rbNetSize=(RadioButton)findViewById(R.id.sort_rbNetSize);
		rbNetAscen=(RadioButton)findViewById(R.id.sort_rbNetAscen);
		rbNetDescen=(RadioButton)findViewById(R.id.sort_rbNetDescen);
		
		btnOK = (Button)findViewById(R.id.sort_btnOK);
		btnCancel = (Button)findViewById(R.id.sort_btnCancel);
		btnOK.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		
		sp = getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
		
		switch(sp.getInt(SharPrefMan.SAVED_LOC_SORT, MyCollections.FILE_NAME)){
			case MyCollections.FILE_NAME:
				rbLocName.setChecked(true);					
				break;
			case MyCollections.FILE_MODIFIED:
				rbLocModif.setChecked(true);
				break;
			case MyCollections.FILE_LENGTH:
				rbLocSize.setChecked(true);
				break;
		}
		if(sp.getBoolean(SharPrefMan.SAVED_LOC_DIRECT, true))
			rbLocAscen.setChecked(true);
		else
			rbLocDescen.setChecked(true);
			
		switch(sp.getInt(SharPrefMan.SAVED_NET_SORT, MyCollections.FILE_NAME)){
			case MyCollections.FILE_NAME:
				rbNetName.setChecked(true);					
				break;
			case MyCollections.FILE_MODIFIED:
				rbNetModif.setChecked(true);
				break;
			case MyCollections.FILE_LENGTH:
				rbNetSize.setChecked(true);
				break;
		}
		if(sp.getBoolean(SharPrefMan.SAVED_NET_DIRECT, true))
			rbNetAscen.setChecked(true);
		else
			rbNetDescen.setChecked(true);
		
	}
}
