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

import android.net.wimax.base.Connection;
import android.net.wimax.base.Driver;
import android.net.wimax.structs.NSPInfo;
import android.net.wimax.types.ConnectionProgressStatus;
import android.net.wimax.types.DeviceStatus;
import android.net.wimax.types.WimaxApiResponse;

/**
 * This class will stay in the connecting state until it's completed
 * successfully and then it will change the state to connected.
 *
 * Script:
 * <code>	private int[] script = new int[] {
 * 			Connection.CmdConnectToNetwork,
 * 			Connection.GetDeviceStatus,
 * 			Connection.GetLinkStatus,
 * 			Connection.GetIPInterfaceIndex };</code>
 * @hide
 */
public class StateConnecting extends StateAbstract
{
	private int[] script = new int[] {
			Connection.CmdConnectToNetwork,
			Connection.GetDeviceStatus,
			Connection.GetLinkStatus,
			Connection.GetIPInterfaceIndex };

	/**
	 * Constructor that requires the driver and parent monitor so that callbacks can
	 * be sent back to the receiver(s) and current state can be updated.
	 *
	 * @param driver android.net.wimax.base.Driver
	 * @param parentMonitor android.net.wimax.session.StateMonitor
	 */
	public StateConnecting(Driver driver, StateMonitor parentMonitor)
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
		if (parentMonitor.requestedNetworkState == NetworkStates.STATE_CONNECTED)
		{
			int status = execute();
			if (status == COMPLETED)
			{
				setCurrentState(parentMonitor.states[parentMonitor.STATE_CONNECTED]);
			}
			else
			if (status == SCANNING)
			{
				setCurrentState(parentMonitor.states[parentMonitor.STATE_INITIALIZING]);
			}
			else
			if (status == FAILED)
			{
				setCurrentState(parentMonitor.states[parentMonitor.STATE_READY]);
			}
		}
		else
			checkTheStateChange(NetworkStates.STATE_CONNECTING);

		return getCurrentState();
	}

	/**
	 * Connect to the network.  For the first release the id/password
	 * is not used, authentication occurs on the network side.
	 *
	 * @return int
	 */
	public int execute()
	{
		int returnCode = RUNNING;

		if (scriptPosition < 0) scriptPosition = 0;

		if (script[scriptPosition] == Connection.CmdConnectToNetwork)
		{
			returnCode = executeCmdConnectToNetwork(returnCode, driver);
		}
		else
		if (script[scriptPosition] == Connection.GetDeviceStatus)
		{
			returnCode = executeGetDeviceStatus(returnCode, driver);
		}
		else
			returnCode = executeLinkAndIPInterfaceIndex(returnCode, driver);

		return returnCode;
	}

	/**
	 * Execute the WiMax Network Connect command.
	 *
	 * @param rc int
	 * @param driver android.net.wimax.base.Driver
	 * @return int
	 */
	private int executeCmdConnectToNetwork(int rc, Driver driver)
	{
		int returnCode = rc;

		if (driver != null &&
			 driver.getNSPInfo() != null && driver.getNSPInfo().length > 0)
		{
			NSPInfo nspInfo = driver.getNSPInfo(parentMonitor.getNSPToConnect());
			if(nspInfo == null){
				nspInfo = driver.getNSPInfo()[0];
			}

			String nspName = nspInfo.getNspName();
			int nspId      = nspInfo.getNspId();
			Object[] args = { nspName, new Integer(nspId), "" };
			WimaxApiResponse results = driver.getConnection().executeCommand(script[scriptPosition], args);

			if (results == WimaxApiResponse.SUCCESS ||
				 results == WimaxApiResponse.ALREADY_CONNECTED)
			{
				// Eventually need to add one to position and leave returnCode as running
				scriptPosition = scriptPosition + 1;
				returnCode = RUNNING;
			}else
				if (results == WimaxApiResponse.INVALID_DEVICE)
				{
					scriptPosition = -1;
					returnCode = FAILED;
				}
		}

		return returnCode;
	}

	/**
	 * Get the device status, if we're in ready mode then contiunue executing
	 * the script.
	 *
	 * @param rc int
	 * @param driver android.net.wimax.base.Driver
	 * @return int
	 */
	private int executeGetDeviceStatus(int rc, Driver driver)
	{
		int returnCode = rc;

		WimaxApiResponse results = driver.getConnection().executeCommand(script[scriptPosition], null);
		if (results == WimaxApiResponse.SUCCESS)
		{
			if(driver.getDeviceStatus() == DeviceStatus.SCANNING) {
					returnCode = RUNNING;
			}else if(driver.getDeviceStatus() == DeviceStatus.CONNECTING) {
				returnCode = RUNNING;
			}else if(driver.getDeviceStatus() == DeviceStatus.DATA_CONNECTED ||
					driver.getDeviceStatus() == DeviceStatus.READY)
			{
				if (driver.getConnectionProgressInfo() != ConnectionProgressStatus.RANGING)
					scriptPosition = scriptPosition + 1;
				returnCode = RUNNING;
			}else if(driver.getDeviceStatus() == DeviceStatus.UNINITIALIZED ||
					driver.getDeviceStatus() == DeviceStatus.RF_OFF_HW ||
					driver.getDeviceStatus() == DeviceStatus.RF_OFF_HW_SW ||
					driver.getDeviceStatus() == DeviceStatus.RF_OFF_SW)
			{
				scriptPosition = -1;
				returnCode = FAILED;
			}
		}

		return returnCode;
	}

	/**
	 * We're connected, continue issuing connecting script commands get
	 * link status and if successful, get Ip interface index.
	 *
	 * @param rc int
	 * @param driver android.net.wimax.base.Driver
	 * @return int
	 */
	private int executeLinkAndIPInterfaceIndex(int rc, Driver driver)
	{
		int returnCode = rc;

		//GetLinkStatus
		WimaxApiResponse results = driver.getConnection().executeCommand(script[scriptPosition], null);

		/**
		 * Check to see if really connected with a success or on a Mac 10.5 return code
		 * 8 see if there is link connected info (should be rc 0 in this case).
		 */
		if (results == WimaxApiResponse.SUCCESS ||
			 (results == WimaxApiResponse.LINK_NOT_CONNECTED &&
			  driver.getLinkStatusInfo() != null))
		{
			//GetIPInterfaceIndex
			results = driver.getConnection().executeCommand(script[scriptPosition+1], null);

//********************** Commented Out Because of Mac 10.5 Samsung Driver Failure *****************************
//			if (results == WimaxApiResponse.WIMAX_API_RET_SUCCESS)
//			{
				scriptPosition = -1;
				returnCode = COMPLETED;
//			}
//			else
//			{
//				returnCode = RUNNING;
//				scriptPosition = 0;  // Start back at the beginning
//			}
//********************** End Comments *****************************
		}
		else
			if (results == WimaxApiResponse.LINK_NOT_CONNECTED)
			{
				returnCode = SCANNING;
				scriptPosition = -1;
			}

		return returnCode;
	}
}
