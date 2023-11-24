package com.makuzmin.apps.filemanager;
import android.view.*;
import android.app.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import android.net.nsd.*;
import android.content.*;



public class FragmentInfo extends ListFragment {
	
	Activity prntActivity;
	ListView lvNetDiscover;
	ArrayList<String> listDiscover;
	NsdHelper mNsdMan;
	Handler h;
	ArrayAdapter<String> adapter; 

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragmentinfo, null);

		return v;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		prntActivity = activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		lvNetDiscover = getListView();
//		mNsdMan = new NsdHelper();
		listDiscover = new ArrayList<String>();

//		mNsdMan.initializeNsd();

//		mNsdMan.discoverServices();
		
		adapter = new ArrayAdapter<String>(prntActivity,
			android.R.layout.simple_list_item_1, listDiscover);
		setListAdapter(adapter);
		
//		h = new Handler(){
//			public void handleMessage(android.os.Message msg) {
//				
//        		ArrayAdapter<String> adapter = new ArrayAdapter<String>(prntActivity,
//        			android.R.layout.simple_list_item_1, listDiscover);
//        		setListAdapter(adapter);
//				}
//		};
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mNsdMan != null)
			mNsdMan.stopDiscovery();
	}
	
	public class NsdHelper {

		NsdManager mNsdManager;
		NsdManager.ResolveListener mResolveListener;
		NsdManager.DiscoveryListener mDiscoveryListener;
		NsdManager.RegistrationListener mRegistrationListener;

		public static final String SERVICE_TYPE = "_http._tcp.";

		public static final String TAG = "myLogs";
//		public String mServiceName = "NsdChat";

		NsdServiceInfo mService;

		public NsdHelper() {
			mNsdManager = (NsdManager) prntActivity.getSystemService(Context.NSD_SERVICE);
		}

		public void initializeNsd() {
			initializeResolveListener();
			initializeDiscoveryListener();

		}

		public void initializeDiscoveryListener() {
			mDiscoveryListener = new NsdManager.DiscoveryListener() {

				@Override
				public void onDiscoveryStarted(String regType) {
//					Log.d(TAG, "Service discovery started");
				}

				@Override
				public void onServiceFound(NsdServiceInfo service) {
//					Log.d(TAG, "Service discovery success; " + service);
					listDiscover.add(service.toString());
					adapter.notifyDataSetChanged();
//					h.sendEmptyMessage(1);
//					mNsdManager.resolveService(service, mResolveListener);
//					if (!service.getServiceType().equals(SERVICE_TYPE)) {
//						Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
//					} else if (service.getServiceName().equals(mServiceName)) {
//						Log.d(TAG, "Same machine: " + mServiceName);
//					} else if (service.getServiceName().contains(mServiceName)){
//						mNsdManager.resolveService(service, mResolveListener);
//					}
				}

				@Override
				public void onServiceLost(NsdServiceInfo service) {
					try{
		    			listDiscover.remove(service.toString());
					}catch(ArrayIndexOutOfBoundsException e){}
					adapter.notifyDataSetChanged();
					
//					Log.e(TAG, "service lost" + service);
//					if (mService == service) {
//						mService = null;
//					}
				}

				@Override
				public void onDiscoveryStopped(String serviceType) {
//					Log.i(TAG, "Discovery stopped: " + serviceType);        
				}

				@Override
				public void onStartDiscoveryFailed(String serviceType, int errorCode) {
//					Log.e(TAG, "Discovery failed: Error code:" + errorCode);
					mNsdManager.stopServiceDiscovery(this);
				}

				@Override
				public void onStopDiscoveryFailed(String serviceType, int errorCode) {
//					Log.e(TAG, "Discovery failed: Error code:" + errorCode);
					mNsdManager.stopServiceDiscovery(this);
				}
			};
		}

		public void initializeResolveListener() {
			mResolveListener = new NsdManager.ResolveListener() {

				@Override
				public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
//					Log.e(TAG, "Resolve failed" + errorCode);
				}

				@Override
				public void onServiceResolved(NsdServiceInfo serviceInfo) {
//					Log.d(TAG, "Resolve Succeeded. name " + serviceInfo.getServiceName() 
//						  + "; type " + serviceInfo.getServiceType() + "; port " + serviceInfo.getPort()
//						  + "; ip " + serviceInfo.getHost());

//					if (serviceInfo.getServiceName().equals(mServiceName)) {
//						Log.d(TAG, "Same IP.");
//						return;
//					}
					mService = serviceInfo;
				}
			};
		}

		public void discoverServices() {
			mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
		}

		public void stopDiscovery() {
			mNsdManager.stopServiceDiscovery(mDiscoveryListener);
		}

		public NsdServiceInfo getChosenServiceInfo() {
			return mService;
		}

		public void tearDown() {
			mNsdManager.unregisterService(mRegistrationListener);
		}
	}
	
}
