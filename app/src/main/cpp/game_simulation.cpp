#include "game_simulation.h"
#include "json_serializer.h"
#include <chrono>
#include <algorithm>
#include <cmath>
#include <android/log.h>

#define LOG_TAG "StreetTycoon"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace streettycoon {

GameSimulation::GameSimulation() {
    initializeNewGame();
}

GameSimulation::~GameSimulation() {
}

void GameSimulation::initializeNewGame() {
    state_.initializeDefaultState();
    updateTimestamp();
    LOGD("New game initialized");
}

bool GameSimulation::initializeFromJson(const std::string& json) {
    bool success = JsonSerializer::deserialize(json, state_);
    if (success) {
        LOGD("Game initialized from JSON");
    } else {
        LOGE("Failed to initialize from JSON");
        initializeNewGame();
    }
    return success;
}

int64_t GameSimulation::getCurrentTimestamp() const {
    auto now = std::chrono::system_clock::now();
    auto duration = now.time_since_epoch();
    return std::chrono::duration_cast<std::chrono::milliseconds>(duration).count();
}

void GameSimulation::updateTimestamp() {
    state_.lastUpdateTimestamp = getCurrentTimestamp();
}

void GameSimulation::tick(int64_t deltaTimeMs) {
    processPassiveIncome(deltaTimeMs);
    updateTimestamp();
}

std::string GameSimulation::getSnapshot() const {
    return JsonSerializer::serialize(state_);
}

void GameSimulation::processPassiveIncome(int64_t deltaTimeMs) {
    double deltaSeconds = deltaTimeMs / 1000.0;

    for (const auto& stall : state_.stalls) {
        if (!stall.isUnlocked) continue;

        double income = stall.getTotalIncomePerSecond() * deltaSeconds;
        if (income > 0) {
            state_.playerCash += income;
            state_.totalEarnings += income;
        }
    }
}

double GameSimulation::calculateOfflineEarnings(int64_t offlineTimeMs) {
    // Cap offline time to 4 hours (14400000 ms)
    const int64_t MAX_OFFLINE_TIME = 14400000;
    int64_t cappedTime = std::min(offlineTimeMs, MAX_OFFLINE_TIME);

    double totalIncome = 0.0;
    double deltaSeconds = cappedTime / 1000.0;

    for (const auto& stall : state_.stalls) {
        if (!stall.isUnlocked) continue;
        totalIncome += stall.getTotalIncomePerSecond() * deltaSeconds;
    }

    // Apply offline penalty (70% efficiency)
    return totalIncome * 0.7;
}

