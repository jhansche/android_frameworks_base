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
public enum DeviceStatusChangeReason {
	
	NORMAL(0),
	FAILED_TO_CONNECT_TO_NETWORK(1),
	FAILED_TO_CONNECT_RANGING(2),
	FAILED_TO_CONNECT_SBC(3),
	FAILED_TO_CONNECT_EAP_AUTH_DEVICE(4),
	FAILED_TO_CONNECT_EAP_AUTH_USER(5),
	FAILED_TO_CONNECT_THREE_WAY_HANDSHAKE(6),
	FAILED_TO_CONNECT_REGISTRATION(7),
	FAILED_TO_CONNECT_DATA_PATH(8);
	
	private int mValue;

    DeviceStatusChangeReason(int value) {
        mValue = value;
    }
    
    private String[] descriptions = new String[]
	     	{
	    		"Normal.",
	    		"Failed to complete NW entry with the selected operator (unspecified reason).",
	    		"Failed to complete ranging.",
	    		"SBC phase failed.",
	    		"Security error. EAP authentication failed device level.",
	    		"Security error. EAP authentication failed user level.",
	    		"Security error. Handshake failed.",
	    		"Registration failed.",
	    		"Failed to initialize the data path (failed to perform DSA to one UL and one DL SFs)."
	     	};
	
    /**
     * Create a DeviceStatusChangeReason object.
     * @param value Integer value to be converted to a DeviceStatusChangeReason object.
     * @return DeviceStatusChangeReason object whose value is {@code value}. If no DeviceStatusChangeReason object has
     *         that value, null is returned.
     */
    public static DeviceStatusChangeReason fromInt(int value) {
        for (DeviceStatusChangeReason e : DeviceStatusChangeReason.values()) {
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
