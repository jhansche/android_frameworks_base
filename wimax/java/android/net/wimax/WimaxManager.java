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

package android.net.wimax;

import android.annotation.SdkConstant;
import android.annotation.SdkConstant.SdkConstantType;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.RemoteException;

import android.net.wimax.structs.ConnectedNspInfo;
import android.net.wimax.structs.ConnectionStatistics;
import android.net.wimax.structs.ContactInfo;
import android.net.wimax.structs.DeviceInfo;
import android.net.wimax.structs.HardwareDeviceId;
import android.net.wimax.structs.LinkStatusInfo;
import android.net.wimax.structs.NSPInfo;
import android.net.wimax.structs.PackageInfo;
import android.net.wimax.types.PackageUpdateState;

import java.util.List;

/**
 * This class provides the primary API for managing all aspects of Wimax connectivity.
 * Get an instance of this class by calling
 * {@link android.content.Context#getSystemService(String) Context.getSystemService(Context.WIMAX_SERVICE)}.
 */
public class WimaxManager {

    /**
     * Broadcast intent action indicating that Wimax has been enabled, disabled,
     * enabling, disabling, or unknown. One extra provides this state as an int.
     * Another extra provides the previous state, if available.
     *
     * @see #EXTRA_WIMAX_STATUS
     * @see #EXTRA_PREVIOUS_WIMAX_STATUS
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String WIMAX_STATUS_CHANGED_ACTION =
        "android.net.wimax.WIMAX_STATUS_CHANGED";
    /**
     * The lookup key for an int that indicates whether Wimax is enabled,
     * disabled, enabling, disabling, or unknown.  Retrieve it with
     * {@link android.content.Intent#getIntExtra(String,int)}.
     *
     * @see #WIMAX_DISABLING
     * @see #WIMAX_DISABLED
     * @see #WIMAX_ENABLING
     * @see #WIMAX_ENABLED
     * @see #WIMAX_STATUS_UNKNOWN
     */
    public static final String EXTRA_WIMAX_STATUS = "wimax_status";
    /**
     * The previous Wimax state.
     *
     * @see #EXTRA_WIMAX_STATUS
     */
    public static final String EXTRA_PREVIOUS_WIMAX_STATUS = "previous_wimax_status";

    /**
     * Wimax is currently being disabled. The state will change to {@link #WIMAX_DISABLED} if
     * it finishes successfully.
     *
     * @see #WIMAX_STATUS_CHANGED_ACTION
     * @see #getWimaxStatus()
     */
    public static final int WIMAX_DISABLING = 0;
    /**
     * Wimax is disabled.
     *
     * @see #WIMAX_STATUS_CHANGED_ACTION
     * @see #getWimaxStatus()
     */
    public static final int WIMAX_DISABLED = 1;
    /**
     * Wimax is currently being enabled. The state will change to {@link #WIMAX_ENABLED} if
     * it finishes successfully.
     *
     * @see #WIMAX_STATUS_CHANGED_ACTION
     * @see #getWimaxStatus()
     */
    public static final int WIMAX_ENABLING = 2;
    /**
     * Wimax is enabled.
     *
     * @see #WIMAX_STATUS_CHANGED_ACTION
     * @see #getWimaxStatus()
     */
    public static final int WIMAX_ENABLED = 3;
    /**
     * Wimax is in an unknown state. This state will occur when an error happens while enabling
     * or disabling.
     *
     * @see #WIMAX_STATUS_CHANGED_ACTION
     * @see #getWimaxStatus()
     */
    public static final int WIMAX_STATUS_UNKNOWN = 4;

    /**
     * Broadcast intent action indicating that the state of Wimax connectivity
     * has changed. One extra provides the new state
     * in the form of a {@link android.net.wimax.WimaxState} object. If the new state is
     * CONNECTED, a second extra may provide the BSID for the base station,
     * as a {@code String}.
     * @see #EXTRA_WIMAX_STATE
     * @see #EXTRA_BSSID
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String WIMAX_STATE_CHANGED_ACTION = "android.net.wimax.STATE_CHANGED";
    /**
     * The lookup key for a {@link android.net.wimax.WimaxState} object associated with the
     * Wimax network. Retrieve with
     * {@link android.content.Intent#getParcelableExtra(String)}.
     */
    public static final String EXTRA_WIMAX_STATE = "wimax_state";
    /**
     * The lookup key for a String giving the BSID for the base station to which
     * we are connected. Only present when the new state is CONNECTED.
     * Retrieve with
     * {@link android.content.Intent#getStringExtra(String)}.
     */
    public static final String EXTRA_BSID = "bsid";