std::string GameSimulation::applyAction(const std::string& actionJson) {
    // Parse action type
    std::string actionType = JsonSerializer::extractString(actionJson, "action");

    if (actionType == "tap_serve") {
        int stallId = JsonSerializer::extractInt(actionJson, "stallId");
        bool success = handleTapServe(stallId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Customer served" : "Stall not found or locked");
    }
    else if (actionType == "upgrade_stall") {
        int stallId = JsonSerializer::extractInt(actionJson, "stallId");
        bool success = handleUpgradeStall(stallId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Stall upgraded" : "Not enough cash or stall not found");
    }
    else if (actionType == "hire_helper") {
        int stallId = JsonSerializer::extractInt(actionJson, "stallId");
        bool success = handleHireHelper(stallId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Helper hired" : "Not enough cash or stall not found");
    }
    else if (actionType == "unlock_stall") {
        int stallId = JsonSerializer::extractInt(actionJson, "stallId");
        bool success = handleUnlockStall(stallId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Stall unlocked" : "Not enough cash or stall not found");
    }
    else if (actionType == "unlock_zone") {
        int zoneId = JsonSerializer::extractInt(actionJson, "zoneId");
        bool success = handleUnlockZone(zoneId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Zone unlocked" : "Not enough cash or zone not found");
    }
    else if (actionType == "claim_daily_reward") {
        bool success = handleClaimDailyReward();
        return JsonSerializer::serializeActionResult(success,
            success ? "Daily reward claimed" : "Daily reward already claimed");
    }
    else if (actionType == "apply_offline_earnings") {
        int64_t offlineTime = JsonSerializer::extractInt64(actionJson, "offlineTimeMs");
        double earnings = calculateOfflineEarnings(offlineTime);
        state_.playerCash += earnings;
        state_.totalEarnings += earnings;
        return JsonSerializer::serializeActionResult(true, "Offline earnings applied");
    }

    return JsonSerializer::serializeActionResult(false, "Unknown action");
}

bool GameSimulation::handleTapServe(int stallId) {
    Stall* stall = state_.findStall(stallId);
    if (!stall || !stall->isUnlocked) return false;

    double earnings = stall->tapIncome * (1.0 + (stall->level - 1) * 0.5);
    state_.playerCash += earnings;
    state_.totalEarnings += earnings;
    state_.totalCustomersServed++;
    stall->lastServedTimestamp = getCurrentTimestamp();

    LOGD("Tap serve: +%.2f cash (stall %d)", earnings, stallId);
    return true;
}

bool GameSimulation::handleUpgradeStall(int stallId) {
    Stall* stall = state_.findStall(stallId);
    if (!stall || !stall->isUnlocked) return false;

    double cost = stall->getUpgradeCost();
    if (state_.playerCash < cost) return false;

    state_.playerCash -= cost;
    stall->level++;

    LOGD("Upgraded stall %d to level %d (cost: %.2f)", stallId, stall->level, cost);
    return true;
}

bool GameSimulation::handleHireHelper(int stallId) {
    Stall* stall = state_.findStall(stallId);
    if (!stall || !stall->isUnlocked) return false;

    double cost = stall->getHelperCost();
    if (state_.playerCash < cost) return false;

    state_.playerCash -= cost;

    int helperId = static_cast<int>(stall->helpers.size());
    double incomePerSecond = stall->baseIncome * 0.5;
    stall->helpers.push_back(Helper(helperId, 1, incomePerSecond));

    LOGD("Hired helper for stall %d (cost: %.2f, income: %.2f/s)",
         stallId, cost, incomePerSecond);
    return true;
}

bool GameSimulation::handleUnlockStall(int stallId) {
    Stall* stall = state_.findStall(stallId);
    if (!stall || stall->isUnlocked) return false;

    // Check if zone is unlocked
    Zone* zone = state_.findZone(stall->zoneId);
    if (!zone || !zone->isUnlocked) return false;

    double cost = stall->baseIncome * 5.0;
    if (state_.playerCash < cost) return false;

    state_.playerCash -= cost;
    stall->isUnlocked = true;

    LOGD("Unlocked stall %d (cost: %.2f)", stallId, cost);
    return true;
}

bool GameSimulation::handleUnlockZone(int zoneId) {
    Zone* zone = state_.findZone(zoneId);
    if (!zone || zone->isUnlocked) return false;

    if (state_.playerCash < zone->unlockCost) return false;

    state_.playerCash -= zone->unlockCost;
    zone->isUnlocked = true;

    LOGD("Unlocked zone %d: %s (cost: %.2f)", zoneId, zone->name.c_str(), zone->unlockCost);
    return true;
}

bool GameSimulation::handleClaimDailyReward() {
    int64_t now = getCurrentTimestamp();
    const int64_t DAY_MS = 86400000; // 24 hours in milliseconds

    if (state_.lastDailyRewardTimestamp > 0) {
        int64_t timeSinceLastReward = now - state_.lastDailyRewardTimestamp;
        if (timeSinceLastReward < DAY_MS) {
            return false; // Already claimed today
        }
    }

    // Grant daily reward
    double reward = 50.0 * state_.currentDay;
    state_.playerCash += reward;
    state_.totalEarnings += reward;
    state_.lastDailyRewardTimestamp = now;
    state_.currentDay++;

    LOGD("Daily reward claimed: %.2f cash (day %d)", reward, state_.currentDay - 1);
    return true;
}

} // namespace streettycoon
