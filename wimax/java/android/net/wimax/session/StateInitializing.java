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

import android.net.wimax.WimaxState;
import android.net.wimax.base.Connection;
import android.net.wimax.base.Driver;
import android.net.wimax.types.DeviceStatus;
import android.net.wimax.types.WimaxApiResponse;

/**
 * This class will stay in the initializing state until it's completed
 * successfully and then it will change the state to initialized.
 *
 * Status:
 * <code>int[] script = new int[] {
 * 			Connection.WiMaxAPIOpen,
 * 			Connection.GetListDevice,
 * 			Connection.WiMaxDeviceOpen,
 * 			Connection.SubscribeConnectToNetwork,
 * 			Connection.SubscribeDeviceInsertRemove,
 * 			Connection.SubscribeDisconnectToNetwork,
 * 			Connection.SubscribeControlPowerManagement,
 * 			Connection.SubscribeNetworkSearchWideScan,
 * 			Connection.SubscribePackageUpdate,
 * 			Connection.SubscribeProvisioningOperation,
 * 			Connection.SubscribeDeviceStatusChange,
 * 			Connection.GetDeviceStatus,
 * 			Connection.CmdControlPowerManagementOn,
 * 			Connection.GetDeviceStatus,
 * 			Connection.GetNetworkList,
 * 			Connection.GetDeviceStatus };</code>
 *
 * @hide
 */
public class StateInitializing extends StateAbstract
{
	private int networkScanCount = 0;
	int[] script = new int[] {
			Connection.WiMaxAPIOpen,
			Connection.GetListDevice,
			Connection.WiMaxDeviceOpen,
			Connection.SubscribeConnectToNetwork,
			Connection.SubscribeDeviceInsertRemove,
			Connection.SubscribeDisconnectToNetwork,
			Connection.SubscribeControlPowerManagement,
			Connection.SubscribeNetworkSearchWideScan,
			Connection.SubscribePackageUpdate,
			Connection.SubscribeProvisioningOperation,
			Connection.SubscribeDeviceStatusChange,
			Connection.GetDeviceStatus,
			Connection.CmdControlPowerManagementOn,
			Connection.GetDeviceStatus,
			Connection.GetNetworkList,
			Connection.GetDeviceStatus };

	/**
	 * Constructor that requires the driver and parent monitor so that callbacks can
	 * be sent back to the receiver(s) and current state can be updated.
	 *
	 * @param driver android.net.wimax.base.Driver
	 * @param parentMonitor android.net.wimax.session.StateMonitor
	 */
	public StateInitializing(Driver driver, StateMonitor parentMonitor)
	{
		this.driver = driver;
		this.parentMonitor = parentMonitor;
	}

	/**
	 * Check to see if we need to execute a command or move on to another state.
	 *
	 * @return android.net.wimax.dc.StateAbstract
	 */
	public StateAbstract checkStateChange()
	{
		if (parentMonitor.requestedNetworkState == NetworkStates.STATE_INITIALIZED ||
			parentMonitor.requestedNetworkState == NetworkStates.STATE_CONNECTED)
		{
			// Issue initialize command;
			int status = execute();
			if (status == SCANNING &&
				 getLastMessage() != null)
			{
				parentMonitor.sendStateChangedBroadcast(WimaxState.SCANNING);
			}
			else
			if (status == COMPLETED)
			{
				setCurrentState(parentMonitor.states[parentMonitor.STATE_INITIALIZED]);
				parentMonitor.sendStateChangedBroadcast(WimaxState.INITIALIZED);
			}
			else
			if (status == FAILED)
			{
				parentMonitor.goReady();
				parentMonitor.sendStateChangedBroadcast(WimaxState.READY);
			}
		}
		else
			checkTheStateChange(NetworkStates.STATE_INITIALIZING);

		return getCurrentState();
	}

	/**
	 * This method executes the initialization script that opens the
	 * common API, the device, device status and gets the initial device
	 * list.  It then returns true if successfully completed.
	 *
	 * @return int
	 */
	private int execute()
	{
		int returnCode = RUNNING;
		lastMessage = null;

		if (scriptPosition == -1) scriptPosition = 0;

		WimaxApiResponse results = null;
		try
		{
			results = driver.getConnection().executeCommand(script[scriptPosition], null);
			if (script[scriptPosition] == Connection.GetNetworkList) {
				networkScanCount++;
			}

			if (results == WimaxApiResponse.SUCCESS)
			{
				int rc = checkSuccessfulCall(driver);
				if (rc > -1) returnCode = rc;
			}
			else
			{
				// The power management call can fail, its okay.
				if (script[scriptPosition] == Connection.CmdControlPowerManagementOn)
				{
					scriptPosition = scriptPosition + 1;
				}
				else
				{
					int rc = checkUnsuccessfulCall(driver, results);
					if (rc > -1) returnCode = rc;
				}
			}
		}
		catch (Exception e)
		{

		}

		return returnCode;
	}

