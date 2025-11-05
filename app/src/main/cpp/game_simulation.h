#ifndef STREETTYCOON_GAME_SIMULATION_H
#define STREETTYCOON_GAME_SIMULATION_H

#include "game_state.h"
#include <string>
#include <cstdint>

namespace streettycoon {

class GameSimulation {
public:
    GameSimulation();
    ~GameSimulation();

    // Initialize with new game state
    void initializeNewGame();

    // Initialize from JSON snapshot
    bool initializeFromJson(const std::string& json);

    // Tick the simulation (deltaTime in milliseconds)
    void tick(int64_t deltaTimeMs);

    // Get current state as JSON snapshot
    std::string getSnapshot() const;

    // Apply an action (returns JSON result with success/failure)
    std::string applyAction(const std::string& actionJson);

    // Calculate offline earnings
    double calculateOfflineEarnings(int64_t offlineTimeMs);

private:
    GameState state_;
    int64_t getCurrentTimestamp() const;

    // Action handlers
    bool handleTapServe(int stallId);
    bool handleUpgradeStall(int stallId);
    bool handleHireHelper(int stallId);
    bool handleUnlockStall(int stallId);
    bool handleUnlockZone(int zoneId);
    bool handleClaimDailyReward();
    bool handleHireCharacter(const std::string& characterType, const std::string& name, int stallId);
    bool handleLevelUpCharacter(const std::string& characterId);
    bool handleAssignCharacter(const std::string& characterId, int stallId);
    bool handleUpgradeCategory(const std::string& categoryId);
    bool handleMarriage(const std::string& spouseName);
    bool handleHaveBaby(const std::string& babyName);

    // Helper methods
    void processPassiveIncome(int64_t deltaTimeMs);
    void updateTimestamp();
    std::string generateCharacterId();
};

} // namespace streettycoon

#endif // STREETTYCOON_GAME_SIMULATION_H
