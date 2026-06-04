package com.garci.pokegarci.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.garci.pokegarci.data.mapper.AbilityJsonCodec
import com.garci.pokegarci.domain.model.Ability

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

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pokemon_new (
                id INTEGER NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                imageUrl TEXT NOT NULL,
                type1 TEXT NOT NULL,
                type2 TEXT,
                description TEXT NOT NULL,
                hp INTEGER NOT NULL,
                attack INTEGER NOT NULL,
                defense INTEGER NOT NULL,
                specialAttack INTEGER NOT NULL,
                specialDefense INTEGER NOT NULL,
                speed INTEGER NOT NULL,
                height INTEGER NOT NULL,
                weight INTEGER NOT NULL,
                abilitiesJson TEXT NOT NULL
            )
            """.trimIndent(),
        )

        val cursor = db.query(
            """
            SELECT id, name, imageUrl, type1, type2, description, hp, attack, defense,
                   specialAttack, specialDefense, speed, height, weight,
                   abilityOriginalName, abilityDisplayName
            FROM pokemon
            """.trimIndent(),
        )

        cursor.use {
            while (it.moveToNext()) {
                val abilitiesJson = AbilityJsonCodec.encode(
                    listOf(
                        Ability(
                            originalName = it.getString(14) ?: "unknown",
                            displayName = it.getString(15) ?: "Unknown",
                        ),
                    ),
                )
                db.execSQL(
                    """
                    INSERT INTO pokemon_new (
                        id, name, imageUrl, type1, type2, description, hp, attack, defense,
                        specialAttack, specialDefense, speed, height, weight, abilitiesJson
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    arrayOf(
                        it.getInt(0),
                        it.getString(1),
                        it.getString(2),
                        it.getString(3),
                        it.getString(4),
                        it.getString(5),
                        it.getInt(6),
                        it.getInt(7),
                        it.getInt(8),
                        it.getInt(9),
                        it.getInt(10),
                        it.getInt(11),
                        it.getInt(12),
                        it.getInt(13),
                        abilitiesJson,
                    ),
                )
            }
        }

        db.execSQL("DROP TABLE pokemon")
        db.execSQL("ALTER TABLE pokemon_new RENAME TO pokemon")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE pokemon ADD COLUMN legacyCryUrl TEXT NOT NULL DEFAULT ''",
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE pokemon ADD COLUMN backImageUrl TEXT NOT NULL DEFAULT ''",
        )
    }
}
