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

package android.net.wimax.base;

import android.net.wimax.WimaxCommonAPI;
import android.net.wimax.structs.*;
import android.net.wimax.types.*;

import java.util.*;


/**
 * This class contains the common API calls that are used to establish,
 * maintain and disconnect WiMax connections.  The android.net.wimax.Driver class
 * is used as a container of information provided by each of the calls as a
 * stepping stone to the next call in order to establish the connection.  JNA
 * is used to facilitate the JNI calls to the common API library.
 *
 * The scripts are as follows:
 *
 *  Opening API and device detection<br>
 *	-# WiMaxAPIOpen <br>
 *	-# Subscribe all indicator call back.<br>
 *	-# GetListDevice <br>
 *	-# WiMaxDeviceOpen <br>
 *	-# Device available detecting device status and configuration<br>
 *	-# GetDeviceStatus<br>
 *	-# CmdControlPowerManagement (if required, RF must be configure to ON)<br>
 *	-# GetSelectProfileList<br>
 *	-# GetNetworkList<br>
 *	-# CmdNetworkSearchWideScan (if user request wide scan search)<br>
 *	-# Call GetNetworkList to refresh CM
 *
 * Connection call flow<br>
 *	-# CmdConnectToNetwork<br>
 *	-# GetLinkStatus<br>
 *	-# GetIPInterfaceIndex
 *
 * Disconnection call flow<br>
 *	-# CmdDisconnectFromNetwork<br>
 *	-# GetDeviceStatus
 *
 * Upon device removal or closing device<br>
 *	-# WiMaxDeviceClose
 *
 * Closing the SDK<br>
 *	-# Unsubscribe all indicator call back<br>
 *	-# WiMaxAPIClose
 */
public class CommonAPICalls
{
	// Used for setting callback subscriptions
	protected boolean setupCallbacks = true;

    private WimaxCommonAPI.DeviceStatusChangeCB deviceStatusChangeCB = null;
    private WimaxCommonAPI.DeviceInsertRemoveCB deviceInsertRemoveCB = null;
    private WimaxCommonAPI.ControlPowerManagementCB controlPowerManagementCB = null;
    private WimaxCommonAPI.ConnectToNetworkCB connectToNetworkCB = null;
    private WimaxCommonAPI.DisconnectToNetworkCB disconnectToNetworkCB = null;
    private WimaxCommonAPI.NetworkSearchWideScanCB networkSearchWideScanCB = null;
    private WimaxCommonAPI.ProvisioningOperationCB provisioningOperationCB = null;
    private WimaxCommonAPI.PackageUpdateCB packageUpdateCB = null;

   // Properties
	private Vector<CallbackListener> listeners      = new Vector<CallbackListener>();

	// Constants
	public static final String EMPTY_STRING = "";
	public static final int MAX_LIST_SIZE   = 10;

	public CommonAPICalls()
	{
	}

	/**
	 * This method sets an indicator for whether or not the connection
	 * manager should setup the callbacks or not.  Callbacks are not
	 * supported currently for Mac or Linux OS's.
	 *
	 * @param setCallbacks boolean
	 */
	public void setupCallbacks(boolean setCallbacks)
	{
		setupCallbacks = setCallbacks;
	}


	/**
	 * This method sets the callback listener object that will be
	 * receiving callback information from the driver.
	 *
	 * @param callback {@link android.net.wimax.base.CallbackListener}
	 */
	public void subscribeToCallbacks(CallbackListener callback)
	{
		listeners.addElement(callback);
	}



