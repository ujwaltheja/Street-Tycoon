#include "json_serializer.h"
#include <sstream>
#include <iomanip>
#include <cstring>
#include <algorithm>

namespace streettycoon {

std::string JsonSerializer::escapeJson(const std::string& str) {
    std::string result;
    for (char c : str) {
        switch (c) {
            case '"':  result += "\\\""; break;
            case '\\': result += "\\\\"; break;
            case '\b': result += "\\b"; break;
            case '\f': result += "\\f"; break;
            case '\n': result += "\\n"; break;
            case '\r': result += "\\r"; break;
            case '\t': result += "\\t"; break;
            default:   result += c; break;
        }
    }
    return result;
}

std::string JsonSerializer::stallTypeToString(StallType type) {
    switch (type) {
        case StallType::TEA: return "TEA";
        case StallType::DOSA: return "DOSA";
        case StallType::MOMOS: return "MOMOS";
        case StallType::JUICE: return "JUICE";
        default: return "TEA";
    }
}

StallType JsonSerializer::stringToStallType(const std::string& str) {
    if (str == "DOSA") return StallType::DOSA;
    if (str == "MOMOS") return StallType::MOMOS;
    if (str == "JUICE") return StallType::JUICE;
    return StallType::TEA;
}

std::string JsonSerializer::serialize(const GameState& state) {
    std::ostringstream oss;
    oss << std::fixed << std::setprecision(2);

    oss << "{";
    oss << "\"version\":" << state.version << ",";
    oss << "\"playerCash\":" << state.playerCash << ",";
    oss << "\"playerTokens\":" << state.playerTokens << ",";
    oss << "\"lastUpdateTimestamp\":" << state.lastUpdateTimestamp << ",";
    oss << "\"totalCustomersServed\":" << state.totalCustomersServed << ",";
    oss << "\"totalEarnings\":" << state.totalEarnings << ",";
    oss << "\"currentDay\":" << state.currentDay << ",";
    oss << "\"lastDailyRewardTimestamp\":" << state.lastDailyRewardTimestamp << ",";

    // Serialize zones
    oss << "\"zones\":[";
    for (size_t i = 0; i < state.zones.size(); i++) {
        const Zone& zone = state.zones[i];
        if (i > 0) oss << ",";
        oss << "{";
        oss << "\"id\":" << zone.id << ",";
        oss << "\"name\":\"" << escapeJson(zone.name) << "\",";
        oss << "\"isUnlocked\":" << (zone.isUnlocked ? "true" : "false") << ",";
        oss << "\"unlockCost\":" << zone.unlockCost;
        oss << "}";
    }
    oss << "],";

    // Serialize stalls
    oss << "\"stalls\":[";
    for (size_t i = 0; i < state.stalls.size(); i++) {
        const Stall& stall = state.stalls[i];
        if (i > 0) oss << ",";
        oss << "{";
        oss << "\"id\":" << stall.id << ",";
        oss << "\"type\":\"" << stallTypeToString(stall.type) << "\",";
        oss << "\"level\":" << stall.level << ",";
        oss << "\"zoneId\":" << stall.zoneId << ",";
        oss << "\"baseIncome\":" << stall.baseIncome << ",";
        oss << "\"tapIncome\":" << stall.tapIncome << ",";
        oss << "\"lastServedTimestamp\":" << stall.lastServedTimestamp << ",";
        oss << "\"isUnlocked\":" << (stall.isUnlocked ? "true" : "false") << ",";

        // Serialize helpers
        oss << "\"helpers\":[";
        for (size_t j = 0; j < stall.helpers.size(); j++) {
            const Helper& helper = stall.helpers[j];
            if (j > 0) oss << ",";
            oss << "{";
            oss << "\"id\":" << helper.id << ",";
            oss << "\"level\":" << helper.level << ",";
            oss << "\"incomePerSecond\":" << helper.incomePerSecond;
            oss << "}";
        }
        oss << "]";
        oss << "}";
    }
    oss << "]";

    oss << "}";
    return oss.str();
}

// Simple extraction helpers (basic implementation - in production use proper JSON library)
std::string JsonSerializer::extractString(const std::string& json, const std::string& key) {
    std::string searchKey = "\"" + key + "\":\"";
    size_t pos = json.find(searchKey);
    if (pos == std::string::npos) return "";

    pos += searchKey.length();
    size_t endPos = json.find("\"", pos);
    if (endPos == std::string::npos) return "";

    return json.substr(pos, endPos - pos);
}

double JsonSerializer::extractDouble(const std::string& json, const std::string& key) {
    std::string searchKey = "\"" + key + "\":";
    size_t pos = json.find(searchKey);
    if (pos == std::string::npos) return 0.0;

    pos += searchKey.length();
    return std::strtod(json.c_str() + pos, nullptr);
}

int JsonSerializer::extractInt(const std::string& json, const std::string& key) {
    return static_cast<int>(extractDouble(json, key));
}

int64_t JsonSerializer::extractInt64(const std::string& json, const std::string& key) {
    std::string searchKey = "\"" + key + "\":";
    size_t pos = json.find(searchKey);
    if (pos == std::string::npos) return 0;

    pos += searchKey.length();
    return std::strtoll(json.c_str() + pos, nullptr, 10);
}

bool JsonSerializer::extractBool(const std::string& json, const std::string& key) {
    std::string searchKey = "\"" + key + "\":";
    size_t pos = json.find(searchKey);
    if (pos == std::string::npos) return false;

    pos += searchKey.length();
    return json.substr(pos, 4) == "true";
}

bool JsonSerializer::deserialize(const std::string& json, GameState& state) {
    // Basic deserialization (in production, use proper JSON library)
    state.version = extractInt(json, "version");
    state.playerCash = extractDouble(json, "playerCash");
    state.playerTokens = extractInt(json, "playerTokens");
    state.lastUpdateTimestamp = extractInt64(json, "lastUpdateTimestamp");
    state.totalCustomersServed = extractInt64(json, "totalCustomersServed");
    state.totalEarnings = extractDouble(json, "totalEarnings");
    state.currentDay = extractInt(json, "currentDay");
    state.lastDailyRewardTimestamp = extractInt64(json, "lastDailyRewardTimestamp");

    // Note: Full deserialization of arrays would require proper JSON parsing
    // For this MVP, we'll initialize default state and apply changes via actions
    return true;
}

std::string JsonSerializer::serializeActionResult(bool success, const std::string& message) {
    std::ostringstream oss;
    oss << "{";
    oss << "\"success\":" << (success ? "true" : "false") << ",";
    oss << "\"message\":\"" << escapeJson(message) << "\"";
    oss << "}";
    return oss.str();
}

} // namespace streettycoon
