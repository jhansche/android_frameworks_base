/*
 * Copyright 2009, The Android Open Source Project
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

#define LOG_TAG "wimax"

#include "jni.h"
#include <utils/misc.h>
#include <android_runtime/AndroidRuntime.h>
#include <utils/Log.h>

#include "WiMaxAPI.h"

#define WIMAX_PKG_NAME "android/net/wimax/WimaxCommonAPI"
typedef unsigned char byte;

namespace android {

static jboolean android_net_wimax_loadLibrary(JNIEnv* env, jobject clazz);
static jboolean android_net_wimax_unloadLibrary(JNIEnv* env, jobject clazz);
void fun_IndDeviceStatusUpdate(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_DEVICE_STATUS deviceStatus,
	WIMAX_API_STATUS_REASON statusReason, WIMAX_API_CONNECTION_PROGRESS_INFO connectionProgressInfo);
void fun_IndDeviceInsertRemove(WIMAX_API_DEVICE_ID_P pDeviceId, BOOL cardPresence);
void fun_IndControlPowerManagement(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_RF_STATE powerState);
void fun_IndConnectToNetwork(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_NETWORK_CONNECTION_RESP networkConnectionResponse);
void fun_IndDisconnectToNetwork(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_NETWORK_CONNECTION_RESP networkDisconnectResponse);
void fun_IndNetworkSearchWideScan(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_NSP_INFO_P pNspList, UINT32 listSize);
void fun_IndProvisioningOperation(WIMAX_API_DEVICE_ID_P pDeviceId,  WIMAX_API_PROV_OPERATION provisoningOperation,
	WIMAX_API_CONTACT_TYPE contactType);
void fun_IndPackageUpdate(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_PACK_UPDATE packageUpdate);

static JavaVM *g_jVM;

static jobject deviceStatusChangeCB;
static jobject deviceInsertRemoveCB;
static jobject controlPowerManagementCB;
static jobject connectToNetworkCB;
static jobject disconnectToNetworkCB;
static jobject networkSearchWideScanCB;
static jobject provisioningOperationCB;
static jobject packageUpdateCB;

static struct fieldIds {
    jclass deviceIdClass;
    jmethodID constructorId;
    jfieldID structureSize;
    jfieldID sdkHandle;
    jfieldID privilege;
    jfieldID deviceIndex;
    jfieldID apiVersion;
    jfieldID devicePresenceStatus;
} deviceIdFieldIds;

static WIMAX_API_DEVICE_ID_P deviceId;

static void accessDeviceId(JNIEnv* env, jobject jdeviceId, WIMAX_API_DEVICE_ID_P deviceId)
{
	if (deviceIdFieldIds.deviceIdClass != NULL) {
        deviceId->structureSize = env->GetIntField(jdeviceId, deviceIdFieldIds.structureSize);
        deviceId->sdkHandle = env->GetIntField(jdeviceId, deviceIdFieldIds.sdkHandle);
        deviceId->privilege = (WIMAX_API_PRIVILEGE)env->GetIntField(jdeviceId, deviceIdFieldIds.privilege);
        deviceId->deviceIndex = env->GetByteField(jdeviceId, deviceIdFieldIds.deviceIndex);
        deviceId->apiVersion = env->GetIntField(jdeviceId, deviceIdFieldIds.apiVersion);
        deviceId->devicePresenceStatus = env->GetBooleanField(jdeviceId, deviceIdFieldIds.devicePresenceStatus);
    }
}

static void assignDeviceId(JNIEnv* env, jobject jdeviceId, WIMAX_API_DEVICE_ID_P deviceId)
{
	if (deviceIdFieldIds.deviceIdClass != NULL) {
        env->SetIntField(jdeviceId, deviceIdFieldIds.structureSize, deviceId->structureSize);
        env->SetIntField(jdeviceId, deviceIdFieldIds.sdkHandle, deviceId->sdkHandle);
        env->SetIntField(jdeviceId, deviceIdFieldIds.privilege, deviceId->privilege);
        env->SetByteField(jdeviceId, deviceIdFieldIds.deviceIndex, deviceId->deviceIndex);
        env->SetIntField(jdeviceId, deviceIdFieldIds.apiVersion, deviceId->apiVersion);
        env->SetBooleanField(jdeviceId, deviceIdFieldIds.devicePresenceStatus, deviceId->devicePresenceStatus);
    }
}

static void assignString(JNIEnv* env, jclass jCls, jobject jObj, const char* fieldName, WIMAX_API_STRING cstring)
{
	jstring jStr = env->NewStringUTF(cstring);
	if (jStr) {
		jfieldID strFieldId = env->GetFieldID(jCls, fieldName,"Ljava/lang/String;");
		if (strFieldId) {
			env->SetObjectField(jObj, strFieldId, jStr);
		}
	}
}

static void assignLong(JNIEnv* env, jclass jCls, jobject jObj, const char* fieldName, long clong)
{
	jfieldID longFieldId = env->GetFieldID(jCls, fieldName, "J");
	if(longFieldId) {
		env->SetLongField(jObj, longFieldId, clong);
	}
}

static void assignInt(JNIEnv* env, jclass jCls, jobject jObj, const char* fieldName, int cint)
{
	jfieldID intFieldId = env->GetFieldID(jCls, fieldName, "I");
	if(intFieldId) {
		env->SetIntField(jObj, intFieldId, cint);
	}
}

static void assignByte(JNIEnv* env, jclass jCls, jobject jObj, const char* fieldName, byte cbyte)
{
	jfieldID byteFieldId = env->GetFieldID(jCls, fieldName, "B");
	if(byteFieldId) {
		env->SetByteField(jObj, byteFieldId, cbyte);
	}
}

static void assignBoolean(JNIEnv* env, jclass jCls, jobject jObj, const char* fieldName, bool cboolean)
{
	jfieldID boolFieldId = env->GetFieldID(jCls, fieldName, "Z");
	if(boolFieldId) {
		env->SetBooleanField(jObj, boolFieldId, cboolean);
	}
}

static void assignChar(JNIEnv* env, jclass jCls, jobject jObj, const char* fieldName, char cchar)
{
	jfieldID charFieldId = env->GetFieldID(jCls, fieldName, "C");
	if(charFieldId) {
		env->SetCharField(jObj, charFieldId, cchar);
	}
}

static jobject getObjectReference(JNIEnv *env, jclass jCls, jobject jObj, const char* fieldName, const char* lpszFieldSig)
{
	jfieldID objFieldId = env->GetFieldID(jCls, fieldName, lpszFieldSig);
	if (objFieldId) {
		jobject objField = env->GetObjectField(jObj, objFieldId);
		return(objField);
   	}else
   		return (0);
}

// ----------------------------------------------------------------------------

/*
 * Common Api methods.
 */

