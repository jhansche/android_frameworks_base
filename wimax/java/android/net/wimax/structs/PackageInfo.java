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
 * This class contains the information regarding the Update Package.
 */
public class PackageInfo implements Parcelable {
	
	/** {@hide} */
	public int structureSize       = 0;
	/** {@hide} */
	public String filePath         = new String();
	/** {@hide} */
	public String fileName         = new String();
	/** {@hide} */
	public boolean forceReboot     = false;
	/** {@hide} */
	public boolean mandatoryUpdate = false;
	/** {@hide} */
	public boolean warnUser        = true;
	/** {@hide} */
	
	/**
	 * This is the default constructor for the Package Information.
	 */
	public PackageInfo() { }
		
	/** Constructor with fields */
	public PackageInfo(String filePath, String fileName, boolean forceReboot,
			boolean mandatoryUpdate, boolean warnUser) {
		super();
		this.filePath = filePath;
		this.fileName = fileName;
		this.forceReboot = forceReboot;
		this.mandatoryUpdate = mandatoryUpdate;
		this.warnUser = warnUser;
	}
	
	/**
	 * Return the file path for this update package.
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Return the file name of this update package.
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Force a reboot?
	 * @return the forceReboot
	 */
	public boolean isForceReboot() {
		return forceReboot;
	}

	/**
	 * @param forceReboot the forceReboot to set
	 */
	public void setForceReboot(boolean forceReboot) {
		this.forceReboot = forceReboot;
	}

	/**
	 * Is this a mandatoryUpdate?
	 * @return the mandatoryUpdate
	 */
	public boolean isMandatoryUpdate() {
		return mandatoryUpdate;
	}

	/**
	 * @param mandatoryUpdate the mandatoryUpdate to set
	 */
	public void setMandatoryUpdate(boolean mandatoryUpdate) {
		this.mandatoryUpdate = mandatoryUpdate;
	}

	/**
	 * Warn user of update/reboot?
	 * @return the warnUser
	 */
	public boolean isWarnUser() {
		return warnUser;
	}

	/**
	 * @param warnUser the warnUser to set
	 */
	public void setWarnUser(boolean warnUser) {
		this.warnUser = warnUser;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: ");sb.append(this.getClass().getName());
  		sb.append("[  Structure size: ");sb.append(structureSize);
  		sb.append(",  File Path: ");sb.append(filePath);
  		sb.append(",  File Name: ");sb.append(fileName);
  		sb.append(",  Force Reboot: ");sb.append(forceReboot);
  		sb.append(",  Mandatory Update: ");sb.append(mandatoryUpdate);
  		sb.append(",  Warn User: ");sb.append(warnUser);
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
        dest.writeString(filePath);
        dest.writeString(fileName);
        dest.writeInt(forceReboot ?1 :0);
        dest.writeInt(mandatoryUpdate ?1 :0);
        dest.writeInt(warnUser ?1 :0);
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<PackageInfo> CREATOR =
        new Creator<PackageInfo>() {
            public PackageInfo createFromParcel(Parcel in) {
                PackageInfo info = new PackageInfo();
                info.structureSize = in.readInt();
                info.filePath = in.readString();
                info.fileName = in.readString();
                info.forceReboot = (in.readInt() == 1) ?true :false;
                info.mandatoryUpdate = (in.readInt() == 1) ?true :false;
                info.warnUser = (in.readInt() == 1) ?true :false;
                return info;
            }

            public PackageInfo[] newArray(int size) {
                return new PackageInfo[size];
            }
        };	
}
