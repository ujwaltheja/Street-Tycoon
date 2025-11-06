#include "game_state.h"
#include <cmath>
#include <algorithm>
#include <android/log.h>

#define LOG_TAG "StreetTycoon"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace streettycoon {

// Stall implementation
Stall::Stall()
    : id(0), type(StallType::TEA), level(1), zoneId(0),
      baseIncome(10.0), tapIncome(1.0), lastServedTimestamp(0), isUnlocked(false) {}

Stall::Stall(int id, StallType type, int zoneId)
    : id(id), type(type), level(1), zoneId(zoneId),
      baseIncome(10.0), tapIncome(1.0), lastServedTimestamp(0), isUnlocked(false) {

    // Set base income based on stall type
    switch(type) {
        case StallType::TEA:
            baseIncome = 10.0;
            tapIncome = 1.0;
            break;
        case StallType::DOSA:
            baseIncome = 25.0;
            tapIncome = 2.5;
            break;
        case StallType::MOMOS:
            baseIncome = 50.0;
            tapIncome = 5.0;
            break;
        case StallType::JUICE:
            baseIncome = 15.0;
            tapIncome = 1.5;
            break;
    }
}

double Stall::getTotalIncomePerSecond() const {
    double income = 0.0;
    for (const auto& helper : helpers) {
        income += helper.incomePerSecond;
    }
    // Apply level multiplier
    return income * (1.0 + (level - 1) * 0.5);
}

double Stall::getUpgradeCost() const {
    // Exponential growth: baseCost * (1.15^level)
    // Prevent overflow by capping level
    const int MAX_SAFE_LEVEL = 100;
    const double MAX_COST = 1e15;  // Cap at reasonable maximum

    int safeLevel = std::min(level, MAX_SAFE_LEVEL);
    double baseCost = baseIncome * 10.0;
    double multiplier = std::pow(1.15, safeLevel);

    // Prevent overflow
    if (multiplier > MAX_COST / baseCost || baseCost * multiplier > MAX_COST) {
        LOGE("Upgrade cost overflow prevented for level %d", level);
        return MAX_COST;
    }

    return baseCost * multiplier;
}

double Stall::getHelperCost() const {
    // Cost increases with number of helpers
    // Prevent overflow by capping helper count
    const int MAX_SAFE_HELPERS = 50;
    const double MAX_COST = 1e15;  // Cap at reasonable maximum

    int helperCount = static_cast<int>(helpers.size());
    int safeHelperCount = std::min(helperCount, MAX_SAFE_HELPERS);
    double baseCost = baseIncome * 20.0;
    double multiplier = std::pow(1.3, safeHelperCount);

    // Prevent overflow
    if (multiplier > MAX_COST / baseCost || baseCost * multiplier > MAX_COST) {
        LOGE("Helper cost overflow prevented for count %d", helperCount);
        return MAX_COST;
    }

    return baseCost * multiplier;
}

// GameState implementation
GameState::GameState()
    : version(1), playerCash(100.0), playerTokens(0), lastUpdateTimestamp(0),
      totalCustomersServed(0), totalEarnings(0.0), currentDay(1), lastDailyRewardTimestamp(0),
      totalUpgradesCompleted(0), totalHelpersHired(0), totalPlaytimeSeconds(0), gameStartTimestamp(0) {}

