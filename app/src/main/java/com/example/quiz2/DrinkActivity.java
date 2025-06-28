package com.example.quiz2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;
import com.example.quiz2.adapter.DrinkAdapter;
import com.example.quiz2.database.AppDatabase;
import com.example.quiz2.model.Drink;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DrinkActivity extends AppCompatActivity {
    private ListView listView;
    private Button btnAddDrink, btnDeleteDrink, btnUpdateDrink;
    private DrinkAdapter adapter;
    private List<Drink> drinkList;
    private AppDatabase database;
    private ExecutorService executor;
    private Handler mainHandler;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);

        initViews();
        initDatabase();
        setupListeners();
        loadDrinks();
    }

    private void initViews() {
        listView = findViewById(R.id.lv_drinks);
        btnAddDrink = findViewById(R.id.btn_add_drink);
        btnDeleteDrink = findViewById(R.id.btn_delete_drink);
        btnUpdateDrink = findViewById(R.id.btn_update_drink);
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(this);
        executor = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize default data if database is empty
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        executor.execute(() -> {
            List<Drink> existingDrinks = database.drinkDao().getAllDrinks();
            if (existingDrinks.isEmpty()) {
                database.drinkDao().insertDrink(new Drink("Pepsi", "Nước ngọt Pepsi mát lạnh", 15000, "pepsi"));
                database.drinkDao().insertDrink(new Drink("Heineken", "Bia Heineken nhập khẩu", 25000, "heineken"));
                database.drinkDao().insertDrink(new Drink("Tiger", "Bia Tiger thơm ngon", 20000, "tiger"));
                database.drinkDao().insertDrink(new Drink("Sài Gòn Đỏ", "Bia Sài Gòn Đỏ truyền thống", 18000, "saigon_do"));
            }
        });
    }

    private void setupListeners() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            adapter.setSelectedPosition(position); // highlight selected
            adapter.notifyDataSetChanged();
            showQuantityDialog(drinkList.get(position));
        });

        btnAddDrink.setOnClickListener(v -> showAddDrinkDialog());
        btnDeleteDrink.setOnClickListener(v -> deleteSelectedDrink());
        btnUpdateDrink.setOnClickListener(v -> showEditDrinkDialog());
    }

    private void showQuantityDialog(Drink drink) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Quantity");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null);
        final EditText inputQuantity = viewInflated.findViewById(R.id.input_quantity);
        inputQuantity.setText("1");
        builder.setView(viewInflated);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String quantityStr = inputQuantity.getText().toString();
            int quantity = 1;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            // Add to cart instead of returning result
            com.example.quiz2.model.Cart.getInstance().addItem(drink.getName(), drink.getPrice(), quantity, "drink");
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddDrinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Drink");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_item, null);
        final EditText inputName = viewInflated.findViewById(R.id.input_name);
        final EditText inputDesc = viewInflated.findViewById(R.id.input_desc);
        final EditText inputPrice = viewInflated.findViewById(R.id.input_price);
        final EditText inputImage = viewInflated.findViewById(R.id.input_image);
        builder.setView(viewInflated);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String name = inputName.getText().toString();
            String desc = inputDesc.getText().toString();
            String priceStr = inputPrice.getText().toString();
            String image = inputImage.getText().toString();
            if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            int price = Integer.parseInt(priceStr);
            executor.execute(() -> {
                Drink newDrink = new Drink(name, desc, price, image);
                database.drinkDao().insertDrink(newDrink);
                mainHandler.post(() -> {
                    loadDrinks();
                    Toast.makeText(this, "Drink added", Toast.LENGTH_SHORT).show();
                });
            });
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditDrinkDialog() {
        if (selectedPosition < 0 || selectedPosition >= drinkList.size()) {
            Toast.makeText(this, "Select a drink to edit", Toast.LENGTH_SHORT).show();
            return;
        }
        Drink drink = drinkList.get(selectedPosition);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Drink");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_item, null);
        final EditText inputName = viewInflated.findViewById(R.id.input_name);
        final EditText inputDesc = viewInflated.findViewById(R.id.input_desc);
        final EditText inputPrice = viewInflated.findViewById(R.id.input_price);
        final EditText inputImage = viewInflated.findViewById(R.id.input_image);
        inputName.setText(drink.getName());
        inputDesc.setText(drink.getDescription());
        inputPrice.setText(String.valueOf(drink.getPrice()));
        inputImage.setText(drink.getImageName());
        builder.setView(viewInflated);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String name = inputName.getText().toString();
            String desc = inputDesc.getText().toString();
            String priceStr = inputPrice.getText().toString();
            String image = inputImage.getText().toString();
            if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            double price = Double.parseDouble(priceStr);
            drink.setName(name);
            drink.setDescription(desc);
            drink.setPrice(price);
            drink.setImageName(image);
            executor.execute(() -> {
                database.drinkDao().updateDrink(drink);
                mainHandler.post(() -> {
                    loadDrinks();
                    Toast.makeText(this, "Drink updated", Toast.LENGTH_SHORT).show();
                });
            });
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadDrinks() {
        executor.execute(() -> {
            drinkList = database.drinkDao().getAllDrinks();
            mainHandler.post(() -> {
                adapter = new DrinkAdapter(this, drinkList);
                listView.setAdapter(adapter);
            });
        });
    }

    private void deleteSelectedDrink() {
        if (selectedPosition >= 0 && selectedPosition < drinkList.size()) {
            Drink drinkToDelete = drinkList.get(selectedPosition);
            executor.execute(() -> {
                database.drinkDao().deleteDrink(drinkToDelete);

                mainHandler.post(() -> {
                    loadDrinks();
                    selectedPosition = -1;
                    Toast.makeText(this, "Đã xóa đồ uống", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            Toast.makeText(this, "Vui lòng chọn một đồ uống để xóa", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}