#include "game_state.h"
#include <cmath>
#include <algorithm>

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
    double baseCost = baseIncome * 10.0;
    return baseCost * std::pow(1.15, level);
}

double Stall::getHelperCost() const {
    // Cost increases with number of helpers
    int helperCount = static_cast<int>(helpers.size());
    double baseCost = baseIncome * 20.0;
    return baseCost * std::pow(1.3, helperCount);
}

// GameState implementation
GameState::GameState()
    : version(1), playerCash(100.0), playerTokens(0), lastUpdateTimestamp(0),
      totalCustomersServed(0), totalEarnings(0.0), currentDay(1), lastDailyRewardTimestamp(0) {}

void GameState::initializeDefaultState() {
    version = 1;
    playerCash = 100.0;
    playerTokens = 0;
    totalCustomersServed = 0;
    totalEarnings = 0.0;
    currentDay = 1;
    lastDailyRewardTimestamp = 0;

    // Initialize zones
    zones.clear();
    zones.push_back(Zone(0, "Marketplace", 0.0));
    zones.push_back(Zone(1, "Temple Street", 500.0));
    zones.push_back(Zone(2, "Tech Park", 2000.0));
    zones.push_back(Zone(3, "Beach Road", 5000.0));
    zones.push_back(Zone(4, "Old City", 10000.0));
    zones.push_back(Zone(5, "Downtown", 25000.0));

    // Unlock first zone
    zones[0].isUnlocked = true;

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

} // namespace streettycoon
