package com.makuzmin.apps.filemanager;
import android.app.*;
import android.os.*;
import android.content.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import android.app.PendingIntent.*;
import jcifs.smb.*;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbFileInputStream;
import java.net.*;
import makuzmin.apps.filemanager.*;
import android.util.*;

public class FileCmdService extends Service
{	
	final static int CMD_COPY = 1;
	final static int CMD_MOVE = 2;
	final static int CMD_NEW_DIR = 3;
	final static int CMD_DELETE = 4;
	final static int CMD_COPY_FLASH = 10;
	final static int CMD_NEW_DIR_NET = 30;
	final static int CMD_DELETE_NET = 40;
	final static int CMD_COPY_NET = 70;

	BroadcastReceiver br;
	public Boolean callCancel = false;
	
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try{
    		unregisterReceiver(br);
		} catch (RuntimeException ne){}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	final int stId = startId;
	DoCmd dc;
		try{		
		Bundle extras = intent.getExtras();
		if(extras != null){
     		ArrayList<String> fileName = intent.getStringArrayListExtra("filename");
    		String from = intent.getStringExtra("from");
			String to = intent.getStringExtra("to");
    		int code = intent.getIntExtra("code", 0);
    		PendingIntent pi = intent.getParcelableExtra("pendingIntent");
    		//add code checking if parameters are not null !!!
			if(code >= 10){
				String npaString = intent.getStringExtra("npa");
				dc = new DoCmd(fileName, from, to, code, pi, startId, npaString);
			} else {dc = new DoCmd(fileName, from, to, code, pi, startId);}
    		new Thread(dc).start();
			
			// create BroadcastReceiver
			br = new BroadcastReceiver() {
				// receive Cancel call
				public void onReceive(Context context, Intent intent) {
					String status = intent.getStringExtra("status");
					int id = intent.getIntExtra("startId", 0);
					if (status.equals("cancel") /*&& id == stId*/) callCancel = true;
					else callCancel = false;
				}
			};

			// create filter for BroadcastReceiver
			IntentFilter intFilt = new IntentFilter("com.makuzmin.apps.filemanager.cancelcall");
			// register BroadcastReceiver
			registerReceiver(br, intFilt);
		}
		} catch(NullPointerException ne){
			stopSelf(startId);
		}
		return super.onStartCommand(intent, flags, startId);
//		return START_NOT_STICKY;
	}
	
	class DoCmd implements Runnable
	{
		
		Activity activity;
		String fromPath = "";
		ArrayList<String> fileName;
		String toPath = "";
		int operCode = 0;
		Boolean status;
		NotificationManager nm;
		PendingIntent pi;
		int startId;
		int count; //count processed elements
		int countByte; //count processed bytes
		NtlmPasswordAuthentication npa;
		
		String errString;
		
	
		
		DoCmd (ArrayList<String> name, String from, String to, int code, PendingIntent pi, int startId){
			fileName = name;
			fromPath = from;
			toPath = to;
			operCode = code;
			this.pi = pi;
			this.startId = startId;
			
		}
		
		DoCmd (ArrayList<String> name, String from, String to, int code, PendingIntent pi, int startId, String npaString){
			fileName = name;
			fromPath = from;
			toPath = to;
			operCode = code;
			this.pi = pi;
			this.startId = startId;
			npa = new NtlmPasswordAuthentication(npaString);

		}

		@Override
		public void run()
		{
//			nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
			try
			{	
			    Intent intent = new Intent().putExtra("startId", startId);
			    pi.send(FileCmdService.this, 100, intent);
			
    			String result = "";
    			if(doAction()) result = "success";
	    		else if(callCancel) result = "cancelled";
//				else result = "error";
                else result = "error: " + errString;
//				doAction();
//				result = errString;
    			
    			intent = new Intent().putExtra("result", result);
			
				pi.send(FileCmdService.this, 300, intent);
			}
			catch (PendingIntent.CanceledException e)
			{}

			
//			Notification notif = new Notification(R.drawable.ic_launcher, "Text in status bar", System.currentTimeMillis());	
//			notif.setLatestEventInfo(this, "Notification's title", String.valueOf(doAction()), null);
//			notif.flags |= Notification.FLAG_AUTO_CANCEL;
//			nm.notify(1, notif);
			
			stopSelf(startId);
		}
		
		public void sendCount(){
			Intent intent = new Intent().putExtra("count", count++);
			try
			{
				pi.send(FileCmdService.this, 150, intent);
			}
			catch (PendingIntent.CanceledException e)
			{}
		}
		
		public void sendByte(){
			Intent intent = new Intent().putExtra("byte", countByte);
			try
			{
				pi.send(FileCmdService.this, 160, intent);
			}
			catch (PendingIntent.CanceledException e)
			{}
		}
		
		protected Boolean doAction(){
			if(operCode == 0) return null;
			count = 1;
//		Log.d("myLogs", "size  = " + fileName.size() + " class");
//		for(int i = 0; i < fileName.size(); i++){
//			Log.d("myLogs", "file  = " + fileName.get(i) + " class");	
			switch(operCode){
				case CMD_COPY:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null) | toPath.equals("")) return false;
						if(copy(fromPath + "/" + fileName.get(i), toPath + "/" + fileName.get(i))){
							status = true;
						}else{status = false;}
//					Log.d("myLogs", "status = " + status);
						if (isCancelled()) return false;	
					}
					break;
				case CMD_MOVE:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null) | toPath.equals("")) return false;
						if(move(fromPath + "/" + fileName.get(i), toPath + "/" + fileName.get(i))){
							status = true;
						}else{status = false;}
						if (isCancelled()) return false;
					}
					break;
				case CMD_NEW_DIR:
					if(fromPath.equals("") | fileName.equals(null)) return false;
					createDir(fromPath + "/" + fileName.get(0));
					status = true;
					break;
				case CMD_NEW_DIR_NET:
					if(fromPath.equals("") | fileName.equals(null)) return false;
					if(createNetDir(fromPath + "/" + fileName.get(0) + "/", npa))
					status = true;
					else status = false;
					break;	
				case CMD_DELETE:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null)) return false;
						if(delete2(fromPath + "/" + fileName.get(i))) status = true;
						else status = false;
						if (isCancelled()) return false;
					}
					try {TimeUnit.SECONDS.sleep(1);}
					catch (InterruptedException e){}
					break;
				case CMD_DELETE_NET:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null)) return false;
						if(deleteNet(fromPath + "/" + fileName.get(i) + "/", npa)) status = true;
						else status = false;
						if (isCancelled()) return false;
					}
					try {TimeUnit.SECONDS.sleep(1);}
					catch (InterruptedException e){}
					break;
				case CMD_COPY_FLASH:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null) | toPath.equals("")) return false;
						if(copyToFlash(fromPath + "/" + fileName.get(i) + "/", toPath + "/" + fileName.get(i), npa)){
							status = true;
						}else{status = false;}
						if (isCancelled()) return false;	
					}
					break;
				case CMD_COPY_NET:
					for(int i = 0; i < fileName.size(); i++){	
						if(fromPath.equals("") | fileName.equals(null) | toPath.equals("")) return false;
						if(toPath.equals("error")) return false;
						if(copyToNet(fromPath + "/" + fileName.get(i), toPath + "/" + fileName.get(i) +"/", npa)){
							status = true;
						}else{status = false;}
						if (isCancelled()) return false;	
					}
					break;
			}
