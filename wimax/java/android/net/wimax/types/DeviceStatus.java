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
public enum DeviceStatus {
	
	UNINITIALIZED(0),
	RF_OFF_HW_SW(1),
	RF_OFF_HW(2),
	RF_OFF_SW(3),
	READY(4),
	SCANNING(5),
	CONNECTING(6),
	DATA_CONNECTED(7);
	
	private int mValue;

    DeviceStatus(int value) {
        mValue = value;
    }
    
    private String[] descriptions = new String[]
	     	{
	     		"Device is uninitialized.",
	     		"Device RF Off(both H/W and S/W).",
	     		"Device RF Off(via H/W switch).",
	     		"Device RF Off(via S/W switch).",
	     		"Device is ready.",
	     		"Device is scanning.",
	     		"Connection in progress.",
	     		"Layer 2 connected."
	     	};
	
    /**
     * Create a DeviceStatus object.
     * @param value Integer value to be converted to a DeviceStatus object.
     * @return DeviceStatus object whose value is {@code value}. If no DeviceStatus object has
     *         that value, null is returned.
     */
    public static DeviceStatus fromInt(int value) {
        for (DeviceStatus e : DeviceStatus.values()) {
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
    
    public int getValue() {
    	return mValue;
    }
}
