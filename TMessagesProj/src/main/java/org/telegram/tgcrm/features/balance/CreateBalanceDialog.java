package org.telegram.tgcrm.features.balance;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.telegram.tgcrm.TgCrmUtilities;
import org.telegram.tgcrm.model.BalanceData;
import org.telegram.tgcrm.model.UserCredentialsData;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

/**
 * Created by : Azamat Kalmurzaev
 * 11/03/25
 */
public class CreateBalanceDialog {

    private final Context context;

    private AlertDialog dialog;

    private TgCrmUtilities tgCrmUtilities;

    RadioButton radioButton40;
    RadioButton radioButton50;
    RadioButton radioButton60;
    RadioButton customPriceRadioButton;

    long dialogId;

    String dialogName;

    public CreateBalanceDialog(Context context, TgCrmUtilities tgCrmUtilities, long dialogId, String dialogName) {
        this.dialogId = dialogId;
        this.context = context;
        this.tgCrmUtilities = tgCrmUtilities;
        this.dialogName = dialogName;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetTextI18n")
    public void onCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        root.setPadding(padding, 8, padding, 8);

        // TextView of balance
        TextView chooseTextView = new TextView(context);
        chooseTextView.setTextSize(16f);
        chooseTextView.setText("Narxni tanlang");
        chooseTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        chooseTextView.setPadding(0, 12, 0, 0);

        root.addView(chooseTextView);

        // RadioGroup to choose balance
        RadioGroup balanceRadioGroup = new RadioGroup(context);
        balanceRadioGroup.setPadding(6, 12, 6, 6);
        balanceRadioGroup.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        radioButton40 = new RadioButton(context);
        radioButton40.setText("40 000 so'm");
        radioButton40.setButtonTintList(getColorRadioButton());
        radioButton40.setTextSize(16f);
        radioButton40.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        balanceRadioGroup.addView(radioButton40);

        radioButton50 = new RadioButton(context);
        radioButton50.setText("50 000 so'm");
        radioButton50.setTextSize(16f);
        radioButton50.setButtonTintList(getColorRadioButton());
        radioButton50.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        balanceRadioGroup.addView(radioButton50);

        radioButton60 = new RadioButton(context);
        radioButton60.setText("60 000 so'm");
        radioButton60.setTextSize(16f);
        radioButton60.setButtonTintList(getColorRadioButton());
        radioButton60.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        balanceRadioGroup.addView(radioButton60);

        root.addView(balanceRadioGroup);

        customPriceRadioButton = new RadioButton(context);
        customPriceRadioButton.setText("Narxni o'zim kiritish");
        customPriceRadioButton.setTextSize(16f);
        customPriceRadioButton.setButtonTintList(getColorRadioButton());
        customPriceRadioButton.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        balanceRadioGroup.addView(customPriceRadioButton);

        EditText balanceEditText = new EditText(context);
        balanceEditText.setHint("Narxni kiriting...");
        balanceEditText.setTextSize(16f);
        balanceEditText.setVisibility(GONE);
        balanceEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        balanceEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        balanceEditText.setHintTextColor(Theme.getColor(Theme.key_dialogSearchHint));
        balanceEditText.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        root.addView(balanceEditText);

        balanceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            balanceEditText.setText("");
            if (customPriceRadioButton.isChecked()) {
                balanceEditText.setVisibility(VISIBLE);
            } else {
                balanceEditText.setVisibility(GONE);
            }
        });

        // root layout for buttons
        LinearLayout frameOfButtonsLayout = new LinearLayout(context);
        frameOfButtonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        frameOfButtonsLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        frameOfButtonsLayout.setPadding(0, 16, 0, 0);

        // Cancel Button
        Button cancelBtn = new Button(context);
        cancelBtn.setText("Bekor qilish");
        cancelBtn.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        frameOfButtonsLayout.addView(cancelBtn);

        // Confirm Button
        Button confirmBtn = new Button(context);
        confirmBtn.setText("Yuborish");
        confirmBtn.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));
        confirmBtn.setOnClickListener(v -> {
            String balance = balanceEditText.getText().toString();
            validateBalance(balance);
        });
        frameOfButtonsLayout.addView(confirmBtn);

        root.addView(frameOfButtonsLayout);
        builder.setView(root);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        tgCrmUtilities.getBalanceData(dialogId, new TgCrmUtilities.FirestoreCallback<Integer>() {
            @Override
            public void onCallback(Integer balance) {
                if (balance == 40000) {
                    radioButton40.setChecked(true);
                } else if (balance == 50000) {
                    radioButton50.setChecked(true);
                } else if (balance == 60000) {
                    radioButton60.setChecked(true);
                } else {
                    if (balance != 0) {
                        customPriceRadioButton.setChecked(true);
                        balanceEditText.setVisibility(VISIBLE);
                        balanceEditText.setText(String.valueOf(balance));
                    }
                }
            }
        });
    }

    public void validateBalance(String balance) {
        if (balance.isEmpty() && customPriceRadioButton.isChecked()) {
            Toast.makeText(context, "Narx kiritilmadi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (balance.isEmpty() && !customPriceRadioButton.isChecked() && !radioButton40.isChecked() && !radioButton50.isChecked() && !radioButton60.isChecked()) {
            Toast.makeText(context, "Narx tanlanmadi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!balance.isEmpty() && customPriceRadioButton.isChecked()) {
            sendDataToServer(Integer.parseInt(balance));
            return;
        }
        sendDataToServer(getPrice());
    }

    private int getPrice() {
        if (radioButton40.isChecked()) {
            return 40000;
        }
        if (radioButton50.isChecked()) {
            return 50000;
        }
        if (radioButton60.isChecked()) {
            return 60000;
        } else {
            return 0;
        }
    }

    public void sendDataToServer(int balance) {
        tgCrmUtilities.saveBalanceData(
                new BalanceData(dialogId, balance, dialogName, TgCrmUtilities.getCachedUserData(UserCredentialsData.USERNAME))
        );
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tgCrmUtilities.saveBalanceByUserName();
        }, 1500);
        Toast.makeText(context, "Narx muvaffaqiyatli saqlandi", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    public ColorStateList getColorRadioButton() {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        Theme.getColor(Theme.key_chats_menuItemText),
                        Theme.getColor(Theme.key_chats_menuItemText)
                }
        );
        return colorStateList;
    }

}
