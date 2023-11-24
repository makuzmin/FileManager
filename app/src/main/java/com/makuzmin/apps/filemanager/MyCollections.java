package com.makuzmin.apps.filemanager;
import java.util.*;

public class MyCollections
{
	//public static final int ASCENDING = 1;
	//public static final int DESCENDING = 2;
	
	public static final int FILE_NAME = 1;
	public static final int FILE_MODIFIED = 2;
	public static final int FILE_LENGTH = 3;
	
	public static void sort(List<FileListData> f, int param, Boolean ascending){
		Comparator<FileListData> comp = FileListData.FileNameAComparator;
		switch(param){
			case FILE_NAME:
				comp = ascending == true ? FileListData.FileNameAComparator : FileListData.FileNameDComparator;
				break;
			case FILE_MODIFIED:
				comp = ascending == true ? FileListData.FileModifiedAComparator : FileListData.FileModifiedDComparator;
				break;
			case FILE_LENGTH:
				comp = ascending == true ? FileListData.FileLengthAComparator : FileListData.FileLengthDComparator;
				break;
		}
		Collections.sort(f, comp);
		Collections.sort(f, FileListData.FileDirComparator);
	}
	
	
}
