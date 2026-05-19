package ua.kpi.hotel.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ua.kpi.hotel.entity.Room;

@Dao
public interface RoomDao {

    @Insert
    void create(Room room);

    @Query("SELECT * FROM rooms ORDER BY roomNumber ASC")
    List<Room> findAll();

    @Query("SELECT * FROM rooms WHERE id = :id")
    Room findById(int id);

    @Update
    void update(Room room);

    @Query("DELETE FROM rooms WHERE id = :id AND isAvailable = 1")
    int deleteOnlyIfAvailable(int id);
}