static jboolean android_net_wimax_loadLibrary(JNIEnv* env, jobject clazz)
{
	env->GetJavaVM(&g_jVM);
    return (jboolean)(JNI_TRUE);
}

static jboolean android_net_wimax_unloadLibrary(JNIEnv* env, jobject clazz)
{
    return (jboolean)(JNI_TRUE);
}

static jint android_net_wimax_wimaxAPIOpen(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
	WIMAX_API_DEVICE_ID_P tempDeviceId = new WIMAX_API_DEVICE_ID();
	accessDeviceId(env, jdeviceId, tempDeviceId);
	deviceId = tempDeviceId;

    WIMAX_API_RET result = ::WiMaxAPIOpen(deviceId);
    assignDeviceId(env, jdeviceId, deviceId);

    return (jint)result;
}

static jint android_net_wimax_getListDevice(JNIEnv* env, jobject clazz, jobject jdeviceId, jobjectArray hwDeviceIdList, jintArray size)
{
	UINT32 listSize = env->GetArrayLength(hwDeviceIdList);
	WIMAX_API_HW_DEVICE_ID_P hwIdList = new WIMAX_API_HW_DEVICE_ID[listSize];

    WIMAX_API_RET result = ::GetListDevice(deviceId, hwIdList, &listSize);
    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->FindClass("android/net/wimax/structs/HardwareDeviceId");
    	jmethodID cons = env->GetMethodID(cls, "<init>", "()V");
    	for(int i=0; i<(int)listSize; i++) {
    		jobject hwId = env->NewObject(cls, cons);
    		assignInt(env, cls, hwId, "structureSize", hwIdList[i].structureSize);
    		assignByte(env, cls, hwId, "deviceIndex", hwIdList[i].deviceIndex);
    		assignString(env, cls, hwId, "deviceName", hwIdList[i].deviceName);
    		assignInt(env, cls, hwId, "deviceType", hwIdList[i].deviceType);
    		env->SetObjectArrayElement(hwDeviceIdList, i, hwId);

    		if(hwIdList[i].deviceIndex != 0) {
    			deviceId->deviceIndex = hwIdList[i].deviceIndex;
    		}
    	}
    }

    jint region[1] = {listSize};
    env->SetIntArrayRegion(size, 0, 1, region);

    return (jint)result;
}

static jint android_net_wimax_wimaxDeviceOpen(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
	WIMAX_API_DEVICE_ID_P tempDeviceId = new WIMAX_API_DEVICE_ID();
	accessDeviceId(env, jdeviceId, tempDeviceId);
	if(tempDeviceId->deviceIndex != 0) {
		deviceId = tempDeviceId;
	}
    return (jint)::WiMaxDeviceOpen(deviceId);
}

static jint android_net_wimax_wimaxDeviceClose(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::WiMaxDeviceClose(deviceId);
}

static jint android_net_wimax_wimaxAPIClose(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    WIMAX_API_RET result = ::WiMaxAPIClose(deviceId);
    deviceId = NULL;
    return (jint)result;
}

static jint android_net_wimax_cmdControlPowerManagement(JNIEnv* env, jobject clazz, jint pwrState)
{
	WIMAX_API_RF_STATE state = WIMAX_API_RF_ON;
	if(pwrState == 1) {
		state = WIMAX_API_RF_OFF;
	}
    return (jint)::CmdControlPowerManagement(deviceId, state);
}

static jint android_net_wimax_cmdResetWimaxDevice(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::CmdResetWimaxDevice(deviceId);
}

static jint android_net_wimax_cmdResetToFactorySettings(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::CmdResetToFactorySettings(deviceId);
}

static jint android_net_wimax_getErrorString(JNIEnv* env, jobject clazz, jobject jdeviceId, jint errorCode, jobjectArray buffer, jintArray length)
{
	UINT32 buf_size;

	WIMAX_API_STRING c_buffer = new CHAR[MAX_SIZE_OF_STRING_BUFFER];
	WIMAX_API_RET errCode = (WIMAX_API_RET) errorCode;
	WIMAX_API_RET result = ::GetErrorString(deviceId, errCode, c_buffer, &buf_size);
    if(result == WIMAX_API_RET_SUCCESS) {
    	env->SetObjectArrayElement(buffer, 0, env->NewStringUTF(c_buffer));
    }

	jint region[1] = {buf_size};
    env->SetIntArrayRegion(length, 0, 1, region);

    return (jint)result;
}

static jint android_net_wimax_setServiceProviderUnLock(JNIEnv* env, jobject clazz, jobject jdeviceId, jstring lockCode)
{
	jboolean isCopy;
	const char *code = env->GetStringUTFChars(lockCode, &isCopy);

    WIMAX_API_RET result = ::SetServiceProviderUnLock(deviceId, (WIMAX_API_STRING)code);
    env->ReleaseStringUTFChars(lockCode, code);

    return (jint)result;
}

