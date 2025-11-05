package com.streettycoon.utils

import com.streettycoon.game.model.CharacterType

/**
 * Generates authentic Indian names for characters
 * Names are presented in both Hindi/Kannada and romanized format
 */
object CharacterNameGenerator {

    private val chefNames = listOf(
        "रजत (Rajat)",
        "संजय (Sanjay)",
        "विक्रम (Vikram)",
        "अर्जुन (Arjun)",
        "कमल (Kamal)",
        "विनोद (Vinod)",
        "सुरेश (Suresh)",
        "राजेश (Rajesh)",
        "रमेश (Ramesh)",
        "दिनेश (Dinesh)"
    )

    private val managerNames = listOf(
        "प्रिया (Priya)",
        "नीता (Neeta)",
        "राज (Raj)",
        "अमित (Amit)",
        "मीरा (Meera)",
        "अंजली (Anjali)",
        "रोहित (Rohit)",
        "नेहा (Neha)",
        "स्वाति (Swati)",
        "अनिल (Anil)"
    )

    private val staffNames = listOf(
        "मोहन (Mohan)",
        "पवन (Pawan)",
        "बलजीत (Baljit)",
        "गुरप्रीत (Gurpreet)",
        "राहुल (Rahul)",
        "विजय (Vijay)",
        "दीपक (Deepak)",
        "अनिल (Anil)",
        "सुनील (Sunil)",
        "मनीष (Manish)"
    )

    private val specialistNames = listOf(
        "डॉ. शर्मा (Dr. Sharma)",
        "श्रीमती गुप्ता (Mrs. Gupta)",
        "श्री वर्मा (Mr. Verma)",
        "डॉ. राव (Dr. Rao)",
        "श्रीमती पटेल (Mrs. Patel)",
        "श्री मेहता (Mr. Mehta)",
        "डॉ. नायर (Dr. Nair)",
        "श्रीमती देसाई (Mrs. Desai)",
        "श्री रेड्डी (Mr. Reddy)",
        "डॉ. खान (Dr. Khan)"
    )

    /**
     * Generate a random name for the given character type
     */
    fun generateName(type: CharacterType): String {
        return when (type) {
            CharacterType.CHEF -> chefNames.random()
            CharacterType.MANAGER -> managerNames.random()
            CharacterType.STAFF -> staffNames.random()
            CharacterType.SPECIALIST -> specialistNames.random()
        }
    }

    /**
     * Get a list of suggested names for a character type
     */
    fun getSuggestedNames(type: CharacterType, count: Int = 5): List<String> {
        val names = when (type) {
            CharacterType.CHEF -> chefNames
            CharacterType.MANAGER -> managerNames
            CharacterType.STAFF -> staffNames
            CharacterType.SPECIALIST -> specialistNames
        }
        return names.shuffled().take(count)
    }

    /**
     * Extract just the romanized name (without Hindi/Kannada script)
     */
    fun getRomanizedName(fullName: String): String {
        return fullName.substringAfter("(").substringBefore(")")
    }
}
