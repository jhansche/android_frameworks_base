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

import java.util.HashMap;

/**
 * Global Connection State Properties for the connection manager application.
 *
 * 1 = "Ready."
 * 3 = "Initializing."
 * 4 = "Scanning."
 * 5 = "Initialized."
 * 6 = "Connecting."
 * 7 = "Connected."
 * 8 = "Disconnecting."
 * 9 = "Disconnected."
 *
 * @hide
 */

@SuppressWarnings("serial")
public class NetworkStates
{
	// States
	public static final int STATE_READY                = 1;
	public static final int STATE_INITIALIZING         = 3;
	public static final int STATE_SCANNING		       = 4;
	public static final int STATE_INITIALIZED          = 5;
	public static final int STATE_CONNECTING           = 6;
	public static final int STATE_CONNECTED            = 7;
	public static final int STATE_DISCONNECTING        = 8;
	public static final int STATE_DISCONNECTED         = 9;

	public static final HashMap<Object, String> networkStateValues = new HashMap<Object, String>()
	{{
		put(1, "Ready.");
		put(3, "Initializing.");
		put(4, "Scanning.");
		put(5, "Initialized.");
		put(6, "Connecting.");
		put(7, "Connected.");
		put(8, "Disconnecting.");
		put(9, "Disconnected.");
	}};
}
