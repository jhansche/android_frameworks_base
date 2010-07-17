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

package android.net.wimax.session;

import android.content.Context;
import android.content.Intent;
import android.net.wimax.WimaxManager;
import android.net.wimax.WimaxState;
import android.net.wimax.base.CallbackListener;
import android.net.wimax.base.Driver;
import android.net.wimax.structs.NSPInfo;
import android.net.wimax.types.ConnectionProgressStatus;
import android.net.wimax.types.DeviceStatus;
import android.net.wimax.types.DeviceStatusChangeReason;
import android.net.wimax.types.PackageUpdateStatus;
import android.net.wimax.types.ProvisioningOperationStatus;
import android.os.Parcelable;

/**
 * This class monitors the state of the device based on the requested state and
 * manages the callbacks back to the listeners.
 *
 * @hide
 */

public class StateMonitor implements Runnable, CallbackListener
{
	public static final int NETWORK_SCAN_RETRY_COUNT = 10;
	public static final int CHECK_CONNECTED_INTERVAL = 1000;

	public int requestedNetworkState     = NetworkStates.STATE_READY;
	public StateAbstract currentState    = null;

	// States
	public StateAbstract[] states = null;
	public final int STATE_READY         = 0;
	public final int STATE_INITIALIZING  = 1;
	public final int STATE_INITIALIZED   = 2;
	public final int STATE_CONNECTING    = 3;
	public final int STATE_CONNECTED     = 4;
	public final int STATE_DISCONNECTING = 5;
	public final int STATE_DISCONNECTED  = 6;

	private Driver driver = null;
	private boolean monitor			= true;
	private WimaxState prevState	= WimaxState.UNKNOWN;

	private String defaultNSP = "CLEAR";

    private Context mContext;

