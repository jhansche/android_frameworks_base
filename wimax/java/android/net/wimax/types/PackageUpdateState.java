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

public enum PackageUpdateState {
	
	DELAY(0),
	ACCEPTED(1),
	DENIED(2);
	
	private int mValue;

    PackageUpdateState(int value) {
        mValue = value;
    }
    
    private String[] descriptions = new String[]
	     	{
	    		"Package update was not accepted by the user.",
	    		"Package update was accepted by the user.",
	    		"Package update was denied by the user."
	     	};
	
    /**
     * Create a PackageUpdateState object.
     * @param value Integer value to be converted to a PackageUpdateState object.
     * @return PackageUpdateState object whose value is {@code value}. If no PackageUpdateState object has
     *         that value, null is returned.
     */
    public static PackageUpdateState fromInt(int value) {
        for (PackageUpdateState e : PackageUpdateState.values()) {
            if (e.mValue == value) {
                return e;
            }
        }
        return null;
    }
    
    public int getValue() {
    	return mValue;
    }
    
    /**
     * Returns the description of the current enum value.
     * @return String description
     */
    public String getDescription() {
    	return descriptions[ordinal()];
    }
}
