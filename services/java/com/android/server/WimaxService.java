/**
 * Copyright (c) 2009, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server;

import static android.net.wimax.WimaxManager.WIMAX_DISABLING;
import static android.net.wimax.WimaxManager.WIMAX_DISABLED;
import static android.net.wimax.WimaxManager.WIMAX_ENABLING;
import static android.net.wimax.WimaxManager.WIMAX_ENABLED;
import static android.net.wimax.WimaxManager.WIMAX_STATUS_UNKNOWN;

import static android.net.wimax.WimaxManager.WIMAX_STATE_CHANGED_ACTION;
import static android.net.wimax.WimaxManager.EXTRA_WIMAX_STATE;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wimax.IWimaxManager;
import android.net.wimax.WimaxCommonAPI;
import android.net.wimax.WimaxManager;
import android.net.wimax.WimaxState;
import android.net.wimax.base.Connection;
import android.net.wimax.session.NetworkStates;
import android.net.wimax.session.StateMonitor;
import android.net.wimax.structs.ConnectedNspInfo;
import android.net.wimax.structs.ConnectionStatistics;
import android.net.wimax.structs.ContactInfo;
import android.net.wimax.structs.DeviceInfo;
import android.net.wimax.structs.HardwareDeviceId;
import android.net.wimax.structs.LinkStatusInfo;
import android.net.wimax.structs.NSPInfo;
import android.net.wimax.structs.PackageInfo;
import android.net.wimax.types.WimaxApiResponse;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * WimaxService handles remote Wimax operation requests by implementing
 * the IWimaxManager interface.
 *
 * @hide
 */
public class WimaxService extends IWimaxManager.Stub {

	private static final String TAG = "WimaxService";
    private static final boolean DBG = false;

    private Context mContext;
    private int mWimaxStatus;
    private boolean persist = true;

    private static final int MESSAGE_ENABLE_WIMAX      = 0;
    private static final int MESSAGE_DISABLE_WIMAX     = 1;

    private final WimaxHandler mWimaxHandler;

    private final LockList mLocks = new LockList();
    private static PowerManager.WakeLock sWakeLock;

    private StateMonitor monitor = null;

    private static final String WIMAX_SERVICE = "WimaxService";

    WimaxService(Context context) {
    	mContext = context;

        HandlerThread wimaxThread = new HandlerThread(WIMAX_SERVICE);
        wimaxThread.start();
        mWimaxHandler = new WimaxHandler(wimaxThread.getLooper());

        mWimaxStatus = WIMAX_DISABLED;
        boolean wimaxEnabled = getPersistedWimaxEnabled();

        PowerManager powerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        sWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WIMAX_SERVICE);

