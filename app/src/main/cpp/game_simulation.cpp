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
    // Initialize game start timestamp on first tick
    if (state_.gameStartTimestamp == 0) {
        state_.gameStartTimestamp = getCurrentTimestamp();
    }

    // Track playtime
    state_.totalPlaytimeSeconds += deltaTimeMs / 1000;

    // Process passive income
    processPassiveIncome(deltaTimeMs);

    // Update gate progress
    state_.updateGateProgress();

    // Process monthly family expenses
    state_.processMonthlyExpenses(getCurrentTimestamp());

    // Update timestamp
    updateTimestamp();
}

std::string GameSimulation::getSnapshot() const {
    return JsonSerializer::serialize(state_);
}

void GameSimulation::processPassiveIncome(int64_t deltaTimeMs) {
    double deltaSeconds = deltaTimeMs / 1000.0;

    for (const auto& stall : state_.stalls) {
        if (!stall.isUnlocked) continue;

        // Base income from helpers
        double baseIncome = stall.getTotalIncomePerSecond() * deltaSeconds;

        // Apply character bonuses (multiplicative)
        double characterBonus = state_.getCharacterBonusForStall(stall.id);
        double income = baseIncome * characterBonus;

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
    else if (actionType == "hire_character") {
        std::string charType = JsonSerializer::extractString(actionJson, "characterType");
        std::string name = JsonSerializer::extractString(actionJson, "name");
        int stallId = JsonSerializer::extractInt(actionJson, "stallId");
        bool success = handleHireCharacter(charType, name, stallId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Character hired" : "Cannot hire character");
    }
    else if (actionType == "level_up_character") {
        std::string charId = JsonSerializer::extractString(actionJson, "characterId");
        bool success = handleLevelUpCharacter(charId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Character leveled up" : "Cannot level up");
    }
    else if (actionType == "assign_character") {
        std::string charId = JsonSerializer::extractString(actionJson, "characterId");
        int stallId = JsonSerializer::extractInt(actionJson, "stallId");
        bool success = handleAssignCharacter(charId, stallId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Character assigned to stall" : "Cannot assign character");
    }
    else if (actionType == "upgrade_category") {
        std::string categoryId = JsonSerializer::extractString(actionJson, "categoryId");
        bool success = handleUpgradeCategory(categoryId);
        return JsonSerializer::serializeActionResult(success,
            success ? "Category upgraded" : "Cannot upgrade category");
    }
    else if (actionType == "get_married") {
        std::string spouseName = JsonSerializer::extractString(actionJson, "spouseName");
        bool success = handleMarriage(spouseName);
        return JsonSerializer::serializeActionResult(success,
            success ? "Got married!" : "Cannot get married");
    }
    else if (actionType == "have_baby") {
        std::string babyName = JsonSerializer::extractString(actionJson, "babyName");
        bool success = handleHaveBaby(babyName);
        return JsonSerializer::serializeActionResult(success,
            success ? "Baby born!" : "Cannot have a baby");
    }

    return JsonSerializer::serializeActionResult(false, "Unknown action");
}

bool GameSimulation::handleTapServe(int stallId) {
    Stall* stall = state_.findStall(stallId);
    if (!stall || !stall->isUnlocked) return false;

    // Base tap earnings with level multiplier
    double earnings = stall->tapIncome * (1.0 + (stall->level - 1) * 0.5);

    // Apply character bonuses (additive tap bonus)
    double tapBonus = state_.getTapBonusForStall(stallId);
    earnings = earnings * (1.0 + tapBonus);

    state_.playerCash += earnings;
    state_.totalEarnings += earnings;
    state_.totalCustomersServed++;
    stall->lastServedTimestamp = getCurrentTimestamp();

    LOGD("Tap serve: +%.2f cash (stall %d, tap bonus: %.2f)", earnings, stallId, tapBonus);
    return true;
}

bool GameSimulation::handleUpgradeStall(int stallId) {
    Stall* stall = state_.findStall(stallId);
    if (!stall || !stall->isUnlocked) return false;

    // Get base cost and apply character cost reduction
    double baseCost = stall->getUpgradeCost();
    double costMultiplier = state_.getUpgradeCostMultiplierForStall(stallId);
    double finalCost = baseCost * costMultiplier;

    if (state_.playerCash < finalCost) return false;

    state_.playerCash -= finalCost;
    stall->level++;
    state_.totalUpgradesCompleted++;  // Track for gate progression

    LOGD("Upgraded stall %d to level %d (cost: %.2f, reduction: %.1f%%)",
         stallId, stall->level, finalCost, (1.0 - costMultiplier) * 100);
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
    state_.totalHelpersHired++;  // Track for gate progression

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

    // Check if all gates are completed
    if (!zone->checkAllGatesComplete()) {
        LOGD("Cannot unlock zone %d: gates not completed (%d/%zu)",
             zoneId, zone->getCompletedGatesCount(), zone->gates.size());
        return false;
    }

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

std::string GameSimulation::generateCharacterId() {
    return "char_" + std::to_string(getCurrentTimestamp()) + "_" +
           std::to_string(state_.characters.size());
}

bool GameSimulation::handleHireCharacter(const std::string& characterType,
                                          const std::string& name,
                                          int stallId) {
    // Validate stall exists and is unlocked
    Stall* stall = state_.findStall(stallId);
    if (!stall || !stall->isUnlocked) return false;

    // Parse character type
    CharacterType type = CharacterType::STAFF;
    if (characterType == "CHEF") type = CharacterType::CHEF;
    else if (characterType == "MANAGER") type = CharacterType::MANAGER;
    else if (characterType == "SPECIALIST") type = CharacterType::SPECIALIST;
    else if (characterType == "STAFF") type = CharacterType::STAFF;
    else return false;

    // Check cost
    CharacterStats stats = CharacterStats::getStatsForType(type);
    if (state_.playerCash < stats.baseCost) return false;

    // Deduct cost
    state_.playerCash -= stats.baseCost;

    // Create character
    std::string charId = generateCharacterId();
    Character character(charId, type, name, stallId);
    state_.characters.push_back(character);

    LOGD("Hired character '%s' (%s) for stall %d (cost: %d)",
         name.c_str(), characterType.c_str(), stallId, stats.baseCost);
    return true;
}

bool GameSimulation::handleLevelUpCharacter(const std::string& characterId) {
    Character* character = state_.findCharacter(characterId);
    if (!character || !character->isUnlocked) return false;

    if (!character->canLevelUp()) {
        LOGD("Character %s cannot level up (level: %d, xp: %d)",
             characterId.c_str(), character->level, character->experience);
        return false;
    }

    character->levelUp();
    LOGD("Character %s leveled up to %d (multiplier: %.2f)",
         characterId.c_str(), character->level, character->productivityMultiplier);
    return true;
}

bool GameSimulation::handleAssignCharacter(const std::string& characterId, int stallId) {
    Character* character = state_.findCharacter(characterId);
    if (!character || !character->isUnlocked) return false;

    // Validate stall exists and is unlocked
    Stall* stall = state_.findStall(stallId);
    if (!stall || !stall->isUnlocked) {
        LOGD("Cannot assign character %s to stall %d (stall not found or locked)",
             characterId.c_str(), stallId);
        return false;
    }

    int oldStallId = character->assignedStallId;
    character->assignedStallId = stallId;

    LOGD("Character %s reassigned from stall %d to stall %d",
         characterId.c_str(), oldStallId, stallId);
    return true;
}

bool GameSimulation::handleUpgradeCategory(const std::string& categoryId) {
    bool success = state_.upgradeCategoryLevel(categoryId);

    if (success) {
        SpendingCategory* category = state_.findSpendingCategory(categoryId);
        LOGD("Upgraded %s to level %d (%s)", categoryId.c_str(), category->level, category->currentItem.c_str());
    } else {
        LOGD("Failed to upgrade category %s", categoryId.c_str());
    }

    return success;
}

bool GameSimulation::handleMarriage(const std::string& spouseName) {
    // Check if already married
    if (state_.familyState.isMarried) {
        LOGD("Already married");
        return false;
    }

    // Check minimum cash requirement
    const double MARRIAGE_COST_MIN = 10000;
    const double MARRIAGE_COST_MAX = 50000;
    const double SPOUSE_MONTHLY_EXPENSE = 500;
    double marriageCost = MARRIAGE_COST_MIN;  // For simplicity, use minimum cost

    if (state_.playerCash < marriageCost) {
        LOGD("Not enough cash for marriage (need %.2f, have %.2f)", marriageCost, state_.playerCash);
        return false;
    }

    // CRITICAL: Check if player can afford spouse's monthly expenses
    // Calculate projected monthly expense with spouse
    double projectedMonthlyExpense = state_.familyState.totalMonthlyExpense + SPOUSE_MONTHLY_EXPENSE;
    double monthlyIncome = state_.getMonthlyIncomeEstimate();

    // Warn if expense ratio would exceed 30% of monthly income (unsustainable)
    // Ideal: 5-20%, Warning: 20-30%, Critical: >30%
    if (monthlyIncome > 0) {
        double expenseRatio = (projectedMonthlyExpense / monthlyIncome) * 100.0;
        if (expenseRatio > 30.0) {
            LOGD("Cannot afford spouse (monthly expense would be %.2f, income is %.2f, ratio: %.1f%%)",
                 projectedMonthlyExpense, monthlyIncome, expenseRatio);
            return false;
        }
    }

    // Deduct cost
    state_.playerCash -= marriageCost;

    // Add spouse
    std::string spouseId = "spouse_" + std::to_string(getCurrentTimestamp());
    FamilyMember spouse(spouseId, spouseName, "spouse", 25);
    spouse.monthlyExpense = SPOUSE_MONTHLY_EXPENSE;
    spouse.happiness = 100.0f;
    state_.familyState.members.push_back(spouse);

    state_.familyState.isMarried = true;
    state_.familyState.calculateMonthlyExpense();
    state_.familyState.calculateAverageHappiness();

    LOGD("Got married to %s (cost: %.2f, new monthly expense: %.2f)",
         spouseName.c_str(), marriageCost, state_.familyState.totalMonthlyExpense);
    return true;
}

bool GameSimulation::handleHaveBaby(const std::string& babyName) {
    // Check if married
    if (!state_.familyState.isMarried) {
        LOGD("Must be married to have a baby");
        return false;
    }

    // Check family size limit (max 4: player + spouse + 2 children)
    const int MAX_FAMILY_SIZE = 4;
    const double BABY_COST = 5000;  // One-time cost
    const double BABY_MONTHLY_EXPENSE = 1500;

    if (state_.familyState.members.size() >= MAX_FAMILY_SIZE) {
        LOGD("Family size limit reached (current: %zu, max: %d)",
             state_.familyState.members.size(), MAX_FAMILY_SIZE);
        return false;
    }

    // Check cost
    if (state_.playerCash < BABY_COST) {
        LOGD("Not enough cash for baby (need %.2f, have %.2f)", BABY_COST, state_.playerCash);
        return false;
    }

    // CRITICAL: Check if player can afford baby's monthly expenses
    // Calculate projected monthly expense with baby
    double projectedMonthlyExpense = state_.familyState.totalMonthlyExpense + BABY_MONTHLY_EXPENSE;
    double monthlyIncome = state_.getMonthlyIncomeEstimate();

    // Warn if expense ratio would exceed 30% of monthly income (unsustainable)
    // Ideal: 5-20%, Warning: 20-30%, Critical: >30%
    if (monthlyIncome > 0) {
        double expenseRatio = (projectedMonthlyExpense / monthlyIncome) * 100.0;
        if (expenseRatio > 30.0) {
            LOGD("Cannot afford baby (monthly expense would be %.2f, income is %.2f, ratio: %.1f%%)",
                 projectedMonthlyExpense, monthlyIncome, expenseRatio);
            return false;
        }
    }

    // Deduct cost
    state_.playerCash -= BABY_COST;

    // Add baby
    std::string babyId = "child_" + std::to_string(getCurrentTimestamp());
    FamilyMember baby(babyId, babyName, "child", 0);
    baby.monthlyExpense = BABY_MONTHLY_EXPENSE;
    baby.happiness = 100.0f;
    state_.familyState.members.push_back(baby);

    state_.familyState.totalChildren++;
    state_.familyState.calculateMonthlyExpense();
    state_.familyState.calculateAverageHappiness();

    LOGD("Had a baby named %s (cost: %.2f, new monthly expense: %.2f, family size: %zu)",
         babyName.c_str(), BABY_COST, state_.familyState.totalMonthlyExpense,
         state_.familyState.members.size());
    return true;
}

} // namespace streettycoon
