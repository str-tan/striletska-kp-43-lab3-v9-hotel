package ua.kpi.hotel;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.materialswitch.MaterialSwitch;
import ua.kpi.hotel.database.HotelDatabase;
import ua.kpi.hotel.entity.Room;

public class DetailActivity extends AppCompatActivity {

    private EditText etRoomNumber, etPrice, etMaxGuests, etNights;
    private Spinner spinnerRoomType;
    private MaterialSwitch switchAvailable;
    private Button btnSave, btnDelete, btnBook;
    private TextView tvFormTitle, tvTotalAmount;
    private LinearLayout layoutClientBooking;

    private HotelDatabase db;
    private int roomId = -1;
    private boolean isAdminMode = false;
    private boolean isRoomAvailableInDb = true;
    private double currentRoomPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = HotelDatabase.getInstance(getApplicationContext());

        tvFormTitle = findViewById(R.id.tvFormTitle);
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etPrice = findViewById(R.id.etPrice);
        etMaxGuests = findViewById(R.id.etMaxGuests);
        spinnerRoomType = findViewById(R.id.spinnerRoomType);
        switchAvailable = findViewById(R.id.switchAvailable);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        layoutClientBooking = findViewById(R.id.layoutClientBooking);
        etNights = findViewById(R.id.etNights);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnBook = findViewById(R.id.btnBook);