static jint android_net_wimax_getServiceProviderLockStatus(JNIEnv* env, jobject clazz, jobject jdeviceId, jintArray lockStatus, jobjectArray nspName)
{
	WIMAX_API_STRING c_nsp = new CHAR[MAX_SIZE_OF_NSP_NAME];
	WIMAX_API_LOCK_STATUS status;

    WIMAX_API_RET result = ::GetServiceProviderLockStatus(deviceId, &status, c_nsp);
    if(result == WIMAX_API_RET_SUCCESS) {
    	env->SetObjectArrayElement(nspName, 0, env->NewStringUTF(c_nsp));
    }

	jint region[1] = {(int)status};
    env->SetIntArrayRegion(lockStatus, 0, 1, region);

    return (jint)result;
}

static jint android_net_wimax_getNetworkList(JNIEnv* env, jobject clazz, jobject jdeviceId, jobjectArray networkList, jintArray size)
{
	UINT32 listSize = env->GetArrayLength(networkList);
	WIMAX_API_NSP_INFO_P nspList = new WIMAX_API_NSP_INFO[listSize];

    WIMAX_API_RET result = ::GetNetworkList(deviceId, nspList, &listSize);
    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->FindClass("android/net/wimax/structs/NSPInfo");
    	jmethodID cons = env->GetMethodID(cls, "<init>", "()V");
    	for(int i=0; i<(int)listSize; i++) {
    		jobject nsp = env->NewObject(cls, cons);
    		assignInt(env, cls, nsp, "structureSize", nspList[i].structureSize);
    		assignString(env, cls, nsp, "nspName", nspList[i].NSPName);
    		assignInt(env, cls, nsp, "nspId", nspList[i].NSPid);
    		assignByte(env, cls, nsp, "rssi", nspList[i].RSSI);
    		assignByte(env, cls, nsp, "cinr", nspList[i].CINR);
    		assignInt(env, cls, nsp, "networkType", nspList[i].networkType);
    		env->SetObjectArrayElement(networkList, i, nsp);
    	}
    }

    jint region[1] = {listSize};
    env->SetIntArrayRegion(size, 0, 1, region);

    return (jint)result;
}

static jint android_net_wimax_cmdConnectToNetwork(JNIEnv* env, jobject clazz, jobject jdeviceId, jstring nspName, jint profileId, jstring password)
{
    jboolean isCopy;
	const char *nsp = env->GetStringUTFChars(nspName, &isCopy);
	const char *passwd = env->GetStringUTFChars(password, &isCopy);

    WIMAX_API_RET result = ::CmdConnectToNetwork(deviceId, (WIMAX_API_STRING)nsp, profileId, (WIMAX_API_STRING)passwd);
    env->ReleaseStringUTFChars(nspName, nsp);
    env->ReleaseStringUTFChars(password, passwd);

    return (jint)result;
}

static jint android_net_wimax_cmdDisconnectFromNetwork(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::CmdDisconnectFromNetwork(deviceId);
}

static jint android_net_wimax_cmdNetworkSearchWideScan(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::CmdNetworkSearchWideScan(deviceId);
}

static jint android_net_wimax_getIPInterfaceIndex(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject interfaceInfo)
{
	WIMAX_API_INTERFACE_INFO_P info = new WIMAX_API_INTERFACE_INFO();
    WIMAX_API_RET result = ::GetIPInterfaceIndex(deviceId, info);

    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->GetObjectClass(interfaceInfo);
		assignInt(env, cls, interfaceInfo, "structureSize", info->structureSize);
		assignString(env, cls, interfaceInfo, "interfaceName", info->interfaceName);
    }

    return (jint)result;
}

static jint android_net_wimax_getSelectProfileList(JNIEnv* env, jobject clazz, jobject jdeviceId, jobjectArray profileList, jintArray size)
{
    UINT32 listSize = env->GetArrayLength(profileList);
	WIMAX_API_PROFILE_INFO_P profiles = new WIMAX_API_PROFILE_INFO[listSize];

    WIMAX_API_RET result = ::GetSelectProfileList(deviceId, profiles, &listSize);
    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->FindClass("android/net/wimax/structs/ProfileInfo");
    	jmethodID cons = env->GetMethodID(cls, "<init>", "()V");
    	for(int i=0; i<(int)listSize; i++) {
    		jobject prof = env->NewObject(cls, cons);
    		assignInt(env, cls, prof, "structureSize", profiles[i].structureSize);
    		assignInt(env, cls, prof, "profileId", profiles[i].profileID);
    		assignString(env, cls, prof, "profileName", profiles[i].profileName);
    		env->SetObjectArrayElement(profileList, i, prof);
    	}
    }

    jint region[1] = {listSize};
    env->SetIntArrayRegion(size, 0, 1, region);

    return (jint)result;
}

static jint android_net_wimax_getLinkStatus(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject linkStatus)
{
	WIMAX_API_LINK_STATUS_INFO_P info = new WIMAX_API_LINK_STATUS_INFO();
    WIMAX_API_RET result = ::GetLinkStatus(deviceId, info);

    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->GetObjectClass(linkStatus);
		assignInt(env, cls, linkStatus, "structureSize", info->structureSize);
		assignInt(env, cls, linkStatus, "centerFrequency", info->centerFrequency);
		assignByte(env, cls, linkStatus, "rssi", info->RSSI);
		assignByte(env, cls, linkStatus, "cinr", info->CINR);
		assignByte(env, cls, linkStatus, "txPwr", info->txPWR);

		jfieldID bsidFieldId = env->GetFieldID(cls, "bsid", "[B");
		if(bsidFieldId) {
			jbyteArray bsid = (jbyteArray)env->GetObjectField(linkStatus, bsidFieldId);
			jbyte* jb_array = env->GetByteArrayElements(bsid, NULL);
   			for(int i=0; i<6; i++)
   				jb_array[i] = info->bsId[i];
    		env->ReleaseByteArrayElements(bsid, jb_array, 0);
		}
    }

    return (jint)result;
}

