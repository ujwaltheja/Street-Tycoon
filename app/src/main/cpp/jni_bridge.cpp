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

    // Check for pending exceptions
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        LOGE("Pending exception before GetStringUTFChars");
        return "";
    }

    const char* cstr = env->GetStringUTFChars(jStr, nullptr);
    if (!cstr) {
        LOGE("GetStringUTFChars returned NULL");
        // Check if an exception was thrown
        if (env->ExceptionCheck()) {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }
        return "";
    }

    std::string str(cstr);
    env->ReleaseStringUTFChars(jStr, cstr);
    return str;
}

// Helper function to convert std::string to jstring
jstring string2jstring(JNIEnv* env, const std::string& str) {
    jstring result = env->NewStringUTF(str.c_str());
    if (!result && env->ExceptionCheck()) {
        LOGE("NewStringUTF failed");
        env->ExceptionDescribe();
        env->ExceptionClear();
        return env->NewStringUTF(""); // Return empty string on failure
    }
    return result;
}

// Helper function to validate and cast handle to GameSimulation*
GameSimulation* getValidSimulation(jlong handle, const char* funcName) {
    if (handle == 0) {
        LOGE("Null handle passed to %s", funcName);
        return nullptr;
    }

    GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
    if (!sim->isValid()) {
        LOGE("Invalid simulation handle in %s (corrupted or destroyed)", funcName);
        return nullptr;
    }

    return sim;
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
        GameSimulation* sim = getValidSimulation(handle, "nativeDestroy");
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

// Calculate offline expenses
JNIEXPORT jdouble JNICALL
Java_com_streettycoon_game_native_GameSimulation_nativeCalculateOfflineExpenses(
    JNIEnv* env, jobject thiz, jlong handle, jlong offlineTimeMs) {
    try {
        GameSimulation* sim = reinterpret_cast<GameSimulation*>(handle);
        if (!sim) {
            LOGE("Invalid simulation handle in calculateOfflineExpenses");
            return 0.0;
        }

        return sim->calculateOfflineExpenses(offlineTimeMs);
    } catch (const std::exception& e) {
        LOGE("Exception in nativeCalculateOfflineExpenses: %s", e.what());
        return 0.0;
    } catch (...) {
        LOGE("Unknown exception in nativeCalculateOfflineExpenses");
        return 0.0;
    }
}

} // extern "C"
