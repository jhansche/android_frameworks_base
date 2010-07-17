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
 * This class will stay in the Connected state until a state change request
 * comes in.
 *
 * Script that defines the Connected State:
 * <code>private int[] script = new int[] {
 * 			Connection.GetDeviceStatus,
 * 			Connection.GetLinkStatus };</code>
 *
 * @hide
 */
public class StateConnected extends StateAbstract
{
	private boolean broadcastSent = false;
	private int prevRssi = -1;
	private int[] script = new int[] {
			Connection.GetDeviceStatus,
			Connection.GetLinkStatus };

	/**
	 * Constructor that requires the driver and parent monitor so that callbacks can
	 * be sent back to the receiver(s) and current state can be updated.
	 *
	 * @param driver android.net.wimax.base.Driver
	 * @param parentMonitor android.net.wimax.session.StateMonitor
	 */
	public StateConnected(Driver driver, StateMonitor parentMonitor)
	{
		this.driver = driver;
		this.parentMonitor = parentMonitor;
	}

	/**
	 * Check to see if we need to execute a command or move on to another state.
	 *
	 * @return android.net.wimax.dc.StateAbstract - Super class of all states.
	 */
	public StateAbstract checkStateChange()
	{
		if (!broadcastSent)
		{
			parentMonitor.sendStateChangedBroadcast(WimaxState.CONNECTED);
		}

		monitor();

		if (parentMonitor.requestedNetworkState != NetworkStates.STATE_CONNECTED)
		{
			broadcastSent = false;
			checkTheStateChange(NetworkStates.STATE_CONNECTED);
		}

		return getCurrentState();
	}

	/**
	 * We're in the desired connected state, now just monitor the amount
	 * of data transfered and time elapsed.  If we lose network then go
	 * into the scanning state.
	 */
	private void monitor()
	{
		int status = execute();
		if (status == COMPLETED)
		{
			//Everything is fine
			int rssi = driver.getLinkStatusInfo().getRssiInDBm();
			if(prevRssi != rssi) {
				parentMonitor.sendRssiChangedBroadcast(rssi);
				prevRssi = rssi;
			}
		}
		else
		if (status == FAILED)
		{
			broadcastSent = false;

			// Something failed, lets connect again
			setCurrentState(parentMonitor.states[parentMonitor.STATE_CONNECTING]);
			parentMonitor.sendStateChangedBroadcast(WimaxState.CONNECTING);
		}
	}

	/**
	 * This script checks the state of the connection and the card, if
	 * it changes then it either goes into scanning or failed state.
	 *
	 * @return int - Return code (0=Running, 1=Scanning, 2=Completed, 3=Failed)
	 */
	public int execute()
	{
		int returnCode = RUNNING;

		if (scriptPosition < 0 ||
			 scriptPosition >= script.length) scriptPosition = 0;

		// Check if we're still connected (Health)
		WimaxApiResponse results = driver.getConnection().executeCommand(script[scriptPosition], null);

		if (script[scriptPosition] == Connection.GetDeviceStatus)
		{
			if (results == WimaxApiResponse.SUCCESS &&
				 driver.getDeviceStatus() != null &&
				 driver.getDeviceStatus() == DeviceStatus.DATA_CONNECTED)
			{
				scriptPosition = scriptPosition + 1;
			}
			else
			{
				if (driver.getDeviceStatus() == DeviceStatus.SCANNING ||
					 driver.getDeviceStatus() == DeviceStatus.CONNECTING)
				{
					returnCode = SCANNING;
					lastMessage = "Scanning for best possible connection.";
				}
				else
				{
					returnCode = FAILED;
				}
			}
		}
		else
		{
			scriptPosition = scriptPosition + 1;
			returnCode = COMPLETED;
		}

		return returnCode;
	}
}
