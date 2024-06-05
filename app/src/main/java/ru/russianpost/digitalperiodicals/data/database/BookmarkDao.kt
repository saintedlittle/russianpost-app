package ru.russianpost.digitalperiodicals.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.russianpost.digitalperiodicals.entities.Bookmark

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks_list WHERE editionId == :editionId AND toRemove == :toRemove ORDER BY page")
    suspend fun getBookmarks(editionId: Int, toRemove: Boolean = false): List<Bookmark>

    @Query("SELECT * FROM bookmarks_list WHERE editionId == :editionId AND serverId IS NULL AND toRemove == :toRemove")
    suspend fun getLocalBookmarks(editionId: Int, toRemove: Boolean = false): List<Bookmark>

    @Query("SELECT * FROM bookmarks_list WHERE editionId == :editionId AND serverId IS NOT NULL AND toRemove == :toRemove")
    suspend fun getSynchronizedBookmarks(editionId: Int, toRemove: Boolean = false): List<Bookmark>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmarkList(bookmarks: List<Bookmark>)

    @Query("UPDATE bookmarks_list SET serverId = :serverId WHERE localId == :localId")
    suspend fun updateServerId(localId: Int, serverId: Int?)

    @Query("DELETE FROM bookmarks_list WHERE localId == :localId")
    suspend fun deleteBookmark(localId: Int)

    @Query("DELETE FROM bookmarks_list WHERE localId IN (:idList)")
    suspend fun deleteBookmarkList(idList: String)

    @Query("UPDATE bookmarks_list SET toRemove = :toRemove WHERE localId == :localId")
    suspend fun setToRemoveToTrue(localId: Int, toRemove: Boolean = true)

    @Query("SELECT * FROM bookmarks_list WHERE editionId == :editionId AND toRemove == :toRemove")
    suspend fun getBookmarksMarkedToRemove(editionId: Int, toRemove: Boolean = true): List<Bookmark>

    @Query("DELETE FROM bookmarks_list WHERE editionId == :editionId AND toRemove == :toRemove")
    suspend fun deleteBookmarksMarkedToRemove(editionId: Int, toRemove: Boolean = true)

    @Query("DELETE FROM bookmarks_list")
    suspend fun deleteAllBookmarks()
}