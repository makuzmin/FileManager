package com.makuzmin.apps.filemanager;

import android.app.ListFragment;
import android.app.PendingIntent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import java.io.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.webkit.*;
import android.content.*;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.*;
import android.widget.AbsListView.*;
import android.app.*;
import android.graphics.*;
import android.provider.DocumentsContract;

public class FragmentFile extends ListFragment 
{
	
	final int DIALOG_FILE_INFO = 1;
	final int DIALOG_DELETE = 2;
	final int DIALOG_NEWFOLDER = 3;
	final int DIALOG_RENAME = 4;
	final int DIALOG_FILE_EXISTS = 5;	
	
	final int CMD_COPY = 1;
	final int CMD_MOVE = 2;
	final int CMD_NEW_DIR = 3;
	final int CMD_DELETE = 4;
	final int CMD_COPY_NET = 70;
	
	File fDir;
	File fDirOld;
	String[] fList;
	AdapterHelper ah;
	ListView lv;
	ArrayList<String> fArrList;
	ArrayList<String> fileName;
	ArrayList<String> file2; //for paste code
	MenuItem menuPaste, menuNew, menuRename, menuSend;
	String fromPath = "";
	int operCode = 0;
	FileCmd fc;
	Activity attActivity;
	SimpleAdapter sAdapter;
	
	// fot thumbnail test
	
	Handler h1111;
	
	// end variables for thumbnail test
	
//	SharedPreferences sPref;
	
	TextView tvPath;
	
	OnItemClickListener mCallback;
	
	public interface OnItemClickListener {
		public void clearStatusBar()
		public boolean isRunBkgrProcess()
	}

//	String data[] = new String[] { "one", "two", "three", "four" };

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		attActivity = activity;
		try {
            mCallback = (OnItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnItemClickListener");
        }	
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		h1111 = new Handler();
		
		lv = getListView();
		
		fileName = new ArrayList<String>();	
		
		// ACTION MENU ON LONG CLICK --------------------------------------------------------	
		
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lv.setMultiChoiceModeListener(new MultiChoiceModeListener(){

				int n = 0;  //counter

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu)
				{
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.action_menu, menu);
					menuRename = menu.findItem(R.id.menu_rename);
					menuSend = menu.findItem(R.id.menu_send);
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
						case R.id.menu_cut:
							fromPath = fDir.getAbsolutePath();
							operCode = CMD_MOVE;	
							break;
						case R.id.menu_copy:
							fromPath = fDir.getAbsolutePath();
							operCode = CMD_COPY;	
							break;
						case R.id.menu_rename:
							fromPath = fDir.getAbsolutePath();
							SelectDialogFragment sdf2 = new SelectDialogFragment(DIALOG_RENAME, SelectDialogFragment.DIALOG_SD);
							sdf2.show(getFragmentManager(), "selectdialog");
							break;
						case R.id.menu_send:
							File f = new File(fDir.getAbsolutePath() + "/" + fileName.get(0));
							if(f.isFile()){
								Intent intent = new Intent();
								intent.setAction(android.content.Intent.ACTION_SEND);
								intent.setType("*/*");
								intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
								startActivity(intent);
							}
							break;
						case R.id.menu_delete:
							fromPath = fDir.getAbsolutePath();
							operCode = CMD_DELETE;
							SelectDialogFragment sdf = new SelectDialogFragment(DIALOG_DELETE, String.valueOf(fileName.size()),
		    					SelectDialogFragment.DIALOG_SD);
							sdf.show(getFragmentManager(), "delfiledialog");
							break;	
						case R.id.menu_copy_net:
							fromPath = fDir.getAbsolutePath();
							operCode = CMD_COPY_NET;
							startCmdServiceNet(fileName, fromPath, SharPrefMan.getPrefString(attActivity, SharPrefMan.SAVED_SMB, "error"), CMD_COPY_NET);
							break;
					}
					mode.finish();
					if(menu.getItemId() != R.id.menu_send) pereverToz(true);
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode)
				{
					n = 0;
				}

				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
				{
					try{
						if (checked) {
							fileName.add(fArrList.get(position));
							n++;
						}
						else {
							fileName.remove(fArrList.get(position));
							n--;
						}
						mode.setTitle("Selected: " + fileName.size() + " items");
						if(fileName.size() == 1) {
							menuRename.setVisible(true);
							menuSend.setVisible(true);
						}else{
							menuRename.setVisible(false);
							menuSend.setVisible(false);
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						Toast.makeText(getActivity(), "There is no logic in this operation!", Toast.LENGTH_SHORT).show();
						mode.finish();
					}
				}			
				
		});
		
		// END OF ACTION MENU ON LONG CLICK -------------------------------------------------
		

