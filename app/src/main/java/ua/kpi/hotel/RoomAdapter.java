package ua.kpi.hotel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;
import ua.kpi.hotel.entity.Room;

public class RoomAdapter extends BaseAdapter {

    private final Context context;
    private final List<Room> roomList;

    public RoomAdapter(Context context, List<Room> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public Object getItem(int position) {
        return roomList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.room_item, parent, false);
        }

        TextView tvRoomNumber = convertView.findViewById(R.id.itemRoomNumber);
        TextView tvRoomType = convertView.findViewById(R.id.itemRoomType);
        TextView tvMaxGuests = convertView.findViewById(R.id.itemMaxGuests);
        TextView tvPrice = convertView.findViewById(R.id.itemPrice);
        TextView tvStatus = convertView.findViewById(R.id.itemStatus);

        Room room = roomList.get(position);

        tvRoomNumber.setText("Номер №" + room.getRoomNumber());
        tvRoomType.setText("Тип: " + room.getRoomType());
        tvMaxGuests.setText("Місткість: до " + room.getMaxGuests() + " осіб");
        tvPrice.setText(String.format("%.2f $", room.getPricePerNight()));

        if (room.isAvailable()) {
            tvStatus.setText("Вільний");
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStatus.setText("Зайнятий");
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        return convertView;
    }
}