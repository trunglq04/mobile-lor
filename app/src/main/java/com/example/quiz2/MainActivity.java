package com.example.quiz2;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.quiz2.model.Cart;
import com.example.quiz2.model.CartItem;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int FOOD_REQUEST_CODE = 1;
    private static final int DRINK_REQUEST_CODE = 2;

    private TextView tvSelectedFood, tvSelectedDrink, tvTotalPrice, tvCartItems;
    private Button btnChooseFood, btnChooseDrink, btnOrder;

    private String selectedFoodName = "";
    private double selectedFoodPrice = 0;
    private String selectedDrinkName = "";
    private double selectedDrinkPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        updateDisplay();
    }

    private void initViews() {
        tvSelectedFood = findViewById(R.id.tv_selected_food);
        tvSelectedDrink = findViewById(R.id.tv_selected_drink);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnChooseFood = findViewById(R.id.btn_choose_food);
        btnChooseDrink = findViewById(R.id.btn_choose_drink);
        btnOrder = findViewById(R.id.btn_order);
        tvCartItems = findViewById(R.id.tv_cart_items); // Add this TextView to your layout
    }

    private void setupListeners() {
        btnChooseFood.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FoodActivity.class);
            startActivityForResult(intent, FOOD_REQUEST_CODE);
        });

        btnChooseDrink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DrinkActivity.class);
            startActivityForResult(intent, DRINK_REQUEST_CODE);
        });

        btnOrder.setOnClickListener(v -> {
            if (selectedFoodName.isEmpty() && selectedDrinkName.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một món!", Toast.LENGTH_SHORT).show();
            } else {
                String orderDetails = "Đơn hàng của bạn:\n";
                if (!selectedFoodName.isEmpty()) {
                    orderDetails += "Món ăn: " + selectedFoodName + "\n";
                }
                if (!selectedDrinkName.isEmpty()) {
                    orderDetails += "Đồ uống: " + selectedDrinkName + "\n";
                }
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                orderDetails += "Tổng tiền: " + formatter.format(selectedFoodPrice + selectedDrinkPrice);

                Toast.makeText(this, orderDetails, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == FOOD_REQUEST_CODE) {
                selectedFoodName = data.getStringExtra("food_name");
                selectedFoodPrice = data.getDoubleExtra("food_price", 0);
            } else if (requestCode == DRINK_REQUEST_CODE) {
                selectedDrinkName = data.getStringExtra("drink_name");
                selectedDrinkPrice = data.getDoubleExtra("drink_price", 0);
            }
            updateDisplay();
        }
    }

    private void updateDisplay() {
        // Show all items in cart with quantity and allow remove
        StringBuilder cartDetails = new StringBuilder();
        for (CartItem item : Cart.getInstance().getItems()) {
            cartDetails.append(item.getName())
                .append(" (x")
                .append(item.getQuantity())
                .append(") - ")
                .append(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(item.getPrice() * item.getQuantity()))
                .append("  [X]")
                .append("\n");
        }
        if (cartDetails.length() == 0) {
            tvCartItems.setText("Chưa có món nào trong giỏ hàng");
        } else {
            tvCartItems.setText(cartDetails.toString());
        }
        // Show total price
        tvTotalPrice.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(Cart.getInstance().getTotalPrice()));

        // Set up click to remove item
        tvCartItems.setOnClickListener(v -> {
            if (Cart.getInstance().getItems().isEmpty()) return;
            String[] itemsArr = new String[Cart.getInstance().getItems().size()];
            for (int i = 0; i < itemsArr.length; i++) {
                CartItem item = Cart.getInstance().getItems().get(i);
                itemsArr[i] = item.getName() + " (" + item.getType() + ") x" + item.getQuantity();
            }
            new AlertDialog.Builder(this)
                .setTitle("Xóa món khỏi giỏ hàng")
                .setItems(itemsArr, (dialog, which) -> {
                    CartItem item = Cart.getInstance().getItems().get(which);
                    Cart.getInstance().removeItem(item.getName(), item.getType());
                    updateDisplay();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
    }
}