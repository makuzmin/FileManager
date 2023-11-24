package com.makuzmin.apps.filemanager;
import java.io.*;
import android.os.*;
import android.widget.*;
import android.app.*;
import java.util.*;
//import android.util.*;
import java.util.concurrent.*;
import android.content.DialogInterface;
import android.content.DialogInterface.*;
import makuzmin.apps.filemanager.*;

public class FileCmd extends AsyncTask<Void, Void, Boolean>
{
	Activity activity;
	String fromPath = "";
	ArrayList<String> fileName;
	String toPath = "";
	int operCode = 0;
	ProgressDialog pd;
    Boolean status;	
	
	final int CMD_COPY = 1;
	final int CMD_MOVE = 2;
	final int CMD_NEW_DIR = 3;
	final int CMD_DELETE = 4;
	
	void connectActivity (Activity act){
		activity = act;
	}
	
	void disconnectActivity(){
		activity = null;
	}
	
	void setFromPath(String from){
		fromPath = from;
//		Log.d("myLogs", "from  = " + fromPath.get(0) + " class");
	}
	
	void setToPath(String to){
		toPath = to;
//		Log.d("myLogs", "to = " + toPath + " class");
	}
	
	void setOperCode(int code){
		operCode = code;
	}
	
	void setData(Activity act, ArrayList<String> name, String from, String to, int code){
		activity = act;
		fileName = name;
		fromPath = from;
		toPath = to;
		operCode = code;
	}
	
	void setData(Activity act, ArrayList<String> name, String from, int code){
		activity = act;
		fileName = name;
		fromPath = from;
		operCode = code;		
	}
	
	void resetVar(){
		fileName.clear();
		fromPath = "";
		toPath = "";
		operCode = 0;
	}

	@Override
	protected void onPreExecute(){
		super.onPreExecute();
		pd = new ProgressDialog(activity);
		pd.setTitle(R.string.msg_Process);
		pd.setMessage("Progress");
		pd.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					mListener.onFileCmdCancel();
				}
			});			
		pd.show(); 
		try {
			mListener = (FileCmdListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
	}
	
	@Override
	protected Boolean doInBackground(Void[] p1)
	{
		if(operCode == 0) return null;
//		Log.d("myLogs", "size  = " + fileName.size() + " class");
//		for(int i = 0; i < fileName.size(); i++){
//			Log.d("myLogs", "file  = " + fileName.get(i) + " class");	
			switch(operCode){
				case CMD_COPY:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null) | toPath.equals("")) return null;
						if(copy(fromPath + "/" + fileName.get(i), toPath + "/" + fileName.get(i))){
							status = true;
						}else{status = false;}
//					Log.d("myLogs", "status = " + status);
						if (isCancelled()) return null;	
					}
				break;
				case CMD_MOVE:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null) | toPath.equals("")) return null;
						if(move(fromPath + "/" + fileName.get(i), toPath + "/" + fileName.get(i))){
							status = true;
						}else{status = false;}
						if (isCancelled()) return null;
					}
				break;
				case CMD_NEW_DIR:
					if(fromPath.equals("") | fileName.equals(null)) return null;
					createDir(fromPath + "/" + fileName.get(0));
					status = true;
				break;
				case CMD_DELETE:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null)) return null;
						if(delete2(fromPath + "/" + fileName.get(i))) status = true;
						else status = false;
						if (isCancelled()) return null;
					}
					try {TimeUnit.SECONDS.sleep(1);}
					catch (InterruptedException e){}
				break;
			}