//		mountDrive();  // test part !!!!!!!!!!!!!!!!!!!!!!!!!!
		fDir = Environment.getExternalStorageDirectory();
		showFileView(fDir);
	}
	
	// --- END OF onActivityCreated ---
	
	// -------------------------------------------------
//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//																android.R.layout.simple_list_item_1, data);
//		setListAdapter(adapter);
	// -------------------------------------------------
			
	// adapter for list view
	public void showFileView(File dir) {	
		try{	
			fList = dir.list();
			Arrays.sort(fList);
			fArrList = new ArrayList<String>();
			for(int i=0; i < fList.length; i++){
				File f = new File(dir.getAbsolutePath() + "/" + fList[i]);
				if (f.isDirectory()) fArrList.add(fList[i]);
	//			if(i==0) getDrawableFldr(f);
			}
			for(int i=0; i < fList.length; i++){
				File f = new File(dir.getAbsolutePath() + "/" + fList[i]);
				if (!f.isDirectory()) fArrList.add(fList[i]);
	//			if(i==0) getDrawableFile(f);
			}
		
			tvPath.setText(dir.toString());		
			ah = new AdapterHelper(getActivity(), lv, fArrList, dir);
			sAdapter = ah.getAdapter();
			setListAdapter(sAdapter);
			SharPrefMan.savePrefString(attActivity, SharPrefMan.SAVED_PATH, dir.getAbsolutePath());
			
			Thread t = new Thread(new Runnable(){

					@Override
					public void run()
					{
						ah.addThumbs();
						h1111.post(updateAdapter);
					}				
			});
			t.start();

		} catch(NullPointerException e){
			Toast.makeText(getActivity(), "Access denied!", Toast.LENGTH_SHORT).show();
			fDir = fDirOld;
			showFileView(fDir);
		}
	}
	
	Runnable updateAdapter = new Runnable(){

		@Override
		public void run()
		{
			sAdapter.notifyDataSetChanged();
		}
	};
	
	
//	//		ListView lv = getListView();
////		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//	setListAdapter(adapter);
//}
	
	// needed to inflate ListFragment with custom layout and find components on the layout
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragmentfile, null);
		
		tvPath = (TextView) v.findViewById(R.id.ffiletvPath);
		Button button = (Button) v.findViewById(R.id.fFileBtnUp);
		button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v)
				{
					mCallback.clearStatusBar();
					goToBack();
				} 
		});
	
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
//		fragMenu.removeItem(R.id.menu_net_authorize);
//		fragMenu.removeItem(R.id.menu_new_net_folder);
		super.onSaveInstanceState(outState);
		outState.putString("dirstring", fDir.getAbsolutePath());
//		outState.putString("urlstrinold", fDirOld.getAbsolutePath());
//		Log.d("myLogs", "onSaveInstanceState");
	}
	
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		try{
		fDir = new File(savedInstanceState.getString("dirstring"));
//		fDirOld = new File(savedInstanceState.getString("urlstringold"));
		showFileView(fDir);
		}catch(NullPointerException ne){}