static jint android_net_wimax_getDeviceInformation(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject deviceInfo)
{
	WIMAX_API_DEVICE_INFO_P info = new WIMAX_API_DEVICE_INFO();
    WIMAX_API_RET result = ::GetDeviceInformation(deviceId, info);

    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->GetObjectClass(deviceInfo);
		assignInt(env, cls, deviceInfo, "structureSize", info->structureSize);

		jclass cls0 = env->FindClass("android/net/wimax/structs/DeviceVersion");
		jobject hwVersion = getObjectReference(env, cls, deviceInfo, "hwVersion", "Landroid/net/wimax/structs/DeviceVersion;");
		assignInt(env, cls0, hwVersion, "structureSize", info->hwVersion.structureSize);
		assignString(env, cls0, hwVersion, "name", info->hwVersion.name);
		assignString(env, cls0, hwVersion, "version", info->hwVersion.version);

		jobject swVersion = getObjectReference(env, cls, deviceInfo, "swVersion", "Landroid/net/wimax/structs/DeviceVersion;");
		assignInt(env, cls0, swVersion, "structureSize", info->swVersion.structureSize);
		assignString(env, cls0, swVersion, "name", info->swVersion.name);
		assignString(env, cls0, swVersion, "version", info->swVersion.version);

		jobject rfVersion = getObjectReference(env, cls, deviceInfo, "rfVersion", "Landroid/net/wimax/structs/DeviceVersion;");
		assignInt(env, cls0, rfVersion, "structureSize", info->rfVersion.structureSize);
		assignString(env, cls0, rfVersion, "name", info->rfVersion.name);
		assignString(env, cls0, rfVersion, "version", info->rfVersion.version);

		jobject asicVersion = getObjectReference(env, cls, deviceInfo, "asicVersion", "Landroid/net/wimax/structs/DeviceVersion;");
		assignInt(env, cls0, asicVersion, "structureSize", info->asicVersion.structureSize);
		assignString(env, cls0, asicVersion, "name", info->asicVersion.name);
		assignString(env, cls0, asicVersion, "version", info->asicVersion.version);

		jfieldID macFieldId = env->GetFieldID(cls, "macAddress", "[B");
		if(macFieldId) {
			jbyteArray macAddress = (jbyteArray)env->GetObjectField(deviceInfo, macFieldId);
			jbyte* jb_array = env->GetByteArrayElements(macAddress, NULL);
   			for(int i=0; i<6; i++)
   				jb_array[i] = info->macAddress[i];
    		env->ReleaseByteArrayElements(macAddress, jb_array, 0);
		}

		assignString(env, cls, deviceInfo, "vendorName", info->vendorName);
		assignBoolean(env, cls, deviceInfo, "vendorSpecificInfoIncl", info->vendorSpecificInfoIncl);
		assignString(env, cls, deviceInfo, "vendorSpecificInfo", info->vendorSpecificInfo);
	}

    return (jint)result;
}

static jint android_net_wimax_getDeviceStatus(JNIEnv* env, jobject clazz, jobject jdeviceId, jintArray deviceStatus, jintArray connectionStatus)
{
	WIMAX_API_DEVICE_STATUS devStatus;
	WIMAX_API_CONNECTION_PROGRESS_INFO connStatus;

    WIMAX_API_RET result = ::GetDeviceStatus(deviceId, &devStatus, &connStatus);

    jint status[1];

    status[0] = (jint) devStatus;
    env->SetIntArrayRegion(deviceStatus, 0, 1, status);

    status[0] = (jint) connStatus;
    env->SetIntArrayRegion(connectionStatus, 0, 1, status);

    return (jint)result;
}

static jint android_net_wimax_getConnectedNSP(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject connectedNspInfo)
{
	WIMAX_API_CONNECTED_NSP_INFO_P info = new WIMAX_API_CONNECTED_NSP_INFO();
    WIMAX_API_RET result = ::GetConnectedNSP(deviceId, info);

    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->GetObjectClass(connectedNspInfo);
		assignInt(env, cls, connectedNspInfo, "structureSize", info->structureSize);
		assignString(env, cls, connectedNspInfo, "name", info->NSPName);
		assignString(env, cls, connectedNspInfo, "realm", info->NSPRealm);
		assignInt(env, cls, connectedNspInfo, "nspId", info->NSPid);
		assignBoolean(env, cls, connectedNspInfo, "activated", info->activated);
		assignByte(env, cls, connectedNspInfo, "rssi", info->RSSI);
		assignByte(env, cls, connectedNspInfo, "cinr", info->CINR);
		assignInt(env, cls, connectedNspInfo, "networkType", info->networkType);
    }

    return (jint)result;
}

static jint android_net_wimax_setRoamingMode(JNIEnv* env, jobject clazz, jobject jdeviceId, jboolean roamingMode)
{
	WIMAX_API_ROAMING_MODE mode = WIMAX_API_ROAMING_DISABLED;
	if(roamingMode == JNI_TRUE)
		mode = WIMAX_API_ROAMING_ENABLED;

    return (jint)::SetRoamingMode(deviceId, mode);
}

static jint android_net_wimax_getRoamingMode(JNIEnv* env, jobject clazz, jobject jdeviceId, jbooleanArray roamingMode)
{
	WIMAX_API_ROAMING_MODE mode;
	WIMAX_API_RET result = ::GetRoamingMode(deviceId, &mode);

	jboolean isEnabled = JNI_FALSE;
	if(mode == WIMAX_API_ROAMING_ENABLED)
		isEnabled = JNI_TRUE;

	jboolean region[1] = {isEnabled};
    env->SetBooleanArrayRegion(roamingMode, 0, 1, region);

    return (jint)result;
}

