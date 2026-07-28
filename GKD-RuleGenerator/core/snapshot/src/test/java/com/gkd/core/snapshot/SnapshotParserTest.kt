// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.core.snapshot

import com.gkd.core.snapshot.parser.SnapshotParser
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SnapshotParserTest {
    @Test
    fun `parse valid snapshot json`() {
        val json = """
        {
            "id": 123,
            "appId": "com.test.app",
            "activityId": "com.test.app.MainActivity",
            "screenHeight": 1920,
            "screenWidth": 1080,
            "isLandscape": false,
            "appInfo": {
                "id": "com.test.app",
                "name": "Test App",
                "versionCode": 1,
                "versionName": "1.0",
                "isSystem": false
            },
            "gkdAppInfo": {
                "id": "li.songe.gkd",
                "name": "GKD",
                "versionCode": 1,
                "versionName": "1.0"
            },
            "device": {
                "device": "test",
                "model": "Test",
                "manufacturer": "Test",
                "brand": "Test",
                "sdkInt": 30,
                "release": "11"
            },
            "nodes": []
        }
        """.trimIndent()

        val snapshot = SnapshotParser.parseOrNull(json)
        assertNotNull(snapshot)
    }

    @Test
    fun `parse invalid json returns null`() {
        val json = "invalid json"
        val snapshot = SnapshotParser.parseOrNull(json)
        assertNull(snapshot)
    }
}
