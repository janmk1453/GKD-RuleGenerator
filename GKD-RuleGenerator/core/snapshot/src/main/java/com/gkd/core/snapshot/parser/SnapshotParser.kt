// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.core.snapshot.parser

import com.gkd.core.snapshot.model.Snapshot
import kotlinx.serialization.json.Json

object SnapshotParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parse(jsonString: String): Snapshot {
        return json.decodeFromString<Snapshot>(jsonString)
    }

    fun parseOrNull(jsonString: String): Snapshot? {
        return try {
            parse(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
