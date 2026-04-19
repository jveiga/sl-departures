package veiga.sl.departures.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorite_stops")
data class FavoriteStopEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Dao
interface FavoriteStopDao {
    @Query("SELECT * FROM favorite_stops")
    fun getAllFavorites(): Flow<List<FavoriteStopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(stop: FavoriteStopEntity)

    @Query("DELETE FROM favorite_stops WHERE id = :id")
    suspend fun deleteFavorite(id: String)
}

@Database(entities = [FavoriteStopEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteStopDao(): FavoriteStopDao
}
