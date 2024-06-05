package ru.russianpost.digitalperiodicals.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.russianpost.digitalperiodicals.entities.Bookmark
import ru.russianpost.digitalperiodicals.entities.Edition

@Database(entities = [Bookmark::class, Edition::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun editionsDao(): EditionsDao
}