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

// Gate types for map progression
enum class GateType {
    UPGRADES_COMPLETED = 0,
    HELPERS_HIRED = 1,
    EARNINGS_THRESHOLD = 2,
    PLAYTIME_HOURS = 3
};

// Map gate data for progression tracking
struct MapGate {
    GateType type;
    int targetValue;
    int currentValue;
    bool isCompleted;
    std::string description;

    MapGate()
        : type(GateType::UPGRADES_COMPLETED),
          targetValue(0),
          currentValue(0),
          isCompleted(false),
          description("") {}

    MapGate(GateType t, int target, const std::string& desc)
        : type(t),
          targetValue(target),
          currentValue(0),
          isCompleted(false),
          description(desc) {}

    void updateProgress(int newValue) {
        currentValue = std::max(currentValue, newValue);
        isCompleted = (currentValue >= targetValue);
    }

    float getProgress() const {
        if (targetValue == 0) return 0.0f;
        return static_cast<float>(currentValue) / static_cast<float>(targetValue);
    }
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
    std::vector<MapGate> gates;  // Progression gates

    Zone() : id(0), isUnlocked(false), unlockCost(0.0) {}

    Zone(int id, const std::string& name, double cost)
        : id(id), name(name), isUnlocked(false), unlockCost(cost) {}

    bool checkAllGatesComplete() const {
        if (gates.empty()) return true;  // No gates = always unlockable
        for (const auto& gate : gates) {
            if (!gate.isCompleted) return false;
        }
        return true;
    }

    int getCompletedGatesCount() const {
        int count = 0;
        for (const auto& gate : gates) {
            if (gate.isCompleted) count++;
        }
        return count;
    }
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

    // Progression tracking
    int totalUpgradesCompleted;
    int totalHelpersHired;
    int64_t totalPlaytimeSeconds;
    int64_t gameStartTimestamp;

    GameState();
    void initializeDefaultState();

    Stall* findStall(int stallId);
    Zone* findZone(int zoneId);

    // Gate progression methods
    void updateGateProgress();
    int getTotalUpgradesCompleted() const;
    int getTotalHelpersHired() const;
    int64_t getPlaytimeHours() const;
};

} // namespace streettycoon

#endif // STREETTYCOON_GAME_STATE_H