static jint android_net_wimax_getStatistics(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject connectionStatistics)
{
	WIMAX_API_CONNECTION_STAT_P info = new WIMAX_API_CONNECTION_STAT();
    WIMAX_API_RET result = ::GetStatistics(deviceId, info);

    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->GetObjectClass(connectionStatistics);
		assignInt(env, cls, connectionStatistics, "structureSize", info->structureSize);
		assignLong(env, cls, connectionStatistics, "totalRxBytes", info->totalRxByte);
		assignLong(env, cls, connectionStatistics, "totalTxBytes", info->totalTxByte);
		assignLong(env, cls, connectionStatistics, "totalRxPackets", info->totalRxPackets);
		assignLong(env, cls, connectionStatistics, "totalTxPackets", info->totalTxPackets);
    }

    return (jint)result;
}

static jint android_net_wimax_getProvisioningStatus(JNIEnv* env, jobject clazz, jobject jdeviceId, jstring nspName, jbooleanArray provisioningStatus)
{
	BOOL status;
	jboolean isCopy;

	const char *nsp = env->GetStringUTFChars(nspName, &isCopy);
	WIMAX_API_RET result = ::GetProvisioningStatus(deviceId, (WIMAX_API_STRING)nsp, &status);
	env->ReleaseStringUTFChars(nspName, nsp);

	jboolean region[1] = {status};
    env->SetBooleanArrayRegion(provisioningStatus, 0, 1, region);

    return (jint)result;
}

static jint android_net_wimax_getContactInformation(JNIEnv* env, jobject clazz, jstring nspName, jobjectArray contactList, jintArray size)
{
    jboolean isCopy;

	const char *nsp = env->GetStringUTFChars(nspName, &isCopy);
    UINT32 listSize = env->GetArrayLength(contactList);
	WIMAX_API_CONTACT_INFO_P contacts = new WIMAX_API_CONTACT_INFO[listSize];

    WIMAX_API_RET result = ::GetContactInformation(deviceId, (WIMAX_API_STRING)nsp, contacts, &listSize);
    env->ReleaseStringUTFChars(nspName, nsp);

    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->FindClass("android/net/wimax/structs/ContactInfo");
    	jmethodID cons = env->GetMethodID(cls, "<init>", "()V");
    	for(int i=0; i<(int)listSize; i++) {
    		jobject contact = env->NewObject(cls, cons);
    		assignInt(env, cls, contact, "structureSize", contacts[i].structureSize);
    		assignString(env, cls, contact, "textForUri", contacts[i].textForURI);
    		assignString(env, cls, contact, "uri", contacts[i].URI);
    		assignInt(env, cls, contact, "contactType", contacts[i].contactType);
    		env->SetObjectArrayElement(contactList, i, contact);
    	}
    }

    jint region[1] = {listSize};
    env->SetIntArrayRegion(size, 0, 1, region);

    return (jint)result;
}

static jint android_net_wimax_getPackageInformation(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject packageInfo)
{
	WIMAX_API_PACKAGE_INFO_P info = new WIMAX_API_PACKAGE_INFO();
    WIMAX_API_RET result = ::GetPackageInformation(deviceId, info);

    if(result == WIMAX_API_RET_SUCCESS) {
    	jclass cls = env->GetObjectClass(packageInfo);
		assignInt(env, cls, packageInfo, "structureSize", info->structureSize);
		assignString(env, cls, packageInfo, "filePath", info->filePath);
		assignString(env, cls, packageInfo, "fileName", info->fileName);
		assignBoolean(env, cls, packageInfo, "forceReboot", info->forceReboot);
		assignBoolean(env, cls, packageInfo, "mandatoryUpdate", info->mandatoryUpdate);
		assignBoolean(env, cls, packageInfo, "warnUser", info->warnUser);
    }

    return (jint)result;
}

static jint android_net_wimax_setPackageUpdateState(JNIEnv* env, jobject clazz, jobject jdeviceId, jint state)
{
	WIMAX_API_PACKAGE_UPDATE_STATE updateState = WIMAX_API_PACKAGE_UPDATE_DELAY;
	switch(state) {
		case 0: updateState = WIMAX_API_PACKAGE_UPDATE_DELAY; break;
		case 1: updateState = WIMAX_API_PACKAGE_UPDATE_ACCEPTED; break;
		case 2: updateState = WIMAX_API_PACKAGE_UPDATE_DENIED; break;
	}
    return (jint)::SetPackageUpdateState(deviceId, updateState);
}

static jint android_net_wimax_subscribeDeviceStatusChange(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject callback)
{
	deviceStatusChangeCB = callback;
    return (jint)::SubscribeDeviceStatusChange(deviceId, fun_IndDeviceStatusUpdate);
}

void fun_IndDeviceStatusUpdate(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_DEVICE_STATUS deviceStatus,
	WIMAX_API_STATUS_REASON statusReason, WIMAX_API_CONNECTION_PROGRESS_INFO connectionProgressInfo)
{
	JNIEnv* env = NULL;
	if(g_jVM->AttachCurrentThread(&env, NULL) == JNI_OK) {
		jclass cls = env->GetObjectClass(deviceStatusChangeCB);
		jmethodID methodId = env->GetMethodID(cls, "callback", "(Landroid/net/wimax/structs/DeviceId;III)V");
		if (methodId) {
			env->CallVoidMethod(deviceStatusChangeCB, methodId, pDeviceId, deviceStatus, statusReason, connectionProgressInfo);
		}

		if (g_jVM->DetachCurrentThread() != JNI_OK) {
	        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	    }
	}else {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	}
}

