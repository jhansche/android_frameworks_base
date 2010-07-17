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

public enum ProvisioningOperationStatus {
	
	STARTED(0),
	COMPLETED(1),
	FAILED_NETWORK_DISCONNECT(2),
	FAILED(3),
	FAILED_INVALID_PROVISIONING(4),
	FAILED_BAD_AUTHENTICATION(5),
	REQUEST_INITIAL_PROVISIONING(6),
	REQUEST_ONGOING_PROVISIONING(7),
	REQUEST_RESET_PROVISIONING(8),
	TRIGGER_CONTACT(9);
	
	private int mValue;

    ProvisioningOperationStatus(int value) {
        mValue = value;
    }
    
    private String[] descriptions = new String[]
	     	{
	    		"WiMAX UPDATE: Your WiMAX Adapter is being updated by the network. DO NOT REMOVE THE CARD UNTIL COMPLETION!",
	    		"WiMAX UPDATE: Your WiMAX Adapter has been updated.",
	    		"Update failed because of network disconnect.",
	    		"WiMAX UPDATE: Update failed. Please contact customer care for more details or help.",
	    		"Update failed because of invalid provisioning.",
	    		"Update failed because of authentication failure.",
	    		"Update requested initial provisioning.",
	    		"Update ongoing provisioning.",
	    		"Update requested reset provisioning.",
	    		"Specifies to invoke the URI specified in the contact information."
	     	};
	
    /**
     * Create a ProvisioningStatus object.
     * @param value Integer value to be converted to a ProvisioningStatus object.
     * @return ProvisioningStatus object whose value is {@code value}. If no ProvisioningStatus object has
     *         that value, null is returned.
     */
    public static ProvisioningOperationStatus fromInt(int value) {
        for (ProvisioningOperationStatus e : ProvisioningOperationStatus.values()) {
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