	/**
	 * This method checks the status of the current call and determines if we
	 * are to move on to the call in the script.
	 *
	 * @param driver android.net.wimax.cm.objs.Driver
	 * @return int
	 */
	private int checkSuccessfulCall(Driver driver)
	{
		int returnCode = -1;

		// If we're on the device list and we don't have any devices then fail
		if (script[scriptPosition] == Connection.GetListDevice &&
			 !driver.hasDevice())
		{
			lastMessage = "Device removed.";
			returnCode = FAILED;
		}
		else
		{
			// If we're getting device status then it has to be in device ready mode before moving on.
			if (script[scriptPosition] != Connection.GetDeviceStatus ||
				 (script[scriptPosition] == Connection.GetDeviceStatus &&
				  driver.getDeviceStatus() != null &&
				  (driver.getDeviceStatus() == DeviceStatus.READY ||
					driver.getDeviceStatus() == DeviceStatus.UNINITIALIZED)))
			{
				/*if (script[scriptPosition] == Connection.GetNetworkList)
				{
					if (driver.getNSPInfo() == null ||
						 driver.getNSPInfo().length == 0 ||
						 driver.getNSPInfo()[0] == null ||
						 driver.getNSPInfo()[0].getNspName() == null ||
						 driver.getNSPInfo()[0].getNspName().trim().equals(""))
					{
						scriptPosition = scriptPosition - 1;
						if(networkScanCount >= StateMonitor.NETWORK_SCAN_RETRY_COUNT) {
							returnCode = FAILED;
							lastMessage = "No WiMax Network Found";
							finalMessage = lastMessage;
							networkScanCount = 0;
						}else
							returnCode = RUNNING;
					}
				}*/

				scriptPosition = scriptPosition + 1;
				if (scriptPosition >= script.length)
				{
					scriptPosition = -1;
					returnCode = COMPLETED;
				}
			}
			else
			{
				int scanning = checkToSeeIfWeAreScanning(driver);
				if (scanning == SCANNING)
				{
					returnCode = SCANNING;
				}
			}
		}

		return returnCode;
	}

	/**
	 * This method checks the status of the current call and determines if we
	 * are to fail this script.
	 *
	 * @param driver android.net.wimax.cm.objs.Driver
	 * @param results int
	 * @return int
	 */
	private int checkUnsuccessfulCall(Driver driver, WimaxApiResponse results)
	{
		int returnCode = -1;

		if (results == WimaxApiResponse.INVALID_DEVICE)
		{
			if (driver.getConnection().isCardConnected())
				lastMessage = "Card is invalid.";
			else
				lastMessage = "WiMax Card missing, cannot initialize.";

			returnCode = FAILED;
		}
		else
		{
			if (results == WimaxApiResponse.FAILED)
			{
				if (script[scriptPosition] == Connection.GetNetworkList)
				{
					lastMessage = "No WiMax Network Found";
					finalMessage = lastMessage;
				}
				else
					lastMessage = "Call Failure.";

				returnCode = FAILED;
			}
		}

		return returnCode;
	}

	/**
	 * This method checks the status of the current call and determines if we
	 * are scanning for a network or not.
	 *
	 * @param driver android.net.wimax.cm.objs.Driver
	 * @return int
	 */
	private int checkToSeeIfWeAreScanning(Driver driver)
	{
		int scanning = -1;

		if (driver.getDeviceStatus() != null &&
			 driver.getDeviceStatus() == DeviceStatus.SCANNING)
		{
			// Don't want to continue to connect is we are still scanning (i.e. the prev getNetworkList
			// command failed or we just went into scan mode again.)
			if (script[scriptPosition-1] == Connection.GetNetworkList)
				scriptPosition = scriptPosition - 2;

			scanning = SCANNING;
			lastMessage = "Scanning for best possible connection.";
		}

		return scanning;
	}
}
