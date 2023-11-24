package com.makuzmin.apps.filemanager;
import android.widget.*;
import android.content.*;
import java.util.*;
import java.io.*;
import android.graphics.drawable.*;
import android.content.pm.*;
import android.net.*;
import android.webkit.*;
import android.view.*;
import android.text.format.*;
import android.os.*;
import android.graphics.*;
import android.provider.DocumentsContract;
import android.graphics.drawable.BitmapDrawable;
import android.provider.*;

public class AdapterHelper
{
	final String ATTRIBUTE_FILE_NAME = "name";
	final String ATTRIBUTE_FILE_IMAGE = "image";
	final String ATTRIBUTE_FILE_DESCR = "description";
	
	ListView listView;
	final Context ctx;
	CancellationSignal signal;
	ArrayList<String> dirList;
//	String[] dirList;
	File dir;
	ArrayList<Map<String, Object>> fData;
	Map<String, Object> m;
	
//	AdapterHelper(Context _ctx, ListView _listview, String[] _list, File _dir){
//		ctx = _ctx;
//		listView = _listview;
//		dirList = _list;
//		dir = _dir;
//	}
	
	AdapterHelper(Context _ctx, ListView _listview, ArrayList<String> _list, File _dir){
		dirList = new ArrayList<String>();
		ctx = _ctx;
		listView = _listview;
		dirList = _list;
		dir = _dir;
		signal = new CancellationSignal();
	}
	
	SimpleAdapter sAdapter;	
	
	SimpleAdapter getAdapter(){	
		fData = new ArrayList<Map<String, Object>>(dirList.size());
	//	Map<String, Object> m;

		if(dirList.size() == 0) {
			m = new HashMap<String, Object>();
			m.put(ATTRIBUTE_FILE_NAME, "--- this folder is empty ---"); 
			fData.add(m); 
		} else {
			for (int i = 0; i < dirList.size(); i++) {
				m = new HashMap<String, Object>();
				String p = dir.getAbsolutePath() + "/" + dirList.get(i);
				File f = new File(p);
				String descr = "Modified: " + millsToDate(String.valueOf(f.lastModified()), "dd/MM/yyyy hh:mm:ss") + "; Size: " + String.valueOf(f.length()/1024) + " Kbyte";
				m.put(ATTRIBUTE_FILE_NAME, dirList.get(i));
				m.put(ATTRIBUTE_FILE_DESCR, descr);
				if(f.isDirectory()) m.put(ATTRIBUTE_FILE_IMAGE, ctx.getResources().getDrawable(R.drawable.ic_folder /*R.drawable.ic_action_collection*/));
				else m.put(ATTRIBUTE_FILE_IMAGE, /* ctx.getResources().getDrawable(R.drawable.ic_action_view_as_list)); */ getFileIcon(getFileMime(p), Uri.fromFile(f), p));
	//			m.put(ATTRIBUTE_FILE_IMAGE, new BitmapDrawable(ctx.getResources(), getImageThumbnail(ctx, f)));
				fData.add(m);
			}
		}
		String[] from = {ATTRIBUTE_FILE_NAME, ATTRIBUTE_FILE_IMAGE, ATTRIBUTE_FILE_DESCR};
		int[] to = {R.id.tvText, R.id.ivImg, R.id.tvDescr};
		sAdapter = new SimpleAdapter(ctx, fData, R.layout.item, from, to);
		sAdapter.setViewBinder(new MyViewBinder());
		return sAdapter;
	}
	
	public void addThumbs(){
		if(dirList.size() != 0) {
			for (int i = 0; i < dirList.size(); i++) {
				m = new HashMap<String, Object>();
				String p = dir.getAbsolutePath() + "/" + dirList.get(i);
				File f = new File(p);
				Bitmap b111 = ThumbManage.createThumbFromFile(p);
				if(b111 != null){
		    		String descr = "Modified: " + millsToDate(String.valueOf(f.lastModified()), "dd/MM/yyyy hh:mm:ss") + "; Size: " + String.valueOf(f.length()/1024) + " Kbyte";
		     		m.put(ATTRIBUTE_FILE_NAME, dirList.get(i));
		    		m.put(ATTRIBUTE_FILE_DESCR, descr);
		    		m.put(ATTRIBUTE_FILE_IMAGE, new BitmapDrawable(ctx.getResources(), b111));
		    		fData.set(i,m);
				}
			}
		}
	}
	