	/*************************************************************************
	 * Commands                                                              *
	 *************************************************************************/
	/**
	 * Open the common API on the driver using the privilage requested
	 * by the receiver.  Return the results of the open.
	 *
	 * The driver API can only be opened once so an internal indicator
	 * is checked to see if it has already been opened and it will do
	 * nothing if it is already opened otherwise it will open the API.
	 * The method will return successful if the API is already opened
	 * otherwise it will return the received results.
	 *
	 * @param driver {@link android.net.wimax.base.Driver}
	 * @param privilege int
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse openApi(Driver driver, int privilege)
	{
		WimaxApiResponse response = null;

		if (!driver.isApiOpened())
		{
			DeviceId deviceId = new DeviceId();
			deviceId.privilege = privilege;

			int results = WimaxCommonAPI.WiMaxAPIOpen(deviceId);
			response = WimaxApiResponse.fromInt(results);
			driver.setDeviceId(deviceId);
			driver.getDeviceId().deviceIndex = 1;  // Need to set this

			if (response == WimaxApiResponse.SUCCESS)
				driver.isApiOpened(true);
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Close the driver API and return the results to the receiver.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse closeApi(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isApiOpened())
		{
			DeviceId tempId = driver.getDeviceId();
			driver.setDeviceId(null);

			int results = WimaxCommonAPI.WiMaxAPIClose(tempId);
			response = WimaxApiResponse.fromInt(results);

			if (response == WimaxApiResponse.SUCCESS)
				driver.reset();
		}

		return response;
	}

	/**
	 * Get the list of devices this machine has connected to it and
	 * return successful boolean indicator to the receiver.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @param listSize int
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getListOfDevices(Driver driver)
	{
		WimaxApiResponse response = null;

		HardwareDeviceId[] deviceList = new HardwareDeviceId[MAX_LIST_SIZE];
		int[] listSize = {MAX_LIST_SIZE};

  		int results = WimaxCommonAPI.GetListDevice(driver.getDeviceId(), deviceList, listSize);
  		response = WimaxApiResponse.fromInt(results);
  		int deviceListSize = listSize[0];
  		if(response == WimaxApiResponse.SUCCESS && deviceListSize > 0 ) {
  		    HardwareDeviceId[] tempDeviceList = new HardwareDeviceId[deviceListSize];
  		    for(int i=0; i<deviceListSize; i++) {
  		        tempDeviceList[i] = deviceList[i];
  		    }
  			driver.setList(tempDeviceList);
  			driver.setListSize(deviceListSize);

	  		//workaround for ZTE devices
	  		driver.getDeviceId().devicePresenceStatus = true;
  		}else {
  			driver.setList(null);
  			driver.setListSize(0);
  			driver.setDeviceInfo(null);
  		}

  		return response;
	}

	/**
	 * Open the device and return the results to the receiver.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse openTheDevice(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isDeviceOpened())
		{
			// Do this because the deviceId is a [in/out] paramater and it shouldn't be.
			DeviceId cloneDeviceId = new DeviceId();
			cloneDeviceId.apiVersion  = driver.getDeviceId().apiVersion;
			cloneDeviceId.deviceIndex = driver.getDeviceId().deviceIndex;
			cloneDeviceId.devicePresenceStatus = driver.getDeviceId().devicePresenceStatus;
			cloneDeviceId.privilege   = driver.getDeviceId().privilege;
			cloneDeviceId.sdkHandle   = driver.getDeviceId().sdkHandle;
			cloneDeviceId.structureSize = driver.getDeviceId().structureSize;

			int results = WimaxCommonAPI.WiMaxDeviceOpen(cloneDeviceId);
			response = WimaxApiResponse.fromInt(results);

			driver.isDeviceOpened(true);
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Close the device, this has to be done before the application
	 * exits because it may leave a hanging process if it doesn't.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse closeTheDevice(Driver driver)
	{
		WimaxApiResponse response = null;
		if (driver.isApiOpened())
		{
			int results = WimaxCommonAPI.WiMaxDeviceClose(driver.getDeviceId());
			response = WimaxApiResponse.fromInt(results);
			driver.isDeviceOpened(false);
		}

		return response;
	}

	/**
	 * Control the power management of the device, currently not used
	 * for the first release.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @param state int - "0" on, "1" off
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse controlPowerManagement(Driver driver, int state)
	{
		WimaxApiResponse response = null;

		if (driver.isApiOpened())
		{
			// state = 0 (On), 1 (Off)
			int results = WimaxCommonAPI.CmdControlPowerManagement(driver.getDeviceId(), state);
			response = WimaxApiResponse.fromInt(results);
		}

		return response;
	}

	/**
	 * Reset the WiMax device.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse resetWiMaxDevice(Driver driver)
	{
		WimaxApiResponse response = null;

		int results = WimaxCommonAPI.CmdResetWimaxDevice(driver.getDeviceId());
		response = WimaxApiResponse.fromInt(results);

		return response;
	}

	/**
	 * Get the list of networks available to this machine and return
	 * whether or not it was successful.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getNetworkList(Driver driver)
	{
		WimaxApiResponse response = null;

		NSPInfo[] nspList = new NSPInfo[MAX_LIST_SIZE];
		int[] listSize = {MAX_LIST_SIZE};

		int results = WimaxCommonAPI.GetNetworkList(driver.getDeviceId(), nspList, listSize);
		response = WimaxApiResponse.fromInt(results);

		int nspListSize = listSize[0];
		if(response == WimaxApiResponse.SUCCESS && nspListSize > 0) {
		    NSPInfo[] tempNspList = new NSPInfo[nspListSize];
            for(int i=0; i<nspListSize; i++) {
                tempNspList[i] = nspList[i];
            }
			driver.setNSPInfo(tempNspList);
		}else {
			driver.setNSPInfo(null);
		}

		return response;
	}

	/**
	 * Connect to the network using the arguments (String[]) where
	 * the first entry [0] is the id and the second [1] is the
	 * password.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @param args java.lang.Object
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse connectToNetwork(Driver driver, Object args)
	{
		WimaxApiResponse response = null;

		// Clear the Link Status and Statistics
		driver.setConnectionStatistics(null);
		driver.setLinkStatusInfo(null);

		// Try and make the connection
		String nspName  = "";
		int profileId   = 0;
		String password = "";

		Object[] argArray = (Object[])args;
		if (argArray != null) {
			if(!argArray[0].equals(EMPTY_STRING))
				nspName = (String)argArray[0];
			if (!argArray[2].equals(EMPTY_STRING))
				password = (String)argArray[2];
		}

		int results = WimaxCommonAPI.CmdConnectToNetwork(driver.getDeviceId(), nspName, profileId, password);
		response = WimaxApiResponse.fromInt(results);

		return response;
	}

	/**
	 * Do a network wide scan and return the results of whether or not
	 * it worked to the receiver.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse networkWideScan(Driver driver)
	{
		WimaxApiResponse response = null;

		int results = WimaxCommonAPI.CmdNetworkSearchWideScan(driver.getDeviceId());
		response = WimaxApiResponse.fromInt(results);

		return response;
	}

	/**
	 * Get the IP address assigned to this connection.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getIPInterfaceIndex(Driver driver)
	{
		WimaxApiResponse response = null;

		InterfaceInfo interfaceInfo = new InterfaceInfo();
		int results = WimaxCommonAPI.GetIPInterfaceIndex(driver.getDeviceId(), interfaceInfo);
		response = WimaxApiResponse.fromInt(results);
		driver.setInterfaceInfo(interfaceInfo);

		return response;
	}

	/**
	 * Need to set the profile to "xohm.com" so that we can get a list of networks.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getSelectProfileList(Driver driver)
	{
		WimaxApiResponse response = null;

		ProfileInfo[] profileList = new ProfileInfo[MAX_LIST_SIZE];
		int[] listSize = {MAX_LIST_SIZE};

		int results = WimaxCommonAPI.GetSelectProfileList(driver.getDeviceId(), profileList, listSize);
		response = WimaxApiResponse.fromInt(results);

		int profileListSize = listSize[0];
		if(response == WimaxApiResponse.SUCCESS && profileListSize > 0) {
		    ProfileInfo[] tempProfileList = new ProfileInfo[profileListSize];
            for(int i=0; i<profileListSize; i++) {
                tempProfileList[i] = profileList[i];
            }
			driver.setProfileList(tempProfileList);
			driver.setProfileListSize(profileListSize);
		}else {
			driver.setProfileList(null);
			driver.setProfileListSize(0);
		}

		return response;
	}

	/**
	 * Disconnect from the network, this must be done before exiting the
	 * application.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getLinkStatus(Driver driver)
	{
		WimaxApiResponse response = null;

		LinkStatusInfo linkStatus = new LinkStatusInfo();
		int results = WimaxCommonAPI.GetLinkStatus(driver.getDeviceId(), linkStatus);
		response = WimaxApiResponse.fromInt(results);
		driver.setLinkStatusInfo(linkStatus);

		return response;
	}

	/**
	 * Disconnect from the network, this must be done before exiting the
	 * application.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse disconnectFromNetwork(Driver driver)
	{
		WimaxApiResponse response = null;

		int results = WimaxCommonAPI.CmdDisconnectFromNetwork(driver.getDeviceId());
		response = WimaxApiResponse.fromInt(results);

		return response;
	}

	/**
	 * Get the device information and return whether or not this command
	 * was successful.  Check the variables pDeviceInfo for the results
	 * of the call.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getDeviceInformation(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isApiOpened())
		{
			DeviceInfo deviceInfo = new DeviceInfo();
			int results = WimaxCommonAPI.GetDeviceInformation(driver.getDeviceId(), deviceInfo);
			response = WimaxApiResponse.fromInt(results);
			driver.setDeviceInfo(deviceInfo);
		}

		return response;
	}


	/**
	 * Get the status of the device and return whether or not this command
	 * was successful.  Check the variables pDeviceStatus and pConnectionProgressInfo
	 * for the results of the call.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getDeviceStatus(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isApiOpened())
		{
			int[] deviceStatus = {-1};
			int[] connectionProgressInfo = {-1};

			//Get Device Status api call is updating the deviceId.
			DeviceId cloneDeviceId = new DeviceId();
			cloneDeviceId.apiVersion  = driver.getDeviceId().apiVersion;
			cloneDeviceId.deviceIndex = driver.getDeviceId().deviceIndex;
			cloneDeviceId.devicePresenceStatus = driver.getDeviceId().devicePresenceStatus;
			cloneDeviceId.privilege   = driver.getDeviceId().privilege;
			cloneDeviceId.sdkHandle   = driver.getDeviceId().sdkHandle;
			cloneDeviceId.structureSize = driver.getDeviceId().structureSize;

			int results = WimaxCommonAPI.GetDeviceStatus(cloneDeviceId, deviceStatus, connectionProgressInfo);
			response = WimaxApiResponse.fromInt(results);
			driver.setDeviceStatus(DeviceStatus.fromInt(deviceStatus[0]));
			driver.setConnectionProgressInfo(ConnectionProgressStatus.fromInt(connectionProgressInfo[0]));
		}

		return response;
	}

	/**
	 * Get the connected status of the device and return whether or not this command
	 * was successful.  Check the variables pDeviceStatus and pConnectionProgressInfo
	 * for the results of the call.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getConnectedNSP(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isApiOpened())
		{
			ConnectedNspInfo connectedNspInfo = new ConnectedNspInfo();
			int results = WimaxCommonAPI.GetConnectedNSP(driver.getDeviceId(), connectedNspInfo);
			response = WimaxApiResponse.fromInt(results);
			driver.setConnectedNspInfo(connectedNspInfo);
		}

		return response;
	}

	/**
	 * Get the status of the device and return whether or not this command
	 * was successful.  Check the variables pDeviceStatus and pConnectionProgressInfo
	 * for the results of the call.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getStatistics(Driver driver)
	{
		WimaxApiResponse response = null;

		ConnectionStatistics connectionStatistics = new ConnectionStatistics();

		int results = WimaxCommonAPI.GetStatistics(driver.getDeviceId(), connectionStatistics);
		response = WimaxApiResponse.fromInt(results);
		driver.setConnectionStatistics(connectionStatistics);

		return response;
	}

	/**
	 * Get the provisioning status for the device on a network and return
	 * whether or not it was successful.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getProvisioningStatus(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isApiOpened())
		{
			String nspName = "";
			boolean[] provisioningStatus = {false};

			if(driver.getNSPInfo() != null && driver.getNSPInfo().length > 0 && !driver.getNSPInfo()[0].getNspName().equals("")) {
				nspName = driver.getNSPInfo()[0].getNspName();
			}else {
				return response;
			}

			int results = WimaxCommonAPI.GetProvisioningStatus(driver.getDeviceId(), nspName, provisioningStatus);
			response = WimaxApiResponse.fromInt(results);
			driver.setProvisioningStatus(provisioningStatus[0]);
		}

		return response;
	}

	/**
	 * Get the contact Information for the device on a network and return
	 * whether or not it was successful.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getContactInformation(Driver driver)
	{
		WimaxApiResponse response = null;
		if (driver.isApiOpened())
		{
			String nspName = "";

			ContactInfo[] contactInfo = new ContactInfo[MAX_LIST_SIZE];
			int[] listSize = {MAX_LIST_SIZE};

			if(driver.getNSPInfo() != null && driver.getNSPInfo().length > 0 && !driver.getNSPInfo()[0].getNspName().equals("")) {
				nspName = driver.getNSPInfo()[0].getNspName();
			}else {
				return response;
			}

			int results = WimaxCommonAPI.GetContactInformation(driver.getDeviceId(), nspName, contactInfo, listSize);
			response = WimaxApiResponse.fromInt(results);
			int contactListSize = listSize[0];
			if(response == WimaxApiResponse.SUCCESS && contactListSize > 0) {
			    ContactInfo[] tempContactList = new ContactInfo[contactListSize];
	            for(int i=0; i<contactListSize; i++) {
	                tempContactList[i] = contactInfo[i];
	            }
	            driver.setContactInfo(tempContactList);
	        }else {
	            driver.setContactInfo(null);
	        }

		}

		return response;
	}

	/**
	 * Get the Package Information for FUMO update for the device on a network and return
	 * whether or not it was successful.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse getPackageInformation(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isApiOpened())
		{
			PackageInfo packageInfo =  new PackageInfo();
			int results = WimaxCommonAPI.GetPackageInformation(driver.getDeviceId(), packageInfo);
			response = WimaxApiResponse.fromInt(results);
			driver.setPackageInfo(packageInfo);
		}

		return response;
	}

	/**
	 * Set the Package update state for FUMO update and return
	 * whether or not it was successful.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @param args java.lang.Object
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse setPackageUpdateState(Driver driver, Object args)
	{
		WimaxApiResponse response = null;

		if (driver.isApiOpened())
		{
			int state = PackageUpdateState.DELAY.getValue();
			if (args != null)
				state = (Integer)args;

			int results = WimaxCommonAPI.SetPackageUpdateState(driver.getDeviceId(), state);
			response = WimaxApiResponse.fromInt(results);
		}

		return response;
	}

	/**
	 * Subscribe to the device status change notifier, this will send notifications
	 * to registered receivers from the API method callbackSubscriptions.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse subscribeDeviceStatusChange(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isSubscribeDeviceStatusChange())
		{
			if (setupCallbacks)
			{
				deviceStatusChangeCB = new WimaxCommonAPI.DeviceStatusChangeCB()
				{
					public void callback(DeviceId deviceId, int deviceStatus, int statusReason, int connectionProgressInfo)
					{
						deviceStatusChangeCB(deviceId, deviceStatus, statusReason, connectionProgressInfo);
					}
				};

				int results = WimaxCommonAPI.SubscribeDeviceStatusChange(driver.getDeviceId(), deviceStatusChangeCB);
				response = WimaxApiResponse.fromInt(results);

				driver.isSubscribeDeviceStatusChange(true);
			}
			else
				response = WimaxApiResponse.SUCCESS;
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Subscribe to the device status change notifier, this will send notifications
	 * to registered receivers from the API method callbackSubscriptions.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse subscribeDeviceInsertRemove(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isSubscribeDeviceInsertRemove())
		{
			if (setupCallbacks)
			{
				deviceInsertRemoveCB = new WimaxCommonAPI.DeviceInsertRemoveCB()
				{
					public void callback(DeviceId deviceId, boolean cardPresent)
					{
						deviceInsertRemoveCB(deviceId, cardPresent);
					}
				};

				int results = WimaxCommonAPI.SubscribeDeviceInsertRemove(driver.getDeviceId(), deviceInsertRemoveCB);
				response = WimaxApiResponse.fromInt(results);

				driver.isSubscribeDeviceInsertRemove(true);
			}
			else
				response = WimaxApiResponse.SUCCESS;
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Subscribe to the control power management notifier, this will send notifications
	 * to registered receivers from the API method callbackSubscriptions.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse subscribeControlPowerManagement(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isSubscribeControlPowerManagement())
		{
			if (setupCallbacks)
			{
				controlPowerManagementCB = new WimaxCommonAPI.ControlPowerManagementCB()
				{
					public void callback(DeviceId deviceId, int status)
					{
						controlPowerManagementCB(deviceId, status);
					}
				};

				int results = WimaxCommonAPI.SubscribeControlPowerManagement(driver.getDeviceId(), controlPowerManagementCB);
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeControlPowerManagement(true);
			}
			else
				response = WimaxApiResponse.SUCCESS;
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Subscribe to the connect to network notifier, this will send notifications
	 * to registered receivers from the API method callbackSubscriptions.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse subscribeConnectToNetwork(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isSubscribeConnectToNetwork())
		{
			if (setupCallbacks)
			{
				connectToNetworkCB = new WimaxCommonAPI.ConnectToNetworkCB()
				{
					public void callback(DeviceId deviceId, int status)
					{
						connectToNetworkCB(deviceId, status);
					}
				};

				int results = WimaxCommonAPI.SubscribeConnectToNetwork(driver.getDeviceId(), connectToNetworkCB);
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeConnectToNetwork(true);
			}
			else
				response = WimaxApiResponse.SUCCESS;
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Subscribe to the disconnect to network notifier, this will send notifications
	 * to registered receivers from the API method callbackSubscriptions.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse subscribeDisconnectToNetwork(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isSubscribeDisconnectToNetwork())
		{
			if (setupCallbacks)
			{
				disconnectToNetworkCB = new WimaxCommonAPI.DisconnectToNetworkCB()
				{
					public void callback(DeviceId deviceId, int status)
					{
						disconnectToNetworkCB(deviceId, status);
					}
				};

				int results = WimaxCommonAPI.SubscribeDisconnectToNetwork(driver.getDeviceId(), disconnectToNetworkCB);
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeDisconnectToNetwork(true);
			}
			else
				response = WimaxApiResponse.SUCCESS;
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Subscribe to the network search wide scan notifier, this will send notifications
	 * to registered receivers from the API method callbackSubscriptions.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse subscribeNetworkSearchWideScan(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isSubscribeNetworkSearchWideScan())
		{
			if (setupCallbacks)
			{
				networkSearchWideScanCB = new WimaxCommonAPI.NetworkSearchWideScanCB()
				{
					public void callback(DeviceId deviceId, NSPInfo[] nspInfo, int listSize)
					{
						networkSearchWideScanCB(deviceId, nspInfo, listSize);
					}
				};

				int results = WimaxCommonAPI.SubscribeNetworkSearchWideScan(driver.getDeviceId(), networkSearchWideScanCB);
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeNetworkSearchWideScan(true);
			}
			else
				response = WimaxApiResponse.SUCCESS;
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Subscribe to the provisioning operation notifier, this will send notifications
	 * to registered receivers from the API method callbackSubscriptions.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse subscribeProvisioningOperation(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isSubscribeProvisioningOperation())
		{
			if (setupCallbacks)
			{
				provisioningOperationCB = new WimaxCommonAPI.ProvisioningOperationCB()
				{
					public void callback(DeviceId deviceId, int provisoningOperation, int contactType)
					{
						provisioningOperationCB(deviceId, provisoningOperation, contactType);
					}
				};

				int results = WimaxCommonAPI.SubscribeProvisioningOperation(driver.getDeviceId(), provisioningOperationCB);
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeProvisioningOperation(true);
			}
			else
				response = WimaxApiResponse.SUCCESS;
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Subscribe to the package update notifier, this will send notifications
	 * to registered receivers from the API method callbackSubscriptions.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse subscribePackageUpdate(Driver driver)
	{
		WimaxApiResponse response = null;

		if (!driver.isSubscribePackageUpdate())
		{
			if (setupCallbacks)
			{
				packageUpdateCB = new WimaxCommonAPI.PackageUpdateCB()
				{
					public void callback(DeviceId deviceId, int packageUpdate)
					{
						packageUpdateCB(deviceId, packageUpdate);
					}
				};

				int results = WimaxCommonAPI.SubscribePackageUpdate(driver.getDeviceId(), packageUpdateCB);
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribePackageUpdate(true);
			}
			else
				response = WimaxApiResponse.SUCCESS;
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Unsubscribe to the device status change notifier.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse unsubscribeDeviceStatusChange(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isSubscribeDeviceStatusChange())
		{
			if (setupCallbacks)
			{
				int results = WimaxCommonAPI.UnsubscribeDeviceStatusChange(driver.getDeviceId());
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeDeviceStatusChange(false);
			}
			else
			{
				driver.isSubscribeDeviceStatusChange(false);
				response = WimaxApiResponse.SUCCESS;
			}
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Unsubscribe to the device insert/remove notifier.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse unsubscribeDeviceInsertRemove(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isSubscribeDeviceInsertRemove())
		{
			if (setupCallbacks)
			{
				int results = WimaxCommonAPI.UnsubscribeDeviceInsertRemove(driver.getDeviceId());
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeDeviceInsertRemove(false);
			}
			else
			{
				driver.isSubscribeDeviceInsertRemove(false);
				response = WimaxApiResponse.SUCCESS;
			}
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Unsubscribe to the control power management notifier.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse unsubscribeControlPowerManagement(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isSubscribeControlPowerManagement())
		{
			if (setupCallbacks)
			{
				int results = WimaxCommonAPI.UnsubscribeControlPowerManagement(driver.getDeviceId());
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeControlPowerManagement(false);
			}
			else
			{
				response = WimaxApiResponse.SUCCESS;
				driver.isSubscribeControlPowerManagement(false);
			}
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Unsubscribe to the connect to network notifier.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse unsubscribeConnectToNetwork(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isSubscribeConnectToNetwork())
		{
			if (setupCallbacks)
			{
				int results = WimaxCommonAPI.UnsubscribeConnectToNetwork(driver.getDeviceId());
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeConnectToNetwork(false);
			}
			else
			{
				driver.isSubscribeConnectToNetwork(false);
				response = WimaxApiResponse.SUCCESS;
			}
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Unsubscribe to the disconnect to network notifier.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse unsubscribeDisconnectToNetwork(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isSubscribeDisconnectToNetwork())
		{
			if (setupCallbacks)
			{
				int results = WimaxCommonAPI.UnsubscribeDisconnectToNetwork(driver.getDeviceId());
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeDisconnectToNetwork(false);
			}
			else
			{
				driver.isSubscribeDisconnectToNetwork(false);
				response = WimaxApiResponse.SUCCESS;
			}
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Unsubscribe to the network wide scan notifier.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse unsubscribeNetworkSearchWideScan(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isSubscribeNetworkSearchWideScan())
		{
			if (setupCallbacks)
			{
				int results = WimaxCommonAPI.UnsubscribeNetworkSearchWideScan(driver.getDeviceId());
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeNetworkSearchWideScan(false);
			}
			else
			{
				driver.isSubscribeNetworkSearchWideScan(false);
				response = WimaxApiResponse.SUCCESS;
			}
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Unsubscribe to the provisioning operation notifier.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse unsubscribeProvisioningOperation(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isSubscribeProvisioningOperation())
		{
			if (setupCallbacks)
			{
				int results = WimaxCommonAPI.UnsubscribeProvisioningOperation(driver.getDeviceId());
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribeProvisioningOperation(false);
			}
			else
			{
				driver.isSubscribeProvisioningOperation(false);
				response = WimaxApiResponse.SUCCESS;
			}
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}

	/**
	 * Unsubscribe to the package update notifier.
	 *
	 * @param driver {@link android.net.wimax.Driver}
	 * @return WimaxApiResponse - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse unsubscribePackageUpdate(Driver driver)
	{
		WimaxApiResponse response = null;

		if (driver.isSubscribePackageUpdate())
		{
			if (setupCallbacks)
			{
				int results = WimaxCommonAPI.UnsubscribePackageUpdate(driver.getDeviceId());
				response = WimaxApiResponse.fromInt(results);
				driver.isSubscribePackageUpdate(false);
			}
			else
			{
				driver.isSubscribePackageUpdate(false);
				response = WimaxApiResponse.SUCCESS;
			}
		}
		else
			response = WimaxApiResponse.SUCCESS;

		return response;
	}



	/*************************************************************************
	 * Callbacks                                                             *
	 *************************************************************************/