static jint android_net_wimax_subscribeDeviceInsertRemove(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject callback)
{
	deviceInsertRemoveCB = callback;
    return (jint)::SubscribeDeviceInsertRemove(deviceId, fun_IndDeviceInsertRemove);
}

void fun_IndDeviceInsertRemove(WIMAX_API_DEVICE_ID_P pDeviceId, BOOL cardPresence)
{
	JNIEnv* env = NULL;
	if(g_jVM->AttachCurrentThread(&env, NULL) == JNI_OK) {
		jclass cls = env->GetObjectClass(deviceInsertRemoveCB);
		jmethodID methodId = env->GetMethodID(cls, "callback", "(Landroid/net/wimax/structs/DeviceId;I)V");
		if (methodId) {
			env->CallVoidMethod(deviceInsertRemoveCB, methodId, pDeviceId, cardPresence);
		}

		if (g_jVM->DetachCurrentThread() != JNI_OK) {
	        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	    }
    }else {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	}
}

static jint android_net_wimax_subscribeControlPowerManagement(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject callback)
{
	controlPowerManagementCB = callback;
    return (jint)::SubscribeControlPowerManagement(deviceId, fun_IndControlPowerManagement);
}

void fun_IndControlPowerManagement(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_RF_STATE powerState)
{
	JNIEnv* env = NULL;
	if(g_jVM->AttachCurrentThread(&env, NULL) == JNI_OK) {
		jclass cls = env->GetObjectClass(controlPowerManagementCB);
		jmethodID methodId = env->GetMethodID(cls, "callback", "(Landroid/net/wimax/structs/DeviceId;I)V");
		if (methodId) {
			env->CallVoidMethod(controlPowerManagementCB, methodId, pDeviceId, powerState);
		}

		if (g_jVM->DetachCurrentThread() != JNI_OK) {
	        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	    }
    }else {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	}
}

static jint android_net_wimax_subscribeConnectToNetwork(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject callback)
{
	connectToNetworkCB = callback;
    return (jint)::SubscribeConnectToNetwork(deviceId, fun_IndConnectToNetwork);
}

void fun_IndConnectToNetwork(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_NETWORK_CONNECTION_RESP status)
{
	JNIEnv* env = NULL;
	if(g_jVM->AttachCurrentThread(&env, NULL) == JNI_OK) {
		jclass cls = env->GetObjectClass(connectToNetworkCB);
		jmethodID methodId = env->GetMethodID(cls, "callback", "(Landroid/net/wimax/structs/DeviceId;I)V");
		if (methodId) {
			env->CallVoidMethod(connectToNetworkCB, methodId, pDeviceId, status);
		}

		if (g_jVM->DetachCurrentThread() != JNI_OK) {
	        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	    }
    }else {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	}
}

static jint android_net_wimax_subscribeDisconnectToNetwork(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject callback)
{
	disconnectToNetworkCB = callback;
    return (jint)::SubscribeDisconnectToNetwork(deviceId, fun_IndDisconnectToNetwork);
}

void fun_IndDisconnectToNetwork(WIMAX_API_DEVICE_ID_P pDeviceId,WIMAX_API_NETWORK_CONNECTION_RESP status)
{
	JNIEnv* env = NULL;
	if(g_jVM->AttachCurrentThread(&env, NULL) == JNI_OK) {
		jclass cls = env->GetObjectClass(disconnectToNetworkCB);
		jmethodID methodId = env->GetMethodID(cls, "callback", "(Landroid/net/wimax/structs/DeviceId;I)V");
		if (methodId) {
			env->CallVoidMethod(disconnectToNetworkCB, methodId, pDeviceId, status);
		}

		if (g_jVM->DetachCurrentThread() != JNI_OK) {
	        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	    }
    }else {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	}
}

static jint android_net_wimax_subscribeNetworkSearchWideScan(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject callback)
{
	networkSearchWideScanCB = callback;
    return (jint)::SubscribeNetworkSearchWideScan(deviceId, fun_IndNetworkSearchWideScan);
}

void fun_IndNetworkSearchWideScan(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_NSP_INFO_P nspList, UINT32 listSize)
{
	JNIEnv* env = NULL;
	if(g_jVM->AttachCurrentThread(&env, NULL) == JNI_OK) {
		jclass cls = env->GetObjectClass(networkSearchWideScanCB);
		jmethodID methodId = env->GetMethodID(cls, "callback", "(Landroid/net/wimax/structs/DeviceId;[Landroid/net/wimax/structs/NSPInfo;I)V");
		if (methodId) {
			jclass cls = env->FindClass("android/net/wimax/structs/NSPInfo");
			jmethodID cons = env->GetMethodID(cls, "<init>", "()V");
			jobjectArray networkList = env->NewObjectArray(listSize, cls, NULL);
			for(int i=0; i<(int)listSize; i++) {
				jobject nsp = env->NewObject(cls, cons);
	    		assignInt(env, cls, nsp, "structureSize", nspList[i].structureSize);
	    		assignString(env, cls, nsp, "nspName", nspList[i].NSPName);
	    		assignInt(env, cls, nsp, "nspId", nspList[i].NSPid);
	    		assignByte(env, cls, nsp, "rssi", nspList[i].RSSI);
	    		assignByte(env, cls, nsp, "cinr", nspList[i].CINR);
	    		assignInt(env, cls, nsp, "networkType", nspList[i].networkType);
	    		env->SetObjectArrayElement(networkList, i, nsp);
			}
			env->CallVoidMethod(networkSearchWideScanCB, methodId, pDeviceId, networkList, listSize);
		}

		if (g_jVM->DetachCurrentThread() != JNI_OK) {
	        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	    }
    }else {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	}
}

