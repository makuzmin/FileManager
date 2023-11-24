package com.makuzmin.apps.filemanager;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.OnTabChangeListener;
import android.util.*;
//import android.annotation.*;
//import android.provider.*;
//import android.support.v4.view.*;
//import android.support.v4.app.*;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.view.PagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.support.v4.view.ViewPager.OnPageChangeListener;
//import android.support.v4.app.FragmentStatePagerAdapter;

public class MainActivity extends Activity implements SelectDialogFragment.SelectDialogListener, 
       FileCmd.FileCmdListener, FragmentFile.OnItemClickListener, FragmentStatus.OnShowNetClickListener,
	   FragmentNet.MenuVisibilitySet
{

	@Override
	public void menuVisibilitySet(Boolean visibility)
	{
		menuNewNetFolder.setEnabled(visibility);
	}


	@Override
	public void showNetClick(Boolean checked)
	{
		menuAuthorize.setVisible(!checked);
		menuNewNetFolder.setVisible(!checked);
		fTrans = getFragmentManager().beginTransaction();
		if(!checked){	
			frgmt_Cont.setVisibility(View.VISIBLE);
   			fNet = new FragmentNet();
    	   	fTrans.add(R.id.main_frgmt_Cont, fNet);
			if(!isLarge()) 
				{tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
				tabHost.setCurrentTabByTag("tag2");}
			}
		if(checked) {			
    		fTrans.remove(fNet);
			frgmt_Cont.setVisibility(View.GONE);
			if(!isLarge()) 
				{tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.GONE);
				tabHost.setCurrentTabByTag("tag1");}
			}
		fTrans.commit();
	}


	@Override
	public boolean isRunBkgrProcess()
	{
		// check if background process is running
		return fStatus.isProcRunning();
	}
	
	public int processId = 0; //background process id

// interface from FragmentFile
	@Override
	public void clearStatusBar()
	{
/*		pbIcon.setVisibility(View.INVISIBLE);
		tvpbText.setVisibility(View.INVISIBLE);
		btnCancel.setVisibility(View.INVISIBLE);
		tvinfoText.setVisibility(View.INVISIBLE);
		tvinfoText2.setVisibility(View.INVISIBLE);  */
		if(!isRunBkgrProcess()) fStatus.clearStatusBar();
	}

	
	
//	ArrayList<String> fileName;
/*	ProgressBar pbIcon;
	TextView tvpbText, tvinfoText, tvinfoText2;
	Button btnCancel; */
	
	FragmentFile fFile; 
	FragmentNet fNet;
	FragmentStatus fStatus;
	FrameLayout frgmt_Cont;
//	FragmentInfo fInfo;
	FragmentTransaction fTrans;
	TabHost tabHost;
//	Boolean isFragAttached = false;
	MenuItem menuNewNetFolder, menuAuthorize;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
				
//		fileName = new ArrayList<String>();	
		
		fFile = (FragmentFile) getFragmentManager().findFragmentById(R.id.frgmtFile);
//		fNet = (FragmentNet) getFragmentManager().findFragmentById(R.id.frgmtNet);
		fStatus = (FragmentStatus) getFragmentManager().findFragmentById(R.id.frgmtStatus);
		frgmt_Cont =(FrameLayout) findViewById(R.id.main_frgmt_Cont);