void GameState::initializeDefaultState() {
    version = 1;
    playerCash = 100.0;
    playerTokens = 0;
    totalCustomersServed = 0;
    totalEarnings = 0.0;
    currentDay = 1;
    lastDailyRewardTimestamp = 0;

    // Initialize progression tracking
    totalUpgradesCompleted = 0;
    totalHelpersHired = 0;
    totalPlaytimeSeconds = 0;
    gameStartTimestamp = 0;  // Will be set on first tick

    // Reserve capacity to prevent reallocation and dangling pointers
    // 6 zones * 4 stalls = 24 stalls
    stalls.reserve(24);
    zones.reserve(6);
    characters.reserve(20);  // Reserve space for characters
    familyState.members.reserve(10);  // Reserve space for family members

    // Initialize zones
    zones.clear();
    zones.push_back(Zone(0, "Marketplace", 0.0));
    zones.push_back(Zone(1, "Temple Street", 500.0));
    zones.push_back(Zone(2, "Tech Park", 2000.0));
    zones.push_back(Zone(3, "Beach Road", 5000.0));
    zones.push_back(Zone(4, "Old City", 10000.0));
    zones.push_back(Zone(5, "Downtown", 25000.0));

    // Unlock first zone (no gates)
    zones[0].isUnlocked = true;

    // Add progression gates to zones 1+
    // Zone 1 gates
    zones[1].gates.push_back(MapGate(GateType::UPGRADES_COMPLETED, 10, "Complete 10 upgrades"));
    zones[1].gates.push_back(MapGate(GateType::HELPERS_HIRED, 5, "Hire 5 helpers"));
    zones[1].gates.push_back(MapGate(GateType::EARNINGS_THRESHOLD, 5000, "Earn ₹5,000"));

    // Zone 2 gates
    zones[2].gates.push_back(MapGate(GateType::UPGRADES_COMPLETED, 20, "Complete 20 upgrades"));
    zones[2].gates.push_back(MapGate(GateType::HELPERS_HIRED, 10, "Hire 10 helpers"));
    zones[2].gates.push_back(MapGate(GateType::EARNINGS_THRESHOLD, 25000, "Earn ₹25,000"));

    // Zone 3 gates
    zones[3].gates.push_back(MapGate(GateType::UPGRADES_COMPLETED, 35, "Complete 35 upgrades"));
    zones[3].gates.push_back(MapGate(GateType::HELPERS_HIRED, 20, "Hire 20 helpers"));
    zones[3].gates.push_back(MapGate(GateType::EARNINGS_THRESHOLD, 75000, "Earn ₹75,000"));
    zones[3].gates.push_back(MapGate(GateType::PLAYTIME_HOURS, 1, "Play for 1 hour"));

    // Zone 4 gates
    zones[4].gates.push_back(MapGate(GateType::UPGRADES_COMPLETED, 50, "Complete 50 upgrades"));
    zones[4].gates.push_back(MapGate(GateType::HELPERS_HIRED, 35, "Hire 35 helpers"));
    zones[4].gates.push_back(MapGate(GateType::EARNINGS_THRESHOLD, 200000, "Earn ₹200,000"));
    zones[4].gates.push_back(MapGate(GateType::PLAYTIME_HOURS, 2, "Play for 2 hours"));

    // Zone 5 gates
    zones[5].gates.push_back(MapGate(GateType::UPGRADES_COMPLETED, 75, "Complete 75 upgrades"));
    zones[5].gates.push_back(MapGate(GateType::HELPERS_HIRED, 50, "Hire 50 helpers"));
    zones[5].gates.push_back(MapGate(GateType::EARNINGS_THRESHOLD, 500000, "Earn ₹500,000"));
    zones[5].gates.push_back(MapGate(GateType::PLAYTIME_HOURS, 4, "Play for 4 hours"));

    // Initialize stalls (one of each type per zone)
    stalls.clear();
    for (int zoneId = 0; zoneId < 6; zoneId++) {
        stalls.push_back(Stall(zoneId * 4 + 0, StallType::TEA, zoneId));
        stalls.push_back(Stall(zoneId * 4 + 1, StallType::DOSA, zoneId));
        stalls.push_back(Stall(zoneId * 4 + 2, StallType::MOMOS, zoneId));
        stalls.push_back(Stall(zoneId * 4 + 3, StallType::JUICE, zoneId));
    }

    // Unlock first stall in first zone
    stalls[0].isUnlocked = true;

    // Initialize family state
    familyState.initializeDefaultCategories();

    // Add player as first family member
    FamilyMember player("player", "You", "player", 25);
    player.monthlyExpense = 0;  // Player has no personal expense
    player.happiness = 100.0f;
    familyState.members.push_back(player);

    familyState.calculateMonthlyExpense();
    familyState.calculateAverageHappiness();
}

