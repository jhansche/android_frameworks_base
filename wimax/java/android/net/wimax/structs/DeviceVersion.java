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

package android.net.wimax.structs;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class contains the device version information.
 */
public class DeviceVersion implements Parcelable {
	
	/** {@hide} */
	public int structureSize = 0;
	/** {@hide} */
	public String name       = new String();
	/** {@hide} */
	public String version    = new String();
	
	/**
	 * This is the default constructor for the Device Version.
	 */
	public DeviceVersion() { }
	
	/** Constructor with fields. */	
	public DeviceVersion(String name, String version) {
		super();
		this.name = name;
		this.version = version;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: ");sb.append(this.getClass().getName());
  		sb.append("[  Structure size: ");sb.append(structureSize);
  		sb.append(",  Name: ");sb.append(name);
  		sb.append(",  Version: ");sb.append(version);
  		sb.append("  ]");
  		
		return sb.toString();
	}
	
    /** Implement the Parcelable interface {@hide} */
    public int describeContents() {
        return 0;
    }

    /** Implement the Parcelable interface {@hide} */
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeInt(structureSize);
        dest.writeString(name);
        dest.writeString(version);
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<DeviceVersion> CREATOR =
        new Creator<DeviceVersion>() {
            public DeviceVersion createFromParcel(Parcel in) {
                DeviceVersion info = new DeviceVersion();
                info.structureSize = in.readInt();
                info.name = in.readString();
                info.version = in.readString();
                return info;
            }

            public DeviceVersion[] newArray(int size) {
                return new DeviceVersion[size];
            }
        };
}
