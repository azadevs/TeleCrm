package org.telegram.tgcrm.features.balance;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.tgcrm.TgCrmUtilities;
import org.telegram.tgcrm.model.BalanceOfWorker;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

/**
 * Created by : Azamat Kalmurzaev
 * 15/03/25
 */
public class UserBalanceDialog {

    private TextView currentEarningsTextView, totalEarningsTextView,moneyStillInProgressTextView;

    private TgCrmUtilities tgCrmUtilities;

    private Context context;

    private AlertDialog dialog;

    public UserBalanceDialog(TgCrmUtilities tgCrmUtilities, Context context) {
        this.tgCrmUtilities = tgCrmUtilities;
        this.context=context;
    }

    @SuppressLint("SetTextI18n")
    public void onCreateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        int padding = (int) (24 * context.getResources().getDisplayMetrics().density);
        root.setPadding(padding, 16, padding, 16);

        currentEarningsTextView = new TextView(context);
        currentEarningsTextView.setText((tgCrmUtilities.balanceOfWorker!=null) ? "Yechishga tayyor mablag' : "+TgCrmUtilities.numberFormatter(tgCrmUtilities.balanceOfWorker.readyToWithdrawn) + " UZS" : "Yechishga tayyor mablag: 0");
        currentEarningsTextView.setTextSize(16);
        currentEarningsTextView.setPadding(0,0,0,24);
        currentEarningsTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        root.addView(currentEarningsTextView);

        moneyStillInProgressTextView = new TextView(context);
        moneyStillInProgressTextView.setText((tgCrmUtilities.balanceOfWorker!=null) ? "Ishlovda turgan mablag' : "+TgCrmUtilities.numberFormatter(tgCrmUtilities.balanceOfWorker.moneyStillInProcess) + " UZS":"Ishlovda turgan mablag' : 0");
        moneyStillInProgressTextView.setTextSize(16);
        moneyStillInProgressTextView.setPadding(0,0,0,24);
        moneyStillInProgressTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        root.addView(moneyStillInProgressTextView);

        totalEarningsTextView = new TextView(context);
        totalEarningsTextView.setText((tgCrmUtilities.balanceOfWorker!=null) ? "Umumiy ishlagan mablag' : "+TgCrmUtilities.numberFormatter(tgCrmUtilities.balanceOfWorker.fullPaid) + " UZS":"Umumiy ishlagan mablag' : 0");
        totalEarningsTextView.setTextSize(16);
        totalEarningsTextView.setPadding(0,0,0,24);
        totalEarningsTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        root.addView(totalEarningsTextView);

        Button closeButton = new Button(context);
        closeButton.setText("Yopish");
        closeButton.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        closeButton.setOnClickListener(v -> dialog.dismiss());
        closeButton.setTextSize(16);
        closeButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        closeButton.setAllCaps(false);
        closeButton.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(closeButton);
        builder.setView(root);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }
}
