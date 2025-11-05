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

// Character types for staff system
enum class CharacterType {
    CHEF = 0,       // +50% tap income, +10% helper efficiency
    MANAGER = 1,    // -20% upgrade cost, manages multiple stalls
    STAFF = 2,      // +40% passive income, generic helper
    SPECIALIST = 3  // +60% zone-specific income
};

// Character stats based on type
struct CharacterStats {
    CharacterType type;
    int baseCost;
    float incomeMultiplier;
    float tapIncomeBonus;
    float upgradeCostReduction;
    int maxLevel;

    CharacterStats()
        : type(CharacterType::STAFF), baseCost(800), incomeMultiplier(1.4f),
          tapIncomeBonus(0.0f), upgradeCostReduction(0.0f), maxLevel(5) {}

    static CharacterStats getStatsForType(CharacterType type) {
        CharacterStats stats;
        stats.type = type;

        switch (type) {
            case CharacterType::CHEF:
                stats.baseCost = 1500;
                stats.incomeMultiplier = 1.5f;
                stats.tapIncomeBonus = 0.5f;
                stats.upgradeCostReduction = 0.0f;
                stats.maxLevel = 5;
                break;
            case CharacterType::MANAGER:
                stats.baseCost = 2500;
                stats.incomeMultiplier = 1.3f;
                stats.tapIncomeBonus = 0.0f;
                stats.upgradeCostReduction = 0.2f;
                stats.maxLevel = 5;
                break;
            case CharacterType::STAFF:
                stats.baseCost = 800;
                stats.incomeMultiplier = 1.4f;
                stats.tapIncomeBonus = 0.0f;
                stats.upgradeCostReduction = 0.0f;
                stats.maxLevel = 3;
                break;
            case CharacterType::SPECIALIST:
                stats.baseCost = 3500;
                stats.incomeMultiplier = 1.6f;
                stats.tapIncomeBonus = 0.3f;
                stats.upgradeCostReduction = 0.1f;
                stats.maxLevel = 5;
                break;
        }
        return stats;
    }
};

// Character data (named, levelable staff)
struct Character {
    std::string characterId;
    CharacterType type;
    std::string name;
    int level;
    int experience;
    float productivityMultiplier;
    int assignedStallId;
    bool isUnlocked;

    Character()
        : characterId(""), type(CharacterType::STAFF), name(""),
          level(1), experience(0), productivityMultiplier(1.0f),
          assignedStallId(-1), isUnlocked(false) {}

    Character(const std::string& id, CharacterType t, const std::string& n, int stallId)
        : characterId(id), type(t), name(n), level(1), experience(0),
          productivityMultiplier(1.0f), assignedStallId(stallId), isUnlocked(true) {}

    double getEffectiveIncomeBonus() const {
        CharacterStats stats = CharacterStats::getStatsForType(type);
        return stats.incomeMultiplier * productivityMultiplier;
    }

    double getEffectiveTapBonus() const {
        CharacterStats stats = CharacterStats::getStatsForType(type);
        return stats.tapIncomeBonus * productivityMultiplier;
    }

    double getEffectiveUpgradeCostReduction() const {
        CharacterStats stats = CharacterStats::getStatsForType(type);
        return stats.upgradeCostReduction;
    }

    bool canLevelUp() const {
        CharacterStats stats = CharacterStats::getStatsForType(type);
        int xpRequired = level * 100;
        return level < stats.maxLevel && experience >= xpRequired;
    }

    void levelUp() {
        if (canLevelUp()) {
            level++;
            experience = 0;
            productivityMultiplier += 0.1f;
        }
    }

    int getHireCost() const {
        CharacterStats stats = CharacterStats::getStatsForType(type);
        return stats.baseCost;
    }
};

// Helper data (legacy, kept for compatibility)
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
    std::vector<Character> characters;  // Named staff characters

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
    Character* findCharacter(const std::string& characterId);

    // Gate progression methods
    void updateGateProgress();
    int getTotalUpgradesCompleted() const;
    int getTotalHelpersHired() const;
    int64_t getPlaytimeHours() const;

    // Character methods
    double getCharacterBonusForStall(int stallId) const;
    double getTapBonusForStall(int stallId) const;
    double getUpgradeCostMultiplierForStall(int stallId) const;
};

} // namespace streettycoon

#endif // STREETTYCOON_GAME_STATE_H
