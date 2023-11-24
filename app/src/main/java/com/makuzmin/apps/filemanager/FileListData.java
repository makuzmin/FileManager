package com.makuzmin.apps.filemanager;

import java.util.Comparator;

public class FileListData
{

	public final String name;
	public final Long modified;
	public final Long length;
	public final Boolean directory;
	public final Boolean hidden;

	public FileListData(String name, Long modified, Long length, Boolean directory, Boolean hidden){
		super();
		this.name = name;
		this.modified = modified;
		this.length = length;
		this.directory = directory;
		this.hidden = hidden;
	}

	public static Comparator<FileListData> FileNameAComparator
	= new Comparator<FileListData>() {

		public int compare(FileListData file1, FileListData file2) {

			String fileName1 = file1.getName().toUpperCase();
			String fileName2 = file2.getName().toUpperCase();

			//ascending order
			return fileName1.compareTo(fileName2);

			//descending order
			//return fileName2.compareTo(fileName1);
		}

	};
	
	public static Comparator<FileListData> FileNameDComparator
	= new Comparator<FileListData>() {

		public int compare(FileListData file1, FileListData file2) {

			String fileName1 = file1.getName().toUpperCase();
			String fileName2 = file2.getName().toUpperCase();

			//ascending order
			//return fileName1.compareTo(fileName2);

			//descending order
			return fileName2.compareTo(fileName1);
		}

	};
	
	public static Comparator<FileListData> FileModifiedAComparator
	= new Comparator<FileListData>() {

		public int compare(FileListData file1, FileListData file2) {

			Long fileModif1 = file1.getModified();
			Long fileModif2 = file2.getModified();

			//ascending order
			return fileModif1.compareTo(fileModif2);

			//descending order
			//return fileModif2.compareTo(fileModif1);
		}

	};
	
	public static Comparator<FileListData> FileModifiedDComparator
	= new Comparator<FileListData>() {

		public int compare(FileListData file1, FileListData file2) {

			Long fileModif1 = file1.getModified();
			Long fileModif2 = file2.getModified();

			//ascending order
			//return fileModif1.compareTo(fileModif2);

			//descending order
			return fileModif2.compareTo(fileModif1);
		}

	};
	
	public static Comparator<FileListData> FileLengthAComparator
	= new Comparator<FileListData>() {

		public int compare(FileListData file1, FileListData file2) {

			Long fileLength1 = file1.getLength();
			Long fileLength2 = file2.getLength();

			//ascending order
			return fileLength1.compareTo(fileLength2);

			//descending order
			//return fileLength2.compareTo(fileLength1);
		}

	};

	public static Comparator<FileListData> FileLengthDComparator
	= new Comparator<FileListData>() {

		public int compare(FileListData file1, FileListData file2) {

			Long fileLength1 = file1.getLength();
			Long fileLength2 = file2.getLength();

			//ascending order
			//return fileLength1.compareTo(fileLength2);

			//descending order
			return fileLength2.compareTo(fileLength1);
		}

	};
	
	public static Comparator<FileListData> FileDirComparator
	= new Comparator<FileListData>() {

		public int compare(FileListData file1, FileListData file2) {

			Boolean fileDir1 = file1.getDirectory();
			Boolean fileDir2 = file2.getDirectory();

			//dir second
			//return fileDir1.compareTo(fileDir2);

			//dir first
			return fileDir2.compareTo(fileDir1);
		}

	};
	
	public String getName(){
		return name;
	}

	public Long getModified(){
		return modified;
	}

	public Long getLength(){
		return length;
	}

	public Boolean getDirectory(){
		return directory;
	}
}