Stall* GameState::findStall(int stallId) {
    auto it = std::find_if(stalls.begin(), stalls.end(),
        [stallId](const Stall& s) { return s.id == stallId; });
    return (it != stalls.end()) ? &(*it) : nullptr;
}

Zone* GameState::findZone(int zoneId) {
    auto it = std::find_if(zones.begin(), zones.end(),
        [zoneId](const Zone& z) { return z.id == zoneId; });
    return (it != zones.end()) ? &(*it) : nullptr;
}

// Update all gate progress based on current game state
void GameState::updateGateProgress() {
    int totalUpgrades = getTotalUpgradesCompleted();
    int totalHelpers = getTotalHelpersHired();
    int64_t totalEarningsInt = static_cast<int64_t>(totalEarnings);
    int64_t playtimeHours = getPlaytimeHours();

    for (auto& zone : zones) {
        if (zone.isUnlocked) continue;  // Skip already unlocked zones

        for (auto& gate : zone.gates) {
            int newValue = 0;

            switch (gate.type) {
                case GateType::UPGRADES_COMPLETED:
                    newValue = totalUpgrades;
                    break;
                case GateType::HELPERS_HIRED:
                    newValue = totalHelpers;
                    break;
                case GateType::EARNINGS_THRESHOLD:
                    newValue = totalEarningsInt;
                    break;
                case GateType::PLAYTIME_HOURS:
                    newValue = static_cast<int>(playtimeHours);
                    break;
            }

            gate.updateProgress(newValue);
        }
    }
}

int GameState::getTotalUpgradesCompleted() const {
    return totalUpgradesCompleted;
}

int GameState::getTotalHelpersHired() const {
    return totalHelpersHired;
}

int64_t GameState::getPlaytimeHours() const {
    if (gameStartTimestamp == 0) return 0;
    return totalPlaytimeSeconds / 3600;  // Convert seconds to hours
}

// Find character by ID
Character* GameState::findCharacter(const std::string& characterId) {
    auto it = std::find_if(characters.begin(), characters.end(),
        [&characterId](const Character& c) { return c.characterId == characterId; });
    return (it != characters.end()) ? &(*it) : nullptr;
}

// Get income bonus from all characters assigned to a stall
double GameState::getCharacterBonusForStall(int stallId) const {
    double totalBonus = 1.0;  // Start with 1.0 (100%)

    for (const auto& character : characters) {
        if (character.assignedStallId == stallId && character.isUnlocked) {
            totalBonus *= character.getEffectiveIncomeBonus();
        }
    }

    return totalBonus;
}

// Get tap income bonus from characters assigned to a stall
double GameState::getTapBonusForStall(int stallId) const {
    double totalBonus = 0.0;

    for (const auto& character : characters) {
        if (character.assignedStallId == stallId && character.isUnlocked) {
            totalBonus += character.getEffectiveTapBonus();
        }
    }

    return totalBonus;
}

// Get upgrade cost multiplier (reduction) from characters
double GameState::getUpgradeCostMultiplierForStall(int stallId) const {
    double maxReduction = 0.0;

    for (const auto& character : characters) {
        if (character.assignedStallId == stallId && character.isUnlocked) {
            double reduction = character.getEffectiveUpgradeCostReduction();
            maxReduction = std::max(maxReduction, reduction);
        }
    }

    // Return multiplier (e.g., 0.2 reduction = 0.8 multiplier)
    return 1.0 - maxReduction;
}

// Family helper methods
SpendingCategory* GameState::findSpendingCategory(const std::string& categoryId) {
    auto it = std::find_if(familyState.categories.begin(), familyState.categories.end(),
        [&categoryId](const SpendingCategory& cat) { return cat.categoryId == categoryId; });
    return (it != familyState.categories.end()) ? &(*it) : nullptr;
}

