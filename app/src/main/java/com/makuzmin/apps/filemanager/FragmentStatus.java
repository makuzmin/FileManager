package com.makuzmin.apps.filemanager;
import android.app.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.os.*;
import android.widget.*;
import android.content.*;

public class FragmentStatus extends Fragment
{
	public int processId = 0; //background process id
	TextView tvProcInfo, tvInfoItems, tvInfoBytes, tv_cbText;
	Button btnCancel;
	ProgressBar pb_bkgrProcess;
	CheckBox cb_showNetwork;
	Activity parentActivity;
	int countByte, currentByte;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		parentActivity = activity;
		nListener = (OnShowNetClickListener)activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragmentstatus, null);
		pb_bkgrProcess = (ProgressBar) v.findViewById(R.id.fragmentstatus_pb_bkgrProcess);
		tvProcInfo = (TextView) v.findViewById(R.id.fragmentstatus_tvProcInfo);
		tvInfoItems = (TextView) v.findViewById(R.id.fragmentstatus_tvInfoItems);
		tvInfoBytes = (TextView) v.findViewById(R.id.fragmentstatus_InfoBytes);
		btnCancel = (Button) v.findViewById(R.id.fragmentstatus_btnCancel);
		cb_showNetwork = (CheckBox) v.findViewById(R.id.fragmentstatus_cb_showNetwork);
		tv_cbText = (TextView) v.findViewById(R.id.fragmentstatus_tv_cbText);
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		clearStatusBar();
		btnCancel.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent("com.makuzmin.apps.filemanager.cancelcall")
						.putExtra("startId", processId).putExtra("status", "cancel");
					parentActivity.sendBroadcast(intent);
					btnCancel.setClickable(false);
				}
		});
		cb_showNetwork.setChecked(SharPrefMan.getPrefBool(parentActivity, SharPrefMan.SAVED_NCHECKED, false));
		cb_showNetwork.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v)
				{
					boolean checked = ((CheckBox) v).isChecked();
					SharPrefMan.savePrefBool(parentActivity, SharPrefMan.SAVED_NCHECKED, checked);
//					Toast.makeText(parentActivity, String.valueOf(checked), Toast.LENGTH_SHORT).show();
					nListener.showNetClick(checked);
				}
				
			
		});
	}
	
