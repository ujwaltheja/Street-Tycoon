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

void GameState::processMonthlyExpenses(int64_t currentTimestamp) {
    // Monthly expenses are deducted every 30 in-game days
    // For now, we'll use a simpler approach: deduct every 24 hours of real time
    const int64_t MONTH_DURATION_MS = 24 * 60 * 60 * 1000;  // 24 hours in milliseconds

    if (familyState.lastMonthlyDeductionTimestamp == 0) {
        familyState.lastMonthlyDeductionTimestamp = currentTimestamp;
        return;
    }

    int64_t timeSinceLastDeduction = currentTimestamp - familyState.lastMonthlyDeductionTimestamp;

    if (timeSinceLastDeduction >= MONTH_DURATION_MS) {
        // Deduct monthly expenses
        double monthlyExpense = familyState.totalMonthlyExpense;

        if (playerCash >= monthlyExpense) {
            playerCash -= monthlyExpense;
            familyState.savingsBalance -= monthlyExpense;

            // Update happiness based on financial health
            double monthlyIncome = getMonthlyIncomeEstimate();
            float financialHealth = familyState.getFinancialHealthScore(monthlyIncome);

            if (financialHealth < 50.0f) {
                // Poor financial health - decrease happiness
                for (auto& member : familyState.members) {
                    member.happiness = std::max(0.0f, member.happiness - 5.0f);
                }
            } else if (financialHealth > 80.0f) {
                // Good financial health - increase happiness
                for (auto& member : familyState.members) {
                    member.happiness = std::min(100.0f, member.happiness + 2.0f);
                }
            }

            familyState.calculateAverageHappiness();
            familyState.lastMonthlyDeductionTimestamp = currentTimestamp;
        } else {
            // Can't afford expenses - happiness decreases significantly
            for (auto& member : familyState.members) {
                member.happiness = std::max(0.0f, member.happiness - 10.0f);
            }
            familyState.calculateAverageHappiness();
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
