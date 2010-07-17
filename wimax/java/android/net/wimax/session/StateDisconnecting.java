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

/**
 * This class will stay in the disconnecting state until it's completed
 * successfully and then it will change the state to disconnected. *
 * Script:
 * <code>private int[] script = new int[] {
 * 			Connection.UnsubscribeDeviceStatusChange,
 * 			Connection.UnsubscribeDeviceInsertRemove,
 * 			Connection.UnsubscribeControlPowerManagement,
 * 			Connection.UnsubscribeConnectToNetwork,
 * 			Connection.UnsubscribeDisconnectToNetwork,
 * 			Connection.UnsubscribeNetworkSearchWideScan,
 * 			Connection.UnsubscribeProvisioningOperation,
 * 			Connection.UnsubscribePackageUpdate,
 * 			Connection.CmdDisconnectFromNetwork,
 * 			Connection.WiMaxDeviceClose,
 * 			Connection.WiMaxAPIClose
 * 			};</code>
 * @hide
 */
public class StateDisconnecting extends StateAbstract
{
	private int[] script = new int[] {
			Connection.UnsubscribeDeviceStatusChange,
			Connection.UnsubscribeDeviceInsertRemove,
			Connection.UnsubscribeControlPowerManagement,
			Connection.UnsubscribeConnectToNetwork,
			Connection.UnsubscribeDisconnectToNetwork,
			Connection.UnsubscribeNetworkSearchWideScan,
			Connection.UnsubscribeProvisioningOperation,
			Connection.UnsubscribePackageUpdate,
			Connection.CmdDisconnectFromNetwork,
			Connection.WiMaxDeviceClose,
			Connection.WiMaxAPIClose
			};

	/**
	 * Constructor that requires the driver and parent monitor so that callbacks can
	 * be sent back to the receiver(s) and current state can be updated.
	 *
	 * @param driver android.net.wimax.base.Driver
	 * @param parentMonitor android.net.wimax.session.StateMonitor
	 */
	public StateDisconnecting(Driver driver, StateMonitor parentMonitor)
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
		if (parentMonitor.requestedNetworkState == NetworkStates.STATE_CONNECTED ||
		    parentMonitor.requestedNetworkState == NetworkStates.STATE_DISCONNECTED ||
			parentMonitor.requestedNetworkState == NetworkStates.STATE_READY)
		{
			int status = execute();
			if (status == COMPLETED ||
				 status == FAILED)
			{
				setCurrentState(parentMonitor.states[parentMonitor.STATE_DISCONNECTED]);
			}
			else
			if (status == RUNNING)
			{
				parentMonitor.sendStateChangedBroadcast(WimaxState.DISCONNECTING);
			}
			else
				parentMonitor.goReady();
		}
		else
			checkTheStateChange(NetworkStates.STATE_DISCONNECTING);

		return getCurrentState();
	}

	/**
	 * This method disconnects from the network and returns the results
	 * to the receiver.
	 *
	 * @return int
	 */
	private int execute()
	{
		int returnCode = RUNNING;

		// Disconnect from the network
		if (scriptPosition < 0) scriptPosition = 0;

		driver.getConnection().executeCommand(script[scriptPosition], null);

		if ((scriptPosition+1) == script.length)
		{
			returnCode = COMPLETED;
			scriptPosition = 0;
		}
		else
			scriptPosition = scriptPosition + 1;

		return returnCode;
	}
}
