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

// Family member data
struct FamilyMember {
    std::string memberId;
    std::string name;
    std::string relation;  // "player", "spouse", "child", "parent"
    int age;
    double monthlyExpense;
    float happiness;  // 0-100

    FamilyMember() : age(0), monthlyExpense(0), happiness(100.0f) {}
    FamilyMember(const std::string& id, const std::string& n, const std::string& rel, int a)
        : memberId(id), name(n), relation(rel), age(a), monthlyExpense(0), happiness(100.0f) {}
};

// Spending category data
struct SpendingCategory {
    std::string categoryId;
    std::string name;
    std::string type;  // "housing", "transport", "food", "education", "health"
    double monthlyExpense;
    int level;  // 0-3
    double nextUpgradeCost;
    std::string currentItem;  // e.g., "Small Room", "Bicycle", etc.

    SpendingCategory() : monthlyExpense(0), level(0), nextUpgradeCost(0) {}
    SpendingCategory(const std::string& id, const std::string& n, const std::string& t)
        : categoryId(id), name(n), type(t), monthlyExpense(0), level(0), nextUpgradeCost(0) {}
};

// Family state container
struct FamilyState {
    std::vector<FamilyMember> members;
    std::vector<SpendingCategory> categories;
    double totalMonthlyExpense;
    float averageHappiness;
    double savingsBalance;
    int64_t lastMonthlyDeductionTimestamp;
    bool isMarried;
    int totalChildren;

    FamilyState()
        : totalMonthlyExpense(0),
          averageHappiness(100.0f),
          savingsBalance(0),
          lastMonthlyDeductionTimestamp(0),
          isMarried(false),
          totalChildren(0) {}

    void calculateMonthlyExpense() {
        totalMonthlyExpense = 0;
        for (const auto& cat : categories) {
            totalMonthlyExpense += cat.monthlyExpense;
        }
        for (const auto& member : members) {
            totalMonthlyExpense += member.monthlyExpense;
        }
    }

    void calculateAverageHappiness() {
        if (members.empty()) {
            averageHappiness = 100.0f;
            return;
        }
        float totalHappiness = 0;
        for (const auto& member : members) {
            totalHappiness += member.happiness;
        }
        averageHappiness = totalHappiness / members.size();
    }

    float getFinancialHealthScore(double monthlyIncome) const {
        if (monthlyIncome < 0.01) return 50.0f;

        float expenseRatio = static_cast<float>(totalMonthlyExpense / monthlyIncome);

        // Optimal range is 5-20% of income spent on family
        if (expenseRatio < 0.05f) {
            // Too little spending - family unhappy
            return 30.0f;
        } else if (expenseRatio > 0.20f) {
            // Too much spending - financial strain
            return 40.0f;
        } else {
            // Healthy balance
            return 100.0f;
        }
    }

    void initializeDefaultCategories() {
        categories.clear();

        // Housing (starts at level 0 - Walking)
        SpendingCategory housing("housing", "Housing", "housing");
        housing.level = 0;
        housing.currentItem = "Street";
        housing.monthlyExpense = 0;
        housing.nextUpgradeCost = 5000;
        categories.push_back(housing);

        // Transport (starts at level 0 - Walking)
        SpendingCategory transport("transport", "Transportation", "transport");
        transport.level = 0;
        transport.currentItem = "Walking";
        transport.monthlyExpense = 0;
        transport.nextUpgradeCost = 2000;
        categories.push_back(transport);

        // Food (starts at level 0 - Basic)
        SpendingCategory food("food", "Food", "food");
        food.level = 0;
        food.currentItem = "Street Food";
        food.monthlyExpense = 300;
        food.nextUpgradeCost = 1000;
        categories.push_back(food);

        // Education (starts at level 0 - None)
        SpendingCategory education("education", "Education", "education");
        education.level = 0;
        education.currentItem = "None";
        education.monthlyExpense = 0;
        education.nextUpgradeCost = 3000;
        categories.push_back(education);

        // Health (starts at level 0 - None)
        SpendingCategory health("health", "Health", "health");
        health.level = 0;
        health.currentItem = "No Insurance";
        health.monthlyExpense = 0;
        health.nextUpgradeCost = 1500;
        categories.push_back(health);

        calculateMonthlyExpense();
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
    FamilyState familyState;  // Family and spending system

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
    SpendingCategory* findSpendingCategory(const std::string& categoryId);
    FamilyMember* findFamilyMember(const std::string& memberId);

    // Gate progression methods
    void updateGateProgress();
    int getTotalUpgradesCompleted() const;
    int getTotalHelpersHired() const;
    int64_t getPlaytimeHours() const;

    // Character methods
    double getCharacterBonusForStall(int stallId) const;
    double getTapBonusForStall(int stallId) const;
    double getUpgradeCostMultiplierForStall(int stallId) const;

    // Family methods
    double getMonthlyIncomeEstimate() const;
    void processMonthlyExpenses(int64_t currentTimestamp);
    bool upgradeCategoryLevel(const std::string& categoryId);
};

} // namespace streettycoon

#endif // STREETTYCOON_GAME_STATE_H
