package ua.kpi.hotel.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rooms")
public class Room {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int roomNumber;
    private double pricePerNight;
    private boolean isAvailable;
    private String roomType;
    private int maxGuests;

    public Room() {
    }

    public static class Builder {
        private final Room instance = new Room();

        public Builder setRoomNumber(int number) { instance.roomNumber = number; return this; }
        public Builder setPrice(double price) { instance.pricePerNight = price; return this; }
        public Builder setAvailable(boolean available) { instance.isAvailable = available; return this; }
        public Builder setRoomType(String type) { instance.roomType = type; return this; }
        public Builder setMaxGuests(int guests) { instance.maxGuests = guests; return this; }

        public Room build() { return instance; }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getMaxGuests() { return maxGuests; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }
}