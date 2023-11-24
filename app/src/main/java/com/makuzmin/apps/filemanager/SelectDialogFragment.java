package com.makuzmin.apps.filemanager;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.view.*;

public class SelectDialogFragment extends DialogFragment
{
	final int DIALOG_FILE_INFO = 1;
	final static int DIALOG_DELETE = 2;
	final static int DIALOG_NEWFOLDER = 3;
	final static int DIALOG_RENAME = 4;
	final int DIALOG_FILE_EXISTS = 5;
	final static int DIALOG_AUTHORIZE = 6;
	final static int DIALOG_HOST = 7;

	final static int DIALOG_NET= 2;
	final static int DIALOG_SD = 1;
	
	LinearLayout view;
	EditText etNewFolder, etUser, etPassword, etDomain, etHost;
	int id;
	String txt;
	int type; // origin code, net or SD
	MainActivity act;
	LayoutInflater inflater;

	public SelectDialogFragment(int _id, int _type){
		id = _id;
		type = _type;
	}
	
	public SelectDialogFragment(int _id, String _txt, int _type){
		id = _id;
		txt = _txt;
		type = _type;
	}
	
	void linkActivity(MainActivity _act){
		act = _act;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	
		switch(id){
			case DIALOG_DELETE:
        	// Use the Builder class for convenient dialog construction
        	AlertDialog.Builder adb2 = new AlertDialog.Builder(getActivity());
        	adb2.setTitle("Delete File")
				.setMessage("Are you sure to delete " + txt + " item(s)?")
				.setPositiveButton(R.string.btn_Delete, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mListener.onDeleteDialogPositiveClick(SelectDialogFragment.this, type);
					}
				})
				.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mListener.onDeleteDialogNegativeClick(SelectDialogFragment.this, type);
						
						// User cancelled the dialog
					}
				});
        	// Create the AlertDialog object and return it
        	return adb2.create();		
			case DIALOG_NEWFOLDER:
				AlertDialog.Builder adb3 = new AlertDialog.Builder(getActivity());
				inflater = getActivity().getLayoutInflater();
				view = (LinearLayout) inflater.inflate(R.layout.newfolder, null);
				adb3.setTitle("New Folder Name")
					.setView(view)
					// Add action buttons
					.setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// sign in the user ..
							mListener.onNewFolderDialogPositiveClick(SelectDialogFragment.this, 
								etNewFolder.getText().toString().equals("") ? etNewFolder.getHint().toString() 
								:etNewFolder.getText().toString(), type);
					//		act.newFolderCreate(etNewFolder.getText().toString());
						}
					})
					.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							SelectDialogFragment.this.getDialog().cancel();
						}
					});      
				etNewFolder = (EditText) view.findViewById(R.id.etNewFolder);
				return adb3.create();
			case DIALOG_RENAME:
				AlertDialog.Builder adb4 = new AlertDialog.Builder(getActivity());
				inflater = getActivity().getLayoutInflater();
				view = (LinearLayout) inflater.inflate(R.layout.newfolder, null);
				adb4.setTitle("Change File Name")
					.setView(view)
					// Add action buttons
					.setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// sign in the user ...
							mListener.onRenameDialogPositiveClick(SelectDialogFragment.this, etNewFolder.getText().toString(), type);
							//		act.newFolderCreate(etNewFolder.getText().toString());
						}
					})
					.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							SelectDialogFragment.this.getDialog().cancel();
							mListener.onRenameDialogNegativeClick(SelectDialogFragment.this, type);
						}
					});      
				etNewFolder = (EditText) view.findViewById(R.id.etNewFolder);
				etNewFolder.setText(txt);
				etNewFolder.setHint("New Name");
				return adb4.create();
			case DIALOG_FILE_EXISTS:
				AlertDialog.Builder adb5 = new AlertDialog.Builder(getActivity());
				adb5.setTitle("File or Folder Exists!")
					.setMessage("The item " + txt + " exists. Overwrite it?")
					.setPositiveButton(R.string.btn_Yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mListener.onExistsDialogPositiveClick(SelectDialogFragment.this, txt, type);
						}
					})
					.setNegativeButton(R.string.btn_No, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mListener.onExistsDialogNegativeClick(SelectDialogFragment.this, txt, type);
							// User cancelled the dialog
						}
					});
				return adb5.create();	
			case DIALOG_AUTHORIZE:
				AlertDialog.Builder adb6 = new AlertDialog.Builder(getActivity());
				inflater = getActivity().getLayoutInflater();
				view = (LinearLayout) inflater.inflate(R.layout.authorize, null);
				adb6.setTitle("Enter Account Info")
					.setView(view)
					// Add action buttons
					.setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// sign in the user ...
							mListener.onAuthorizeDialogPositiveClick(SelectDialogFragment.this, etUser.getText().toString(),
	    						etPassword.getText().toString(), etDomain.getText().toString(), type);
							//		act.newFolderCreate(etNewFolder.getText().toString());
						}
					})
					.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							SelectDialogFragment.this.getDialog().cancel();
						}
					});      
				etUser = (EditText) view.findViewById(R.id.etUser);
				etPassword = (EditText) view.findViewById(R.id.etPassword);
				etDomain = (EditText) view.findViewById(R.id.etDomain);
				return adb6.create();
			case DIALOG_HOST:
				AlertDialog.Builder adb7 = new AlertDialog.Builder(getActivity());
				inflater = getActivity().getLayoutInflater();
				view = (LinearLayout) inflater.inflate(R.layout.host_dialog, null);
				adb7.setTitle("Enter Host Name, or Cancel to clear")
					.setView(view)
					// Add action buttons
					.setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// sign in the user ...
							mListener.onHostDialogClick(SelectDialogFragment.this, etHost.getText().toString(), type);
							//		act.newFolderCreate(etNewFolder.getText().toString());
						}
					})
					.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							mListener.onHostDialogClick(SelectDialogFragment.this, "", type);
						}
					});      
				etHost = (EditText) view.findViewById(R.id.etHost);
				return adb7.create();
		}
		return super.onCreateDialog(savedInstanceState);
	}	
	
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
	public interface SelectDialogListener {
		public void onDeleteDialogPositiveClick(DialogFragment dialog, int type);
		public void onDeleteDialogNegativeClick(DialogFragment dialog, int type);
		public void onNewFolderDialogPositiveClick(DialogFragment dialog, String text, int type);
		public void onRenameDialogPositiveClick(DialogFragment dialog, String text, int type);
		public void onRenameDialogNegativeClick(DialogFragment dialog, int type);
		public void onExistsDialogPositiveClick(DialogFragment dialog, String text, int type);
		public void onExistsDialogNegativeClick(DialogFragment dialog, String text, int type);
		public void onAuthorizeDialogPositiveClick(DialogFragment dialog, String user, String password, String domain, int type);
		public void onHostDialogClick(DialogFragment dialog, String host, int type);

	}
	
	// Use this instance of the interface to deliver action events
    SelectDialogListener mListener;	

    // Override the Fragment.onAttach() method to instantiate the SelectDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the SelectDialogListener so we can send events to the host
            mListener = (SelectDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
										 + " must implement SelectDialogListener");
        }
    }
	
