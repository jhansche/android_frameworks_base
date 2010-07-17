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

public enum ConnectionProgressStatus {
	RANGING(0),
	SBC(1),
	EAP_AUTHENTICATION_DEVICE(2),
	EAP_AUTHENTICATION_USER(3),
	THREE_WAY_HANDSHAKE(4),
	REGISTERATION(5),
	DEREGISTRATION(6),
	REGISTERED(7);
	
	private int mValue;

    ConnectionProgressStatus(int value) {
        mValue = value;
    }
    
    private String[] descriptions = new String[]
	        {
	    		"Device is in Ranging.",
	    		"Device is in SBC.",
	    		"Device is in EAP authentication Device.",
	    		"Device is in EAP authentication User.",
	    		"Device is in 3-way-handshake.",
	    		"Device is in Registration.",
	    		"Device is in De-registration.",
	    		"Device is registered (operational)."
	    	};	
    /**
     * Create a ConnectionProgressStatus object.
     * @param value Integer value to be converted to a ConnectionProgressStatus object.
     * @return ConnectionProgressStatus object whose value is {@code value}. If no ConnectionProgressStatus object has
     *         that value, null is returned.
     */
    public static ConnectionProgressStatus fromInt(int value) {
        for (ConnectionProgressStatus e : ConnectionProgressStatus.values()) {
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