	private Drawable getFileIcon(String mime, Uri fileUri, String path){
		final Intent innt = new Intent(Intent.ACTION_VIEW);
	//	final CharSequence label;
	//	innt.setData(fileUri);
	//	innt.setType(mime);
	
	//  Bitmap b111 = ThumbManage.createThumbFromFile(path);
	//	if(b111 == null){
		innt.setDataAndType(fileUri, mime);
        
		final List<ResolveInfo> matches = ctx.getPackageManager().queryIntentActivities(innt, 0);
		if (matches.size() != 0 && !mime.equals("*/*")) return matches.get(0).loadIcon(ctx.getPackageManager());
		else return ctx.getResources().getDrawable(R.drawable.ic_file);
	//	{label = matches.get(0).loadLabel(ctx.getPackageManager());		
	//	return ("size = " + String.valueOf(matches.size()) + ", application = " + label.toString());}
	//	else return ("size = " + String.valueOf(matches.size()));
	//	}else return new BitmapDrawable(ctx.getResources(), b111);
	}
	
	private String getFileMime(String path){

		String extension = "";
		if ((path).contains(".")) 
			extension = (path).substring( (path).lastIndexOf("."));
		String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
		if (mimeType == null) mimeType = "*/*";
		return mimeType;
	}
	
	class MyViewBinder implements SimpleAdapter.ViewBinder {

/*		int red = getResources().getColor(R.color.Red);
		int orange = getResources().getColor(R.color.Orange);
		int green = getResources().getColor(R.color.Green); */

		@Override
		public boolean setViewValue(View view, Object data,
									String textRepresentation) {
			
//			int i = 0;
			switch (view.getId()) {
					// LinearLayout
/*				case R.id.llLoad:
					i = ((Integer) data).intValue();
					if (i < 40) view.setBackgroundColor(green); else 
					if (i < 70) view.setBackgroundColor(orange); else
						view.setBackgroundColor(red);
					return true; */
					// ProgressBar  
				case R.id.ivImg:
//					i = ((Integer) data).intValue();
					Drawable dr = ((Drawable) data);
//					((ProgressBar)view).setProgress(i);
					((ImageView)view).setImageDrawable(dr);
					return true;
			}
			return false;
		}
	}
	
	private String millsToDate(String mills, String dateFormat){
		return DateFormat.format(dateFormat, Long.parseLong(mills)).toString();
	}
	
	private Bitmap getFileThumbnail(Context ctx, File f, CancellationSignal signal){
		final ContentResolver resolver = ctx.getContentResolver();
		Uri uri = Uri.fromFile(f);
		Point pnt = new Point();
		pnt.set(50,50);
		return DocumentsContract.getDocumentThumbnail(resolver, uri, pnt, signal);
	}
	public static Bitmap getImageThumbnail(Context context, File f){ 
		final ContentResolver resolver = context.getContentResolver();
		long id;
		Uri uri = Uri.fromFile(f);
		try {
			if (/*UIUtils.hasKitKat() && */ DocumentsContract.isDocumentUri(context, uri)) {
				String wholeID = DocumentsContract.getDocumentId(uri);
				// Split at colon, use second item in the array
				id = Long.parseLong(wholeID.split(":")[1]);
			}
			else if (isMediaUri(uri)){
				id = ContentUris.parseId(uri);
			}
			else return null;
			return MediaStore.Images.Thumbnails.getThumbnail(
                resolver,
                id,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null);
		} 
		catch (Exception e) {
		//	if (DEBUG) Log.e(TAG, "getThumbnail", e);
			return null;
		}
	}
	public static boolean isMediaUri(Uri uri) {
		return "media".equalsIgnoreCase(uri.getAuthority());
	}
}