static jint android_net_wimax_subscribeProvisioningOperation(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject callback)
{
	provisioningOperationCB = callback;
    return (jint)::SubscribeProvisioningOperation(deviceId, fun_IndProvisioningOperation);
}

void fun_IndProvisioningOperation(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_PROV_OPERATION provisioningOperation,
	WIMAX_API_CONTACT_TYPE contactType)
{
	JNIEnv* env = NULL;
	if(g_jVM->AttachCurrentThread(&env, NULL) == JNI_OK) {
		jclass cls = env->GetObjectClass(provisioningOperationCB);
		jmethodID methodId = env->GetMethodID(cls, "callback", "(Landroid/net/wimax/structs/DeviceId;II)V");
		if (methodId) {
			env->CallVoidMethod(provisioningOperationCB, methodId, pDeviceId, provisioningOperation, contactType);
		}

		if (g_jVM->DetachCurrentThread() != JNI_OK) {
	        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	    }
    }else {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	}
}

static jint android_net_wimax_subscribePackageUpdate(JNIEnv* env, jobject clazz, jobject jdeviceId, jobject callback)
{
	packageUpdateCB = callback;
    return (jint)::SubscribePackageUpdate(deviceId, fun_IndPackageUpdate);
}

void fun_IndPackageUpdate(WIMAX_API_DEVICE_ID_P pDeviceId, WIMAX_API_PACK_UPDATE packageUpdate)
{
	JNIEnv* env = NULL;
	if(g_jVM->AttachCurrentThread(&env, NULL) == JNI_OK) {
		jclass cls = env->GetObjectClass(packageUpdateCB);
		jmethodID methodId = env->GetMethodID(cls, "callback", "(Landroid/net/wimax/structs/DeviceId;I)V");
		if (methodId) {
			env->CallVoidMethod(packageUpdateCB, methodId, pDeviceId, packageUpdate);
		}

		if (g_jVM->DetachCurrentThread() != JNI_OK) {
	        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	    }
    }else {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	}
}

static jint android_net_wimax_unsubscribeDeviceStatusChange(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::UnsubscribeDeviceStatusChange(deviceId);
}

static jint android_net_wimax_unsubscribeDeviceInsertRemove(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::UnsubscribeDeviceInsertRemove(deviceId);
}

static jint android_net_wimax_unsubscribeControlPowerManagement(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::UnsubscribeControlPowerManagement(deviceId);
}

static jint android_net_wimax_unsubscribeConnectToNetwork(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::UnsubscribeConnectToNetwork(deviceId);
}

static jint android_net_wimax_unsubscribeDisconnectToNetwork(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::UnsubscribeDisconnectToNetwork(deviceId);
}

static jint android_net_wimax_unsubscribeNetworkSearchWideScan(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::UnsubscribeNetworkSearchWideScan(deviceId);
}

static jint android_net_wimax_unsubscribeProvisioningOperation(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::UnsubscribeProvisioningOperation(deviceId);
}

static jint android_net_wimax_unsubscribePackageUpdate(JNIEnv* env, jobject clazz, jobject jdeviceId)
{
    return (jint)::UnsubscribePackageUpdate(deviceId);
}

// ----------------------------------------------------------------------------

/*
 * JNI registration.
 */
