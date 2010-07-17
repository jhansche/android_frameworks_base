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
 * This class contains the information regarding the package update contacts.
 */
public class ContactInfo implements Parcelable {
	
	/** {@hide} */
	public int structureSize = 0;
	/** {@hide} */
	public String textForUri = new String();
	/** {@hide} */
	public String uri        = new String();
	/** {@hide} */
	public int contactType   = 0;
	
	/**
	 * This is the default constructor for the Contact Information.
	 */
	public ContactInfo() { }	
	
	/**
	 * Constructor with fields.
	 */
	public ContactInfo(String textForUri, String uri, int contactType) {
		super();
		this.textForUri = textForUri;
		this.uri = uri;
		this.contactType = contactType;
	}
	
	/**
	 * Return the text associated with the URI.
	 * @return the textForUri
	 */
	public String getTextForUri() {
		return textForUri;
	}

	/**
	 * @param textForUri the textForUri to set
	 */
	public void setTextForUri(String textForUri) {
		this.textForUri = textForUri;
	}

	/**
	 * Return the string value of the URI.
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Return the contact type.
	 * @return the contactType
	 */
	public int getContactType() {
		return contactType;
	}

	/**
	 * @param contactType the contactType to set
	 */
	public void setContactType(int contactType) {
		this.contactType = contactType;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: ");sb.append(this.getClass().getName());
  		sb.append("[  Structure size: ");sb.append(structureSize);
  		sb.append(",  Text For URI: ");sb.append(textForUri);
  		sb.append(",  URI: ");sb.append(uri);
  		sb.append(",  Contact Type: ");sb.append(contactType);
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
        dest.writeString(textForUri);
        dest.writeString(uri);
        dest.writeInt(contactType);
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<ContactInfo> CREATOR =
        new Creator<ContactInfo>() {
            public ContactInfo createFromParcel(Parcel in) {
                ContactInfo info = new ContactInfo();
                info.structureSize = in.readInt();
                info.textForUri = in.readString();
                info.uri = in.readString();
                info.contactType = in.readInt();
                return info;
            }

            public ContactInfo[] newArray(int size) {
                return new ContactInfo[size];
            }
        };
}
