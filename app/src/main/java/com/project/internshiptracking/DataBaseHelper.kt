package com.project.internshiptracking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "internship_tracker.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "fullName TEXT, " +
                    "password TEXT)"
        )
        db.execSQL(
            "CREATE TABLE interns (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "school TEXT, " +
                    "startDate TEXT, " +
                    "endDate TEXT, " +
                    "salary TEXT, " +
                    "instructor TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS interns")
        onCreate(db)
    }

    fun insertUser(fullName: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("fullName", fullName)
        values.put("password", password)
        return db.insert("users", null, values)
    }

    fun checkUser(fullName: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM users WHERE fullName = ? AND password = ?",
            arrayOf(fullName, password)
        )
        val found = cursor.moveToFirst()
        cursor.close()
        return found
    }

    fun insertIntern(
        name: String,
        school: String,
        startDate: String,
        endDate: String,
        salary: String,
        instructor: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("school", school)
        values.put("startDate", startDate)
        values.put("endDate", endDate)
        values.put("salary", salary)
        values.put("instructor", instructor)
        return db.insert("interns", null, values)
    }

    fun updateIntern(
        id: Int,
        name: String,
        school: String,
        startDate: String,
        endDate: String,
        salary: String,
        instructor: String
    ) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("school", school)
        values.put("startDate", startDate)
        values.put("endDate", endDate)
        values.put("salary", salary)
        values.put("instructor", instructor)
        db.update("interns", values, "id = ?", arrayOf(id.toString()))
    }

    fun getAllInterns(): List<Intern> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, school, startDate, endDate, salary, instructor FROM interns",
            null
        )
        val result = mutableListOf<Intern>()
        while (cursor.moveToNext()) {
            result.add(
                Intern(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    school = cursor.getString(2),
                    startDate = cursor.getString(3),
                    endDate = cursor.getString(4),
                    salary = cursor.getString(5),
                    instructor = cursor.getString(6)
                )
            )
        }
        cursor.close()
        return result
    }

    fun deleteIntern(id: Int) {
        val db = writableDatabase
        db.delete("interns", "id = ?", arrayOf(id.toString()))
    }
}