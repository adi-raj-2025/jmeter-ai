# Model Readme - Feather Wand (JMeter AI Agent)

This document is intended for AI assistants and developer tools to quickly ramp up and understand the code organization, key workflows, design patterns, and features of the **Feather Wand** JMeter plugin.

---

## 📌 Project Overview
Feather Wand is a JMeter plugin designed to introduce AI assistance directly inside the JMeter GUI. It offers a sidebar chat panel, specialized slash-like commands, contextual JSR223 script editing via context menus, and a terminal integration that exposes agentic AI CLI tools (like Claude Code) which are synchronized with the active JMX test plan.

---

## 📂 Codebase & Component Structure

All Java sources are located under `src/main/java/org/qainsights/jmeter/ai/`. Below is a breakdown of each package:

### 1. `gui` (Swing GUI Components & Orchestration)
This package is the entry point for the plugin UI and user interactions.
*   [AI.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/AI.java): The AbstractAction used to bind hotkeys and actions for triggering the Feather Wand panel.
*   [AiChatPanel.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/AiChatPanel.java): The primary UI panel. Holds model selections, chat logs, streaming controls (Stop/Send buttons), and settings input components.
*   [AiMenuCreator.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/AiMenuCreator.java): Implements JMeter's `MenuCreator` to inject "Feather Wand" options into the `Run` top-level menu.
*   [AiMenuItem.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/AiMenuItem.java): Handles rendering toggle actions in JMeter's toolbar and side split-pane. Initiates context menu listener on the JMeter tree.
*   [CommandDispatcher.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/CommandDispatcher.java) & [MessageProcessor.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/MessageProcessor.java): Detect slash-like commands and dispatch execution to specialized handlers.
*   [JSR223ContextMenu.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/JSR223ContextMenu.java): Hooks context menus into the JSR223 editor components inside the GUI to allow AI-guided code refactoring.
*   [TreeNavigationButtons.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/TreeNavigationButtons.java) & [UndoRedoDispatcher.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/UndoRedoDispatcher.java): Provide navigation and action rollbacks for AI-modified trees.

### 2. `service` (AI Integrations & Refactoring)
*   [AiService.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/service/AiService.java): The interface specifying Chat, Streaming, Context, and Prompt configuration logic.
*   **Concrete Services**:
    *   [ClaudeService.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/service/ClaudeService.java) (Anthropic Claude API)
    *   [OpenAiService.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/service/OpenAiService.java) (OpenAI ChatGPT API)
    *   [DeepseekAiService.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/service/DeepseekAiService.java) (DeepSeek API)
    *   [GoogleAiService.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/service/GoogleAiService.java) (Gemini API Studio)
    *   [OllamaAiService.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/service/OllamaAiService.java) (Local Ollama API endpoint)
*   [CodeRefactorer.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/service/CodeRefactorer.java): Coordinates JSR223 inline context menu script transformations.

### 3. `claudecode` (AI CLI Terminal Integration)
*   [ClaudeCodePanel.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/ClaudeCodePanel.java): Displays a JediTerm embedded terminal running CLI agent tools.
*   [AiCliAdapter.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/AiCliAdapter.java) & [BaseCliAdapter.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/BaseCliAdapter.java): Base classes defining interface structures and PATH auto-detection logic for CLIs.
*   **Concrete CLI Adapters**:
    *   [ClaudeCodeCliAdapter.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/ClaudeCodeCliAdapter.java) (`claude`)
    *   [OpenAiCodexCliAdapter.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/OpenAiCodexCliAdapter.java) (`codex`)
    *   [GeminiCliAdapter.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/GeminiCliAdapter.java) (`gemini`)
    *   [OpenCodeCliAdapter.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/OpenCodeCliAdapter.java) (`opencode`)
*   [TestPlanSerializer.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/TestPlanSerializer.java): Renders the current open test plan's structures, headers, requests, and hierarchy into a localized `CLAUDE.md` context document written to the test plan directory.
*   [JMeterActionBridge.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/JMeterActionBridge.java): Translates signals between the terminal session and JMeter core functions.

### 4. `utils` (JMeter Bridge & Configurations)
*   [JMeterElementManager.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/utils/JMeterElementManager.java): Provides extensive utilities to perform operations on the JMeter JMX tree programmatically (e.g. traversing, fetching paths, renaming, wrapping, deleting, creating nodes).
*   [JMeterElementRequestHandler.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/utils/JMeterElementRequestHandler.java): Gathers details on the selected JMX nodes and converts their internal keys/values to context strings for LLM requests.
*   [AiConfig.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/utils/AiConfig.java): Wrapper for accessing configured JMeter/user properties.
*   [Models.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/utils/Models.java): Contains list definitions and filtering regex/logic for OpenAI, Claude, DeepSeek, and Gemini APIs.

### 5. Specialized Command Packages
*   `lint`: Handles `@lint` renaming of elements using [LintCommandHandler.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/lint/LintCommandHandler.java) and [ElementRenamer.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/lint/ElementRenamer.java).
*   `optimizer`: Handles `@optimize` structural critiques using [OptimizeRequestHandler.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/optimizer/OptimizeRequestHandler.java).
*   `wrap`: Groups HTTP requests under transaction controllers using [WrapCommandHandler.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/wrap/WrapCommandHandler.java).
*   `usage`: Provides token counting metrics using [UsageCommandHandler.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/usage/UsageCommandHandler.java).

---

## 🛠️ Key Workflows

### A. Context-Aware Chat Request
1. User enters text referencing `@this`.
2. `CommandDispatcher` captures the command and gets details on the active selected tree node via `JMeterElementRequestHandler`.
3. The selected node's attributes are formatted and appended to the prompt.
4. `ClaudeService` (or other active provider) triggers an asynchronous HTTP request.
5. If streaming is enabled, `AiResponseRouter` directs intermediate chunk tokens straight to the Swing conversation pane in real-time.

### B. Structural Modification via LLM (`@lint` / `@wrap`)
1. User enters `@lint rename elements using lowercase`.
2. `ElementRenamer` serializes the tree structure and prompts the model to return a structured rename mapping.
3. The response is parsed, and `JMeterElementManager` renames target nodes.
4. An entry is saved into `UndoRedoDispatcher` so the user can easily revert changes using GUI navigation buttons.

### C. CLI Terminal Sync
1. On terminal launch, `TestPlanSerializer` generates a `CLAUDE.md` metadata file in the same directory as the active `.jmx` file.
2. The user switches terminal agents via `ClaudeCodePanel`'s selector.
3. JediTerm starts a pseudo-terminal executing the target executable (e.g. `claude`).
4. The CLI agent reads the `CLAUDE.md` to learn about requests, variables, and thread groups in the current test plan.

---

## 🧩 Extension Points for Developers

1.  **Adding new AI services**:
    *   Implement [AiService.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/service/AiService.java).
    *   Register the creation case in [AiMenuItem.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/AiMenuItem.java#L64).
2.  **Adding new Terminal CLIs**:
    *   Extend [BaseCliAdapter.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/claudecode/BaseCliAdapter.java).
    *   Register the adapter in `ClaudeCodePanel.detectAvailableClis()`.
3.  **Adding new Chat Commands**:
    *   Extend command routing inside [CommandDispatcher.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/gui/CommandDispatcher.java) and document the pattern in [InputBoxIntellisense.java](file:///d:/AI/jmeter-ai/feature-jmeter-v1/jmeter-ai/src/main/java/org/qainsights/jmeter/ai/intellisense/InputBoxIntellisense.java).
