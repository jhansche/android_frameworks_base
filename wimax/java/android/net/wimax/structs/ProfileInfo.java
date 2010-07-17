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
 * This class contains the information regarding the profile.
 *
 * @hide
 */
public class ProfileInfo implements Parcelable {

	/** {@hide} */
	public int structureSize  = 0;
	/** {@hide} */
	public int profileId    = 0;
	/** {@hide} */
	public String profileName = new String();

	/**
	 * This is the default constructor for the Profile Information.
	 */
	public ProfileInfo() { }

	/** Constructor with fields */
	public ProfileInfo(int profileId, String profileName) {
		super();
		this.profileId = profileId;
		this.profileName = profileName;
	}

	/**
	 * Return the profile id.
	 * @return the profileId
	 */
	public int getProfileId() {
		return profileId;
	}

	/**
	 * @param profileId the profileId to set
	 */
	public void setProfileId(int profileId) {
		this.profileId = profileId;
	}

	/**
	 * Return the profile name.
	 * @return the profileName
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * Set the profile name. The length of profileName should be less than 256 or it will get trimmed.
	 * @param profileName String
	 */
	public void setProfileName(String profileName)
	{
		int len = profileName.length();
		if (len > 256)
			this.profileName = profileName.substring(0, 256);
		else
			this.profileName = profileName;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: ");sb.append(this.getClass().getName());
  		sb.append("[  Structure Size: ");sb.append(structureSize);
  		sb.append(",  Profile id: ");sb.append(profileId);
  		sb.append(",  Profile name: ");sb.append(profileName);
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
    	dest.writeInt(profileId);
        dest.writeString(profileName);
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<ProfileInfo> CREATOR =
        new Creator<ProfileInfo>() {
            public ProfileInfo createFromParcel(Parcel in) {
                ProfileInfo info = new ProfileInfo();
                info.structureSize = in.readInt();
                info.profileId = in.readInt();
                info.profileName = in.readString();
                return info;
            }

            public ProfileInfo[] newArray(int size) {
                return new ProfileInfo[size];
            }
        };

}
