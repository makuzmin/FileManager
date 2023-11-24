package com.makuzmin.apps.filemanager;
import android.app.*;
import android.app.ListFragment;
import android.os.*;
import android.os.Handler;
import android.view.*;
import android.widget.*;
import jcifs.smb.SmbFile;
import java.net.*;
import jcifs.smb.*;
import jcifs.smb.NtlmPasswordAuthentication;
import java.util.*;
import android.widget.AdapterView.*;
import android.content.*;
import android.text.format.*;
import android.widget.AbsListView.*;
import android.annotation.*;
import android.provider.*;
import android.util.*;
//import android.util.*;

public class FragmentNet extends ListFragment
{
	final int CMD_COPY_FLASH = 10;
	final int CMD_NEW_DIR_NET = 30;
	final int CMD_DELETE_NET = 40;
	
	Activity prntActivity;
	ListView lvNetFiles;
	Button fNetBtnUp, fNetBtnHost;
	TextView tvURL;
	ProgressBar pb1;
	String urlString, urlStringOld; // current and previous URLs
	// SmbFile file = null;
	List<FileListData> fld;
	ArrayList<String> fileName;	
	Handler h, h1, h2;
	NtlmPasswordAuthentication npa;
	String path; //path to show to user
	int shareType; //share type of current folder
	int operCode = 0;
	Boolean showHidden = false; //to view or hide hidden folders
	String errMessage; // to transfer error from background thread to GUI
	String npaString;
//	Boolean isInflatedBefore = false;
	
	MenuItem menuNew, menuRename, menuDelete, menuCopy;

	// needed to inflate ListFragment with custom layout and find components on the layout
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragmentnet, null);
		fNetBtnUp = (Button) v.findViewById(R.id.fNetBtnUp);
		fNetBtnHost = (Button) v.findViewById(R.id.fNetBtnHost);
		tvURL = (TextView) v.findViewById(R.id.fNettvPath);
		pb1 = (ProgressBar) v.findViewById(R.id.fragmentnet_pb1);
		pb1.setVisibility(View.GONE);
		
		// up button on click listener		
		fNetBtnUp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				getParentUrl();
			} 
		});
		fNetBtnHost.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v)
				{
					SelectDialogFragment dialog2 = new SelectDialogFragment(SelectDialogFragment.DIALOG_HOST, SelectDialogFragment.DIALOG_NET);
					dialog2.show(getFragmentManager(), "hostdialog");
				}			
		});
			
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		setHasOptionsMenu(true);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		prntActivity = activity;
		mCallback = (MenuVisibilitySet) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(isAirplaneModeOn(prntActivity)){
			Toast.makeText(prntActivity, "Network part is disabled in airplane mode.", Toast.LENGTH_SHORT).show();
			return;
		}

//		npaString = "192.168.1.45;test:test";
	    npaString = SharPrefMan.getPrefString(prntActivity, SharPrefMan.SAVED_AUTH, "");
		npa = new NtlmPasswordAuthentication(npaString);
		//		urlString ="smb://192.168.1.2/Maksim/Android/;Maksim:Odessa09@192.168.1.2/";
//		urlString ="smb://192.168.1.2/";
		urlString = urlStringOld = getUrlString();