    /**
     * A network search wide scan has completed. One extra provides the scan results
     * as a list of {@link android.net.wimax.structs.NSPInfo} object.
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String WIDE_SCAN_RESULTS_AVAILABLE_ACTION = "android.net.wimax.WIDE_SCAN_RESULTS";
    /**
     * The lookup key for a list of {@link android.net.wimax.structs.NSPInfo} objects.
     * Retrieve with {@link android.content.Intent#getParcelableExtra(String)}.
     */
    public static final String EXTRA_SCAN_RESULTS = "scan_results";

    /**
     * The RSSI (signal strength) has changed.
     * @see #EXTRA_NEW_RSSI
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String RSSI_CHANGED_ACTION = "android.net.wimax.RSSI_CHANGED";
    /**
     * The lookup key for an {@code int} giving the new RSSI in dBm.
     */
    public static final String EXTRA_NEW_RSSI = "new_rssi";

    /**
     * The provisioning update message is available.
     * @see #EXTRA_PROVISIONING_MESSAGE
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String PROVISIONING_UPDATE_ACTION = "android.net.wimax.PROVISIONING_UPDATE";
    /**
     * The lookup key for a {@code String} giving the provisioning update message.
     */
    public static final String EXTRA_PROVISIONING_MESSAGE = "provisioning_message";

    /**
     * The provisioning update message is available.
     * @see #EXTRA_PROVISIONING_MESSAGE
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String PACKAGE_UPDATE_ACTION = "android.net.wimax.PACKAGE_UPDATE";
    /**
     * The lookup key for a {@code String} giving the pacakge update message.
     */
    public static final String EXTRA_PACKAGE_UPDATE_MESSAGE = "package_update_message";

