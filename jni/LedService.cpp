/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "led"

#include "jni.h"
#include "JNIHelp.h"
//#include "android_runtime/AndroidRuntime.h"
#include <utils/misc.h>
#include <utils/Log.h>
#include <hardware/hardware.h>
#include <hardware/led.h>
#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

static led_control_device_t *sLedDevice = NULL;

static inline int led_control_open(struct hw_module_t *module,
		struct led_control_device_t **device){
	return module->methods->open(module, LED_HARDWARE_MODULE_ID, (struct hw_device_t **)device);
}

jboolean
Java_com_embedsky_led_LedActivity_ledClose(JNIEnv *env, jclass clazz){
	if (sLedDevice){
		sLedDevice->common.close(&(sLedDevice->common));
	}
	return 0;
}
jboolean
Java_com_embedsky_led_LedActivity_ledSetOn(JNIEnv *env, jclass clazz, jint number){
	if (sLedDevice){
		if(sLedDevice->set_on(sLedDevice, number) == 0){
			return true;
		}
	}
	return false;
}

jboolean
Java_com_embedsky_led_LedActivity_ledSetOff(JNIEnv *env, jclass clazz, jint number){
	if (sLedDevice){
		if(sLedDevice->set_off(sLedDevice, number) == 0){
			return true;
		}
	}
	return false;
}

jboolean Java_com_embedsky_led_LedActivity_ledInit(JNIEnv *env, jclass clazz){
	led_module_t *module;
	int err = hw_get_module(LED_HARDWARE_MODULE_ID, (hw_module_t const**)&module);
	if (err == 0){
		if (led_control_open(&(module->common), &sLedDevice) == 0){
			return true;
		}
	}
	sLedDevice = NULL;
	return false;
}
static led_control_device_t * get_device(hw_module_t* module, char const* name)
{
    int err;
    hw_device_t* device;
    err = module->methods->open(module, name, &device);
    if (err == 0) {
        return (led_control_device_t*)device;
    } else {
        return NULL;
    }
}


static JNINativeMethod method_table[] = {
    { "led_init", "()Z", (void*)Java_com_embedsky_led_LedActivity_ledInit },
    { "led_setOn", "(I)Z", (void*)Java_com_embedsky_led_LedActivity_ledSetOn },
    { "led_setOff", "(I)Z", (void*)Java_com_embedsky_led_LedActivity_ledSetOff },
    { "led_close", "()Z", (void*)Java_com_embedsky_led_LedActivity_ledClose },
};

int register_android_server_LedService(JNIEnv *env)
{
    static const char* const kClassName = "android/hardware/LedService";
	jclass clazz;

	/* look up the class */
	clazz = env->FindClass(kClassName);
	if (clazz == NULL)
	{
		LOGE("Can't find class %s/n", kClassName);
		return -1;
	}

	/* register all the methods */
	if (env->RegisterNatives(clazz, method_table,
			sizeof(method_table) / sizeof(method_table[0])) != JNI_OK)
	{
		LOGE("Failed registering methods for %s/n", kClassName);
		return -1;
	}
	/* fill out the rest of the ID cache */
	return 0;
}


 jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = NULL;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		LOGE("GetEnv failed!");
		return result;
	}
	LOG_ASSERT(env, "Could not retrieve the env!");

	//register_android_server_LedService(env);
	return JNI_VERSION_1_4;
}

#ifdef __cplusplus
}
#endif