//		Log.d("myLogs", "onViewStateRestored");
	} 

	
	// fragment part of OptionsMenu ----------------------------------------
	@Override 
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.options_menu_frag, menu);
		menuPaste = menu.findItem(R.id.menu_paste);
		menuNew = menu.findItem(R.id.menu_new_folder);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	public void onPrepareOptionsMenu(Menu menu)
	{ 
        if(fileName.size() == 0){
			menuPaste.setVisible(false);
			menuNew.setVisible(true);
		}
		else {
			menuPaste.setVisible(true); 
			menuNew.setVisible(false);
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
				// paste
			case R.id.menu_paste:
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
			case R.id.menu_new_folder:
				operCode = CMD_NEW_DIR;
				SelectDialogFragment dialog = new SelectDialogFragment(DIALOG_NEWFOLDER, SelectDialogFragment.DIALOG_SD);
				dialog.show(getFragmentManager(), "delfiledialog"); 
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// END of OptionsMenu part ----------------------------------------------------
	
	// action when item is clicked
	public void onListItemClick(ListView l,View v, int position, long id){
		super.onListItemClick(l,v, position, id);
		mCallback.clearStatusBar();
//		Toast.makeText(getActivity(), "position = " + position, Toast.LENGTH_SHORT).show(); 
		File fDirNew = new File(fDir.getAbsolutePath() + "/" + fArrList.get(position));
		if(fDirNew.isDirectory()){   //if the clicked item is directory
			fDirOld = fDir;
			fDir = fDirNew;
			showFileView(fDir);
		}
		else {
			String extension = "";
			if ((fDir.getAbsolutePath() + "/" + fArrList.get(position)).contains(".")) 
				extension = (fDir.getAbsolutePath() + "/" + fArrList.get(position)).substring( (fDir.getAbsolutePath() + "/" + fArrList.get(position)).lastIndexOf("."));
			String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
			if (mimeType == null) mimeType = "*/*";
		// Toast.makeText(getBaseContext(), "uri for this file = " + mimeType, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(fDirNew), mimeType);
			try{
			startActivity(intent);
			}catch(Exception e){
				Toast.makeText(getActivity(),"No application found", Toast.LENGTH_SHORT).show();}
		}
	}
	public void goToBack(){
		if(fDir.getParent() == null) {
			Toast.makeText(getActivity(),"You are on top",Toast.LENGTH_LONG).show();
			return;
		}
		fDir = new File(fDir.getParent());
		showFileView(fDir);
	}
	
	// to cancel copy processes when application exits runs from Activity
	public void cancelProcessExit(){
		try{
			if (mCallback.isRunBkgrProcess())
	    		attActivity.stopService(new Intent(attActivity, FileCmdService.class));
		}catch(Exception e) {}
	}
	
	// to modify menu or exit when Activity exit button pressed
	public Boolean exitButtonPressed(){
		Boolean res = true; 
		if (menuPaste.isVisible()){
			pereverToz(false);
			res = false;
		}
		else {
			cancelProcessExit();
		}
		return res;
	}
	
	// true - sets paste button, false - restores buttons and clears copy data
	public void pereverToz(Boolean status){ 
			if(status){
			menuPaste.setVisible(true);
			menuNew.setVisible(false);
		}else{
			menuPaste.setVisible(false);
			menuNew.setVisible(true);
			clearCopyData();
		}
	}
	
	// clears data for copying process
	void clearCopyData(){
		fileName.clear();
		fromPath = "";
		operCode = 0;
	}
	
	// Paste method with FILE_EXISTS Dialogs ----------------------------------------------
	void startPaste(){
		String toPath = fDir.getAbsolutePath();
		for (int i = 0; i < fileName.size(); i++){
			File f = new File(toPath + "/" + fileName.get(i));
			if(f.exists() && !file2.contains(fileName.get(i))){
				SelectDialogFragment sdf = new SelectDialogFragment(DIALOG_FILE_EXISTS, fileName.get(i), SelectDialogFragment.DIALOG_SD);
				sdf.show(getFragmentManager(), "delfiledialog");				
				return;
			}
		}
		if(fileName.size() != 0){
			// EXECUTE CMD SERVICE PASTE ----------------------------------------------
			startCmdService(fileName, fromPath, toPath, operCode);
//			fc = new FileCmd();
//			fc.setData(attActivity, fileName, fromPath, toPath, operCode);
//			fc.execute();			
			file2.clear();
		}
		else {pereverToz(false);}
	}

	// DIALOG and other interface methods -----------------------------------------------------------------
	
	public void onDeleteDialogPositiveClick()
	{
		// EXECUTE CMD SERVICE DELETE ----------------------------------------------
		String toPath ="";
		startCmdService(fileName, fromPath, toPath, operCode);
//		fc = new FileCmd();
//		fc.setData(attActivity, fileName, fromPath, operCode);
//		fc.execute();
	}
	
	public void onDeleteDialogNegativeClick()
	{
		pereverToz(false);
	}

	public void onNewFolderDialogPositiveClick(String text)
	{
//		Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
		fromPath = fDir.getAbsolutePath();
		fileName.add("/" + text);
		// EXECUTE CMD SERVICE NEW FOLDER----------------------------------------------
		String toPath ="";
		startCmdService(fileName, fromPath, toPath, operCode);	
//		fc = new FileCmd();
//		fc.setData(attActivity, fileName, fromPath, operCode);
//		fc.execute();		
	}

	public void onRenameDialogPositiveClick(String text)
	{
		File f1 = new File(fromPath + "/" + fileName.get(0));
		File f2 = new File(fromPath + "/" + text);
		if(!f1.renameTo(f2)) Toast.makeText(getActivity(), R.string.msg_Error, Toast.LENGTH_SHORT).show();
		showFileView(fDir);
		pereverToz(false);
	}

	public void onRenameDialogNegativeClick()
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


	public void onFileCmdPostExecute()
	{
		showFileView(fDir);
		pereverToz(false);

	}

	public void onFileCmdCancel()
	{
		if (fc.getStatus().toString().equals("RUNNING")){
			fc.cancel(true);
		}
	}
	
	//TEST mount network drive method ----------------------------------------------
	
	private boolean mountDrive() {
//		String mountStr = "mount -t cifs //192.168.1.2/Maksim -o username=Maksim,password=odessa9 /mnt/sdcard0/netfolder";
		Runtime runtime = Runtime.getRuntime();  
		try {
			runtime.exec(new String[] {"su", "-c", "mount -t cifs -o username=Maksim, " +
						  "password=Odessa09 //192.168.1.2/Maksim /mnt/sdcard0/netfolder"});
//				runtime.exec(mountStr); //Выполняем системные команды
		} catch (IOException e) {
			if (e != null) return false;
			Toast.makeText(getActivity(), "mount error", Toast.LENGTH_SHORT).show();
			}
		Toast.makeText(getActivity(), "mount success", Toast.LENGTH_SHORT).show();	
    	return true;
	}
	
	private void startCmdService(ArrayList<String> name, String from, String to, int code){
		
		PendingIntent pi = getActivity().createPendingResult(1, new Intent(), 0);
		Intent intent = new Intent(getActivity(), FileCmdService.class).putStringArrayListExtra("filename", name)
		          .putExtra("from", from).putExtra("to", to).putExtra("code", code).putExtra("pendingIntent",pi);
		
		getActivity().startService(intent);
	}
	
	private void startCmdServiceNet(ArrayList<String> name, String from, String to, int code){

		PendingIntent pi = getActivity().createPendingResult(1, new Intent(), 0);
		Intent intent = new Intent(getActivity(), FileCmdService.class).putStringArrayListExtra("filename", name)
			.putExtra("from", from).putExtra("to", to).putExtra("code", code).putExtra("pendingIntent",pi)
			.putExtra("npa",SharPrefMan.getPrefString(getActivity(), SharPrefMan.SAVED_AUTH, ""));

		getActivity().startService(intent);
//		Toast.makeText(getActivity(), from + "/" + name.get(0) + "; " + to + " " + code, Toast.LENGTH_LONG).show();
	}

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data)
//	{
//		// TODO: Implement this method
//		super.onActivityResult(requestCode, resultCode, data);
//		if (resultCode == 100) {
//			Toast.makeText(getActivity(), "Process: " + requestCode + " has started", Toast.LENGTH_SHORT).show();
//			}
//		if (resultCode == 300) {
//			String result = data.getStringExtra("result");
//			Toast.makeText(getActivity(), "Process: " + requestCode + " has finished with " + result, Toast.LENGTH_SHORT).show();
//			onFileCmdPostExecute();
//		}
//	}	
	
}
