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
Java_com_streettycoon_game_native_GameSimulation_nativeCreate(JNIEnv* env, jobject thiz) {
    try {
        LOGD("Creating native GameSimulation instance");
        GameSimulation* sim = new GameSimulation();
        return reinterpret_cast<jlong>(sim);
    } catch (const std::exception& e) {
        LOGE("Exception in nativeCreate: %s", e.what());
        return 0;
    } catch (...) {
        LOGE("Unknown exception in nativeCreate");
        return 0;
    }
}

// Destroy game simulation instance
JNIEXPORT void JNICALL
Java_com_streettycoon_game_native_GameSimulation_nativeDestroy(JNIEnv* env, jobject thiz, jlong handle) {
    try {
        LOGD("Destroying native GameSimulation instance");
        GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
        if (sim) {
            delete sim;
        }
    } catch (const std::exception& e) {
        LOGE("Exception in nativeDestroy: %s", e.what());
    } catch (...) {
        LOGE("Unknown exception in nativeDestroy");
    }
}

// Initialize new game
JNIEXPORT void JNICALL
Java_com_streettycoon_game_native_GameSimulation_nativeInitializeNewGame(JNIEnv* env, jobject thiz, jlong handle) {
    try {
        GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
        if (sim) {
            sim->initializeNewGame();
            LOGD("Initialized new game");
        } else {
            LOGE("Invalid simulation handle in initializeNewGame");
        }
    } catch (const std::exception& e) {
        LOGE("Exception in nativeInitializeNewGame: %s", e.what());
    } catch (...) {
        LOGE("Unknown exception in nativeInitializeNewGame");
    }
}

// Initialize from JSON
JNIEXPORT jboolean JNICALL
Java_com_streettycoon_game_native_GameSimulation_nativeInitializeFromJson(
    JNIEnv* env, jobject thiz, jlong handle, jstring json) {
    try {
        GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
        if (!sim) {
            LOGE("Invalid simulation handle in initializeFromJson");
            return JNI_FALSE;
        }

        std::string jsonStr = jstring2string(env, json);
        bool success = sim->initializeFromJson(jsonStr);

        return success ? JNI_TRUE : JNI_FALSE;
    } catch (const std::exception& e) {
        LOGE("Exception in nativeInitializeFromJson: %s", e.what());
        return JNI_FALSE;
    } catch (...) {
        LOGE("Unknown exception in nativeInitializeFromJson");
        return JNI_FALSE;
    }
}

// Tick simulation
JNIEXPORT void JNICALL
Java_com_streettycoon_game_native_GameSimulation_nativeTick(
    JNIEnv* env, jobject thiz, jlong handle, jlong deltaTimeMs) {
    try {
        GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
        if (sim) {
            sim->tick(deltaTimeMs);
        } else {
            LOGE("Invalid simulation handle in tick");
        }
    } catch (const std::exception& e) {
        LOGE("Exception in nativeTick: %s", e.what());
    } catch (...) {
        LOGE("Unknown exception in nativeTick");
    }
}

// Get snapshot
JNIEXPORT jstring JNICALL
Java_com_streettycoon_game_native_GameSimulation_nativeGetSnapshot(
    JNIEnv* env, jobject thiz, jlong handle) {
    try {
        GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
        if (!sim) {
            LOGE("Invalid simulation handle in getSnapshot");
            return env->NewStringUTF("{}");
        }

        std::string snapshot = sim->getSnapshot();
        return string2jstring(env, snapshot);
    } catch (const std::exception& e) {
        LOGE("Exception in nativeGetSnapshot: %s", e.what());
        return env->NewStringUTF("{}");
    } catch (...) {
        LOGE("Unknown exception in nativeGetSnapshot");
        return env->NewStringUTF("{}");
    }
}

// Apply action
JNIEXPORT jstring JNICALL
Java_com_streettycoon_game_native_GameSimulation_nativeApplyAction(
    JNIEnv* env, jobject thiz, jlong handle, jstring actionJson) {
    try {
        GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
        if (!sim) {
            LOGE("Invalid simulation handle in applyAction");
            return env->NewStringUTF("{\"success\":false,\"message\":\"Invalid simulation\"}");
        }

        std::string actionStr = jstring2string(env, actionJson);
        std::string result = sim->applyAction(actionStr);

        return string2jstring(env, result);
    } catch (const std::exception& e) {
        LOGE("Exception in nativeApplyAction: %s", e.what());
        return env->NewStringUTF("{\"success\":false,\"message\":\"Exception occurred\"}");
    } catch (...) {
        LOGE("Unknown exception in nativeApplyAction");
        return env->NewStringUTF("{\"success\":false,\"message\":\"Unknown exception\"}");
    }
}

// Calculate offline earnings
JNIEXPORT jdouble JNICALL
Java_com_streettycoon_game_native_GameSimulation_nativeCalculateOfflineEarnings(
    JNIEnv* env, jobject thiz, jlong handle, jlong offlineTimeMs) {
    try {
        GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
        if (!sim) {
            LOGE("Invalid simulation handle in calculateOfflineEarnings");
            return 0.0;
        }

        return sim->calculateOfflineEarnings(offlineTimeMs);
    } catch (const std::exception& e) {
        LOGE("Exception in nativeCalculateOfflineEarnings: %s", e.what());
        return 0.0;
    } catch (...) {
        LOGE("Unknown exception in nativeCalculateOfflineEarnings");
        return 0.0;
    }
}

} // extern "C"
