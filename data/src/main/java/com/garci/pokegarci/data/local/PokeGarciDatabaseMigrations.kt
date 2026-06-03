package com.garci.pokegarci.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE cache_metadata ADD COLUMN isFullCatalog INTEGER NOT NULL DEFAULT 0",
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE cache_metadata ADD COLUMN catalogMaxId INTEGER NOT NULL DEFAULT 0",
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ability_names (
                originalName TEXT NOT NULL,
                language TEXT NOT NULL,
                displayName TEXT NOT NULL,
                PRIMARY KEY(originalName, language)
            )
            """.trimIndent(),
        )
    }
}