    /**
     * Activity Action: Pick a Wimax network to connect to.
     * <p>Input: Nothing.
     * <p>Output: Nothing.
     */
    @SdkConstant(SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_PICK_WIMAX_NETWORK = "android.net.wimax.PICK_WIMAX_NETWORK";

    /** Anything worse than or equal to this will show 0 rings. */
    private static final int MIN_RSSI = -100;

    /** Anything better than or equal to this will show the max rings. */
    private static final int MAX_RSSI = -70;

    IWimaxManager mService;
    Handler mHandler;

    /**
     * Create a new WimaxManager instance.
     * Applications should use {@link android.content.Context#getSystemService Context.getSystemService()}
     * with the wimax service name {@link android.content.Context#WIMAX_SERVICE Context.WIMAX_SERVICE}
     * to retrieve the WimaxManager instance.
     * @param service the Binder interface
     * @param handler target for messages
     * @hide - hide this because it takes in a parameter of type IWimaxManager, which
     * is a system private class.
     */
    public WimaxManager(IWimaxManager service, Handler handler) {
        mService = service;
        mHandler = handler;
    }

    /**
     * Return the list of wimax devices present in the system.
     * @return the list of wimax devices.
     */
    public List<HardwareDeviceId> getDeviceList() {
        try {
            return mService.getDeviceList();
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Return the results of the latest network scan.
     * @return the list of wimax networks found in the most recent scan.
     */
    public List<NSPInfo> getNetworkList() {
        try {
            return mService.getNetworkList();
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Commands the device to start the network wide scan.
     * @return {@code true} if the operation succeeded
     */
    public boolean performWideScan() {
        try {
            return mService.performWideScan();
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Connect to the specified wimax network. This may result in the
     * asynchronous delivery of state change events.
     * @param nspName - name of the wimax network
     * @return {@code true} if the operation succeeded
     */
    public boolean connect(String nspName) {
    	try {
            return mService.connect(nspName);
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Disassociate from the connected network and go to disconnected state.
     * This may result in the asynchronous delivery of state change events.
     * @return {@code true} if the operation succeeded
     */
    public boolean disconnect() {
    	try {
            return mService.disconnect();
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Get the current wimax network state.
     * @return the Wimax state information, contained in {@link android.net.wimax.WimaxState}.
     */
    public WimaxState getWimaxState() {
    	try {
            return WimaxState.fromInt(mService.getWimaxState());
        } catch (RemoteException e) {
            return WimaxState.UNKNOWN;
        }
    }

    /**
     * Get the information about the currently connected network. Info is returned only when the
     * wimax state is CONNECTED otherwise null is returned
     * @return the information about the connected network.
     * contained in {@link android.net.wimax.structs.ConnectedNspInfo}.
     */
    public ConnectedNspInfo getConnectedNSP() {
    	try {
            return mService.getConnectedNSP();
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Get the upload/download statistics for the current session. Statistics is returned only when the
     * wimax state is CONNECTED otherwise null is returned
     * @return the statistics for the current session.
     * contained in {@link android.net.wimax.structs.ConnectionStatistics}.
     */
    public ConnectionStatistics getConnectionStatsitics() {
    	try {
            return mService.getConnectionStatsitics();
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Get the link status for the currently connected network. Link status is returned only when the
     * wimax state is CONNECTED otherwise null is returned
     * @return the link status for the connected network.
     * contained in {@link android.net.wimax.structs.LinkStatusInfo}.
     */
    public LinkStatusInfo getLinkStatus() {
    	try {
            return mService.getLinkStatus();
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Get the information about the wimax device on the system.
     * @return the information about the wimax device.
     * contained in {@link android.net.wimax.structs.DeviceInfo}.
     */
    public DeviceInfo getDeviceInformation() {
    	try {
            return mService.getDeviceInformation();
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Get the information about the downloaded firmware update package.
     * @return the information about the update package.
     * contained in {@link android.net.wimax.structs.PackageInfo}.
     */
    public PackageInfo getPackageInformation() {
    	try {
            return mService.getPackageInformation();
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Set the response for the firmware update notification based on the user input.
     * @param state PackageUpdateState - user response
     * @return {@code true} if the operation succeeded
     */
    public boolean setPackageUpdateState(PackageUpdateState state) {
    	try {
            return mService.setPackageUpdateState(state.getValue());
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Get the contact information.
     * @return the contact information contained in {@link android.net.wimax.structs.ContactInfo}.
     */
    public List<ContactInfo> getContactInformation() {
    	try {
            return mService.getContactInformation();
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Return whether network roaming is enabled or disabled.
     * @return {@code true} if roaming is enabled.
     */
    public boolean isRoamingEnabled() {
    	try {
            return mService.isRoamingEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Enable or disable network roaming.
     * @param enabled {@code true} to enable, {@code false} to disable.
     * @return {@code true} if the operation succeeds (or if roaming is already enabled).
     */
    public boolean setRoamingEnabled(boolean enabled) {
        try {
            return mService.setRoamingEnabled(enabled);
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Gets the Wimax status.
     * @return One of {@link #WIMAX_DISABLED},
     *         {@link #WIMAX_DISABLING}, {@link #WIMAX_ENABLED},
     *         {@link #WIMAX_ENABLING}, {@link #WIMAX_STATUS_UNKNOWN}
     * @see #isWimaxEnabled()
     */
    public int getWimaxStatus() {
        try {
            return mService.getWimaxStatus();
        } catch (RemoteException e) {
            return WIMAX_STATUS_UNKNOWN;
        }
    }

    /**
     * Enable or disable Wimax.
     * @param enabled {@code true} to enable, {@code false} to disable.
     * @return {@code true} if the operation succeeds (or if the existing state
     *         is the same as the requested state).
     */
    public boolean setWimaxEnabled(boolean enabled) {
        try {
            return mService.setWimaxEnabled(enabled);
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Return whether Wimax is enabled or disabled.
     * @return {@code true} if Wimax is enabled
     * @see #getWimaxStatus()
     */
    public boolean isWimaxEnabled() {
        return getWimaxStatus() == WIMAX_ENABLED;
    }

    /**
     * Calculates the level of the signal. This should be used any time a signal
     * is being shown.
     *
     * @param rssi The power of the signal measured in RSSI.
     * @param numLevels The number of levels to consider in the calculated
     *            level.
     * @return A level of the signal, given in the range of 0 to numLevels-1
     *         (both inclusive).
     */
    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        } else if (rssi >= MAX_RSSI) {
            return numLevels - 1;
        } else {
            int partitionSize = (MAX_RSSI - MIN_RSSI) / (numLevels - 1);
            return (rssi - MIN_RSSI) / partitionSize;
        }
    }

    /**
     * Allows an application to keep the Wimax radio awake.
     * Normally the Wimax radio may turn off when the user has not used the device in a while.
     * Acquiring a WimaxLock will keep the radio on until the lock is released.  Multiple
     * applications may hold WimaxLocks, and the radio will only be allowed to turn off when no
     * WimaxLocks are held in any application.
     */
    public class WimaxLock {
        private String mTag;
        private final IBinder mBinder;
        private int mRefCount;
        private boolean mRefCounted;
        private boolean mHeld;

        private WimaxLock(String tag) {
            mTag = tag;
            mBinder = new Binder();
            mRefCount = 0;
            mRefCounted = true;
            mHeld = false;
        }

        /**
         * Locks the Wimax radio on until {@link #release} is called.
         *
         * If this WimaxLock is reference-counted, each call to {@code acquire} will increment the
         * reference count, and the radio will remain locked as long as the reference count is
         * above zero.
         *
         * If this WimaxLock is not reference-counted, the first call to {@code acquire} will lock
         * the radio, but subsequent calls will be ignored.  Only one call to {@link #release}
         * will be required, regardless of the number of times that {@code acquire} is called.
         */
        public void acquire() {
            synchronized (mBinder) {
                if (mRefCounted ? (++mRefCount > 0) : (!mHeld)) {
                    try {
                        mService.acquireWimaxLock(mBinder, mTag);
                    } catch (RemoteException ignore) {
                    }
                    mHeld = true;
                }
            }
        }

        /**
         * Unlocks the Wimax radio, allowing it to turn off when the device is idle.
         *
         * If this WimaxLock is reference-counted, each call to {@code release} will decrement the
         * reference count, and the radio will be unlocked only when the reference count reaches
         * zero.  If the reference count goes below zero (that is, if {@code release} is called
         * a greater number of times than {@link #acquire}), an exception is thrown.
         *
         * If this WimaxLock is not reference-counted, the first call to {@code release} (after
         * the radio was locked using {@link #acquire}) will unlock the radio, and subsequent
         * calls will be ignored.
         */
        public void release() {
            synchronized (mBinder) {
                if (mRefCounted ? (--mRefCount == 0) : (mHeld)) {
                    try {
                        mService.releaseWimaxLock(mBinder);
                    } catch (RemoteException ignore) {
                    }
                    mHeld = false;
                }
                if (mRefCount < 0) {
                    throw new RuntimeException("WimaxLock under-locked " + mTag);
                }
            }
        }

        /**
         * Controls whether this is a reference-counted or non-reference-counted WimaxLock.
         *
         * Reference-counted WimaxLocks keep track of the number of calls to {@link #acquire} and
         * {@link #release}, and only allow the radio to sleep when every call to {@link #acquire}
         * has been balanced with a call to {@link #release}.  Non-reference-counted WimaxLocks
         * lock the radio whenever {@link #acquire} is called and it is unlocked, and unlock the
         * radio whenever {@link #release} is called and it is locked.
         *
         * @param refCounted true if this WimaxLock should keep a reference count
         */
        public void setReferenceCounted(boolean refCounted) {
            mRefCounted = refCounted;
        }

        /**
         * Checks whether this WimaxLock is currently held.
         *
         * @return true if this WimaxLock is held, false otherwise
         */
        public boolean isHeld() {
            synchronized (mBinder) {
                return mHeld;
            }
        }

        public String toString() {
            String s1, s2, s3;
            synchronized (mBinder) {
                s1 = Integer.toHexString(System.identityHashCode(this));
                s2 = mHeld ? "held; " : "";
                if (mRefCounted) {
                    s3 = "refcounted: refcount = " + mRefCount;
                } else {
                    s3 = "not refcounted";
                }
                return "WimaxLock{ " + s1 + "; " + s2 + s3 + " }";
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            synchronized (mBinder) {
                if (mHeld) {
                    try {
                        mService.releaseWimaxLock(mBinder);
                    } catch (RemoteException ignore) {
                    }
                }
            }
        }
    }

    /**
     * Creates a new WimaxLock.
     *
     * @param tag a tag for the WimaxLock to identify it in debugging messages.  This string is
     *            never shown to the user under normal conditions, but should be descriptive
     *            enough to identify your application and the specific WimaxLock within it, if it
     *            holds multiple WimaxLocks.
     *
     * @return a new, unacquired WimaxLock with the given tag.
     *
     * @see WimaxLock
     */
    public WimaxLock createWimaxLock(String tag) {
        return new WimaxLock(tag);
    }
}