        registerForBroadcasts();
        setWimaxEnabled(wimaxEnabled, false);
        Log.d(TAG, "WimaxService starting up with Wimax " + (wimaxEnabled ? "enabled" : "disabled"));
    }

    private boolean getPersistedWimaxEnabled() {
        final ContentResolver cr = mContext.getContentResolver();
        try {
            return Settings.Secure.getInt(cr, Settings.Secure.WIMAX_ON) == 1;
        } catch (Settings.SettingNotFoundException e) {
            Settings.Secure.putInt(cr, Settings.Secure.WIMAX_ON, 0);
            return false;
        }
    }

    private void persistWimaxEnabled(boolean enabled) {
        final ContentResolver cr = mContext.getContentResolver();
        Settings.Secure.putInt(cr, Settings.Secure.WIMAX_ON, enabled ? 1 : 0);
    }

    /**
     * see {@link WimaxManager#getWimaxStatus()}
     * @return One of {@link WimaxManager#WIMAX_DISABLED},
     *         {@link WimaxManager#WIMAX_DISABLING},
     *         {@link WimaxManager#WIMAX_ENABLED},
     *         {@link WimaxManager#WIMAX_ENABLING},
     *         {@link WimaxManager#WIMAX_STATUS_UNKNOWN}
     */
    public int getWimaxStatus() {
        enforceAccessPermission();
        return mWimaxStatus;
    }

    /**
     * see {@link WimaxManager#setWimaxEnabled()}
     */
    public boolean setWimaxEnabled(boolean enable) {
    	return setWimaxEnabled(enable, true);
    }

    private boolean setWimaxEnabled(boolean enable, boolean persist) {
    	enforceChangePermission();
    	int requestedStatus = enable ? WIMAX_ENABLED : WIMAX_DISABLED;
        if (mWimaxStatus != requestedStatus) {
        	if((requestedStatus == WIMAX_ENABLED && mWimaxStatus == WIMAX_ENABLING) ||
        		(requestedStatus == WIMAX_DISABLED && mWimaxStatus == WIMAX_DISABLING))
        		return true;

        	sWakeLock.acquire();
        	this.persist = persist;
        	if(enable && isAirplaneModeOn()) {
        	    return false;
        	}else {
		        updateWimaxStatus(enable ? WIMAX_ENABLING : WIMAX_DISABLING);
		        if (enable) {
		        	if(WimaxCommonAPI.loadLibrary()) {
			        	if(monitor == null) {
			        		monitor = new StateMonitor(mContext);
			        	}
			            monitor.initialize();
		        	}else
		        		return false;
		        } else {
		        	monitor.goReady();
		        }
        	}
        }

        return true;
    }

    private void enforceAccessPermission() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.ACCESS_WIMAX_STATE,
        										WIMAX_SERVICE);
    }

    private void enforceChangePermission() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.CHANGE_WIMAX_STATE,
        										WIMAX_SERVICE);
    }

    private void sendEnableMessage(boolean enable) {
        Message msg = Message.obtain(mWimaxHandler,
                                     (enable ? MESSAGE_ENABLE_WIMAX : MESSAGE_DISABLE_WIMAX));
        msg.sendToTarget();
    }

    private void setWimaxEnabledBlocking(boolean enable) {
        final int newStatus = enable ? WIMAX_ENABLED : WIMAX_DISABLED;
    	updateWimaxStatus(newStatus);
    	if(newStatus == WIMAX_DISABLED) {
    		if(monitor != null) {
	    		monitor.stop();
	    		monitor = null;
    		}
    		WimaxCommonAPI.unloadLibrary();
    	}

    	sWakeLock.release();

        if (persist) {
            persistWimaxEnabled(enable);
        }
    }

    private void updateWimaxStatus(int wimaxStatus) {
        final int previousWimaxStatus = mWimaxStatus;

        // Update state
        mWimaxStatus = wimaxStatus;

        // Broadcast
        final Intent intent = new Intent(WimaxManager.WIMAX_STATUS_CHANGED_ACTION);
        intent.putExtra(WimaxManager.EXTRA_WIMAX_STATUS, wimaxStatus);
        intent.putExtra(WimaxManager.EXTRA_PREVIOUS_WIMAX_STATUS, previousWimaxStatus);
        mContext.sendStickyBroadcast(intent);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            	//Enable/Disable the wimax status based on the airplane mode.
            	if(intent.getBooleanExtra("state", false)) {
            		setWimaxEnabled(false, false);
            	}else {
            		setWimaxEnabled(getPersistedWimaxEnabled(), false);
            	}
            }else if(action.equals(WIMAX_STATE_CHANGED_ACTION)) {
            	WimaxState wimaxState = (WimaxState) intent.getParcelableExtra(EXTRA_WIMAX_STATE);
            	if(wimaxState == WimaxState.READY && mWimaxStatus == WIMAX_DISABLING) {
            		synchronized (mWimaxHandler) {
                        sendEnableMessage(false);
                    }
            	}else if(wimaxState == WimaxState.INITIALIZED && mWimaxStatus == WIMAX_ENABLING){
            		synchronized (mWimaxHandler) {
                        sendEnableMessage(true);
                    }
            	}else if(wimaxState == WimaxState.DISCONNECTED && mWimaxStatus == WIMAX_ENABLED){
            	    if(monitor != null && monitor.requestedNetworkState != NetworkStates.STATE_CONNECTED) {
                	    sWakeLock.acquire();
                        monitor.initialize();
            	    }
                }
            }
        }
    };

    private void registerForBroadcasts() {
        String airplaneModeRadios = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_RADIOS);
        boolean isAirplaneSensitive = (airplaneModeRadios == null
            							|| airplaneModeRadios.contains(Settings.System.RADIO_WIMAX));
        if (isAirplaneSensitive) {
            mContext.registerReceiver(mReceiver,
                new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        }
        mContext.registerReceiver(mReceiver,
                new IntentFilter(WIMAX_STATE_CHANGED_ACTION));
    }

    /**
     * Returns true if airplane mode is currently on.
     * @return {@code true} if airplane mode is on.
     */
    private boolean isAirplaneModeOn() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    }

    /**
     * Handler that allows posting to the WimaxThread.
     */
    private class WimaxHandler extends Handler {
        public WimaxHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_ENABLE_WIMAX:
                    setWimaxEnabledBlocking(true);
                    break;

                case MESSAGE_DISABLE_WIMAX:
                    setWimaxEnabledBlocking(false);
                    break;
            }
        }
    }

    private class WimaxLock implements IBinder.DeathRecipient {
        String mTag;
        IBinder mBinder;

        WimaxLock(String tag, IBinder binder) {
            super();
            mTag = tag;
            mBinder = binder;
            try {
                mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        public void binderDied() {
            synchronized (mLocks) {
                releaseWimaxLockLocked(mBinder);
            }
        }

        public String toString() {
            return "WimaxLock{" + mTag + " binder=" + mBinder + "}";
        }
    }

    private class LockList {
        private List<WimaxLock> mList;

        private LockList() {
            mList = new ArrayList<WimaxLock>();
        }

        private synchronized boolean hasLocks() {
            return !mList.isEmpty();
        }

        private void addLock(WimaxLock lock) {
            if (findLockByBinder(lock.mBinder) < 0) {
                mList.add(lock);
            }
        }

        private WimaxLock removeLock(IBinder binder) {
            int index = findLockByBinder(binder);
            if (index >= 0) {
                return mList.remove(index);
            } else {
                return null;
            }
        }

        private int findLockByBinder(IBinder binder) {
            int size = mList.size();
            for (int i = size - 1; i >= 0; i--)
                if (mList.get(i).mBinder == binder)
                    return i;
            return -1;
        }

        private synchronized void clear() {
            if (!mList.isEmpty()) {
                mList.clear();
                updateWimaxLockStatus();
            }
        }

        private void dump(PrintWriter pw) {
            for (WimaxLock l : mList) {
                pw.print("    ");
                pw.println(l);
            }
        }
    }

    /**
     * see {@link WimaxManager#acquireWimaxLock()}
     */
    public boolean acquireWimaxLock(IBinder binder, String tag) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.WAKE_LOCK, null);
        WimaxLock wimaxLock = new WimaxLock(tag, binder);
        synchronized (mLocks) {
            return acquireWimaxLockLocked(wimaxLock);
        }
    }

    private boolean acquireWimaxLockLocked(WimaxLock wimaxLock) {
        mLocks.addLock(wimaxLock);
        updateWimaxLockStatus();
        return true;
    }

    /**
     * see {@link WimaxManager#releaseWimaxLock()}
     */
    public boolean releaseWimaxLock(IBinder lock) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.WAKE_LOCK, null);
        synchronized (mLocks) {
            return releaseWimaxLockLocked(lock);
        }
    }

    private boolean releaseWimaxLockLocked(IBinder lock) {
        boolean result;
        result = (mLocks.removeLock(lock) != null);
        updateWimaxLockStatus();
        return result;
    }

    private void updateWimaxLockStatus() {
        boolean wimaxEnabled = getPersistedWimaxEnabled();
        boolean airplaneMode = isAirplaneModeOn();
        boolean lockHeld = mLocks.hasLocks();

        if(wimaxEnabled && !airplaneMode) {
        	if(lockHeld) {
        		sWakeLock.acquire();
        	}else {
        		sWakeLock.release();
        	}
        }
    }

    private boolean isWimaxDeviceReady() {
    	if(monitor != null && (monitor.getCurrentState() == WimaxState.INITIALIZED ||
    		monitor.getCurrentState() == WimaxState.CONNECTING	||
    		monitor.getCurrentState() == WimaxState.CONNECTED))
    		return true;
    	else
    		return false;
    }

    /**
     * see {@link WimaxManager#getDeviceList()}
     * @return the list of wimax devices present.
     */
    public List<HardwareDeviceId> getDeviceList() {
        enforceAccessPermission();
        List<HardwareDeviceId> deviceList = new ArrayList<HardwareDeviceId>();
        if(isWimaxDeviceReady()) {
        	HardwareDeviceId[] list = monitor.getDriver().getConnection().getDeviceList();
        	for(int i=0; i<list.length; i++)
        		deviceList.add(list[i]);
        }

        return deviceList;
    }

    /**
     * see {@link WimaxManager#getDeviceList()}
     * @return the list of wimax devices present.
     */
    public List<NSPInfo> getNetworkList() {
        enforceAccessPermission();
        List<NSPInfo> networkList = new ArrayList<NSPInfo>();
        if(isWimaxDeviceReady()) {
        	NSPInfo[] nspList = monitor.getDriver().getConnection().getNetworkList();
        	for(int i=0; i<nspList.length; i++)
        		networkList.add(nspList[i]);
        }
        return networkList;
    }

    /**
     * see {@link WimaxManager#performWideScan()}
     * @return {@code true} if the operation succeeded
     */
    public boolean performWideScan() {
        enforceAccessPermission();
        monitor.getDriver().getConnection().executeCommand(Connection.CmdNetworkSearchWideScan, null);
        return true;
    }

    /**
     * see {@link WimaxManager#connect(String)}
     * @param nspName - wimax network name
     * @return {@code true} if the operation succeeded
     */
    public boolean connect(String nspName) {
    	enforceChangePermission();
    	monitor.setNSPToConnect(nspName);
    	monitor.connect();

    	return true;
    }

    /**
     * see {@link WimaxManager#disconnect()}
     * @return {@code true} if the operation succeeded
     */
    public boolean disconnect() {
    	enforceChangePermission();
    	monitor.disconnect();
    	return true;
    }

    /**
     * see {@link WimaxManager#getWimaxState()}
     * @return int - the Wimax state information
     */
    public int getWimaxState() {
    	enforceAccessPermission();
    	return monitor.getCurrentState().getValue();
    }

    /**
     * see {@link WimaxManager#getConnectedNSP()}
     * @return ConnectedNspInfo - the information about connected nsp.
     */
    public ConnectedNspInfo getConnectedNSP() {
    	enforceAccessPermission();
    	ConnectedNspInfo nspInfo = null;

    	if(monitor.getCurrentState() == WimaxState.CONNECTED) {
    		WimaxApiResponse response = monitor.getDriver().getConnection().executeCommand(Connection.GetConnectedNSP, null);
    		if(response == WimaxApiResponse.SUCCESS)
    			nspInfo = monitor.getDriver().getConnectedNspInfo();
    	}

    	return nspInfo;
    }

    /**
     * see {@link WimaxManager#getConnectionStatsitics()}
     * @return ConnectionStatistics - the statistics for the current session.
     */
    public ConnectionStatistics getConnectionStatsitics() {
    	enforceAccessPermission();
    	ConnectionStatistics statistics = null;

    	if(monitor.getCurrentState() == WimaxState.CONNECTED) {
    		WimaxApiResponse response = monitor.getDriver().getConnection().executeCommand(Connection.GetStatistics, null);
    		if(response == WimaxApiResponse.SUCCESS)
    			statistics = monitor.getDriver().getConnectionStatistics();
    	}

    	return statistics;
    }

    /**
     * see {@link WimaxManager#getLinkStatus()}
     * @return LinkStatusInfo - the link status for the connected network.
     */
    public LinkStatusInfo getLinkStatus() {
    	enforceAccessPermission();
    	LinkStatusInfo linkStatus = null;

    	if(monitor.getCurrentState() == WimaxState.CONNECTED) {
    		WimaxApiResponse response = monitor.getDriver().getConnection().executeCommand(Connection.GetLinkStatus, null);
    		if(response == WimaxApiResponse.SUCCESS)
    			linkStatus = monitor.getDriver().getLinkStatusInfo();
    	}

    	return linkStatus;
    }

    /**
     * see {@link WimaxManager#getDeviceInformation()}
     * @return DeviceInfo - the link status for the connected network.
     */
    public DeviceInfo getDeviceInformation() {
    	enforceAccessPermission();
    	DeviceInfo deviceInfo = null;

    	if(isWimaxDeviceReady()) {
    		WimaxApiResponse response = monitor.getDriver().getConnection().executeCommand(Connection.GetDeviceInformation, null);
    		if(response == WimaxApiResponse.SUCCESS)
    			deviceInfo = monitor.getDriver().getDeviceInfo();
    	}

    	return deviceInfo;
    }

    /**
     * see {@link WimaxManager#getPackageInformation()}
     * @return PackageInfo - the information about the update package.
     */
    public PackageInfo getPackageInformation() {
    	enforceAccessPermission();
    	PackageInfo packageInfo = null;

    	if(isWimaxDeviceReady()) {
    		WimaxApiResponse response = monitor.getDriver().getConnection().executeCommand(Connection.GetPackageInformation, null);
    		if(response == WimaxApiResponse.SUCCESS)
    			packageInfo = monitor.getDriver().getPackageInfo();
    	}

    	return packageInfo;
    }

    /**
     * see {@link WimaxManager#setPackageUpdateState()}
     * @return {@code true} if the operation succeeded
     */
    public boolean setPackageUpdateState(int state) {
    	enforceChangePermission();
    	boolean result = false;

    	if(isWimaxDeviceReady()) {
    		WimaxApiResponse response = monitor.getDriver().getConnection().executeCommand(Connection.GetPackageInformation, new Integer(state));
    		if(response == WimaxApiResponse.SUCCESS)
    			result = true;
    	}

    	return result;
    }

    /**
     * see {@link WimaxManager#getContactInformation()}
     * @return ContactInfo - the information about the update package.
     */
    public List<ContactInfo> getContactInformation() {
    	enforceAccessPermission();
        List<ContactInfo> contactList = new ArrayList<ContactInfo>();
        if(isWimaxDeviceReady()) {
        	WimaxApiResponse response = monitor.getDriver().getConnection().executeCommand(Connection.GetContactInformation, null);
        	if(response == WimaxApiResponse.SUCCESS) {
	        	ContactInfo[] contacts = monitor.getDriver().getContactInfo();
	        	for(int i=0; i<contacts.length; i++)
	        		contactList.add(contacts[i]);
        	}
        }

    	return contactList;
    }

    /**
     * see {@link WimaxManager#isRoamingEnabled()}
     * @return {@code true} if the operation succeeded
     */
    public boolean isRoamingEnabled() {
    	enforceAccessPermission();
    	return false;
    }

    /**
     * see {@link WimaxManager#setRoamingEnabled()}
     * @return {@code true} if the operation succeeded
     */
    public boolean setRoamingEnabled(boolean enabled) {
    	enforceChangePermission();
    	return true;
    }

}
