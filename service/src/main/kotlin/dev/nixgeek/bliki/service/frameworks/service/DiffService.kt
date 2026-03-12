package dev.nixgeek.bliki.service.frameworks.service

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import dev.nixgeek.bliki.service.domain.service.DiffApi
import dev.nixgeek.bliki.service.shared.Constants
import org.springframework.stereotype.Service

/**
 * A service that implements the `DiffApi` interface for computing the unified diff
 * between two lists of Strings.
 *
 * This service generates a unified diff output in a textual format by comparing
 * two versions (original and revision) of `String` lists. The diff output
 * adheres to the unified diff format.
 *
 * Responsibilities:
 * - Computes line-by-line differences between the two versions.
 * - Generates a human-readable textual diff using the unified diff format.
 */
@Service
class DiffService : DiffApi<String> {
    /**
     * Computes the unified diff between two lists of Strings.
     *
     * This method compares an original list of Strings with a revised list
     * and generates a human-readable unified diff in textual format.
     *
     * @param original the original list of Strings to be compared
     * @param revision the revised list of Strings objects to be compared
     * @return a string representation of the unified diff between the two lists
     */
    override suspend fun diff(original: List<String>, revision: List<String>): String =
        DiffUtils.diff(original, revision).let { patch ->
            UnifiedDiffUtils
                .generateUnifiedDiff(
                    Constants.Diff.ORIGINAL_FILE_NAME,
                    Constants.Diff.REVISED_FILE_NAME,
                    original,
                    patch,
                    Constants.Diff.CONTEXT_SIZE,
                ).joinToString("\n")
        }
}
