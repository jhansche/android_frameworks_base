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
import android.net.wimax.types.DeviceStatus;
import android.net.wimax.types.WimaxApiResponse;

/**
 * This class extends the CommonAPICalls class in order to provide
 * convience methods to allow the next layer up to script calls to get
 * to a particular state.
 */
public class Connection extends CommonAPICalls
{
	private Driver driver = null;

	public static final int READ_WRITE = 0;
	public static final int READ_ONLY  = 1;

	public static final int POWER_ON  = 0;
	public static final int POWER_OFF = 1;


	// Function Index
   public static final int WiMaxAPIOpen                      = 1;
   public static final int GetListDevice                     = 2;
   public static final int WiMaxDeviceOpen                   = 3;
   public static final int WiMaxDeviceClose                  = 4;
   public static final int WiMaxAPIClose                     = 5;
   public static final int CmdControlPowerManagementOn       = 6;
   public static final int CmdControlPowerManagementOff      = 7;
   public static final int CmdResetWimaxDevice               = 8;
   public static final int CmdResetToFactorySettings         = 9;
   public static final int GetErrorString                    = 10;
   public static final int SetServiceProviderUnLock          = 11;
   public static final int GetServiceProviderLockStatus      = 12;
   public static final int GetNetworkList                    = 13;
   public static final int CmdConnectToNetwork               = 14;
   public static final int CmdDisconnectFromNetwork          = 15;
   public static final int CmdNetworkSearchWideScan          = 16;
   public static final int GetIPInterfaceIndex               = 17;
   public static final int GetSelectProfileList              = 18;
   public static final int GetLinkStatus                     = 19;
   public static final int GetDeviceInformation              = 20;
   public static final int GetDeviceStatus                   = 21;
   public static final int GetConnectedNSP                   = 22;
   public static final int SetRoamingMode                    = 23;
   public static final int GetRoamingMode                    = 24;
   public static final int GetStatistics                     = 25;
   public static final int GetProvisioningStatus             = 26;
   public static final int GetContactInformation             = 27;
   public static final int GetPackageInformation             = 28;
   public static final int SetPackageUpdateState             = 29;
   public static final int SubscribeDeviceStatusChange       = 30;
   public static final int SubscribeDeviceInsertRemove       = 31;
   public static final int SubscribeControlPowerManagement   = 32;
   public static final int SubscribeConnectToNetwork         = 33;
   public static final int SubscribeDisconnectToNetwork      = 34;
   public static final int SubscribeNetworkSearchWideScan    = 35;
   public static final int SubscribeProvisioningOperation    = 36;
   public static final int SubscribePackageUpdate            = 37;
   public static final int UnsubscribeDeviceStatusChange     = 38;
   public static final int UnsubscribeDeviceInsertRemove     = 39;
   public static final int UnsubscribeControlPowerManagement = 40;
   public static final int UnsubscribeConnectToNetwork       = 41;
   public static final int UnsubscribeDisconnectToNetwork    = 42;
   public static final int UnsubscribeNetworkSearchWideScan  = 43;
   public static final int UnsubscribeProvisioningOperation  = 44;
   public static final int UnsubscribePackageUpdate          = 45;


	public Connection(Driver driver)
	{
		this.driver = driver;
	}

	/*************************************************************************
	 * Connection API                                                        *
	 *************************************************************************/

	/**
	 * This method checks to see if the WiMax card is plugged into
	 * the laptop/machine or not.<br><br>
	 *
	 * @return boolean
	 */
	public boolean isCardConnected()
	{
		boolean deviceAvailable = false;

		HardwareDeviceId[] list = getDeviceList();
		if (list != null && list.length > 0)
		{
			deviceAvailable = true;
		}

		return deviceAvailable;
	}

	/**
	 * This method checks to see that we have a valid WiMax connection
	 * and returns it to the receiver.<br><br>
	 *
	 * @return boolean
	 */
	public boolean isConnected()
	{
		boolean connectionEstablished = false;
		WimaxApiResponse results = executeCommand(GetDeviceStatus, null);
		if (results == WimaxApiResponse.SUCCESS)
		{
			if (driver.getDeviceStatus() == DeviceStatus.DATA_CONNECTED)
			{
				connectionEstablished = true;
			}
		}

		return connectionEstablished;
	}

	/**
	 * This method returns a list of available WiMax devices to the
	 * receiver.<br><br>
	 *
	 * @return android.net.wimax.cm.capi.objs.HardwareDeviceIdList[]
	 */
	public HardwareDeviceId[] getDeviceList()
	{
		HardwareDeviceId[] deviceList = null;

		WimaxApiResponse results = executeCommand(Connection.WiMaxAPIOpen, null);
		if (results == WimaxApiResponse.SUCCESS)
		{
			deviceList = driver.getList();
		}

		return deviceList;
	}

	/**
	 * This method returns the device information.<br><br>
	 *
	 * @return android.net.wimax.base.structs.DeviceInfo
	 */
	public DeviceInfo getDeviceInfo() {
		DeviceInfo deviceInfo = null;

		WimaxApiResponse results = executeCommand(Connection.GetDeviceInformation, null);
		if (results == WimaxApiResponse.SUCCESS) {
			if(driver.getDeviceInfo() != null)
				deviceInfo = driver.getDeviceInfo();
		}

		return deviceInfo;
	}

	/**
	 * This method returns the list of available networks.<br><br>
	 *
	 * @return android.net.wimax.base.structs.NSPInfo
	 */
	public NSPInfo[] getNetworkList() {
		NSPInfo[] nspList = null;

		WimaxApiResponse results = executeCommand(Connection.GetNetworkList, null);
		if (results == WimaxApiResponse.SUCCESS) {
			if(driver.getNSPInfo() != null)
				nspList = driver.getNSPInfo();
		}

		return nspList;
	}

