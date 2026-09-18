package com.smartsolarmicrogrid.prosumer.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SessionDbHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "smart_solar_microgrid.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_SESSION = "session"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_SESSION (
                id INTEGER PRIMARY KEY,
                nic TEXT,
                fullName TEXT,
                role TEXT,
                accountStatus TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSION")
        onCreate(db)
    }

    fun saveSession(nic: String, fullName: String, role: String, accountStatus: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", 1)
            put("nic", nic)
            put("fullName", fullName)
            put("role", role)
            put("accountStatus", accountStatus)
        }
        db.insertWithOnConflict(TABLE_SESSION, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getSession(): SessionData? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT nic, fullName, role, accountStatus FROM $TABLE_SESSION WHERE id = 1", null)
        return cursor.use {
            if (it.moveToFirst()) {
                SessionData(
                    nic = it.getString(0),
                    fullName = it.getString(1),
                    role = it.getString(2),
                    accountStatus = it.getString(3)
                )
            } else null
        }
    }

    fun clearSession() {
        writableDatabase.execSQL("DELETE FROM $TABLE_SESSION")
    }
}

data class SessionData(
    val nic: String,
    val fullName: String,
    val role: String,
    val accountStatus: String
)