//			if (isCancelled()) return null;
//		}
			return status;
		}

		//no longer a dummy method
		private boolean isCancelled()
		{
			if (callCancel) return true;
			return false;
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
					InputStream in = new FileInputStream(fFrom); // Создаем потоки
//					test tst = new test(getBaseContext(), fTo);
					OutputStream out = new FileOutputStream(fTo);
//					OutputStream out = tst.write(1024);
					byte[] buf = new byte[1024];
					int len;
					countByte = 0;
//					int cBytetemp = 0;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
						countByte++;
						if(countByte % 100 == 0) 
							{
								sendByte();
								if (isCancelled()) 
									{
										delete2(to);
										return false;
									}
							}
					}
					in.close(); // Закрываем потоки
					out.close();
					sendCount();
				}
			} catch (FileNotFoundException ex) { // Обработка ошибок
				if(ex != null) {errString = ex.getMessage(); return false;}
			} catch (IOException e) { // Обработка ошибок
				if(e != null) {errString = e.getMessage(); return false;}
			}
			return true; // При удачной операции возвращаем true
		}

		// create directory
		private void createDir(String folder) {
			File f1 = new File(folder); //Создаем файловую переменную
	/*		test tst = new test(getBaseContext(), f1);
			try
			{
				tst.mkdir();
			}
			catch (IOException e)
			{Log.d("myLogs", "mkdir error " + e.getMessage());} */
			if (!f1.exists()) { //Если папка не существует
				f1.mkdirs(); //создаем её
			}
		}

		// delete file or directory
		private boolean delete(String path) {
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
					sendCount();
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
					countByte = 0;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
						countByte++;
						if(countByte % 100 == 0) 
							{
				    			sendByte();
				    			if (isCancelled()) 
		    					{
				    				delete2(to);
					    			return false;
							}
						}
					}
					in.close(); // Закрываем потоки
					out.close();
//					sendCount();
				}
			} catch (FileNotFoundException ex) { // Обработка ошибок
				if(ex != null) {errString = ex.getMessage(); return false;}
			} catch (IOException e) { // Обработка ошибок
				if(e != null) {errString = e.getMessage(); return false;}
			}
			
			if(!delete2(from)) return false;
			
