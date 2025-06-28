package com.example.quiz2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;
import com.example.quiz2.adapter.FoodAdapter;
import com.example.quiz2.database.AppDatabase;
import com.example.quiz2.model.Food;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class FoodActivity extends AppCompatActivity {
    private ListView listView;
    private Button btnAddFood, btnDeleteFood, btnUpdateFood;
    private FoodAdapter adapter;
    private List<Food> foodList;
    private AppDatabase database;
    private ExecutorService executor;
    private Handler mainHandler;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        initViews();
        initDatabase();
        setupListeners();
        loadFoods();
    }

    private void initViews() {
        listView = findViewById(R.id.lv_foods);
        btnAddFood = findViewById(R.id.btn_add_food);
        btnDeleteFood = findViewById(R.id.btn_delete_food);
        btnUpdateFood = findViewById(R.id.btn_update_food);
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
            List<Food> existingFoods = database.foodDao().getAllFoods();
            if (existingFoods.isEmpty()) {
                database.foodDao().insertFood(new Food("Phở Hà Nội", "Món phở truyền thống Hà Nội với nước dùng đậm đà", 45000, "pho"));
                database.foodDao().insertFood(new Food("Bún Bò Huế", "Bún bò Huế cay nồng đặc trưng miền Trung", 40000, "bun_bo_hue"));
                database.foodDao().insertFood(new Food("Mì Quảng", "Mì Quảng đậm đà hương vị Quảng Nam", 38000, "mi_quang"));
                database.foodDao().insertFood(new Food("Hủ Tíu Sài Gòn", "Hủ tíu Nam Vang thanh mát", 35000, "hu_tieu"));
            }
        });
    }

    private void setupListeners() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            adapter.setSelectedPosition(position); // highlight selected
            adapter.notifyDataSetChanged();
            showQuantityDialog(foodList.get(position));
        });

        btnAddFood.setOnClickListener(v -> showAddFoodDialog());
        btnDeleteFood.setOnClickListener(v -> deleteSelectedFood());
        btnUpdateFood.setOnClickListener(v -> showEditFoodDialog());
    }

    private void showQuantityDialog(Food food) {
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
            com.example.quiz2.model.Cart.getInstance().addItem(food.getName(), food.getPrice(), quantity, "food");
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddFoodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Food");
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
                Food newFood = new Food(name, desc, price, image);
                database.foodDao().insertFood(newFood);
                mainHandler.post(() -> {
                    loadFoods();
                    Toast.makeText(this, "Food added", Toast.LENGTH_SHORT).show();
                });
            });
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditFoodDialog() {
        if (selectedPosition < 0 || selectedPosition >= foodList.size()) {
            Toast.makeText(this, "Select a food to edit", Toast.LENGTH_SHORT).show();
            return;
        }
        Food food = foodList.get(selectedPosition);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Food");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_item, null);
        final EditText inputName = viewInflated.findViewById(R.id.input_name);
        final EditText inputDesc = viewInflated.findViewById(R.id.input_desc);
        final EditText inputPrice = viewInflated.findViewById(R.id.input_price);
        final EditText inputImage = viewInflated.findViewById(R.id.input_image);
        inputName.setText(food.getName());
        inputDesc.setText(food.getDescription());
        inputPrice.setText(String.valueOf(food.getPrice()));
        inputImage.setText(food.getImageName());
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
            food.setName(name);
            food.setDescription(desc);
            food.setPrice(price);
            food.setImageName(image);
            executor.execute(() -> {
                database.foodDao().updateFood(food);
                mainHandler.post(() -> {
                    loadFoods();
                    Toast.makeText(this, "Food updated", Toast.LENGTH_SHORT).show();
                });
            });
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadFoods() {
        executor.execute(() -> {
            foodList = database.foodDao().getAllFoods();
            mainHandler.post(() -> {
                adapter = new FoodAdapter(this, foodList);
                listView.setAdapter(adapter);
            });
        });
    }

    private void deleteSelectedFood() {
        if (selectedPosition >= 0 && selectedPosition < foodList.size()) {
            Food foodToDelete = foodList.get(selectedPosition);
            executor.execute(() -> {
                database.foodDao().deleteFood(foodToDelete);

                mainHandler.post(() -> {
                    loadFoods();
                    selectedPosition = -1;
                    Toast.makeText(this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            Toast.makeText(this, "Vui lòng chọn một món để xóa", Toast.LENGTH_SHORT).show();
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
