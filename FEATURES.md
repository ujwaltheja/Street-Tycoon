# Street Tycoon - Features Documentation

This document provides detailed information about all major features implemented in Street Tycoon, from core gameplay mechanics to user interface enhancements. Each section outlines the purpose, functionality, and implementation details of a specific feature.

---

## Table of Contents

1.  [Map Progression Lock System](#1-map-progression-lock-system)
2.  [Family Spending System](#2-family-spending-system)
3.  [Character System](#3-character-system)
4.  [UI Enhancement System](#4-ui-enhancement-system)
5.  [Music & Sound System](#5-music--sound-system)

---

## 1. Map Progression Lock System

### Overview
The Map Progression Lock System is designed to create a more engaging and structured gameplay experience by replacing simple cash-based zone unlocking with achievement-based progression. Players must complete a series of goals to unlock new zones, encouraging a more strategic approach to gameplay.

### Key Features

-   **Achievement-Based Unlocking**: Zones are unlocked by meeting specific criteria, such as completing a certain number of upgrades, hiring a set number of helpers, reaching an earnings threshold, or accumulating a specific amount of playtime.
-   **Multiple Gate Types**: The system supports various gate types, including:
    -   **Upgrades Completed**: Tracks the total number of stall upgrades.
    -   **Helpers Hired**: Monitors the total number of helpers hired across all stalls.
    -   **Earnings Threshold**: Measures the total cumulative earnings.
    -   **Playtime Hours**: Tracks the total active playtime.
-   **Visual Feedback**: The UI provides clear visual feedback on the player's progress toward each goal, with progress bars and color-coded status indicators.

### Implementation Details

-   **C++ Backend**: The core logic is implemented in the C++ backend, with a `MapGate` struct that defines the type, target value, and completion status of each gate. The `GameState` tracks all relevant progression metrics.
-   **Kotlin UI**: The user interface is built with Jetpack Compose, featuring a `MapGateProgressCard` that displays individual gate progress and a `ZoneGatesSection` that shows all gates for a given zone.

---

## 2. Family Spending System

### Overview
The Family Spending System introduces an educational layer to the game, requiring players to balance business growth with the financial needs of their family. This feature adds a new dimension to resource management, as players must make life decisions and manage monthly expenses across several categories.

### Key Features

-   **Life Milestones**: Players can make significant life decisions, such as getting married and having children, each with its own one-time cost and recurring monthly expenses.
-   **Monthly Expenses**: The system includes five spending categories: Housing, Transport, Food, Healthcare, and Luxuries. Each category has multiple levels, with associated costs and benefits.
-   **Happiness Mechanic**: The happiness of family members is affected by the player's spending decisions. Failing to meet their needs can lead to negative consequences, while providing a high quality of life can yield in-game bonuses.

### Implementation Details

-   **C++ Backend**: The `FamilyManager` class in the C++ backend manages all family-related data, including life milestones, monthly expenses, and happiness levels.
-   **Kotlin UI**: The UI provides a dedicated screen for managing family expenses, with clear visual indicators for each spending category and the happiness of each family member.

---

## 3. Character System

### Overview
The Character System brings the game world to life with dynamic, interactive characters that populate the streets and interact with the player's stalls. This feature enhances the game's immersion and provides visual feedback on the player's progress.

### Key Features

-   **Dynamic Population**: The number and type of characters that appear in the game world are determined by the player's progression, with more affluent characters appearing as the player's earnings increase.
-   **Interactive Behaviors**: Characters exhibit a range of behaviors, including walking, stopping to purchase from stalls, and displaying satisfaction or dissatisfaction with their purchases.
-   **Visual Variety**: The system supports a wide variety of character models, each with its own unique appearance and animations.

### Implementation Details

-   **C++ Backend**: The `CharacterManager` class is responsible for spawning, managing, and updating all characters in the game world. The system uses a weighted probability model to determine which character types to spawn based on the player's current earnings.
-   **Kotlin UI**: The characters are rendered as part of the main game screen, with their movements and interactions updated in real-time.

---

## 4. UI Enhancement System

### Overview
The UI Enhancement System is a comprehensive overhaul of the game's user interface, designed to provide a more intuitive, visually appealing, and user-friendly experience. This system introduces a range of new UI components and improves existing ones to create a more polished and professional look and feel.

### Key Features

-   **Modern Design**: The UI is built with Material 3, following the latest design trends and providing a clean, modern aesthetic.
-   **Improved Navigation**: The game features a more intuitive navigation system, with a bottom navigation bar that provides quick access to all major game screens.
-   **Enhanced Visual Feedback**: The UI provides clear visual feedback for all player actions, with animated transitions, progress bars, and color-coded indicators.

### Implementation Details

-   **Jetpack Compose**: The entire UI is built with Jetpack Compose, allowing for a more declarative and efficient development process.
-   **Custom Components**: The system includes a range of custom UI components, such as `StyledButton`, `InfoCard`, and `MetricDisplay`, which are used throughout the application to ensure a consistent look and feel.

---

## 5. Music & Sound System

### Overview
The Music & Sound System enhances the game's atmosphere with background music and sound effects that respond to the player's actions and the in-game environment. This system provides a more immersive and engaging auditory experience.

### Key Features

-   **Dynamic Music**: The background music changes based on the time of day and the player's location, with different tracks for day and night, and for different zones.
-   **Contextual Sound Effects**: The system includes a range of sound effects that are triggered by specific in-game events, such as collecting coins, hiring helpers, and unlocking new zones.
-   **Volume Controls**: Players can independently control the volume of the music and sound effects, or mute them entirely.

### Implementation Details

-   **Media3 ExoPlayer**: The background music is managed by `Media3 ExoPlayer`, which provides a robust and flexible solution for audio playback.
-   **SoundPool**: The sound effects are managed by `SoundPool`, which is optimized for low-latency playback of short audio clips.
-   **AudioManager**: The `AudioManager` class serves as the central hub for all audio-related operations, providing a single point of control for music and sound effects.

