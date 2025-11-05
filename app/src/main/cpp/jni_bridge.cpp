#include <jni.h>
#include <string>
#include <android/log.h>
#include "game_simulation.h"

#define LOG_TAG "StreetTycoon-JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace streettycoon;

// Helper function to convert jstring to std::string
std::string jstring2string(JNIEnv* env, jstring jStr) {
    if (!jStr) return "";

    const char* cstr = env->GetStringUTFChars(jStr, nullptr);
    std::string str(cstr);
    env->ReleaseStringUTFChars(jStr, cstr);
    return str;
}

// Helper function to convert std::string to jstring
jstring string2jstring(JNIEnv* env, const std::string& str) {
    return env->NewStringUTF(str.c_str());
}

extern "C" {

// Create new game simulation instance
JNIEXPORT jlong JNICALL
Java_com_streettycoon_game_native_1GameSimulation_nativeCreate(JNIEnv* env, jobject thiz) {
    LOGD("Creating native GameSimulation instance");
    GameSimulation* sim = new GameSimulation();
    return reinterpret_cast<jlong>(sim);
}

// Destroy game simulation instance
JNIEXPORT void JNICALL
Java_com_streettycoon_game_native_1GameSimulation_nativeDestroy(JNIEnv* env, jobject thiz, jlong handle) {
    LOGD("Destroying native GameSimulation instance");
    GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
    if (sim) {
        delete sim;
    }
}

// Initialize new game
JNIEXPORT void JNICALL
Java_com_streettycoon_game_native_1GameSimulation_nativeInitializeNewGame(JNIEnv* env, jobject thiz, jlong handle) {
    GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
    if (sim) {
        sim->initializeNewGame();
        LOGD("Initialized new game");
    } else {
        LOGE("Invalid simulation handle in initializeNewGame");
    }
}

// Initialize from JSON
JNIEXPORT jboolean JNICALL
Java_com_streettycoon_game_native_1GameSimulation_nativeInitializeFromJson(
    JNIEnv* env, jobject thiz, jlong handle, jstring json) {

    GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
    if (!sim) {
        LOGE("Invalid simulation handle in initializeFromJson");
        return JNI_FALSE;
    }

    std::string jsonStr = jstring2string(env, json);
    bool success = sim->initializeFromJson(jsonStr);

    return success ? JNI_TRUE : JNI_FALSE;
}

// Tick simulation
JNIEXPORT void JNICALL
Java_com_streettycoon_game_native_1GameSimulation_nativeTick(
    JNIEnv* env, jobject thiz, jlong handle, jlong deltaTimeMs) {

    GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
    if (sim) {
        sim->tick(deltaTimeMs);
    } else {
        LOGE("Invalid simulation handle in tick");
    }
}

// Get snapshot
JNIEXPORT jstring JNICALL
Java_com_streettycoon_game_native_1GameSimulation_nativeGetSnapshot(
    JNIEnv* env, jobject thiz, jlong handle) {

    GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
    if (!sim) {
        LOGE("Invalid simulation handle in getSnapshot");
        return env->NewStringUTF("{}");
    }

    std::string snapshot = sim->getSnapshot();
    return string2jstring(env, snapshot);
}

// Apply action
JNIEXPORT jstring JNICALL
Java_com_streettycoon_game_native_1GameSimulation_nativeApplyAction(
    JNIEnv* env, jobject thiz, jlong handle, jstring actionJson) {

    GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
    if (!sim) {
        LOGE("Invalid simulation handle in applyAction");
        return env->NewStringUTF("{\"success\":false,\"message\":\"Invalid simulation\"}");
    }

    std::string actionStr = jstring2string(env, actionJson);
    std::string result = sim->applyAction(actionStr);

    return string2jstring(env, result);
}

// Calculate offline earnings
JNIEXPORT jdouble JNICALL
Java_com_streettycoon_game_native_1GameSimulation_nativeCalculateOfflineEarnings(
    JNIEnv* env, jobject thiz, jlong handle, jlong offlineTimeMs) {

    GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
    if (!sim) {
        LOGE("Invalid simulation handle in calculateOfflineEarnings");
        return 0.0;
    }

    return sim->calculateOfflineEarnings(offlineTimeMs);
}

} // extern "C"
