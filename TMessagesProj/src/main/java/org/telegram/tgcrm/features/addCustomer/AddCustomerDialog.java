package org.telegram.tgcrm.features.addCustomer;

import android.content.Context;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.tgcrm.TgCrmUtilities;
import org.telegram.tgcrm.model.Customer;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

/**
 * Created by : Azamat Kalmurzaev
 * 08/03/25
 */
public class AddCustomerDialog {
    private final Context context;
    private AlertDialog dialog;
    private TgCrmUtilities tgCrmUtilities;
    private String type;

    public AddCustomerDialog(Context context, TgCrmUtilities tgCrmUtilities,String type) {
        this.context = context;
        this.tgCrmUtilities = tgCrmUtilities;
        this.type=type;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Root layout
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        layout.setPadding(padding, 12, padding, padding);

        // Name Field
        TextView nameTextView = new TextView(context);
        nameTextView.setText("Ismi");
        nameTextView.setTextSize(16f);
        nameTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        layout.addView(nameTextView);

        EditText nameEditText = new EditText(context);
        nameEditText.setHint("Ismingizni kiriting");
        nameEditText.setTextSize(16f);
        nameEditText.setHintTextColor(Theme.getColor(Theme.key_dialogSearchHint));
        nameEditText.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        layout.addView(nameEditText);

        // Phone Field
        TextView phoneTextView = new TextView(context);
        phoneTextView.setText("Telefon raqami:");
        phoneTextView.setTextSize(16f);
        phoneTextView.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));

        phoneTextView.setPadding(0, 12, 0, 0);
        layout.addView(phoneTextView);

        EditText phoneEditText = new EditText(context);
        phoneEditText.setHint("+998901234567");
        phoneEditText.setTextSize(16f);
        phoneEditText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        layout.addView(phoneEditText);

        LinearLayout horizontal = new LinearLayout(context);
        horizontal.setOrientation(LinearLayout.HORIZONTAL);
        horizontal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontal.setPadding(0, 16, 0, 0);
        horizontal.setWeightSum(2);
        layout.addView(horizontal);

        // Cancel Button
        Button cancelBtn = new Button(context);
        cancelBtn.setText("Bekor qilish");
        cancelBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        horizontal.addView(cancelBtn);

        // Confirm Button
        Button confirmBtn = new Button(context);
        confirmBtn.setText("Yuborish");
        confirmBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        confirmBtn.setOnClickListener(v ->{
            String name = nameEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            checkUserData(name, phone);
        });
        horizontal.addView(confirmBtn);
        builder.setView(layout);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public void sendDataToServer(String name, String phone, String type) {
        tgCrmUtilities.firestore.collection("customers")
                .add(new Customer(name, phone, type))
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Mijoz saqlandi", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Xatolik yuz berdi:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
    }

    public void checkUserData(String name, String phone) {
        if (name.isEmpty()) {
            Toast.makeText(context, "Ismingizni kiriting", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(context, "Telefon raqamingizni kiriting", Toast.LENGTH_SHORT).show();
            return;
        }

        sendDataToServer(name, phone,type);
    }
}

