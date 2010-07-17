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

import android.os.Parcelable;
import android.os.Parcel;

/**
 * This class contains the information regarding the Interface.
 *
 * @hide
 */
public class InterfaceInfo implements Parcelable {

	/** {@hide} */
	public int structureSize 	= 0;
	/** {@hide} */
	public String interfaceName = new String();

	/**
	 * This is the default constructor for the Interface Information.
	 */
	public InterfaceInfo() { }

	/** Constructor with fields */
	public InterfaceInfo(String interfaceName) {
		super();
		this.interfaceName = interfaceName;
	}

	/**
	 * @return the interfaceName
	 */
	public String getInterfaceName() {
		return interfaceName;
	}

	/**
	 * @param interfaceName the interfaceName to set
	 */
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: ");sb.append(this.getClass().getName());
  		sb.append("[  Structure Size: ");sb.append(structureSize);
  		sb.append(",  Interface Name: ");sb.append(interfaceName);
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
        dest.writeString(interfaceName);
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<InterfaceInfo> CREATOR =
        new Creator<InterfaceInfo>() {
            public InterfaceInfo createFromParcel(Parcel in) {
                InterfaceInfo info = new InterfaceInfo();
                info.structureSize = in.readInt();
                info.interfaceName = in.readString();
                return info;
            }

            public InterfaceInfo[] newArray(int size) {
                return new InterfaceInfo[size];
            }
        };
}
