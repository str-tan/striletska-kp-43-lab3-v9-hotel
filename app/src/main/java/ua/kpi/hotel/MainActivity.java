package ua.kpi.hotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ua.kpi.hotel.database.HotelDatabase;
import ua.kpi.hotel.entity.Room;

public class MainActivity extends AppCompatActivity {

    private TextView tvAdminStatus;
    private Button btnAdminLogin, btnSortPrice, btnAddRoom;
    private ListView lvRooms;

    private HotelDatabase db;
    private List<Room> rooms = new ArrayList<>();
    private RoomAdapter adapter;

    private boolean isAdmin = false;
    private boolean isSortedAsc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = HotelDatabase.getInstance(this);

        tvAdminStatus = findViewById(R.id.tvAdminStatus);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        btnSortPrice = findViewById(R.id.btnSortPrice);
        btnAddRoom = findViewById(R.id.btnAddRoom);
        lvRooms = findViewById(R.id.lvRooms);

        btnAdminLogin.setOnClickListener(v -> handleAdminAuth());
        btnSortPrice.setOnClickListener(v -> toggleSortOrder());

        btnAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("IS_ADMIN", isAdmin);
            startActivity(intent);
        });

        lvRooms.setOnItemClickListener((parent, view, position, id) -> {
            Room selectedRoom = rooms.get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("ROOM_ID", selectedRoom.getId());
            intent.putExtra("IS_ADMIN", isAdmin);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRoomsFromDb();
    }

    private void loadRoomsFromDb() {
        new Thread(() -> {
            rooms = db.roomDao().findAll();
            runOnUiThread(() -> {
                adapter = new RoomAdapter(MainActivity.this, rooms);
                lvRooms.setAdapter(adapter);
            });
        }).start();
    }

    private void handleAdminAuth() {
        if (isAdmin) {
            isAdmin = false;
            tvAdminStatus.setText("Режим: Клієнт");
            tvAdminStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            btnAdminLogin.setText("Вхід (Адмін)");
            btnAddRoom.setVisibility(View.GONE);
            Toast.makeText(this, "Ви вийшли з режиму адміна", Toast.LENGTH_SHORT).show();
        } else {
            EditText etPassword = new EditText(this);
            etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            new AlertDialog.Builder(this)
                    .setTitle("Автентифікація")
                    .setMessage("Введіть пароль адміністратора:")
                    .setView(etPassword)
                    .setPositiveButton("Увійти", (dialog, which) -> {
                        String password = etPassword.getText().toString();
                        if ("kpi2026".equals(password)) {
                            isAdmin = true;
                            tvAdminStatus.setText("Режим: Administrator");
                            tvAdminStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            btnAdminLogin.setText("Вийти");
                            btnAddRoom.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Автентифікація успішна!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Неправильний пароль!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Скасувати", null)
                    .show();
        }
    }

    private void toggleSortOrder() {
        if (rooms == null || rooms.isEmpty()) return;

        if (isSortedAsc) {
            Collections.sort(rooms, (r1, r2) -> Double.compare(r2.getPricePerNight(), r1.getPricePerNight()));
            btnSortPrice.setText("Ціна: Спадання ↓");
            isSortedAsc = false;
        } else {
            Collections.sort(rooms, (r1, r2) -> Double.compare(r1.getPricePerNight(), r2.getPricePerNight()));
            btnSortPrice.setText("Ціна: Зростання ↑");
            isSortedAsc = true;
        }
        adapter.notifyDataSetChanged();
    }
}