	/**
	 * This callback notifies the registered receiver of the power management
	 * notification from the driver.
	 *
	 * @param deviceId {@link android.net.wimax.structs.DeviceId}
	 * @param status int
	 */
	public void controlPowerManagementCB(DeviceId deviceId, int status)
	{
		if (listeners.size() > 0)
		{
			Enumeration<CallbackListener> listenersEnum = listeners.elements();
			while (listenersEnum.hasMoreElements())
			{
				(listenersEnum.nextElement()).callbackControlPowerManagement(status);
			}
		}
	}

	/**
	 * This callback notifies the registered receiver of the connect to network
	 * notification from the driver.
	 *
	 * @param deviceId {@link android.net.wimax.structs.DeviceId}
	 * @param status int
	 */
	public void connectToNetworkCB(DeviceId deviceId, int status)
	{
		if (listeners.size() > 0)
		{
			Enumeration<CallbackListener> listenersEnum = listeners.elements();
			while (listenersEnum.hasMoreElements())
			{
				(listenersEnum.nextElement()).callbackConnectToNetwork(status);
			}
		}
	}

	/**
	 * This callback notifies the registered receiver of the disconnect to network
	 * notification from the driver.
	 *
	 * @param deviceId {@link android.net.wimax.structs.DeviceId}
	 * @param status int
	 */
	public void disconnectToNetworkCB(DeviceId deviceId, int status)
	{
		if (listeners.size() > 0)
		{
			Enumeration<CallbackListener> listenersEnum = listeners.elements();
			while (listenersEnum.hasMoreElements())
			{
				(listenersEnum.nextElement()).callbackDisconnectToNetwork(status);
			}
		}
	}

