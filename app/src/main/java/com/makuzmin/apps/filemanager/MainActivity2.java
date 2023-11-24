package com.makuzmin.apps.filemanager;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.ContextMenu.*;
import android.webkit.*;
import android.widget.*;
import android.widget.AdapterView.*;
import java.io.*;
import java.util.*;
import android.view.Window.*;
import android.content.DialogInterface.OnClickListener;
//import android.util.*;
import android.widget.AbsListView.*;
import android.support.v4.app.*;

public class MainActivity2 extends FragmentActivity 
{

	
	
	ListView lvMain;
	AdapterHelper ah;
	SimpleAdapter sAdapter;
	File fDir;
	File fDirOld;
	String[] fList;
	TextView tvPath;
	ArrayList<String> fileName;
//	int i111; //for test code
	int dialogPos;
	MenuItem menuPaste, menuNew, menuRename, menuSend, menuInfo;
	EditText etNewFolder;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		//tvPath = (TextView) findViewById(R.id.tvPath);
		//lvMain = (ListView) findViewById(R.id.lvMain);
		
		registerForContextMenu(lvMain); //context menu element
		
		fDir = Environment.getExternalStorageDirectory();
//		showFileView(fDir);

		// action when item is clicked
		lvMain.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
//				if(fList.length == 0) return;
				File fDirNew = new File(fDir.getAbsolutePath() + "/" + fList[position]);
				if(fDirNew.isDirectory()){   //if the clicked item is directory
					fDirOld = fDir;
					fDir = fDirNew;
//		   		    showFileView(fDir);
				}
				else {
					String extension = "";
					if ((fDir.getAbsolutePath() + "/" + fList[position]).contains(".")) 
						extension = (fDir.getAbsolutePath() + "/" + fList[position]).substring( (fDir.getAbsolutePath() + "/" + fList[position]).lastIndexOf("."));
					String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
					String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
					if (mimeType == null) mimeType = "*/*";
					// Toast.makeText(getBaseContext(), "uri for this file = " + mimeType, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(fDirNew), mimeType);
					startActivity(intent);
				}
			}
		});
		fileName = new ArrayList<String>();	
    }

	// --- END OF onCreate ---
		
	// options menu 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
			menuPaste = menu.findItem(R.id.menu_paste);
			menuNew = menu.findItem(R.id.menu_new_folder);
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onPrepareOptionsMenu(Menu menu)
	{ 
        if(fileName.size() == 0){
			menuPaste.setVisible(false);
			menuNew.setVisible(true);
		}
		else {
			menuPaste.setVisible(true); 
			menuNew.setVisible(false);
		}
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
    }
	
	// on back pressed event
	@Override
	public void onBackPressed(){
/*		try{
			fc.cancel(true);
		}catch(NullPointerException e) {} */
		if(fDir.getParent() == null) {
			Toast.makeText(getBaseContext(),"You are on top",Toast.LENGTH_LONG).show();
			return;
		}
		fDir = new File(fDir.getParent());
//		showFileView(fDir);
	} 
			
/*	public void newFolderCreate(String text){
		//Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
		fromPath = fDir.getAbsolutePath();
		fileName.add("/" + text);
		fc = new FileCmd();
		fc.setData(MainActivity.this, fileName, fromPath, operCode);
		fc.execute();
	}
	*/


	
	
// TEST CODE
	
/*    public void alertFileExistDialog(){
		
		AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
		alert.setTitle("file exists");
		alert.setMessage("would you like to overwrite it?");
		alert.setButton("No", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					fileName.remove(i111);
					startPaste();
					Log.d("myLogs", String.valueOf(i111));
				}
			});
		alert.setButton2("Yes", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					file2.add(fileName.get(i111));
					startPaste();
					Log.d("myLogs", "yes pressed");	
        		}
			});
		alert.show();
	}
	
	void startPaste(){
		String toPath = fDir.getAbsolutePath();
		for (int i = 0; i < fileName.size(); i++){
			Log.d("myLogs",String.valueOf(file2.contains(fileName.get(i)) ));	
			File f = new File(toPath + fileName.get(i));
			if(f.exists() && !file2.contains(fileName.get(i))){
				SelectDialogFragment sdf = new SelectDialogFragment(DIALOG_FILE_EXISTS, fileName.get(i));
				sdf.show(getFragmentManager(), "delfiledialog");				
//				Log.d("myLogs", "file exists");
//				alertFileExistDialog();
				return;
			}
		}
		for (int i = 0; i < fileName.size(); i++){
			Log.d("myLogs", "copy - fileName" + fileName.get(i));
		}
		for (int i = 0; i < file2.size(); i++){
			Log.d("myLogs", "copy - file2" + file2.get(i));
		}
		if(fileName.size() != 0){
		fc = new FileCmd();
		fc.setData(MainActivity.this, fileName, fromPath, toPath, operCode);
		fc.execute();
		file2.clear();
		}
		else {pereverToz(false);}
	} */
	// END OF TEST CODE
}
