package org.telegram.tgcrm.features.balance;

import static org.telegram.ui.Components.LayoutHelper.MATCH_PARENT;
import static org.telegram.ui.Components.LayoutHelper.WRAP_CONTENT;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.tgcrm.TgCrmUtilities;
import org.telegram.tgcrm.model.BalanceData;
import org.telegram.tgcrm.model.BalanceWithOwner;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by : Azamat Kalmurzaev
 * 12/03/25
 */
public class ListBalance extends BaseFragment {

    private AlertDialog progressDialog;

    private BalanceAdapter balanceAdapter;

    public void sortActionListByTime(List<BalanceWithOwner> balanceWithOwners) {
        Collections.sort(balanceWithOwners, (u1, u2) -> {
            if (!u1.balances.isEmpty() && !u2.balances.isEmpty()) {
                return u2.balances.get(0).timestamp.compareTo(u1.balances.get(0).timestamp);
            }
            return 0;
        });
    }

    @Override
    public View createView(Context context) {
        progressDialog = new AlertDialog(getContext(), AlertDialog.ALERT_TYPE_SPINNER);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                finishFragment();
            }
        });

        actionBar.setTitle("History of Balance");

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        balanceAdapter = new BalanceAdapter();
        recyclerView.setAdapter(balanceAdapter);

        getActions();

        linearLayout.addView(recyclerView);

        fragmentView = linearLayout;
        return fragmentView;
    }

    public void getActions() {
        progressDialog.show();
        new TgCrmUtilities().getBalancesWithOwner()
                .addOnSuccessListener(balanceWithOwners -> {
                    sortActionListByTime(balanceWithOwners);
                    balanceAdapter.updateData(balanceWithOwners);
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                });
    }

    @Override
    public void onFragmentDestroy() {
        fragmentView = null;
        super.onFragmentDestroy();
    }

    private static class BalanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<BalanceWithOwner> balanceWithOwners = new ArrayList<>();

        private static final int VIEW_TYPE_USER = 0;
        private static final int VIEW_TYPE_ACTION = 1;

        public void updateData(List<BalanceWithOwner> newData) {
            balanceWithOwners.clear();
            balanceWithOwners.addAll(newData);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            int index = 0;
            for (BalanceWithOwner balance : balanceWithOwners) {
                if (position == index) {
                    return VIEW_TYPE_USER;
                }
                index++;
                for (BalanceData ignored : balance.balances) {
                    if (position == index) {
                        return VIEW_TYPE_ACTION;
                    }
                    index++;
                }
            }
            return VIEW_TYPE_ACTION;
        }


        @Override
        public int getItemCount() {
            int count = 0;
            for (BalanceWithOwner balanceWithOwner : balanceWithOwners) {
                count += 1 + balanceWithOwner.balances.size();
            }
            return count;
        }

        static class UserViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTextView;

            public UserViewHolder(TextView textView) {
                super(textView);
                this.usernameTextView = textView;
            }
        }

        static class BalanceViewHolder extends RecyclerView.ViewHolder {
            TextView actionTextView;

            public BalanceViewHolder(TextView textView) {
                super(textView);
                this.actionTextView = textView;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_USER) {
                TextView usernameTextView = new TextView(parent.getContext());
                usernameTextView.setTextSize(18f);
                usernameTextView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                usernameTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                usernameTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                usernameTextView.setPadding(0, 24, 0, 16);
                usernameTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
                usernameTextView.setTypeface(null, Typeface.BOLD);
                return new UserViewHolder(usernameTextView);
            } else {
                TextView balanceTextView = new TextView(parent.getContext());
                balanceTextView.setTextSize(16f);
                balanceTextView.setPadding(48, 16, 48, 16);
                balanceTextView.setTextColor(Theme.getColor(Theme.key_graySectionText));
                return new BalanceViewHolder(balanceTextView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int index = 0;
            for (BalanceWithOwner balanceWithOwner : balanceWithOwners) {
                if (position == index) {
                    ((UserViewHolder) holder).usernameTextView.setText("👤 " + balanceWithOwner.username);
                    return;
                }
                index++;

                for (BalanceData balanceData : balanceWithOwner.balances) {
                    if (position == index) {
                        ((BalanceViewHolder) holder).actionTextView.setText(
                                "💬 Mijoz: " + balanceData.name + "\n" +
                                        "\uD83D\uDCB0 Narxi " + balanceData.price + "\n" +
                                        "🕒 Vaqti: " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(balanceData.timestamp.toDate())
                        );
                        return;
                    }
                    index++;
                }
            }
        }
    }
}