//		urlString = urlStringOld = "smb://";
		
		lvNetFiles = getListView();
		fileName = new ArrayList<String>();	
		
		// ACTION MENU ON LONG CLICK --------------------------------------------------------	

		lvNetFiles.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lvNetFiles.setMultiChoiceModeListener(new MultiChoiceModeListener(){

//				int n = 0;  //counter

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu)
				{
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.action_menu_net, menu);
					menuRename = menu.findItem(R.id.menu_rename_net);
					menuDelete = menu.findItem(R.id.menu_delete_net);
					menuCopy = menu.findItem(R.id.menu_copy_flash);
					if(checkBackgroundProcess()) {
						menuRename.setEnabled(false);
						menuDelete.setEnabled(false);
						menuCopy.setEnabled(false);
					}else{
						menuRename.setEnabled(true);
						menuDelete.setEnabled(true);
						menuCopy.setEnabled(true);
					}
					// check if background process isrunning !!!!!!
					clearCopyData();
					return true;
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu)
				{
					return false;
				} 

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem menu)
				{
					for(int i = 0; i < fileName.size(); i++){
					}
					switch(menu.getItemId()){
						case R.id.menu_copy_flash:
//							fromPath = fDir.getAbsolutePath();
							operCode = CMD_COPY_FLASH;
							startCmdService(fileName, urlString, 
			    				SharPrefMan.getPrefString(prntActivity, SharPrefMan.SAVED_PATH, 
								Environment.getExternalStorageDirectory().getAbsolutePath()), CMD_COPY_FLASH);
							break;
						case R.id.menu_rename_net:
							SelectDialogFragment sdf2 = new SelectDialogFragment(SelectDialogFragment.DIALOG_RENAME, 
				    			SelectDialogFragment.DIALOG_NET);
							sdf2.show(getFragmentManager(), "selectdialog");
							break; 
						case R.id.menu_delete_net:
	//						fromPath = fDir.getAbsolutePath();
							operCode = CMD_DELETE_NET;
							SelectDialogFragment sdf = new SelectDialogFragment(SelectDialogFragment.DIALOG_DELETE, 
					     		String.valueOf(fileName.size()), SelectDialogFragment.DIALOG_NET);
							sdf.show(getFragmentManager(), "delfiledialog");
							break;	
					}
					mode.finish();
//					if(menu.getItemId() != R.id.menu_send) pereverToz(true);
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode)
				{
//					n = 0;
				}

				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
				{
					try{
						if (checked) {
							fileName.add(fld.get(position).name);
//							n++;
						}
						else {
							fileName.remove(fld.get(position).name);
//							n--;
						}
						mode.setTitle("Selected: " + fileName.size() + " items");
					} catch (ArrayIndexOutOfBoundsException e) {
						Toast.makeText(getActivity(), "There is no logic in this operation!", Toast.LENGTH_SHORT).show();
						mode.finish();
					}
				}			

			});

		// END OF ACTION MENU ON LONG CLICK -------------------------------------------------
		

		// handle result of getFileList()
		h = new Handler(){
			public void handleMessage(android.os.Message msg) {
   				try{
					ListAdapter adapter = new FileListAdapter(prntActivity);
	    			setListAdapter(adapter);
					tvURL.setText(path);
					pb1.setVisibility(View.GONE);
//					lvNetFiles.setVisibility(View.VISIBLE);
					setMenuVisibility();
					SharPrefMan.savePrefString(prntActivity, SharPrefMan.SAVED_SMB, urlString);
	    		}catch(NullPointerException ne){}
			}
		};
		// handle result of getParentUrl()
		h1 = new Handler(){
			public void handleMessage(android.os.Message msg) {
				getFileList();
			}
		};
		// handle errors
		h2 = new Handler(){
			public void handleMessage(android.os.Message msg) {
				Toast.makeText(prntActivity, errMessage + " " + urlString, Toast.LENGTH_SHORT).show();
				if(!urlStringOld.equals(urlString)){
		    		urlString = urlStringOld;
	    			getFileList();
				}else pb1.setVisibility(View.GONE);
				
			}
		};
		getFileList();

	}
	//--------------------------END OF onActivityCreated----------------------------------------------
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
//		fragMenu.removeItem(R.id.menu_net_authorize);
//		fragMenu.removeItem(R.id.menu_new_net_folder);
		super.onSaveInstanceState(outState);
		outState.putString("urlstring", urlString);