	/**
	 * This callback notifies the registered receiver of the network wide scan
	 * notification from the driver.
	 *
	 * @param deviceId {@link android.net.wimax.structs.DeviceId}
	 * @param nspInfo {@link android.net.wimax.structs.NSPInfo}[]
	 * @param listSize int
	 */
	public void networkSearchWideScanCB(DeviceId deviceId, NSPInfo[] nspInfo, int listSize)
	{
		if (listeners.size() > 0)
		{
			Enumeration<CallbackListener> listenersEnum = listeners.elements();
			while (listenersEnum.hasMoreElements())
			{
				(listenersEnum.nextElement()).callbackNetworkSearchWideScan(nspInfo);
			}
		}
	}

	/**
	 * This callback notifies the registered receiver of the package update
	 * notification from the driver.
	 *
	 * @param deviceId {@link android.net.wimax.structs.DeviceId}
	 * @param packageUpdate int
	 * @param contactType int
	 */
	public void packageUpdateCB(DeviceId deviceId, int packageUpdate)
	{

		if (listeners.size() > 0)
		{
			Enumeration<CallbackListener> listenersEnum = listeners.elements();
			while (listenersEnum.hasMoreElements())
			{
				(listenersEnum.nextElement()).callbackPackageUpdate(PackageUpdateStatus.fromInt(packageUpdate));
			}
		}
	}