/*	
	// dialog
	protected Dialog onCreateDialog(int id){
		String path = fDir.getAbsolutePath() + "/" + fList[dialogPos].toString();
		File f = new File(path);	
		switch (id){
			case DIALOG_FILE_INFO:
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				if(fList.length == 0) {
					adb.setTitle("empty folder");
					adb.setMessage("empty folder");
				} else{									
					adb.setTitle(path);
    				adb.setMessage(String.valueOf(f.length()));
				}
				adb.setNeutralButton(R.string.btn_Cancel, myClickListener);
				return adb.create();
			case DIALOG_DELETE:
				AlertDialog.Builder adb2 = new AlertDialog.Builder(this);
				adb2.setTitle(path);
				adb2.setMessage("");
				adb2.setPositiveButton("Yes", myClickListener);
				adb2.setNeutralButton(R.string.btn_Cancel, myClickListener);
				return adb2.create();
			default:
				break;
		}
		return super.onCreateDialog(id);
	}

	protected void onPrepareDialog(int id, Dialog dialog){
		super.onPrepareDialog(id, dialog);
		String path = fDir.getAbsolutePath() + "/" + fList[dialogPos].toString();
		File f = new File(path);		
		switch(id){
			case DIALOG_FILE_INFO:
				if(fList.length == 0) {
					((AlertDialog)dialog).setTitle("empty folder");
					((AlertDialog)dialog).setMessage("empty folder");
				} else{	
					((AlertDialog)dialog).setTitle(path);
					((AlertDialog)dialog).setMessage("Size: " + String.valueOf(f.length()/1024) + " Kbyte"
													 + "\n" + "Modified: " + String.valueOf(f.lastModified()));
				}
		    	break;
			case DIALOG_DELETE:
				((AlertDialog)dialog).setTitle(path);
				((AlertDialog)dialog).setMessage("Delete this item?");
				break;
			default:
				break;
		}
	}

	OnClickListener myClickListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    switch (which){
				case Dialog.BUTTON_NEUTRAL:
					dialog.cancel();
					pereverToz(false);
//					Toast.makeText(getBaseContext(),"cancel, dialog= " + dialog.hashCode(), Toast.LENGTH_SHORT).show();
					break;
				case Dialog.BUTTON_POSITIVE:
//					Toast.makeText(getBaseContext(),"delete, dialog= " + dialog.hashCode(), Toast.LENGTH_SHORT).show();
					fc = new FileCmd();
					fc.setData(MainActivity.this, fileName, fromPath, operCode);
					fc.execute();
			}
		}
	};    */
	
}