//			String deleteCmd = "rm -r " + from; //Создаем текстовую командную строку в которой удаляем начальный файл
//			Runtime runtime = Runtime.getRuntime();  
//			try
//			{
//				runtime.exec(deleteCmd); //Выполняем удаление с помощью команд
//			}
//			catch (IOException e)
//			{}
			return true; // При удачной операции возвращаем true
		}
		
		//-----------------NETWORK METHODS-----------------------------------------
		//copy from network to flash
		// copy file or directory
		//  add propper exception catch for SmbMethods!!!!!!!!!!!!!!!!!!!!!!!!!!!
		private boolean copyToFlash(String from, String to, NtlmPasswordAuthentication auth) {
			try {     
				SmbFile fFrom = new SmbFile(from, auth);
				if (fFrom.isDirectory()) { // If directory, copy all content
					createDir(to);
					String[] FilesList = fFrom.list();
					for (int i = 0; i < FilesList.length; i++){
						if (isCancelled()) return false;
						if (!copyToFlash(from + "/" + FilesList[i] + "/", to + "/" + FilesList[i], auth))
							return false; // Если при копировании произошла ошибка 
					}
//				return true;
				} else if (fFrom.isFile()) { // Если файл просто копируем его
					File fTo = new File(to);
					InputStream in = new SmbFileInputStream(fFrom); // Создаем потоки
					OutputStream out = new FileOutputStream(fTo);
					byte[] buf = new byte[1024];
					int len;
					countByte = 0;
//					int cBytetemp = 0;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
						countByte++;
						if(countByte % 100 == 0) 
						{
							sendByte();
							if (isCancelled()) 
							{
								delete2(to);
								return false;
							}
						}
					}
					in.close(); // Закрываем потоки
					out.close();
					sendCount();
				}
			} catch (FileNotFoundException ex) { // Обработка ошибок
				if(ex != null) {errString = ex.getMessage();return false;}
			} catch (IOException e) { // Обработка ошибок
				if(e != null) {errString = e.getMessage();return false;}
//			} catch (SmbException e){
//				if(e != null) return false;
//			}catch (MalformedURLException e)
//			{if(e != null) return false
			}
			return true; // При удачной операции возвращаем true
		}
		
		//copy from flash to network
		// copy file or directory
		//  add propper exception catch for SmbMethods!!!!!!!!!!!!!!!!!!!!!!!!!!!
		private boolean copyToNet(String from, String to, NtlmPasswordAuthentication auth) {
			try {     
				File fFrom = new File(from);
				if (fFrom.isDirectory()) { // If directory, copy all content
					createNetDir(to, auth);
					String[] FilesList = fFrom.list();
					for (int i = 0; i < FilesList.length; i++){
						if (isCancelled()) return false;
						if (!copyToNet(from + "/" + FilesList[i], to + "/" + FilesList[i] + "/", auth))
							return false; // Если при копировании произошла ошибка 
					}
//				return true;
				} else if (fFrom.isFile()) { // Если файл просто копируем его
					SmbFile fTo = new SmbFile(to, auth);
					InputStream in = new FileInputStream(fFrom); // Создаем потоки
					OutputStream out = new SmbFileOutputStream(fTo);
					byte[] buf = new byte[1024];
					int len;
					countByte = 0;
//					int cBytetemp = 0;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
						countByte++;
						if(countByte % 100 == 0) 
						{
							sendByte();
							if (isCancelled()) 
							{
								deleteNet(to, auth);
								return false;
							}
						}
					}
					in.close(); // Закрываем потоки
					out.close();
					sendCount();
				}
			} catch (FileNotFoundException ex) { // Обработка ошибок
//				if(ex != null) 
    			errString = ex.getMessage();
    			return false;
			} catch (IOException e) { // Обработка ошибок
//				 if(e != null) 
    			errString = e.getMessage();
    			return false;
//			} catch (SmbException e){
//				if(e != null) return false;
//			}catch (MalformedURLException e)
//			{if(e != null) return false
			}
			return true; // При удачной операции возвращаем true
		}

		
		// create network directory
		private boolean createNetDir(String folder, NtlmPasswordAuthentication auth) {
			try
			{
				SmbFile f1 = new SmbFile(folder, auth); //create SmbFile variable
				try
				{
					if (!f1.exists()) f1.mkdirs();
					else {
						errString = "folder already exists";
						return false;
					}
					
				} 
				catch (SmbException e) {
					errString = e.getMessage();
					return false;
				}
			} 
			catch (MalformedURLException e) {
				errString = e.getMessage();
				return false;
			}
			return true;
		}
		
		// delete file on network
		private boolean deleteNet(String path, NtlmPasswordAuthentication auth){
			try
			{
				SmbFile file = new SmbFile(path, auth);
				if (file.exists())
				{
					if (file.isDirectory())
					{
						String[] FilesList = file.list();
						if (FilesList.length == 0)
						{
							file.delete();
						} else {
							for (int i = 0; i < FilesList.length; i++)
							{
								if (isCancelled()) return false;
								if (!deleteNet(path + "/" + FilesList[i] + "/", auth)) return false;
							}
							file.delete();
						}
					} else if (file.isFile()) {
						file.delete();
						sendCount();
					}
				}
			} catch (SmbException e) {errString = e.getMessage(); return false;}
			catch (MalformedURLException e) {errString = e.getMessage(); return false;}
			return true;
		}
		
	}
	
}
