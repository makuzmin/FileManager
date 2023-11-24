package com.makuzmin.apps.filemanager;
import android.graphics.*;

public class ThumbManage
{
	
	public static final int THUMB_WIDTH = 50; //96;
	public static final int THUMB_HEIGHT = 50; //96;
	
	public static Bitmap createThumbFromFile(String path){
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		options.inSampleSize = calculateInSampleSize(options, THUMB_WIDTH, THUMB_HEIGHT);
		
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if(height > reqHeight || width > reqWidth){
			
			final int halfHeight = height/2;
			final int halfWidth = width/2;
			
			while ((halfHeight/inSampleSize) > reqHeight
			    && (halfWidth/inSampleSize) > reqWidth){
			   inSampleSize *= 2;
			}
		}
		
		return inSampleSize;
	}
}
