package org.qainsights.jmeter.ai.gui;

import org.qainsights.jmeter.ai.lint.LintCommandHandler;
import org.qainsights.jmeter.ai.optimizer.OptimizeRequestHandler;
import org.qainsights.jmeter.ai.service.AiService;
import org.qainsights.jmeter.ai.usage.UsageCommandHandler;
import org.qainsights.jmeter.ai.utils.JMeterElementRequestHandler;
import org.qainsights.jmeter.ai.utils.AiConfig;
import org.qainsights.jmeter.ai.wrap.WrapCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

/**
 * Handles dispatching of user messages and special @ commands to the
 * appropriate command handlers, running background work via SwingWorker.
 */
public class CommandDispatcher {
    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final CommandCallback cb;

    public CommandDispatcher(CommandCallback callback) {
        this.cb = callback;
    }

    /**
     * Entry point: processes a raw user message, dispatches special commands or
     * falls back to a general AI request.
     *
     * @param message the trimmed user message
     */
    public void dispatch(String message) {
        if (message.isEmpty()) {
            return;
        }

        log.info("Sending user message: {}", message);
        cb.appendUserMessage("You: " + message);
        cb.addToConversationHistory(message);
        cb.clearMessageField();
        cb.appendLoadingIndicator();

        switch (getCommand(message)) {
            case "@this":
                handleThisCommand();
                return;
            case "@optimize":
                handleOptimizeCommand();
                return;
            case "@code":
                cb.appendRedMessage(
                        "The @code command is disabled. Please use the right-click context menu in the JSR223 editor instead.");
                cb.setInputEnabled(true);
                return;
            case "@lint":
                handleLintCommand(message);
                return;
            case "@wrap":
                handleWrapCommand();
                return;
            case "@usage":
                handleUsageCommand();
                return;
            default:
                break;
        }

        log.info("Checking if message is an element request: '{}'", message);
        cb.setInputEnabled(false);

        String elementResponse = JMeterElementRequestHandler.processElementRequest(message);
        if (elementResponse != null && !elementResponse.contains("I couldn't understand what to do with")) {
            log.info("Detected element request");
            cb.removeLoadingIndicator();
            cb.processAiResponse(elementResponse);
            cb.setInputEnabled(true);
            return;
        }

        if (AiConfig.isStreamingEnabled()) {
            log.info("Processing as streaming AI request");
            cb.showStopButton();

            StringBuilder fullResponse = new StringBuilder();

            Runnable cancelHandle = cb.getAiStreamResponse(message,
                token -> {
                    fullResponse.append(token);
                    cb.appendStreamToken(token);
                },
                () -> {
                    String response = fullResponse.toString();
                    cb.onStreamComplete(response);
                    cb.addToConversationHistory(response);
                },
                e -> {
                    cb.onStreamError("Error getting AI stream response", e,
                        "Sorry, I encountered an error while processing your request. Please try again.");
                }
            );
        } else {
            log.info("Processing as regular AI request");
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return cb.getAiResponse(message);
                }

                @Override
                protected void done() {
                    try {
                        String response = get();
                        cb.onWorkerSuccess(response);
                        cb.addToConversationHistory(response);
                    } catch (InterruptedException | ExecutionException e) {
                        cb.onWorkerError("Error getting AI response", e,
                                "Sorry, I encountered an error while processing your request. Please try again.");
                    }
                }
            }.execute();
        }
    }

    private void handleThisCommand() {
        log.info("Processing @this command");
        cb.setLastCommandType("NONE");
        cb.setInputEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String elementInfo = cb.getCurrentElementInfo();
                return elementInfo != null ? elementInfo
                        : "No element is currently selected in the test plan. Please select an element and try again.";
            }

            @Override
            protected void done() {
                try {
                    cb.onWorkerSuccess(get());
                } catch (InterruptedException | ExecutionException e) {
                    cb.onWorkerError("Error getting element info", e,
                            "Sorry, I encountered an error while getting element information. Please try again.");
                }
            }
        }.execute();
    }

    private void handleOptimizeCommand() {
        log.info("Processing @optimize command");
        cb.setLastCommandType("NONE");
        cb.setInputEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String selectedModel = cb.getSelectedModel();
                AiService serviceToUse = cb.resolveAiService(selectedModel);
                return OptimizeRequestHandler.analyzeAndOptimizeSelectedElement(serviceToUse);
            }

            @Override
            protected void done() {
                try {
                    cb.onWorkerSuccess(get());
                } catch (InterruptedException | ExecutionException e) {
                    cb.onWorkerError("Error getting optimization suggestions", e,
                            "Sorry, I encountered an error while getting optimization suggestions. Please try again.");
                }
            }
        }.execute();
    }

    private void handleLintCommand(String message) {
        log.info("Processing @lint command");
        cb.setLastCommandType("LINT");
        cb.setInputEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String selectedModel = cb.getSelectedModel();
                if (selectedModel == null) {
                    return "Please select a model first.";
                }
                AiService serviceToUse = cb.resolveAiService(selectedModel);
                LintCommandHandler lintCommandHandler = new LintCommandHandler(serviceToUse);
                return lintCommandHandler.processLintCommand(message);
            }

            @Override
            protected void done() {
                try {
                    cb.onWorkerSuccess(get());
                } catch (InterruptedException | ExecutionException e) {
                    cb.onWorkerError("Error processing lint command", e,
                            "Sorry, I encountered an error while processing your lint command. Please try again.");
                }
            }
        }.execute();
    }

    private void handleWrapCommand() {
        log.info("Processing @wrap command");
        cb.setLastCommandType("WRAP");
        cb.setInputEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return new WrapCommandHandler().processWrapCommand();
            }

            @Override
            protected void done() {
                try {
                    cb.onWorkerSuccess(get());
                } catch (InterruptedException | ExecutionException e) {
                    cb.onWorkerError("Error processing @wrap command", e,
                            "Sorry, I encountered an error while processing the @wrap command. Please try again.");
                }
            }
        }.execute();
    }

    private void handleUsageCommand() {
        log.info("Processing @usage command");
        cb.setInputEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String selectedModel = cb.getSelectedModel();
                AiService serviceToUse = selectedModel != null ? cb.resolveAiService(selectedModel) : null;
                return new UsageCommandHandler().processUsageCommand(serviceToUse);
            }

            @Override
            protected void done() {
                try {
                    cb.onWorkerSuccess(get());
                } catch (InterruptedException | ExecutionException e) {
                    cb.onWorkerError("Error processing usage command", e,
                            "Sorry, I encountered an error while processing the usage command. Please try again.");
                }
            }
        }.execute();
    }

    /**
     * Extracts the leading @command token from a message.
     *
     * @param message the raw input message
     * @return the @command token, or "" if none
     */
    static String getCommand(String message) {
        String trimmed = message.trim();
        if (!trimmed.startsWith("@")) {
            return "";
        }
        int spaceIndex = trimmed.indexOf(' ');
        return spaceIndex == -1 ? trimmed : trimmed.substring(0, spaceIndex);
    }

    /**
     * Dispatches the Swagger file contents for JMeter JMX generation.
     * Sets the temporary Swagger system prompt on the active service, sends the request,
     * and resets the system prompt back to default once the request completes or fails.
     */
    public void dispatchSwagger(String filename, String fileContent) {
        log.info("Processing Swagger upload: {}", filename);
        cb.appendUserMessage("You: Uploaded Swagger file " + filename + " (generating JMeter Test Plan...)");
        
        // Build the prompt containing the file content
        String prompt = "Please generate a complete, valid Apache JMeter Test Plan (JMX XML) from the following Swagger/OpenAPI definition. "
                + "Ensure that the generated JMX has a correct structure (TestPlan, ThreadGroup, HTTPSamplerProxy, HeaderManager, etc.) so that it can be loaded directly in Apache JMeter. "
                + "Respond ONLY with the raw JMX XML inside a markdown code block starting with ```xml.\n\n"
                + "Swagger Definition:\n"
                + fileContent;

        // Set the Swagger system prompt on the active service
        String selectedModel = cb.getSelectedModel();
        AiService activeService = cb.resolveAiService(selectedModel);
        
        String swaggerPrompt = AiConfig.getProperty("jmeter.ai.swagger.system.prompt", org.qainsights.jmeter.ai.utils.Constants.DEFAULT_SWAGGER_SYSTEM_PROMPT);
        activeService.setSystemPrompt(swaggerPrompt);

        // Add the prompt to the conversation history so that history-based services get the content
        cb.addToConversationHistory(prompt);
        cb.clearMessageField();
        cb.appendLoadingIndicator();
        cb.setInputEnabled(false);

        if (AiConfig.isStreamingEnabled()) {
            log.info("Processing Swagger as streaming AI request");
            cb.showStopButton();

            StringBuilder fullResponse = new StringBuilder();

            Runnable cancelHandle = cb.getAiStreamResponse(prompt,
                token -> {
                    fullResponse.append(token);
                    cb.appendStreamToken(token);
                },
                () -> {
                    String response = fullResponse.toString();
                    cb.onStreamComplete(response);
                    cb.addToConversationHistory(response);
                    activeService.resetSystemPrompt();
                },
                e -> {
                    cb.onStreamError("Error getting AI stream response for Swagger", e,
                        "Sorry, I encountered an error while processing the Swagger file. Please try again.");
                    activeService.resetSystemPrompt();
                }
            );
        } else {
            log.info("Processing Swagger as regular AI request");
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return cb.getAiResponse(prompt);
                }

                @Override
                protected void done() {
                    try {
                        String response = get();
                        cb.onWorkerSuccess(response);
                        cb.addToConversationHistory(response);
                    } catch (InterruptedException | ExecutionException e) {
                        cb.onWorkerError("Error getting AI response for Swagger", e,
                                "Sorry, I encountered an error while processing the Swagger file. Please try again.");
                    } finally {
                        activeService.resetSystemPrompt();
                    }
                }
            }.execute();
        }
    }
}