static JNINativeMethod gWimaxMethods[] = {
    /* name, signature, funcPtr */

    { "loadLibrary", "()Z",  (void *)android_net_wimax_loadLibrary },
    { "unloadLibrary", "()Z",  (void *)android_net_wimax_unloadLibrary },
    { "WiMaxAPIOpen", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_wimaxAPIOpen },
    { "GetListDevice", "(Landroid/net/wimax/structs/DeviceId;[Landroid/net/wimax/structs/HardwareDeviceId;[I)I",
     		(void *)android_net_wimax_getListDevice },
    { "WiMaxDeviceOpen", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_wimaxDeviceOpen },
    { "WiMaxDeviceClose", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_wimaxDeviceClose },
    { "WiMaxAPIClose", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_wimaxAPIClose },
    { "CmdControlPowerManagement", "(Landroid/net/wimax/structs/DeviceId;I)I", (void *)android_net_wimax_cmdControlPowerManagement },
    { "CmdResetWimaxDevice", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_cmdResetWimaxDevice },
    { "CmdResetToFactorySettings", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_cmdResetToFactorySettings },
    { "GetErrorString", "(Landroid/net/wimax/structs/DeviceId;I[Ljava/lang/String;[I)I", (void *)android_net_wimax_getErrorString },
    { "SetServiceProviderUnLock", "(Landroid/net/wimax/structs/DeviceId;Ljava/lang/String;)I", (void *)android_net_wimax_setServiceProviderUnLock },
    { "GetServiceProviderLockStatus", "(Landroid/net/wimax/structs/DeviceId;[I[Ljava/lang/String;)I",
    		(void *)android_net_wimax_getServiceProviderLockStatus },
    { "GetNetworkList", "(Landroid/net/wimax/structs/DeviceId;[Landroid/net/wimax/structs/NSPInfo;[I)I",
     		(void *)android_net_wimax_getNetworkList },
    { "CmdConnectToNetwork", "(Landroid/net/wimax/structs/DeviceId;Ljava/lang/String;ILjava/lang/String;)I",
     		(void *)android_net_wimax_cmdConnectToNetwork },
    { "CmdDisconnectFromNetwork", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_cmdDisconnectFromNetwork },
    { "CmdNetworkSearchWideScan", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_cmdNetworkSearchWideScan },
    { "GetIPInterfaceIndex", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/structs/InterfaceInfo;)I",
    		(void *)android_net_wimax_getIPInterfaceIndex },
    { "GetSelectProfileList", "(Landroid/net/wimax/structs/DeviceId;[Landroid/net/wimax/structs/ProfileInfo;[I)I",
    		(void *)android_net_wimax_getSelectProfileList },
    { "GetLinkStatus", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/structs/LinkStatusInfo;)I", (void *)android_net_wimax_getLinkStatus },
    { "GetDeviceInformation", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/structs/DeviceInfo;)I",
    		(void *)android_net_wimax_getDeviceInformation },
    { "GetDeviceStatus", "(Landroid/net/wimax/structs/DeviceId;[I[I)I", (void *)android_net_wimax_getDeviceStatus },
    { "GetConnectedNSP", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/structs/ConnectedNspInfo;)I", (void *)android_net_wimax_getConnectedNSP },
    { "SetRoamingMode", "(Landroid/net/wimax/structs/DeviceId;Z)I", (void *)android_net_wimax_setRoamingMode },
    { "GetRoamingMode", "(Landroid/net/wimax/structs/DeviceId;[Z)I", (void *)android_net_wimax_getRoamingMode },
    { "GetStatistics", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/structs/ConnectionStatistics;)I", (void *)android_net_wimax_getStatistics },
    { "GetProvisioningStatus", "(Landroid/net/wimax/structs/DeviceId;Ljava/lang/String;[Z)I", (void *)android_net_wimax_getProvisioningStatus },
    { "GetContactInformation", "(Landroid/net/wimax/structs/DeviceId;Ljava/lang/String;[Landroid/net/wimax/structs/ContactInfo;[I)I", (void *)android_net_wimax_getContactInformation },
    { "GetPackageInformation", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/structs/PackageInfo;)I", (void *)android_net_wimax_getPackageInformation },
    { "SetPackageUpdateState", "(Landroid/net/wimax/structs/DeviceId;I)I", (void *)android_net_wimax_setPackageUpdateState },
    { "SubscribeDeviceStatusChange", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/WimaxCommonAPI$DeviceStatusChangeCB;)I",
    		(void *)android_net_wimax_subscribeDeviceStatusChange },
    { "SubscribeDeviceInsertRemove", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/WimaxCommonAPI$DeviceInsertRemoveCB;)I",
    		(void *)android_net_wimax_subscribeDeviceInsertRemove },
    { "SubscribeControlPowerManagement", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/WimaxCommonAPI$ControlPowerManagementCB;)I",
    		(void *)android_net_wimax_subscribeControlPowerManagement },
    { "SubscribeConnectToNetwork", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/WimaxCommonAPI$ConnectToNetworkCB;)I",
    		(void *)android_net_wimax_subscribeConnectToNetwork },
    { "SubscribeDisconnectToNetwork", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/WimaxCommonAPI$DisconnectToNetworkCB;)I",
    		(void *)android_net_wimax_subscribeDisconnectToNetwork },
    { "SubscribeNetworkSearchWideScan", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/WimaxCommonAPI$NetworkSearchWideScanCB;)I",
    		(void *)android_net_wimax_subscribeNetworkSearchWideScan },
    { "SubscribeProvisioningOperation", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/WimaxCommonAPI$ProvisioningOperationCB;)I",
    		(void *)android_net_wimax_subscribeProvisioningOperation },
    { "SubscribePackageUpdate", "(Landroid/net/wimax/structs/DeviceId;Landroid/net/wimax/WimaxCommonAPI$PackageUpdateCB;)I",
    		(void *)android_net_wimax_subscribePackageUpdate },
    { "UnsubscribeDeviceStatusChange", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_unsubscribeDeviceStatusChange },
    { "UnsubscribeDeviceInsertRemove", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_unsubscribeDeviceInsertRemove },
    { "UnsubscribeControlPowerManagement", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_unsubscribeControlPowerManagement },
    { "UnsubscribeConnectToNetwork", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_unsubscribeConnectToNetwork },
    { "UnsubscribeDisconnectToNetwork", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_unsubscribeDisconnectToNetwork },
    { "UnsubscribeNetworkSearchWideScan", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_unsubscribeNetworkSearchWideScan },
    { "UnsubscribeProvisioningOperation", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_unsubscribeProvisioningOperation },
    { "UnsubscribePackageUpdate", "(Landroid/net/wimax/structs/DeviceId;)I", (void *)android_net_wimax_unsubscribePackageUpdate },
};

int register_android_net_wimax_WimaxManager(JNIEnv* env)
{
    jclass commonAPI = env->FindClass(WIMAX_PKG_NAME);
    LOG_FATAL_IF(commonAPI == NULL, "Unable to find class " WIMAX_PKG_NAME);

    deviceIdFieldIds.deviceIdClass = env->FindClass("android/net/wimax/structs/DeviceId");
    if (deviceIdFieldIds.deviceIdClass != NULL) {
        deviceIdFieldIds.constructorId = env->GetMethodID(deviceIdFieldIds.deviceIdClass, "<init>", "()V");
        deviceIdFieldIds.structureSize = env->GetFieldID(deviceIdFieldIds.deviceIdClass, "structureSize", "I");
        deviceIdFieldIds.sdkHandle = env->GetFieldID(deviceIdFieldIds.deviceIdClass, "sdkHandle", "I");
        deviceIdFieldIds.privilege = env->GetFieldID(deviceIdFieldIds.deviceIdClass, "privilege", "I");
        deviceIdFieldIds.deviceIndex = env->GetFieldID(deviceIdFieldIds.deviceIdClass, "deviceIndex", "B");
        deviceIdFieldIds.apiVersion = env->GetFieldID(deviceIdFieldIds.deviceIdClass, "apiVersion", "I");
        deviceIdFieldIds.devicePresenceStatus = env->GetFieldID(deviceIdFieldIds.deviceIdClass, "devicePresenceStatus", "Z");
    }

    return AndroidRuntime::registerNativeMethods(env,
            WIMAX_PKG_NAME, gWimaxMethods, NELEM(gWimaxMethods));
}

}; // namespace android