        String[] roomTypes = {"Standard", "Deluxe", "Suite"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roomTypes) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.parseColor("#212529"));
                view.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.parseColor("#212529"));
                view.setTextSize(16);
                return view;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoomType.setAdapter(spinnerAdapter);

        if (getIntent() != null) {
            roomId = getIntent().getIntExtra("ROOM_ID", -1);
            isAdminMode = getIntent().getBooleanExtra("IS_ADMIN", false);
        }

        if (roomId != -1) {
            loadRoomData(roomId);
        } else {
            tvFormTitle.setText("Додавання нового номера");
            configureAccessRights();
        }

        etNights.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calculateTotalCost(); }
        });

        btnSave.setOnClickListener(v -> saveRoom());
        btnDelete.setOnClickListener(v -> deleteRoomWithBackendCheck());
        btnBook.setOnClickListener(v -> bookRoomByClient());
    }

    private void configureAccessRights() {
        if (!isAdminMode) {
            etRoomNumber.setEnabled(false);
            etPrice.setEnabled(false);
            etMaxGuests.setEnabled(false);
            spinnerRoomType.setEnabled(false);
            switchAvailable.setEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        } else {
            if (!isRoomAvailableInDb) {
                etRoomNumber.setEnabled(false);
                etPrice.setEnabled(false);
                etMaxGuests.setEnabled(false);
                spinnerRoomType.setEnabled(false);

                switchAvailable.setEnabled(true);
                btnSave.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);

                Toast.makeText(this, "Увага! Номер зайнятий. Дозволено лише зміну статусу доступності.", Toast.LENGTH_LONG).show();
            } else {
                etRoomNumber.setEnabled(true);
                etPrice.setEnabled(true);
                etMaxGuests.setEnabled(true);
                spinnerRoomType.setEnabled(true);
                switchAvailable.setEnabled(true);
                btnSave.setVisibility(View.VISIBLE);
                if (roomId != -1) btnDelete.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadRoomData(int id) {
        new Thread(() -> {
            Room room = db.roomDao().findById(id);
            if (room != null) {
                currentRoomPrice = room.getPricePerNight();
                isRoomAvailableInDb = room.isAvailable();

                runOnUiThread(() -> {
                    tvFormTitle.setText(isAdminMode ? "Редагування номера" : "Бронювання номера №" + room.getRoomNumber());
                    etRoomNumber.setText(String.valueOf(room.getRoomNumber()));
                    etPrice.setText(String.valueOf(room.getPricePerNight()));
                    etMaxGuests.setText(String.valueOf(room.getMaxGuests()));
                    switchAvailable.setChecked(room.isAvailable());

                    if (room.getRoomType().equals("Deluxe")) spinnerRoomType.setSelection(1);
                    else if (room.getRoomType().equals("Suite")) spinnerRoomType.setSelection(2);
                    else spinnerRoomType.setSelection(0);

                    configureAccessRights();

                    if (!isAdminMode) {
                        if (room.isAvailable()) {
                            layoutClientBooking.setVisibility(View.VISIBLE);
                            btnBook.setVisibility(View.VISIBLE);
                            calculateTotalCost();
                        } else {
                            tvFormTitle.setText("Номер №" + room.getRoomNumber() + " (Зайнятий)");
                            Toast.makeText(this, "Цей номер вже заброньовано іншим гостем!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void calculateTotalCost() {
        String nightsStr = etNights.getText().toString().trim();
        if (nightsStr.isEmpty()) {
            tvTotalAmount.setText("Разом до сплати: 0.00 $");
            return;
        }
        try {
            int nights = Integer.parseInt(nightsStr);
            if (nights > 0) {
                double total = nights * currentRoomPrice;
                tvTotalAmount.setText(String.format("Разом до сплати: %.2f $", total));
            } else {
                tvTotalAmount.setText("Кількість ночей має бути не менше 1");
            }
        } catch (NumberFormatException e) {
            tvTotalAmount.setText("Некоректний формат днів");
        }
    }

    private void bookRoomByClient() {
        String nightsStr = etNights.getText().toString().trim();
        if (nightsStr.isEmpty()) {
            showErrorDialog("Помилка!", "Будь ласка, вкажіть кількість ночей.");
            return;
        }

        int nights = Integer.parseInt(nightsStr);
        if (nights <= 0) {
            showErrorDialog("Помилка!", "Кількість ночей для проживання має бути не менше 1!");
            return;
        }

        new Thread(() -> {
            try {
                Room room = db.roomDao().findById(roomId);
                if (room != null && room.isAvailable()) {
                    room.setAvailable(false);
                    db.roomDao().update(room);

                    runOnUiThread(() -> {
                        Toast.makeText(DetailActivity.this, "Вітаємо! Номер успішно заброньовано!", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> showErrorDialog("Помилка!", "Не вдалося обробити запит бронювання."));
            }
        }).start();
    }

    private void deleteRoomWithBackendCheck() {
        new Thread(() -> {
            int rowsDeleted = db.roomDao().deleteOnlyIfAvailable(roomId);
            runOnUiThread(() -> {
                if (rowsDeleted > 0) {
                    Toast.makeText(DetailActivity.this, "Номер успішно видалено!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showErrorDialog("Помилка!", "Заборонено видаляти номер! Цей номер наразі заброньований клієнтом.");
                }
            });
        }).start();
    }

    private void saveRoom() {
        try {
            String numberStr = etRoomNumber.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String guestsStr = etMaxGuests.getText().toString().trim();

            if (numberStr.isEmpty() || priceStr.isEmpty() || guestsStr.isEmpty()) {
                showErrorDialog("Помилка!", "Усі поля повинні бути заповнені!");
                return;
            }

            int roomNumber = Integer.parseInt(numberStr);
            double price = Double.parseDouble(priceStr);
            int maxGuests = Integer.parseInt(guestsStr);

            if (roomNumber <= 0 || price <= 0 || maxGuests <= 0) {
                showErrorDialog("Помилка!", "Номер кімнати, ціна та кількість гостей мають бути не менше 1!");
                return;
            }

            String roomType = spinnerRoomType.getSelectedItem().toString();
            boolean isAvailable = switchAvailable.isChecked();

            new Thread(() -> {
                try {
                    Room room;
                    if (roomId == -1) {
                        room = new Room.Builder()
                                .setRoomNumber(roomNumber)
                                .setPrice(price)
                                .setAvailable(isAvailable)
                                .setRoomType(roomType)
                                .setMaxGuests(maxGuests)
                                .build();
                        db.roomDao().create(room);
                    } else {
                        room = db.roomDao().findById(roomId);
                        room.setRoomNumber(roomNumber);
                        room.setPricePerNight(price);
                        room.setAvailable(isAvailable);
                        room.setRoomType(roomType);
                        room.setMaxGuests(maxGuests);
                        db.roomDao().update(room);
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(DetailActivity.this, "Дані успішно збережено!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> showErrorDialog("Помилка!", "Помилка в опрацюванні запиту."));
                }
            }).start();

        } catch (NumberFormatException e) {
            showErrorDialog("Помилка!", "Введено некоректний формат чисел!");
        }
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}