package com.makuzmin.apps.filemanager;
import java.io.*;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.app.*;
import android.os.*;
import android.net.*;
import android.util.*;
import android.content.*;

public class TestSDWriter
{
	Activity activity;
	File file;
	OutputStream outputStream;
	ParcelFileDescriptor pfd;
	
	public TestSDWriter(Activity activity, File file){
		this.activity = activity;
		this.file = file;
	}
	public OutputStream getOutputStream(){
        Uri uri = Uri.fromFile(file);
		if(!file.exists()) createFile();
		try {
//			pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE);
            pfd = activity.getContentResolver().openFileDescriptor(uri, "w");
            outputStream = new FileOutputStream(pfd.getFileDescriptor());
//        outputStream.write(("Overwritten by MyCloud at " +
//							   System.currentTimeMillis() + "\n").getBytes());
//        // Let the document provider know you're done by closing the stream.
//        
    		return outputStream;
        } catch (FileNotFoundException e) {
			Log.d("myLogs", "not found " + e.getMessage());
			e.printStackTrace(); return null;
		} catch (IOException e) {
			e.printStackTrace();
    		return null;
		}
	}
	
	public void createFile(){
		String fileName = file.getAbsolutePath();
	    int WRITE_REQUEST_CODE = 43;
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		// Create a file with the requested MIME type.
//		intent.setType(mimeType);
		intent.putExtra(Intent.EXTRA_TITLE, fileName);
		if(activity!=null)
		activity.startActivityForResult(intent, WRITE_REQUEST_CODE);
	}

	public void closeOutputStream(){
		try
		{
			outputStream.close();
			pfd.close();
		}
		catch (IOException e)
		{}      
	}
}
