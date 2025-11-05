#ifndef STREETTYCOON_JSON_SERIALIZER_H
#define STREETTYCOON_JSON_SERIALIZER_H

#include "game_state.h"
#include <string>

namespace streettycoon {

class JsonSerializer {
public:
    // Serialize game state to JSON string
    static std::string serialize(const GameState& state);

    // Deserialize JSON string to game state
    static bool deserialize(const std::string& json, GameState& state);

    // Serialize an action result
    static std::string serializeActionResult(bool success, const std::string& message);

private:
    // Helper methods for escaping JSON strings
    static std::string escapeJson(const std::string& str);
    static std::string stallTypeToString(StallType type);
    static StallType stringToStallType(const std::string& str);

    // Simple JSON parsing helpers
    static std::string extractString(const std::string& json, const std::string& key);
    static double extractDouble(const std::string& json, const std::string& key);
    static int extractInt(const std::string& json, const std::string& key);
    static int64_t extractInt64(const std::string& json, const std::string& key);
    static bool extractBool(const std::string& json, const std::string& key);
};

} // namespace streettycoon

#endif // STREETTYCOON_JSON_SERIALIZER_H