	/**
	 * Get the message string of the last error code from the driver.<br><br>
	 *
	 * @param errorCode int
	 * @return java.lang.String
	 */
	public String getErrorString(int errorCode)
	{
		String msg = EMPTY_STRING;

		String[] buffer = {""};
		int[] bufferLength = {0};
		int results = WimaxCommonAPI.GetErrorString(driver.getDeviceId(), errorCode, buffer, bufferLength);
		WimaxApiResponse response = WimaxApiResponse.fromInt(results);
		if (response == WimaxApiResponse.SUCCESS)
		{
			msg = buffer[0];
		}

		return msg;
	}



	/*************************************************************************
	 * Commands                                                              *
	 *************************************************************************/

	/**
	 * This method executes the API command specified by the cmd argument
	 * and passes the Object arguments into the command method.  The results
	 * from the command are stored in the instances variables of this object
	 * and status (int) is return to the receiver.<br><br>
	 *
	 * Not all the available commands are used or available at this time.<br><br>
	 *
	 * @param cmd int
	 * @param driver {@link android.net.wimax.base.Driver}
	 * @param args java.lang.Object
	 * @return int - Return code from the call {@link android.net.wimax.types.WimaxApiResponse}
	 */
	public WimaxApiResponse executeCommand(int cmd, Object args)
	{
		WimaxApiResponse results = null;

		switch (cmd)
		{
			case WiMaxAPIOpen:
				results = openApi(driver, READ_WRITE);
				break;
			case GetListDevice:
				results = getListOfDevices(driver);
				break;
			case WiMaxDeviceOpen:
				results = openTheDevice(driver);
				break;
			case WiMaxDeviceClose:
				results = closeTheDevice(driver);
				break;
			case WiMaxAPIClose:
				results = closeApi(driver);
				break;
			case CmdControlPowerManagementOn:
				results = controlPowerManagement(driver, POWER_ON);
				break;
			case CmdControlPowerManagementOff:
				results = controlPowerManagement(driver, POWER_OFF);
				break;
			case CmdResetWimaxDevice:
				results = resetWiMaxDevice(driver);
				break;
			case CmdResetToFactorySettings:
				break;
			case GetErrorString:
				break;
			case SetServiceProviderUnLock:
				break;
			case GetServiceProviderLockStatus:
				break;
			case GetNetworkList:
				results = getNetworkList(driver);
				break;
			case CmdConnectToNetwork:
				results = connectToNetwork(driver, args);
				break;
			case CmdDisconnectFromNetwork:
				results = disconnectFromNetwork(driver);
				break;
			case CmdNetworkSearchWideScan:
				results = networkWideScan(driver);
				break;
			case GetIPInterfaceIndex:
				results = getIPInterfaceIndex(driver);
				break;
			case GetSelectProfileList:
				results = getSelectProfileList(driver);
				break;
			case GetLinkStatus:
				results = getLinkStatus(driver);
				break;
			case GetDeviceInformation:
				results = getDeviceInformation(driver);
				break;
			case GetDeviceStatus:
				results = getDeviceStatus(driver);
				break;
			case GetConnectedNSP:
				results = getConnectedNSP(driver);
				break;
			case SetRoamingMode:
				break;
			case GetRoamingMode:
				break;
			case GetStatistics:
				results = getStatistics(driver);
				break;
			case GetProvisioningStatus:
				results = getProvisioningStatus(driver);
				break;
			case GetContactInformation:
				results = getContactInformation(driver);
				break;
			case GetPackageInformation:
				results = getPackageInformation(driver);
				break;
			case SetPackageUpdateState:
				results = setPackageUpdateState(driver, args);
				break;
			case SubscribeDeviceStatusChange:
				results = subscribeDeviceStatusChange(driver);
				break;
			case SubscribeDeviceInsertRemove:
				results = subscribeDeviceInsertRemove(driver);
				break;
			case SubscribeControlPowerManagement:
				results = subscribeControlPowerManagement(driver);
				break;
			case SubscribeConnectToNetwork:
				results = subscribeConnectToNetwork(driver);
				break;
			case SubscribeDisconnectToNetwork:
				results = subscribeDisconnectToNetwork(driver);
				break;
			case SubscribeNetworkSearchWideScan:
				results = subscribeNetworkSearchWideScan(driver);
				break;
			case SubscribeProvisioningOperation:
				results = subscribeProvisioningOperation(driver);
				break;
			case SubscribePackageUpdate:
				results = subscribePackageUpdate(driver);
				break;
			case UnsubscribeDeviceStatusChange:
				results = unsubscribeDeviceStatusChange(driver);
				break;
			case UnsubscribeDeviceInsertRemove:
				results = unsubscribeDeviceInsertRemove(driver);
				break;
			case UnsubscribeControlPowerManagement:
				results = unsubscribeControlPowerManagement(driver);
				break;
			case UnsubscribeConnectToNetwork:
				results = unsubscribeConnectToNetwork(driver);
				break;
			case UnsubscribeDisconnectToNetwork:
				results = unsubscribeDisconnectToNetwork(driver);
				break;
			case UnsubscribeNetworkSearchWideScan:
				results = unsubscribeNetworkSearchWideScan(driver);
				break;
			case UnsubscribeProvisioningOperation:
				results = unsubscribeProvisioningOperation(driver);
				break;
			case UnsubscribePackageUpdate:
				results = unsubscribePackageUpdate(driver);
				break;
			case 99: // API Close
				results = closeApi(driver);
				break;
		}

		return results;
	}
}
