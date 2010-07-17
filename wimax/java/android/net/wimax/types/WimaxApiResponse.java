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

package android.net.wimax.types;

/**
 * @hide
 */
public enum WimaxApiResponse {
	
	FIRST_COMMON_ERROR(0),
	SUCCESS(1),
	FAILED(2),
	BUFFER_SIZE_TOO_SMALL(3),
	PERMISSON_DENIED(4),
	INVALID_DEVICE(5),
	INVALID_PARAMETER(6),
	ALREADY_CONNECTED(7),
	LINK_NOT_CONNECTED(8),
	NETWORK_PROHIBITED(9),
	DEVICE_MISSING(10),
	INVALID_PROFILE(11),
	ROAMING_NOT_ALLOWED(12),
	CONNECTION_IN_PROGRESS(13),
	NOT_IMPLEMENTED(14),
	LAST_COMMON_ERROR(15);
	
	private int mValue;

    WimaxApiResponse(int value) {
        mValue = value;
    }
    
    private String[] descriptions = new String[]
	     	{
	    		"",
	    		"Successful.",
	    		"Failed.",
	    		"Return buffer size too small.",
	    		"Invalid privileges to execute the command.",
	    		"Device type is invalid.",
	    		"Invalid parameter passed.",
	    		"Already connected.",
	    		"Link is not connected.",
	    		"Connection to the network is prohibited.",
	    		"Device specified is missing.",
	    		"Profile specified is Invalid.",
	    		"Connection to a roaming network is not allowed.",
	    		"Connection is already in progress.",
	    		"Function not implemented.",
	    		""
	     	};
	
    /**
     * Create a WimaxApiResponse object.
     * @param value Integer value to be converted to a WimaxApiResponse object.
     * @return WimaxApiResponse object whose value is {@code value}. If no WimaxApiResponse object has
     *         that value, null is returned.
     */
    public static WimaxApiResponse fromInt(int value) {
        for (WimaxApiResponse e : WimaxApiResponse.values()) {
            if (e.mValue == value) {
                return e;
            }
        }
        return null;
    }
    
    /**
     * Returns the description of the current enum value.
     * @return String description
     */
    public String getDescription() {
    	return descriptions[ordinal()];
    }

}
