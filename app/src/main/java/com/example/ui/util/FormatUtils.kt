package com.example.ui.util

/**
 * Utility functions to clean raw markdown and AI output artifacts so that
 * raw symbols like **, *, ###, _, bullet points, etc. do not leak into the UI.
 */
object FormatUtils {

    fun cleanMarkdown(text: String): String {
        if (text.isBlank()) return ""
        return text
            // Remove header markers like ### or ## or #
            .replace(Regex("(?m)^#{1,6}\\s*"), "")
            // Replace bold/italic asterisks or underscores: **word** -> word, *word* -> word, __word__ -> word, _word_ -> word
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("___([^_]+)___"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("_([^_]+)_"), "$1")
            // Clean up backticks for inline code: `code` -> code
            .replace(Regex("`([^`]+)`"), "$1")
            // Convert markdown bullets (- or *) at start of line into clean unicode bullet (•)
            .replace(Regex("(?m)^[\\s]*[-*+]\\s+"), "• ")
            // Clean up consecutive blank lines
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    fun cleanSnippet(text: String, maxLength: Int = 120): String {
        val cleaned = cleanMarkdown(text)
        return if (cleaned.length > maxLength) {
            cleaned.take(maxLength).trimEnd() + "..."
        } else {
            cleaned
        }
    }
}
