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

public enum PackageUpdateStatus {
	
	RECEIVED(0),
	RECEIVED_LOWER_STACK(1),
	RECEIVED_FULL_STACK(2),
	RECEIVED_OMA_DM_CLIENT(3),
	STARTED(4),
	COMPLETED(5),
	FAILED_NETWORK_DISCONNECTED(6),
	FAILED_INVALID_PACKAGE(7),
	FAILED_BAD_AUTHENTICATION(8),
	FAILED(9);
	
	private int mValue;

    PackageUpdateStatus(int value) {
        mValue = value;
    }
    
    private String[] descriptions = new String[]
	     	{
				"WiMAX PACKAGE RECEIVED: A software update package is available for your WiMAX device. Do you want to start downloading it?",
				"Update package received for firmware update only.",
				"Update package received for firmware, SDK and driver.",
				"Update package received for OMA-DM client.",
				"WiMAX SOFTWARE UPDATE DOWNLOADED: It is HIGHLY recommended that you install this software update package. Do you want to install it?",
				"Update package successfully installed.",
				"Update package failed to install because of network disconnection.",
				"Update package failed to install because of invalid package.",
				"Update package failed to install because of bas authentication.",
				"WiMAX SOFTWARE FAILED DOWNLOAD: The package you attempted to download seems to have failed."
	     	};
	
    /**
     * Create a PackageUpdateStatus object.
     * @param value Integer value to be converted to a PackageUpdateStatus object.
     * @return PackageUpdateStatus object whose value is {@code value}. If no PackageUpdateStatus object has
     *         that value, null is returned.
     */
    public static PackageUpdateStatus fromInt(int value) {
        for (PackageUpdateStatus e : PackageUpdateStatus.values()) {
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