//			if (isCancelled()) return null;
//		}
		return status;
	}
	
	@Override
	protected void onProgressUpdate(Void[] values){
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onPostExecute(Boolean result){
		super.onPostExecute(result);
		pd.cancel();
//		activity.sAdapter.notifyDataSetChanged();
		resetVar();
		mListener.onFileCmdPostExecute();
		if(result){
	    	Toast.makeText(activity, R.string.msg_Success, Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(activity, R.string.msg_Error, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
    protected void onCancelled() {
		super.onCancelled();
		pd.cancel();
		resetVar();
		mListener.onFileCmdPostExecute();		
		Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show();
	}
	
	/*  Code copied from
	 http://forum.startandroid.ru/viewtopic.php?f=26&t=860
	 */

	// copy file or directory
	private boolean copy(String from, String to) {
		try {     
			File fFrom = new File(from);
			if (fFrom.isDirectory()) { // Если директория, копируем все ее содержимое
				createDir(to);
				String[] FilesList = fFrom.list();
				for (int i = 0; i < FilesList.length; i++){
					if (isCancelled()) return false;
					if (!copy(from + "/" + FilesList[i], to + "/" + FilesList[i]))
						return false; // Если при копировании произошла ошибка 
			    }
//				return true;
			} else if (fFrom.isFile()) { // Если файл просто копируем его
				File fTo = new File(to);
//				TestSDWriter tsw = new TestSDWriter(activity, fTo);
				test tst = new test(activity, fTo);
				OutputStream out = tst.write(1024);
				if(out == null) return false;
				InputStream in = new FileInputStream(fFrom); // Создаем потоки
//				OutputStream out = new FileOutputStream(fTo);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close(); // Закрываем потоки
				out.close();
			}
        } catch (FileNotFoundException ex) { // Обработка ошибок
			if(ex != null) return false;
        } catch (IOException e) { // Обработка ошибок
			if(e != null) return false;
        }
        return true; // При удачной операции возвращаем true
	}

	// create directory
	private static void createDir(String folder) {
		File f1 = new File(folder); //Создаем файловую переменную
		if (!f1.exists()) { //Если папка не существует
			f1.mkdirs(); //создаем её
		}
	}

	// delete file or directory
	private static boolean delete(String path) {
		File file = new File(path); //Создаем файловую переменную
		if (file.exists()) { //Если файл или директория существует
			String deleteCmd = "rm -r " + path; //Создаем текстовую командную строку
			Runtime runtime = Runtime.getRuntime();  
			try {
				runtime.exec(deleteCmd); //Выполняем системные команды
			} catch (IOException e) {
			if (e != null) return false;}
		}
		return true;
	}
	
	private boolean delete2(String path){
		File file = new File(path);
		if (file.exists()){
			if (file.isDirectory()){
				String[] FilesList = file.list();
				if (FilesList.length == 0 ) {
					if(!file.delete())  return false;
				} else {
					for (int i = 0; i < FilesList.length; i++){
						if (isCancelled()) return false;
						if (!delete2(path + "/" + FilesList[i])) return false;
					}
					if(!file.delete())  return false;
				}
			}else if (file.isFile()) {
				if (!file.delete()) return false;
			}
		}
		return true;
	}

	// move file or directory
	private boolean move(String from,String to) {
		try {      
			File fFrom = new File(from);
			if (fFrom.isDirectory()) { // Если директория, копируем все ее содержимое
				createDir(to);
				String[] FilesList = fFrom.list();
				for (int i = 0; i < FilesList.length; i++){
					if (isCancelled()) return false;
					if (!move(from + "/" + FilesList[i], to + "/" + FilesList[i]))
						return false; // Если при копировании произошла ошибка
				}
//				return true;
			} else if (fFrom.isFile()) { // Если файл просто копируем его
				File fTo = new File(to);
				InputStream in = new FileInputStream(fFrom); // Создаем потоки
				OutputStream out = new FileOutputStream(fTo);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close(); // Закрываем потоки
				out.close();
			}
        } catch (FileNotFoundException ex) { // Обработка ошибок
			if(ex != null) return false;
        } catch (IOException e) { // Обработка ошибок
			if(e != null) return false;
        }
		String deleteCmd = "rm -r " + from; //Создаем текстовую командную строку в которой удаляем начальный файл
		Runtime runtime = Runtime.getRuntime();  
		try
		{
			runtime.exec(deleteCmd); //Выполняем удаление с помощью команд
		}
		catch (IOException e)
		{}
        return true; // При удачной операции возвращаем true
	}
	
	
	public interface FileCmdListener {
		public void onFileCmdPostExecute();
		public void onFileCmdCancel();
	}

	// Use this instance of the interface to deliver action events
    FileCmdListener mListener;
	
}