	/**
	 * Constructor that starts the state monitor thread and callback thread.
	 *
	 * @param driver android.net.wimax.base.Driver
	 */
	public StateMonitor(Context context)
	{
		mContext = context;

		this.driver = new Driver();
		this.driver.getConnection().subscribeToCallbacks(this);
		initializeStateMonitor();

		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Initialize the state machine; Start the callback monitor, Send
	 * a "Ready" message to the receivers and zero out the connection
	 * attributes.
	 */
	private void initializeStateMonitor()
	{
		// Build the different state managers
		states = new StateAbstract[7];
		states[STATE_READY]         = new StateReady(driver, this);
		states[STATE_INITIALIZING]  = new StateInitializing(driver, this);
		states[STATE_INITIALIZED]   = new StateInitialized(driver, this);
		states[STATE_CONNECTING]    = new StateConnecting(driver, this);
		states[STATE_CONNECTED]     = new StateConnected(driver, this);
		states[STATE_DISCONNECTING] = new StateDisconnecting(driver, this);
		states[STATE_DISCONNECTED]  = new StateDisconnected(driver, this);

		currentState = states[STATE_READY];
	}

	/**
	 * Loop until we don't want to monitor anymore.  This monitor executes the check
	 * state change every 100 milli seconds by default and then waits the defined
	 * amount of time in the config file between checks after the connection is
	 * established.
	 */
	public void run()
	{
		int waitTime = 100;

		while (monitor)
		{
			currentState.checkStateChange();

			if (driver != null &&
				 StateConnected.class.isInstance(currentState) &&
				 currentState.scriptPosition == 2)
			{
				waitTime = CHECK_CONNECTED_INTERVAL;
			}
			else
			{
				waitTime = 100;
			}

			try { Thread.sleep(waitTime); } catch (Exception e) {}
		}
	}

	/**
	 * Return the driver associated with this session.
	 *
	 * @return android.net.wimax.base.Driver
	 */
	public Driver getDriver()
	{
		return driver;
	}

	/**
	 * Stop the monitoring thread, make sure that we have disconnected
	 * from the WiMax network to put the card/driver in a managable
	 * state.
	 */
	public void stop()
	{
		monitor = false;
	}

	/**
	 * Is the monitor thread still running?
	 *
	 * @return boolean
	 */
	public boolean isMonitoring()
	{
		return monitor;
	}

	/**
	 * Are we connected to the WiMax network?
	 *
	 * @return boolean
	 */
	public boolean isConnected()
	{
		return driver.getConnection().isConnected();
	}

	/**
	 * Is there a card in the laptop?
	 *
	 * @return boolean
	 */
	public boolean hasCardConnected()
	{
		return driver.getConnection().isCardConnected();
	}

	/**
	 * We're being notified of a state change, the device status has changed.
	 *
	 * @param type int
	 * @param data java.util.HashMap
	 */
    public void callbackDeviceStatusChange (DeviceStatus status, DeviceStatusChangeReason reason, ConnectionProgressStatus connectionProgressStatus)
    {
    	//int status = (Integer)data.get("deviceStatus");
    }

	/**
	 * We're being notified of a state change, the card has been inserted into
	 * this machine or has been removed.
	 *
	 * @param type int
	 * @param data java.util.HashMap
	 */
    public void callbackDeviceInsertRemove (boolean cardPresent)
    {
    	driver.getDeviceId().devicePresenceStatus = cardPresent;
    	if(cardPresent) {
    		if (requestedNetworkState == NetworkStates.STATE_READY){
				connect();
			}
    	}else {
    		goReady();
    	}
    }

	/**
	 * We're being notified of a state change, the card maybe recycling.
	 *
	 * @param type int
	 * @param data java.util.HashMap
	 */
    public void callbackControlPowerManagement (int status)
    {
    	if(status == 1 && requestedNetworkState == NetworkStates.STATE_CONNECTED) {
    		goReady();
    	}
    }

	/**
	 * We're being notified of a state change, the card is connecting to
	 * the network.
	 *
	 * @param type int
	 * @param data java.util.HashMap
	 */
    public void callbackConnectToNetwork (int status)
    {
    	if(status == 0 && requestedNetworkState != NetworkStates.STATE_CONNECTED) {
    		checkForStateChange();
    	}
    }

	/**
	 * We're being notified of a state change, the card is disconnecting
	 * from the network.
	 *
	 * @param type int
	 * @param data java.util.HashMap
	 */
    public void callbackDisconnectToNetwork (int status)
    {
    	if(status == 1 && requestedNetworkState != NetworkStates.STATE_DISCONNECTED) {
    		checkForStateChange();
    	}
    }

	/**
	 * We're being notified of a state change, the card could be going
	 * into a network wide scan for some reason.
	 *
	 * @param type int
	 * @param data java.util.HashMap
	 */
    public void callbackNetworkSearchWideScan (NSPInfo[] nspInfo) {
    	checkForStateChange();

        final Intent intent = new Intent(WimaxManager.WIDE_SCAN_RESULTS_AVAILABLE_ACTION);
        intent.putExtra(WimaxManager.EXTRA_SCAN_RESULTS, (Parcelable[])nspInfo);
        mContext.sendStickyBroadcast(intent);
    }

   /**
	 * We're being notified for the provisioning operation.
	 *
	 * @param type int
	 * @param data java.util.HashMap
	 */
    public void callbackProvisioningOperation (ProvisioningOperationStatus provisioningOperation, int contactType) {
    	String message = provisioningOperation.getDescription();
        final Intent intent = new Intent(WimaxManager.PROVISIONING_UPDATE_ACTION);
        intent.putExtra(WimaxManager.EXTRA_PROVISIONING_MESSAGE, message);
        mContext.sendStickyBroadcast(intent);
    }

    /**
     * We're being notified for the package update.
     *
     * @param type int
     * @param data java.util.HashMap
     */
    public void callbackPackageUpdate (PackageUpdateStatus packageUpdate) {
    	String message = packageUpdate.getDescription();
        final Intent intent = new Intent(WimaxManager.PACKAGE_UPDATE_ACTION);
        intent.putExtra(WimaxManager.EXTRA_PACKAGE_UPDATE_MESSAGE, message);
        mContext.sendStickyBroadcast(intent);
    }

    /**
     * Check to see if the state has changed.
     */
    public void checkForStateChange()
    {
		if (driver.getConnection().isCardConnected())
		{
			if (requestedNetworkState == NetworkStates.STATE_READY)
			{
				connect();
			}
		}
		else
		{
			goReady();
		}
	}

    /**
     * Set the default nsp.
     *
     * @param nspName - nsp name
     */
    public void setNSPToConnect(String nspName) {
    	if(!defaultNSP.equalsIgnoreCase(nspName)) {
	    	defaultNSP = nspName;
	    	if (requestedNetworkState == NetworkStates.STATE_CONNECTED){
	    	    currentState = states[STATE_DISCONNECTING];
	    	}
    	}
    }

    /**
     * Get the default nsp.
     *
     * @return String - nsp name
     */
    public String getNSPToConnect() {
    	return defaultNSP;
    }

    /**
     * Returns the current wimax connectivity state
     * @return WimaxState - current state
     */
    public WimaxState getCurrentState() {
    	WimaxState wimaxState = WimaxState.READY;
    	if(StateReady.class.isInstance(currentState))
    	    wimaxState = WimaxState.READY;
    	else if(StateInitializing.class.isInstance(currentState))
    	    wimaxState = WimaxState.INITIALIZING;
    	else if(StateInitialized.class.isInstance(currentState))
    	    wimaxState = WimaxState.INITIALIZED;
    	else if(StateConnecting.class.isInstance(currentState))
    	    wimaxState = WimaxState.CONNECTING;
    	else if(StateConnected.class.isInstance(currentState))
    	    wimaxState = WimaxState.CONNECTED;
    	else if(StateDisconnecting.class.isInstance(currentState))
    	    wimaxState = WimaxState.DISCONNECTING;
    	else if(StateDisconnected.class.isInstance(currentState))
    	    wimaxState = WimaxState.DISCONNECTED;

    	return wimaxState;
    }

    public void sendStateChangedBroadcast(WimaxState wimaxState) {
    	if(prevState != wimaxState) {
	        final Intent intent = new Intent(WimaxManager.WIMAX_STATE_CHANGED_ACTION);
	        intent.putExtra(WimaxManager.EXTRA_WIMAX_STATE, (Parcelable)wimaxState);

	        if(StateConnected.class.isInstance(currentState)) {
	        	String bsid = driver.getLinkStatusInfo().getBsIdString();
	        	intent.putExtra(WimaxManager.EXTRA_BSID, bsid);
	        }
	        mContext.sendStickyBroadcast(intent);
	        prevState = wimaxState;
    	}
    }

    public void sendRssiChangedBroadcast(int rssi) {
        final Intent intent = new Intent(WimaxManager.RSSI_CHANGED_ACTION);
        intent.putExtra(WimaxManager.EXTRA_NEW_RSSI, rssi);
        mContext.sendStickyBroadcast(intent);
    }


	/*******************************************************************/
	/* Public API - Set requested states                               */
	/*******************************************************************/

   /**
    * Request to go into the Ready state.
    */
	public void goReady()
	{
		requestedNetworkState = NetworkStates.STATE_READY;
	}

   /**
    * Request to go into the Initialized state.
    */
	public void initialize()
	{
		requestedNetworkState = NetworkStates.STATE_INITIALIZED;
	}

   /**
    * Request to go into the Connected state.
    */
	public void connect()
	{
		requestedNetworkState = NetworkStates.STATE_CONNECTED;
	}

   /**
    * Request to go into the Disconnected state.
    */
	public void disconnect()
	{
		requestedNetworkState = NetworkStates.STATE_DISCONNECTED;
	}
}
