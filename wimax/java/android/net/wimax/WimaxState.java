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

package android.net.wimax;

import android.os.Parcel;
import android.os.Parcelable;

public enum WimaxState implements Parcelable {
	UNKNOWN(0),
	READY(1),
	INITIALIZING(3),
	SCANNING(4),
	INITIALIZED(5),
	CONNECTING(6),
	CONNECTED(7),
	DISCONNECTING(8),
	DISCONNECTED(9);

	private int mValue;

    WimaxState(int value) {
        mValue = value;
    }

    /**
     * Indicates whether wimax connectivity exists or not
     * @return {@code true} if wimax connectivity exists, {@code false} otherwise.
     */
    public boolean isConnected() {
        return mValue == CONNECTED.mValue;
    }

    /**
     * Create a WimaxState object.
     * @param value Integer value to be converted to a WimaxState object.
     * @return WimaxState object whose value is {@code value}. If no WimaxState object has
     *         that value, null is returned.
     */
    public static WimaxState fromInt(int value) {
        for (WimaxState e : WimaxState.values()) {
            if (e.mValue == value) {
                return e;
            }
        }
        return null;
    }

    public int getValue() {
    	return mValue;
    }

    /** Implement the Parcelable interface {@hide} */
    public int describeContents() {
        return 0;
    }

    /** Implement the Parcelable interface {@hide} */
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Parcelable.Creator<WimaxState> CREATOR = new Parcelable.Creator<WimaxState>() {
        public WimaxState createFromParcel(Parcel in) {
            return WimaxState.values()[in.readInt()];
        }

        public WimaxState[] newArray(int size) {
            return new WimaxState[size];
        }
    };
}