//		outState.putString("urlstrinold", urlStringOld);
	//	Log.d("myLogs", "onSaveInstanceState");
	}
	
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		try{
		urlString = urlStringOld = savedInstanceState.getString("urlstring");
//		urlStringOld = savedInstanceState.getString("urlstringold");
		getFileList();
		}catch(NullPointerException ne){}
	//	Log.d("myLogs", "onViewStateRestored");
	} 

	// fragment part of OptionsMenu ----------------------------------------
	/* @Override 
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
//		if (! isInflatedBefore) {
//			isInflatedBefore = true;
		inflater.inflate(R.menu.options_menu_frag_net, menu);//}
		menuNew = menu.findItem(R.id.menu_new_net_folder);
		super.onCreateOptionsMenu(menu, inflater);
	}

	public void onPrepareOptionsMenu(Menu menu)
	{ 
//		menuNew.setVisible(false);
	try{
		menuNew.setEnabled(false);
		}catch(NullPointerException ne){}
	}

	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
				// paste
/*			case R.id.menu_paste:
				if (mCallback.isRunBkgrProcess()) break;
				if (fromPath.equals(fDir.getAbsolutePath())) {
					Toast.makeText(getActivity(), "This operation is not logical!", Toast.LENGTH_SHORT).show();
					break;
				}
				for (int i = 0; i < fileName.size(); i++){
					String p1 = fromPath + "/" + fileName.get(i);
					if (p1.equals(fDir.getAbsolutePath())) {
						Toast.makeText(getActivity(), "This operation cannot be performed!", Toast.LENGTH_SHORT).show();
						pereverToz(false);
						break;
					}
				}
				file2 = new ArrayList<String>();
				startPaste();
				break;  
			case R.id.menu_new_net_folder:
				
				SelectDialogFragment dialog = new SelectDialogFragment(SelectDialogFragment.DIALOG_NEWFOLDER, SelectDialogFragment.DIALOG_NET);
				dialog.show(getFragmentManager(), "newdirialog"); 
//				createNewFolder("New Folder");
				break;
			case R.id.menu_net_authorize:
				SelectDialogFragment dialog2 = new SelectDialogFragment(SelectDialogFragment.DIALOG_AUTHORIZE, SelectDialogFragment.DIALOG_NET);
				dialog2.show(getFragmentManager(), "authorizedialog");
				break; 
		}
		return super.onOptionsItemSelected(item);
	} */

	// END of OptionsMenu part ----------------------------------------------------
	
	// action when item is clicked
	public void onListItemClick(ListView l,View v, int position, long id){
		super.onListItemClick(l,v, position, id);
		if(fld.get(position).directory){
    		urlStringOld = urlString;
    		if(shareType == SmbFile.TYPE_WORKGROUP) urlString = "smb://" + fld.get(position).name + "/";
    		else urlString = urlString + fld.get(position).name + "/";
    		getFileList();
		}
	}
	
	//sets menu buttons visibility
	private void setMenuVisibility(){
//		if (shareType == SmbFile.TYPE_FILESYSTEM || shareType == SmbFile.TYPE_SHARE) menuNew.setVisible(true);
//		else menuNew.setVisible(false);
		if (shareType == SmbFile.TYPE_FILESYSTEM || shareType == SmbFile.TYPE_SHARE) mCallback.menuVisibilitySet(true);
		else mCallback.menuVisibilitySet(false);
	}


	//create array for the list
	public void getFileList() {
//		lvNetFiles.setVisibility(View.INVISIBLE);
		pb1.setVisibility(View.VISIBLE);
    	Thread t = new Thread(){
			@Override
			public void run(){
				try
				{
					SmbFile dir = new SmbFile(urlString, npa);
					try
					{	
						try
						{
							String[] fList = dir.list();
							// Arrays.sort(fList);
							fld = new ArrayList<FileListData>();
							//add folders to the list
							for(int i=0; i < fList.length; i++){
								SmbFile f = new SmbFile(dir.getCanonicalPath() + "/" + fList[i], npa);
							//	if (f.isDirectory()) {	
									if (!f.isHidden() || showHidden) {
					    				if (dir.getType() != SmbFile.TYPE_WORKGROUP) 
											fld.add(new FileListData(f.getName(), f.getLastModified(), f.length(), f.isDirectory(), f.isHidden()));
										//if WORKGROOP
										else fld.add(new FileListData(fList[i], Long.valueOf(0), Long.valueOf(0), true, false));
									}				      
							//	}						
							}
							//add files to the list
					/*		for(int i=0; i < fList.length; i++){
								SmbFile f = new SmbFile(dir.getCanonicalPath() + "/" + fList[i], npa);
								if (!f.isDirectory()) {	
									if (!f.isHidden() || showHidden) {
					    				if (dir.getType() != SmbFile.TYPE_WORKGROUP) 
											fld.add(new FileListData(f.getName(), f.getLastModified(), f.length(), f.isDirectory(), f.isHidden()));
										//if WORKGROUP
						    			else fld.add(new FileListData(fList[i], Long.valueOf(0), Long.valueOf(0), true, false));
									}
								}
							} */
							
			// testing code				MyCollections.sort(fld, MyCollections.FILE_LENGTH, false);
			                SharedPreferences sp = prntActivity.getSharedPreferences(SortActivity.PACKAGE_NAME, prntActivity.MODE_PRIVATE);               
						//	Log.d("myLogs", "sort by " + sp.getInt(SharPrefMan.SAVED_NET_SORT, MyCollections.FILE_NAME) );
						//	Log.d("myLogs", "ascending " + sp.getBoolean(SharPrefMan.SAVED_NET_DIRECT, true));
							MyCollections.sort(fld, sp.getInt(SharPrefMan.SAVED_NET_SORT, MyCollections.FILE_NAME), 
							   sp.getBoolean(SharPrefMan.SAVED_NET_DIRECT, true));

							path = dir.getUncPath() + " " + findFolderType(dir.getType());
							shareType = dir.getType();
						}
						catch (SmbException e)
						{
							errMessage = e.getMessage();
							h2.sendEmptyMessage(1);
						}
					} catch(NullPointerException e){
						errMessage = e.getMessage();
						h2.sendEmptyMessage(1);
					}
				}
				catch (MalformedURLException e)
				{}
				h.sendEmptyMessage(1);
			}
		};
		t.start();
	}
	
	//set parent folder (to go level up)
	public void getParentUrl(){
		Thread t = new Thread(){
			@Override
			public void run(){
				try
				{
					SmbFile f = new SmbFile(urlString, npa);
					urlString = f.getParent();
					h1.sendEmptyMessage(1);
				}
				catch (MalformedURLException e)
				{}
			}
		};
		t.start();
	}
	
	private void renameNet(String fldrName, String newName){
		final String name = fldrName;
		final String text = newName;
		Thread t = new Thread(){
			@Override
			public void run(){
        		try
            	{
         			SmbFile f1 = new SmbFile(urlString + name + "/", npa);
					SmbFile f2 = new SmbFile(urlString + text + "/", npa);
        			try
        			{
        				if(f1.exists()) f1.renameTo(f2);
        				else {
	        				errMessage = "file or folder does not exist";
	        				h2.sendEmptyMessage(1);
        				}
        			}
        			catch (SmbException e)
         			{errMessage = e.getMessage();
						h2.sendEmptyMessage(1);}
				}
        		catch (MalformedURLException e)
        		{errMessage = e.getMessage();
					h2.sendEmptyMessage(1);}
    		}
    	};
    	t.start();
		getFileList();
	}
	
	MenuVisibilitySet mCallback;
	
	public interface MenuVisibilitySet{
		public void menuVisibilitySet(Boolean visibility)
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static boolean isAirplaneModeOn(Context context) {        
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return Settings.System.getInt(context.getContentResolver(), 
										  Settings.System.AIRPLANE_MODE_ON, 0) != 0;          
		} else {
			return Settings.Global.getInt(context.getContentResolver(), 
										  Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
		}       
	}
	
	// temporary test method to find out the name for folder type
	private String findFolderType(int intType){
		String strType = "empty";
		switch(intType){		
    		case SmbFile.TYPE_FILESYSTEM:
	     		strType = "file system";
			break;
			case SmbFile.TYPE_WORKGROUP:
				strType = "workgroup";
			break;
			case SmbFile.TYPE_SERVER:
				strType = "server";
			break;
			case SmbFile.TYPE_SHARE:
				strType = "share";
			break;
			case SmbFile.TYPE_PRINTER:
				strType = "printer";
			break;
			case SmbFile.TYPE_NAMED_PIPE:
				strType = "named pipe";
			break;
			case SmbFile.TYPE_COMM:
				strType = "comm device";
			break;
		}
		return strType;
	}
	
	private String getUrlString(){
		String host = SharPrefMan.getPrefString(prntActivity, SharPrefMan.SAVED_HOST, "");
		if (host.equals("") || host == null) return "smb://";
		else return "smb://" + host + "/";
	}
	
	private Boolean checkBackgroundProcess(){
		//need to write method checking if background process to prevent starting second process
		return false;
	}
	
	void clearCopyData(){
		fileName.clear();
//		fromPath = "";
		operCode = 0;
	}
	
	private void startCmdService(ArrayList<String> name, String from, String to, int code){

		PendingIntent pi = getActivity().createPendingResult(1, new Intent(), 0);
		Intent intent = new Intent(getActivity(), FileCmdService.class).putStringArrayListExtra("filename", name)
			.putExtra("from", from).putExtra("to", to).putExtra("code", code).putExtra("pendingIntent",pi).putExtra("npa",npaString);

		getActivity().startService(intent);
	}
	
	public void onFileCmdPostExecute()
	{
    	getFileList();
	}
	
		// DIALOG and other interface methods -----------------------------------------------------------------
	
	public void onDeleteDialogPositiveClick()
	{
		String toPath ="";
		startCmdService(fileName, urlString, toPath, operCode);
	}
	
//	public void onDeleteDialogNegativeClick()
//	{
//		
//	}  

	public void onNewFolderDialogPositiveClick(String text)
	{
//		Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
		operCode = CMD_NEW_DIR_NET;
		fileName.clear();
		fileName.add("/" + text);
		String toPath ="";
		startCmdService(fileName, urlString, toPath, operCode);			
	}

	public void onRenameDialogPositiveClick(String text)
	{
		renameNet(fileName.get(0), text);
	}  
	
	public void onAuthorizeDialogPositiveClick(String user, String password, String domain)
	{
		npaString = domain + ";" + user + ":" + password;
//		Toast.makeText(getActivity(), npaString, Toast.LENGTH_SHORT).show();
		npa = new NtlmPasswordAuthentication(npaString);
		SharPrefMan.savePrefString(prntActivity, SharPrefMan.SAVED_AUTH, npaString);
		getFileList();
	}
	
	public void onHostDialogClick(String host){
		SharPrefMan.savePrefString(prntActivity, SharPrefMan.SAVED_HOST, host);
		urlString = getUrlString();
		getFileList();
	}
	

/*	public void onRenameDialogNegativeClick()
	{
		pereverToz(false);
	}

	public void onExistsDialogPositiveClick(String text)
	{
		file2.add(text);
		startPaste();
	}

	public void onExistsDialogNegativeClick(String text)
	{
		fileName.remove(text);
		startPaste();
	}
*/
	
	
	
	// extended adapter
	private class FileListAdapter extends ArrayAdapter<FileListData>{
		public FileListAdapter(Context context){
			super(context, R.layout.item, fld);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FileListData listData = getItem(position);
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext())
					.inflate(R.layout.item, parent, false);
			}
    		((TextView) convertView.findViewById(R.id.tvText)).setText(listData.name);
	   		((TextView) convertView.findViewById(R.id.tvDescr))
    			.setText("Modified: " + millsToDate(String.valueOf(listData.modified), "dd/MM/yyyy hh:mm:ss") + "; Size: " +  String.valueOf(listData.length/1024) + " Kbyte");
    		if(listData.directory) 
	    		((ImageView) convertView.findViewById(R.id.ivImg)).setImageResource(R.drawable.ic_folder);
    		else
	   			((ImageView) convertView.findViewById(R.id.ivImg)).setImageResource(R.drawable.ic_file);
			return convertView;
		}
		
		private String millsToDate(String mills, String dateFormat){
			return DateFormat.format(dateFormat, Long.parseLong(mills)).toString();
		}
	}
}
