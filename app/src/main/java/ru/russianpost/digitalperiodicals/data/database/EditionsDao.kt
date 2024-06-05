package ru.russianpost.digitalperiodicals.data.database

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import ru.russianpost.digitalperiodicals.entities.Edition

@Dao
interface EditionsDao {

    @Query("SELECT * FROM editions_list WHERE publicationId == :publicationId")
    suspend fun getAllEditions(publicationId: String): List<Edition>

    @RawQuery
    suspend fun searchEditions(query: SupportSQLiteQuery): List<Edition>

    @Query("SELECT * FROM EDITIONS_LIST WHERE id == :id")
    suspend fun getEditionById(id: Int): Edition

    @Query("SELECT * FROM editions_list WHERE publicationId == :publicationId AND recentlyReadNum IS NOT NULL ORDER BY recentlyReadNum")
    suspend fun getRecentlyReadEditions(publicationId: String): List<Edition>

    @Query("SELECT * FROM editions_list WHERE publicationId == :publicationId AND recentlyReadNum IS NULL ORDER BY recentlyReadNum")
    suspend fun getOtherEditions(publicationId: String): List<Edition>

    @Query("SELECT * FROM editions_list WHERE publicationId == :publicationId AND isDownloaded == :isDownloaded")
    suspend fun getDownloadedEditions(publicationId: String, isDownloaded: Boolean = true): List<Edition>

    @Query("SELECT * FROM editions_list WHERE isDownloaded == :isDownloaded")
    suspend fun getAllDownloadedEditions(isDownloaded: Boolean = true): List<Edition>

    @Query("UPDATE editions_list SET recentlyReadNum = :recentlyReadNum WHERE id == :id")
    suspend fun updateRecentlyRead(id: Int, recentlyReadNum: Int)

    @Query("UPDATE editions_list SET recentlyReadNum = :recentlyReadNum WHERE id == :id")
    suspend fun setRecentlyReadToNull(id: Int, recentlyReadNum: Int? = null)

    @Query("UPDATE editions_list SET isDownloaded = :isDownloaded WHERE id == :id ")
    suspend fun setIsDownloadedToTrue(id: Int, isDownloaded: Boolean = true)

    @Query("UPDATE editions_list SET isDownloaded = :isDownloaded WHERE id == :id ")
    suspend fun setIsDownloadedToFalse(id: Int, isDownloaded: Boolean = false)

    @Query("UPDATE EDITIONS_LIST SET isFavorite = :isFavorite WHERE id == :id")
    suspend fun updateFavorite(id:Int, isFavorite:Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEdition(edition: Edition)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEditionsList(list: List<Edition>)

    @Delete
    suspend fun deleteEdition(edition: Edition)

    @Query("DELETE FROM editions_list")
    suspend fun deleteAllEditions()

}