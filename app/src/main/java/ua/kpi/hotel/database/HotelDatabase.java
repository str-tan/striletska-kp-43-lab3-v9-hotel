package ua.kpi.hotel.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import ua.kpi.hotel.dao.RoomDao;
import ua.kpi.hotel.entity.Room;

@Database(entities = {Room.class}, version = 1, exportSchema = false)
public abstract class HotelDatabase extends RoomDatabase {

    private static volatile HotelDatabase INSTANCE;

    public abstract RoomDao roomDao();

    public static HotelDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (HotelDatabase.class) {
                if (INSTANCE == null) {
                    if (context == null) {
                        throw new IllegalArgumentException("Context cannot be null");
                    }
                    INSTANCE = androidx.room.Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    HotelDatabase.class,
                                    "hotel_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}