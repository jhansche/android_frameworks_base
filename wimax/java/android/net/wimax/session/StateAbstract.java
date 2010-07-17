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
 * This class provides common attributes and behavior common between all
 * device states.
 *
 * @hide
 */
public abstract class StateAbstract
{
	protected Driver driver = null;
	protected StateMonitor parentMonitor = null;

	public int scriptPosition = -1;
	public String lastMessage  = null;
	public static String finalMessage = null;
	public static boolean registeredCallbackSent = false;  // Only want to send this callback once per connection

	// execute() Return Codes
	public static final int RUNNING   = 0;
	public static final int SCANNING  = 1;
	public static final int COMPLETED = 2;
	public static final int FAILED    = 3;


	/**
	 * Required method by the subclasses so that they can be executed to
	 * try to get to the state requested.
	 *
	 * @return android.net.wimax.session.StateAbstract
	 */
	public abstract StateAbstract checkStateChange();

	/**
	 * Return the current state the session is in.
	 *
	 * @return android.net.wimax.session.StateAbstract
	 */
	public StateAbstract getCurrentState()
	{
		return parentMonitor.currentState;
	}

	/**
	 * Set the current state the session is in.
	 *
	 * @param state android.net.wimax.session.StateAbstract
	 */
	public void setCurrentState(StateAbstract state)
	{
		parentMonitor.currentState = state;
	}

	/**
	 * Resets the script position to the beginning.
	 */
	public void resetScript()
	{
		scriptPosition = -1;
	}

	/**
	 * Return the last state message to the receiver.
	 *
	 * @return java.lang.String
	 */
	public String getLastMessage()
	{
		return lastMessage;
	}

	/**
	 * Requested state may have changed, determine what to do next.
	 *
	 * @param networkState int
	 * @see android.net.wimax.session.NetworkStates
	 */
	protected void checkTheStateChange(int networkState)
	{
		if (parentMonitor.requestedNetworkState == NetworkStates.STATE_READY ||
			parentMonitor.requestedNetworkState == NetworkStates.STATE_DISCONNECTED)
		{
			if (networkState == NetworkStates.STATE_CONNECTING ||
				 networkState == NetworkStates.STATE_CONNECTED ||
				 networkState == NetworkStates.STATE_INITIALIZING ||
				 networkState == NetworkStates.STATE_INITIALIZED)
			{
				setCurrentState(parentMonitor.states[parentMonitor.STATE_DISCONNECTING]);
			}
			else
			{
				if (parentMonitor.requestedNetworkState == NetworkStates.STATE_READY)
					setCurrentState(parentMonitor.states[parentMonitor.STATE_READY]);
			}
		}
		else
		{
			if (parentMonitor.requestedNetworkState == NetworkStates.STATE_CONNECTED ||
				parentMonitor.requestedNetworkState == NetworkStates.STATE_INITIALIZED)
			{
				if (networkState == NetworkStates.STATE_DISCONNECTING ||
					 networkState == NetworkStates.STATE_DISCONNECTED ||
					 networkState == NetworkStates.STATE_READY)
				{
					setCurrentState(parentMonitor.states[parentMonitor.STATE_INITIALIZING]);
					parentMonitor.sendStateChangedBroadcast(WimaxState.INITIALIZING);
				}
				else
				{
					if (networkState == NetworkStates.STATE_INITIALIZING ||
						 networkState == NetworkStates.STATE_INITIALIZED ||
						 networkState == NetworkStates.STATE_CONNECTING)
					{
						// Nothing to do, in correct state
					}
					else
					{
						// Start Connecting
						setCurrentState(parentMonitor.states[parentMonitor.STATE_INITIALIZING]);
						parentMonitor.sendStateChangedBroadcast(WimaxState.INITIALIZING);
					}
				}
			}
		}
	}
}