/*	@Override
	 public void onSaveInstanceState(Bundle outState) {
	 super.onSaveInstanceState(outState);
	 try{
	 	outState.putBoolean("pb_bkgrprogress", pb_bkgrProcess.isShown());
	 	outState.putBoolean("cbText", tv_cbText.isShown());
	 	outState.putBoolean("cb_shownetwork", cb_showNetwork.isShown());
	 	outState.putBoolean("tvprocinfo", tvProcInfo.isShown());
	 	outState.putBoolean("btncancel", btnCancel.isShown());
	 	outState.putBoolean("tvinfoitems", tvInfoItems.isShown());
	 	outState.putBoolean("tvinfobytes", tvInfoBytes.isShown());
	 	outState.putString("tvprocinfo_text", tvProcInfo.getText().toString());
	 	outState.putString("tvinfoitems_text", tvInfoItems.getText().toString());
	 	outState.putString("tvinfobytes_text", tvInfoBytes.getText().toString());
	 }catch(NullPointerException ne) {}
	 //	Log.d("myLogs", "onSaveInstanceState");
	 }

	 @Override
	 public void onViewStateRestored(Bundle savedInstanceState) {
	 super.onViewStateRestored(savedInstanceState);
	 try{
	 	if(savedInstanceState.getBoolean("pb_bkgrprogress")) pb_bkgrProcess.setVisibility(View.VISIBLE);
		else pb_bkgrProcess.setVisibility(View.GONE);
		if(savedInstanceState.getBoolean("cbText")) tv_cbText.setVisibility(View.VISIBLE);
		else tv_cbText.setVisibility(View.GONE);
		if(savedInstanceState.getBoolean("cb_shownetwork")) cb_showNetwork.setVisibility(View.VISIBLE);
		else cb_showNetwork.setVisibility(View.GONE);
		if(savedInstanceState.getBoolean("btncancel")) btnCancel.setVisibility(View.VISIBLE);
		else btnCancel.setVisibility(View.GONE);
		if(savedInstanceState.getBoolean("tvprocinfo")) tvProcInfo.setVisibility(View.VISIBLE);
		else tvProcInfo.setVisibility(View.GONE);
		if(savedInstanceState.getBoolean("tvinfoitems")) tvInfoItems.setVisibility(View.VISIBLE);
		else tvInfoItems.setVisibility(View.GONE);
		if(savedInstanceState.getBoolean("tvinfobytes")) tvInfoBytes.setVisibility(View.VISIBLE);
		else tvInfoBytes.setVisibility(View.GONE);
		tvProcInfo.setText(savedInstanceState.getString("tvprocinfo_text"));
		tvInfoItems.setText(savedInstanceState.getString("tvinfoitems_text"));
		tvInfoBytes.setText(savedInstanceState.getString("tvinfobytes_text"));
	 }catch(NullPointerException ne){}
	 //	Log.d("myLogs", "onViewStateRestored");
	 } */
	
	public void clearStatusBar()
	{
		cb_showNetwork.setVisibility(View.VISIBLE);
		tv_cbText.setVisibility(View.VISIBLE);
		pb_bkgrProcess.setVisibility(View.GONE);
		tvProcInfo.setVisibility(View.GONE);
		btnCancel.setVisibility(View.GONE);
		tvInfoItems.setVisibility(View.GONE);
		tvInfoBytes.setVisibility(View.GONE);
	}
	
	public void onProcessResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode){
			case 100:
				countByte = 0;
				processId = data.getIntExtra("startId", 0);
				cb_showNetwork.setVisibility(View.GONE);
				tv_cbText.setVisibility(View.GONE);
				pb_bkgrProcess.setVisibility(View.VISIBLE);
				tvProcInfo.setVisibility(View.VISIBLE);
				btnCancel.setVisibility(View.VISIBLE);
				btnCancel.setClickable(true);
				tvInfoItems.setText("");
				tvInfoBytes.setText("");
				tvProcInfo.setText("background process: " + requestCode + "with Id: " + processId + " is running");
				break;

			case 150:
				int count = data.getIntExtra("count", 0);
				cb_showNetwork.setVisibility(View.GONE);
				tv_cbText.setVisibility(View.GONE);
				pb_bkgrProcess.setVisibility(View.VISIBLE);
				tvProcInfo.setVisibility(View.VISIBLE);	
				btnCancel.setVisibility(View.VISIBLE);		
				tvInfoItems.setVisibility(View.VISIBLE);
				tvInfoItems.setText("processed items: " + String.valueOf(count));
				countByte = countByte + currentByte;
				break;

			case 160:
				currentByte = data.getIntExtra("byte", 0);
				cb_showNetwork.setVisibility(View.GONE);
				tv_cbText.setVisibility(View.GONE);
				pb_bkgrProcess.setVisibility(View.VISIBLE);
				tvProcInfo.setVisibility(View.VISIBLE);
				btnCancel.setVisibility(View.VISIBLE);	
				tvInfoBytes.setVisibility(View.VISIBLE);
				tvInfoBytes.setText("processed kBytes: " + String.valueOf(countByte + currentByte));
				break;

			case 300:
				String result = data.getStringExtra("result");
				cb_showNetwork.setVisibility(View.GONE);
				pb_bkgrProcess.setVisibility(View.GONE);
				tv_cbText.setVisibility(View.GONE);
				btnCancel.setVisibility(View.INVISIBLE);
				if(result.equals("cancelled")) {
					tvInfoItems.setVisibility(View.INVISIBLE);
					tvInfoBytes.setVisibility(View.INVISIBLE);
				}
				tvProcInfo.setText("background process: " + requestCode + " has finished with " + result);
				break;
		}
	}
	
	// shows if background process is running
	public boolean isProcRunning(){
		if (pb_bkgrProcess.isShown()) return true;
		return false;
	}
	
	public interface OnShowNetClickListener {
		public void showNetClick(Boolean checked);
	}

	OnShowNetClickListener nListener;
}