FamilyMember* GameState::findFamilyMember(const std::string& memberId) {
    auto it = std::find_if(familyState.members.begin(), familyState.members.end(),
        [&memberId](const FamilyMember& member) { return member.memberId == memberId; });
    return (it != familyState.members.end()) ? &(*it) : nullptr;
}

double GameState::getMonthlyIncomeEstimate() const {
    // Calculate total passive income per month from all stalls
    double totalIncomePerSecond = 0;

    for (const auto& stall : stalls) {
        if (!stall.isUnlocked) continue;

        double baseIncome = stall.getTotalIncomePerSecond();
        double characterBonus = getCharacterBonusForStall(stall.id);
        totalIncomePerSecond += baseIncome * characterBonus;
    }

    // Convert to monthly (30 days × 24 hours × 60 minutes × 60 seconds)
    return totalIncomePerSecond * 30 * 24 * 60 * 60;
}

int GameState::getCurrentGameDay(int64_t currentTimestamp) const {
    // Game-day duration: 10 minutes (600000 ms) = 1 game-day
    // This can be adjusted for balancing
    const int64_t GAME_DAY_DURATION_MS = 600000;  // 10 minutes per game-day

    if (gameStartTimestamp == 0) return 1;

    int64_t elapsedTime = currentTimestamp - gameStartTimestamp;

    // Protect against negative elapsed time (clock adjustment)
    if (elapsedTime < 0) {
        LOGE("Negative elapsed time detected (%lld ms), clock may have been adjusted",
             static_cast<long long>(elapsedTime));
        // Return day 1 and log warning rather than crashing
        return 1;
    }

    int gameDay = 1 + static_cast<int>(elapsedTime / GAME_DAY_DURATION_MS);

    // Ensure game day is always at least 1
    return std::max(1, gameDay);
}

void GameState::processMonthlyExpenses(int64_t currentTimestamp) {
    // Monthly expenses are deducted every 30 game-days
    const int GAME_MONTH_DURATION_DAYS = 30;  // 30 game-days = 1 month

    int currentGameDay = getCurrentGameDay(currentTimestamp);

    // Initialize tracking on first call
    if (familyState.lastMonthlyDeductionGameDay == 0) {
        familyState.lastMonthlyDeductionGameDay = currentGameDay;
        familyState.lastMonthlyDeductionTimestamp = currentTimestamp;
        return;
    }

    int gamesDaysSinceDeduction = currentGameDay - familyState.lastMonthlyDeductionGameDay;

    if (gamesDaysSinceDeduction >= GAME_MONTH_DURATION_DAYS) {
        // Deduct monthly expenses
        double monthlyExpense = familyState.totalMonthlyExpense;
        double monthlyIncome = getMonthlyIncomeEstimate();
        float financialHealth = familyState.getFinancialHealthScore(monthlyIncome);

        LOGD("Monthly expense deduction: game-day %d (last: %d, diff: %d)",
             currentGameDay, familyState.lastMonthlyDeductionGameDay, gamesDaysSinceDeduction);

        if (playerCash >= monthlyExpense) {
            // Normal case: can afford expenses
            playerCash -= monthlyExpense;
            familyState.savingsBalance -= monthlyExpense;

            // Update happiness based on financial health
            if (financialHealth < 30.0f) {
                // Critical financial health - large happiness decrease
                for (auto& member : familyState.members) {
                    member.happiness = std::max(0.0f, member.happiness - 15.0f);
                }
                LOGD("Critical financial health (score: %.1f): family unhappy", financialHealth);
            } else if (financialHealth < 50.0f) {
                // Poor financial health - decrease happiness
                for (auto& member : familyState.members) {
                    member.happiness = std::max(0.0f, member.happiness - 8.0f);
                }
                LOGD("Poor financial health (score: %.1f): family unhappy", financialHealth);
            } else if (financialHealth > 80.0f) {
                // Good financial health - increase happiness
                for (auto& member : familyState.members) {
                    member.happiness = std::min(100.0f, member.happiness + 3.0f);
                }
                LOGD("Good financial health (score: %.1f): family happy", financialHealth);
            } else {
                // Acceptable financial health - maintain happiness
                LOGD("Acceptable financial health (score: %.1f): family stable", financialHealth);
            }

            familyState.calculateAverageHappiness();
            familyState.lastMonthlyDeductionGameDay = currentGameDay;
            familyState.lastMonthlyDeductionTimestamp = currentTimestamp;
        } else {
            // CRITICAL: Cannot afford expenses - prevent negative cash but still apply happiness penalty
            double shortfall = monthlyExpense - playerCash;
            LOGD("Cannot afford monthly expenses (need: %.2f, have: %.2f, shortfall: %.2f)",
                 monthlyExpense, playerCash, shortfall);

            // Prevent negative cash - set to 0 instead
            playerCash = 0;
            familyState.savingsBalance = 0;

            // Severe happiness decrease due to financial crisis
            for (auto& member : familyState.members) {
                member.happiness = std::max(0.0f, member.happiness - 20.0f);
            }
            familyState.calculateAverageHappiness();
            familyState.lastMonthlyDeductionGameDay = currentGameDay;
            familyState.lastMonthlyDeductionTimestamp = currentTimestamp;

            LOGD("Financial crisis: cash is now 0, family happiness severely decreased");
        }
    }
}

