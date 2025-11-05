#ifndef STREETTYCOON_GAME_STATE_H
#define STREETTYCOON_GAME_STATE_H

#include <string>
#include <vector>
#include <cstdint>

namespace streettycoon {

// Stall types
enum class StallType {
    TEA = 0,
    DOSA = 1,
    MOMOS = 2,
    JUICE = 3
};

// Helper data
struct Helper {
    int id;
    int level;
    double incomePerSecond;

    Helper() : id(0), level(1), incomePerSecond(0.0) {}
    Helper(int id, int level, double income)
        : id(id), level(level), incomePerSecond(income) {}
};

// Stall data
struct Stall {
    int id;
    StallType type;
    int level;
    int zoneId;
    double baseIncome;
    double tapIncome;
    int64_t lastServedTimestamp;
    std::vector<Helper> helpers;
    bool isUnlocked;

    Stall();
    Stall(int id, StallType type, int zoneId);

    double getTotalIncomePerSecond() const;
    double getUpgradeCost() const;
    double getHelperCost() const;
};

// Zone data
struct Zone {
    int id;
    std::string name;
    bool isUnlocked;
    double unlockCost;

    Zone() : id(0), isUnlocked(false), unlockCost(0.0) {}
    Zone(int id, const std::string& name, double cost)
        : id(id), name(name), isUnlocked(false), unlockCost(cost) {}
};

// Complete game state
struct GameState {
    int version;
    double playerCash;
    int playerTokens;
    int64_t lastUpdateTimestamp;
    std::vector<Stall> stalls;
    std::vector<Zone> zones;

    // Stats
    int64_t totalCustomersServed;
    double totalEarnings;
    int currentDay;
    int64_t lastDailyRewardTimestamp;

    GameState();
    void initializeDefaultState();

    Stall* findStall(int stallId);
    Zone* findZone(int zoneId);
};

} // namespace streettycoon

#endif // STREETTYCOON_GAME_STATE_H