/*		pbIcon = (ProgressBar) findViewById(R.id.main_pbIcon);
		tvpbText = (TextView) findViewById(R.id.main_tvpbText);
		tvinfoText = (TextView) findViewById(R.id.main_tvinfoText);
		tvinfoText2 = (TextView) findViewById(R.id.main_tvinfoText2);	
		btnCancel = (Button) findViewById(R.id.main_btnCancel);   */
		clearStatusBar();
	    setNetFragment();
    }
	
	// options menu 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		menuAuthorize = menu.findItem(R.id.menu_net_authorize);
		menuNewNetFolder = menu.findItem(R.id.menu_new_net_folder);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onPrepareOptionsMenu(Menu menu)
	{ 
		Boolean netVisible = (!SharPrefMan.getPrefBool(this, SharPrefMan.SAVED_NCHECKED, false));
		menuAuthorize.setVisible(netVisible);
		menuNewNetFolder.setVisible(netVisible);
		menuNewNetFolder.setEnabled(false);
		
//        if(fileName.size() == 0){
//			menuPaste.setVisible(false);
//			menuNew.setVisible(true);
//		}
//		else {
//			menuPaste.setVisible(true); 
//			menuNew.setVisible(false);
//		}
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_new_net_folder:
				SelectDialogFragment dialog = new SelectDialogFragment(SelectDialogFragment.DIALOG_NEWFOLDER, SelectDialogFragment.DIALOG_NET);
				dialog.show(getFragmentManager(), "newdirialog"); 
//				createNewFolder("New Folder");
				break; 
			case R.id.menu_net_authorize:
				SelectDialogFragment dialog2 = new SelectDialogFragment(SelectDialogFragment.DIALOG_AUTHORIZE, SelectDialogFragment.DIALOG_NET);
				dialog2.show(getFragmentManager(), "authorizedialog");
				break;
			case R.id.menu_sort:
				startActivityForResult(new Intent(this, SortActivity.class), 1000);
				//startActivity(new Intent(this, SortActivity.class));
				break;
				// exit
			case R.id.menu_exit:
				if(fStatus.isProcRunning()) break;
				if(fFile.exitButtonPressed()) {
					onExitingAppl();
    				finish();
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// on back pressed event
	@Override
	public void onBackPressed(){
		clearStatusBar();
		fFile.goToBack();
	}

	// clean up when Application closes
	protected void onExitingAppl(){
		System.runFinalizersOnExit(true);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode!=1000) fStatus.onProcessResult(requestCode, resultCode, data);
		if(resultCode == 300){
			if(requestCode!=1000) fFile.onFileCmdPostExecute(); //temporarily blocking refresh, while sorting for local is not done
			if(!SharPrefMan.getPrefBool(this, SharPrefMan.SAVED_NCHECKED, false)) fNet.onFileCmdPostExecute();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		onExitingAppl();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
	}
	
/*	@Override
	public void onSaveInstanceState(Bundle outState) {
//		outState.putBoolean("inflated", isInflatedBefore);
		if(fNet !=null){
			fTrans = getFragmentManager().beginTransaction();
			fTrans.remove(fNet);
			fTrans.commit();
		} 
		super.onSaveInstanceState(outState);
	} 
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	} 
	
	@Override
	protected void onResume(){
		super.onResume();
		try{
			if(!fNet.isAdded()) {
				setNetFragment();
			}
		}catch(NullPointerException ne){}
	} */
	
	boolean isLarge() {
		return (getResources().getConfiguration().screenLayout 
			& Configuration.SCREENLAYOUT_SIZE_MASK) 
			>= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	
	private void setNetFragment(){
		Boolean checked = SharPrefMan.getPrefBool(this, SharPrefMan.SAVED_NCHECKED, false);		
		if(!checked){	
			frgmt_Cont.setVisibility(View.VISIBLE);
    		fNet = new FragmentNet();
    		fTrans = getFragmentManager().beginTransaction();
    		fTrans.add(R.id.main_frgmt_Cont, fNet);
    		fTrans.commit();
		}else frgmt_Cont.setVisibility(View.GONE); 
		if(!isLarge()) setTabViews(checked); 
	}
	
/*	public void onClickCancel(View v){
//		if(stopService(new Intent(this, FileCmdService.class))) {
//			pbIcon.setVisibility(View.INVISIBLE);
//			btnCancel.setVisibility(View.INVISIBLE);
//			tvpbText.setText("background process is cancelled");
//    		fFile.onFileCmdPostExecute();
//		}
//		else tvpbText.setText("background process failed to cancel");
		
		Intent intent = new Intent("com.makuzmin.apps.filemanager.cancelcall")
    		.putExtra("startId", processId).putExtra("status", "cancel");
			sendBroadcast(intent);
			btnCancel.setClickable(false);
		
	}  */
	
	@Override
	public void onFileCmdPostExecute()
	{
		fFile.onFileCmdPostExecute();
	}

	@Override
	public void onFileCmdCancel()
	{
		fFile.onFileCmdCancel();
	}

	@Override
	public void onDeleteDialogPositiveClick(DialogFragment dialog, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) fFile.onDeleteDialogPositiveClick();
		if(type == SelectDialogFragment.DIALOG_NET) fNet.onDeleteDialogPositiveClick();
	}

	@Override
	public void onDeleteDialogNegativeClick(DialogFragment dialog, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) fFile.onDeleteDialogNegativeClick();
		if(type == SelectDialogFragment.DIALOG_NET) return;
	}

	@Override
	public void onNewFolderDialogPositiveClick(DialogFragment dialog, String text, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) fFile.onNewFolderDialogPositiveClick(text);
		if(type == SelectDialogFragment.DIALOG_NET) fNet.onNewFolderDialogPositiveClick(text);
	}

	@Override
	public void onRenameDialogPositiveClick(DialogFragment dialog, String text, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) fFile.onRenameDialogPositiveClick(text);
		if(type == SelectDialogFragment.DIALOG_NET) fNet.onRenameDialogPositiveClick(text);
	}

	@Override
	public void onRenameDialogNegativeClick(DialogFragment dialog, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) fFile.onRenameDialogNegativeClick();
		if(type == SelectDialogFragment.DIALOG_NET) return;
	}

	@Override
	public void onExistsDialogPositiveClick(DialogFragment dialog, String text, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) fFile.onExistsDialogPositiveClick(text);
		if(type == SelectDialogFragment.DIALOG_NET) return;
	}

	@Override
	public void onExistsDialogNegativeClick(DialogFragment dialog, String text, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) fFile.onExistsDialogNegativeClick(text);
		if(type == SelectDialogFragment.DIALOG_NET) return;
	}
	
	@Override
	public void onAuthorizeDialogPositiveClick(DialogFragment dialog, String user, String password, String domain, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) return;
		if(type == SelectDialogFragment.DIALOG_NET) fNet.onAuthorizeDialogPositiveClick(user, password, domain);
	}
	
	@Override
	public void onHostDialogClick(DialogFragment dialog, String host, int type)
	{
		if(type == SelectDialogFragment.DIALOG_SD) return;
		if(type == SelectDialogFragment.DIALOG_NET) fNet.onHostDialogClick(host);
	}

	
	
	private void setTabViews(Boolean checked){
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setIndicator("My Device");
        tabSpec.setContent(R.id.frgmtFile);
        tabHost.addTab(tabSpec);
		
		tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("My Network");
        tabSpec.setContent(R.id.main_frgmt_Cont);
        tabHost.addTab(tabSpec);
		
		tabHost.setCurrentTabByTag("tag1");
		
		if(checked) {
			tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.GONE);
		}

		/*     tabHost.setOnTabChangedListener(new OnTabChangeListener() {
		 public void onTabChanged(String tabId) {
		 Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();
		 }
		 });  */
	}

}