	/**
	 * This callback notifies the registered receiver of the provisioning operation
	 * notification from the driver.
	 *
	 * @param deviceId {@link android.net.wimax.structs.DeviceId}
	 * @param provisioningOperation int
	 * @param contactType int
	 */
	public void provisioningOperationCB(DeviceId deviceId, int provisioningOperation, int contactType)
	{
		if (listeners.size() > 0)
		{
			Enumeration<CallbackListener> listenersEnum = listeners.elements();
			while (listenersEnum.hasMoreElements())
			{
				(listenersEnum.nextElement()).callbackProvisioningOperation(ProvisioningOperationStatus.fromInt(provisioningOperation), contactType);
			}
		}
	}

	/**
	 * This callback notifies the registered receiver of the device status change
	 * notification from the driver.
	 *
	 * @param deviceId {@link android.net.wimax.structs.DeviceId}[]
	 * @param cardPresent boolean
	 */
	public void deviceInsertRemoveCB(DeviceId deviceId, boolean cardPresent)
	{

		if (listeners.size() > 0)
		{
			Enumeration<CallbackListener> listenersEnum = listeners.elements();
			while (listenersEnum.hasMoreElements())
			{
				(listenersEnum.nextElement()).callbackDeviceInsertRemove(cardPresent);
			}
		}
	}

	/**
	 * This callback notifies the registered receiver of the device status change
	 * notification from the driver.
	 *
	 * @param deviceId {@link android.net.wimax.structs.DeviceId}[]
	 * @param deviceStatus int
	 * @param statusReason int
	 * @param connectionProgressInfo int
	 */
	public void deviceStatusChangeCB(DeviceId deviceId, int deviceStatus, int statusReason, int connectionProgressInfo)
	{

		if (listeners.size() > 0)
		{
			Enumeration<CallbackListener> listenersEnum = listeners.elements();
			while (listenersEnum.hasMoreElements())
			{
				(listenersEnum.nextElement()).callbackDeviceStatusChange(DeviceStatus.fromInt(deviceStatus),
				        DeviceStatusChangeReason.fromInt(statusReason), ConnectionProgressStatus.fromInt(connectionProgressInfo));
			}
		}
	}
}
