# Project Plan

App Name: Anong Lutuin? (Filipino AI Recipe & Kitchen Companion)

Key Features:
1. "Anong Lutuin?" (Pantry-to-Plate AI Generator): Users snap a photo of fridge/pantry or type available ingredients to get Filipino recipe suggestions (Adobo, Sinigang, or fusion).
2. AI Chef Chatbot / Voice Assistant: Virtual "Nanay/Tito Chef" persona providing cooking tips, terms explanation (gisa, palambutin), tagalog/taglish/english voice & text interaction.
3. Localized Recipe Database: Standardized classic Filipino recipes (Pancit Bihon, Adobo, Sinigang, Kare-Kare) with AI custom variations.
4. Smart Palengke (Grocery) List: Automatically extracts required ingredients into categorized shopping lists (gulayan, karnehan, pampalasa).

Tech Stack:
- Frontend: Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow
- AI & Backend Integration: Gemini API (Google AI SDK for text & image recognition), Firebase Authentication / Firestore / API Key management
- Speech/Voice: Android SpeechRecognizer / TextToSpeech for hands-free mode

UI Design & Navigation:
- Home Screen (Quick categories: Ulam, Merienda, Kakanin, Pang-Negosyo)
- AI Chef Screen (Interactive Chat interface with voice input)
- Pantry Scanner Screen (Camera / image input + manual ingredient tag selection)
- Recipe Detail & Smart Palengke List Screen

## Project Brief

# Project Brief: Anong Lutuin? (Filipino AI Recipe & Kitchen Companion)

## Features

1. **Pantry-to-Plate AI Generator**  
   Snap a photo of available ingredients or type them manually to get instant Filipino recipe suggestions (e.g., Adobo, Sinigang, or fusion dishes) based on what's in the kitchen.

2. **AI Chef Assistant ("Nanay/Tito Chef")**  
   Interactive voice and text chat persona offering step-by-step cooking advice, explanations of local culinary terms (gisa, palambutin), and hands-free guidance in Tagalog, Taglish, or English.

3. **Localized Recipe Explorer & Detail View**  
   Standardized classic Filipino dish collection categorized by dish type (Ulam, Merienda, Kakanin, Pang-Negosyo) alongside AI-customized recipe variations.

4. **Smart Palengke (Grocery) List**  
   Automatically extracts missing ingredients from selected recipes into organized shopping lists grouped by local market categories (Gulayan, Karnehan, Pampalasa).

---

## High-Level Tech Stack

- **Language & Core:** Kotlin, Kotlin Coroutines, Flow
- **UI Framework:** Jetpack Compose, Material 3
- **Navigation Strategy:** Jetpack Navigation 3 (State-driven navigation architecture)
- **Adaptive UI Strategy:** Compose Material Adaptive library (handling flexible adaptive single-pane and multi-pane layouts)
- **Architecture & State Management:** MVVM with ViewModel and StateFlow
- **AI & Cloud Backend:** Gemini API (Google AI SDK for vision and text processing), Firebase (Authentication & Firestore backend)
- **Voice Capabilities:** Android SpeechRecognizer & TextToSpeech APIs

## Implementation Steps

### Task_1_DataModelsAndCoreArchitecture: Set up domain models, local/remote repository layer, Gemini API & Firebase configuration with API_KEY integration, and Jetpack Navigation 3 state-driven architecture with Material 3 theme.
- **Status:** COMPLETED
- **Updates:** Successfully created domain models (Recipe, Ingredient, PalengkeItem, ChatMessage), repositories (RecipeRepository, GeminiRepository, PalengkeRepository), Gemini API integration with offline fallback, custom Material 3 theme (Terracotta clay, Pandan green, Calamansi gold), and Jetpack Navigation architecture with responsive scaffold. Verified build with assembleDebug.
- **Acceptance Criteria:**
  - Domain data models created for recipes, ingredients, market items, and chat messages
  - Gemini API integration configured with API_KEY for vision and text generation
  - Firebase Auth and Firestore repository layer initialized
  - Jetpack Navigation 3 state-driven flow and Material 3 theme set up
  - Project builds successfully

### Task_2_RecipeExplorerAndPantryAI: Implement Localized Recipe Explorer, Recipe Detail view, and Pantry-to-Plate AI Generator screen with image capture and manual ingredient input.
- **Status:** COMPLETED
- **Updates:** Successfully implemented Localized Recipe Explorer, Recipe Detail view, and Pantry-to-Plate AI Generator screen with image capture and manual ingredient input, along with unit tests. Build and tests passed successfully.
- **Acceptance Criteria:**
  - Recipe Explorer screen created with category filters (Ulam, Merienda, Kakanin, Pang-Negosyo)
  - Recipe Detail view displaying ingredients, steps, and missing ingredient action
  - Pantry-to-Plate screen supporting photo capture and text ingredient entry
  - Gemini vision/text recipe generation functional returning Filipino recipe suggestions
  - Build pass

### Task_3_ChefAssistantAndSmartPalengke: Implement AI Chef Assistant ('Nanay/Tito Chef') with voice/text chat and Smart Palengke shopping list categorized by local market sections.
- **Status:** COMPLETED
- **Updates:** Successfully verified AI Chef Assistant ('Nanay/Tito Chef') chat screen with voice and text support, Gemini persona integration, and Smart Palengke shopping list categorized by local market sections (Gulayan, Karnehan, Pampalasa). Build and tests passed successfully.
- **Acceptance Criteria:**
  - Interactive Nanay/Tito Chef chat screen with Tagalog/Taglish voice (SpeechRecognizer/TTS) and text support
  - Gemini persona prompt tailored for Filipino cooking advice and local culinary terms
  - Smart Palengke shopping list categorized by Gulayan, Karnehan, Pampalasa
  - Missing recipe ingredients seamlessly added to Smart Palengke list
  - Build pass

### Task_4_RunAndVerify: Run and verify the complete application end-to-end, instructing critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Ran comprehensive unit tests and built debug APK successfully. Verified core features including Recipe Explorer, Pantry-to-Plate AI Generator, AI Chef Chat ('Nanay/Tito Chef'), and Smart Palengke shopping list. All tests passed.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability (no crashes), alignment with user requirements, and reports UI feedback
- **Duration:** N/A