bool GameState::upgradeCategoryLevel(const std::string& categoryId) {
    SpendingCategory* category = findSpendingCategory(categoryId);
    if (!category || category->level >= 3) return false;

    double cost = category->nextUpgradeCost;
    if (playerCash < cost) return false;

    playerCash -= cost;

    // Upgrade the category
    category->level++;

    // Update based on category type
    if (category->type == "housing") {
        const std::string items[] = {"Street", "Small Room", "Apartment", "House"};
        const double expenses[] = {0, 500, 2000, 5000};
        const double nextCosts[] = {5000, 10000, 20000, 0};

        category->currentItem = items[category->level];
        category->monthlyExpense = expenses[category->level];
        category->nextUpgradeCost = nextCosts[category->level];
    } else if (category->type == "transport") {
        const std::string items[] = {"Walking", "Bicycle", "Scooter", "Car"};
        const double expenses[] = {0, 100, 500, 2000};
        const double nextCosts[] = {2000, 5000, 15000, 0};

        category->currentItem = items[category->level];
        category->monthlyExpense = expenses[category->level];
        category->nextUpgradeCost = nextCosts[category->level];
    } else if (category->type == "food") {
        const std::string items[] = {"Street Food", "Home Cooking", "Restaurant Meals", "Premium Dining"};
        const double expenses[] = {300, 600, 1500, 3000};
        const double nextCosts[] = {1000, 3000, 8000, 0};

        category->currentItem = items[category->level];
        category->monthlyExpense = expenses[category->level];
        category->nextUpgradeCost = nextCosts[category->level];
    } else if (category->type == "education") {
        const std::string items[] = {"None", "Public School", "Private School", "Premium Education"};
        const double expenses[] = {0, 1000, 3000, 8000};
        const double nextCosts[] = {3000, 8000, 20000, 0};

        category->currentItem = items[category->level];
        category->monthlyExpense = expenses[category->level];
        category->nextUpgradeCost = nextCosts[category->level];
    } else if (category->type == "health") {
        const std::string items[] = {"No Insurance", "Basic Insurance", "Premium Insurance", "Complete Coverage"};
        const double expenses[] = {0, 500, 1500, 4000};
        const double nextCosts[] = {1500, 5000, 15000, 0};

        category->currentItem = items[category->level];
        category->monthlyExpense = expenses[category->level];
        category->nextUpgradeCost = nextCosts[category->level];
    }

    // Increase happiness for family members when upgrading
    for (auto& member : familyState.members) {
        member.happiness = std::min(100.0f, member.happiness + 5.0f);
    }

    familyState.calculateMonthlyExpense();
    familyState.calculateAverageHappiness();

    return true;
}

} // namespace streettycoon
