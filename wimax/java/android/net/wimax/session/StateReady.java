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
import android.net.wimax.base.Driver;

/**
 * This class will stay in the ready state until a request state change
 * comes in.
 * @hide
 */
public class StateReady extends StateAbstract
{
	private boolean reset = true;

	/**
	 * Constructor that requires the driver and parent monitor so that callbacks can
	 * be sent back to the receiver(s) and current state can be updated.
	 *
	 * @param driver android.net.wimax.base.Driver
	 * @param parentMonitor android.net.wimax.session.StateMonitor
	 */
	public StateReady(Driver driver, StateMonitor parentMonitor)
	{
		this.driver = driver;
		this.parentMonitor = parentMonitor;
	}

	/**
	 * Check to see if we need to execute a command or move on to another state.
	 *
	 * @return android.net.wimax.session.StateAbstract
	 */
	public StateAbstract checkStateChange()
	{
		// First time through set the current State
		if (getCurrentState() == null)
			setCurrentState(this);

		if (parentMonitor.requestedNetworkState != NetworkStates.STATE_READY)
		{
			reset = false;
			if (parentMonitor.requestedNetworkState == NetworkStates.STATE_INITIALIZED ||
				parentMonitor.requestedNetworkState == NetworkStates.STATE_CONNECTED)
			{
				setCurrentState(parentMonitor.states[parentMonitor.STATE_INITIALIZING]);

				// Initialize
				parentMonitor.sendStateChangedBroadcast(WimaxState.INITIALIZING);
			}
			else
			{
				checkTheStateChange(NetworkStates.STATE_READY);
			}
		}
		else
		{
			if (!reset)
			{
				parentMonitor.states[parentMonitor.STATE_INITIALIZING].resetScript();
				parentMonitor.sendStateChangedBroadcast(WimaxState.READY);

				reset = true;
			}
		}

		return getCurrentState();
	}
}
