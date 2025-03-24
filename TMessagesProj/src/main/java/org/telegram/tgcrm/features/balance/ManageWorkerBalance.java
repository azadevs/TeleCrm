package org.telegram.tgcrm.features.balance;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.tgcrm.TgCrmUtilities;
import org.telegram.tgcrm.model.BalanceOfWorker;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by : Azamat Kalmurzaev
 * 15/03/25
 */
public class ManageWorkerBalance extends BaseFragment {

    private TgCrmUtilities tgCrmUtilities;

    private AlertDialog progressDialog;

    private WorkerAdapter workerAdapter;

    private ArrayList<BalanceOfWorker> balanceOfWorkers = new ArrayList<>();

    @Override
    public View createView(Context context) {
        tgCrmUtilities = new TgCrmUtilities();
        progressDialog = new AlertDialog(getContext(), AlertDialog.ALERT_TYPE_SPINNER);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        actionBar.setTitle("Ishchi balansi");

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setBackgroundColor(Theme.getColor(Theme.key_iv_background));
        recyclerView.setPadding(16, 16, 16, 16);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        workerAdapter = new WorkerAdapter(context, balanceOfWorkers);
        recyclerView.setAdapter(workerAdapter);


        fragmentView = recyclerView;

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void getData() {
        progressDialog.show();
        tgCrmUtilities.fetchBalanceOfWorkers(e -> {
            if (e != null) {
                balanceOfWorkers.clear();
                balanceOfWorkers.addAll(e);
                workerAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }
        });

    }


    public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

        private List<BalanceOfWorker> workerList;
        private Context context;

        public WorkerAdapter(Context context, List<BalanceOfWorker> workerList) {
            this.context = context;
            this.workerList = workerList;
        }

        @NonNull
        @Override
        public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(context);
            layout.setGravity(Gravity.TOP | Gravity.START);
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 16, 16);

            TextView nameTextView = new TextView(context);
            nameTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            nameTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            nameTextView.setTextSize(17);
            layout.addView(nameTextView);

            TextView moneyStillInProgress = new TextView(context);
            moneyStillInProgress.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            moneyStillInProgress.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            moneyStillInProgress.setTextSize(17);
            layout.addView(moneyStillInProgress);

            TextView salaryTextView = new TextView(context);
            salaryTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            salaryTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            salaryTextView.setTextSize(17);
            salaryTextView.setPadding(0, 12, 0, 24);
            layout.addView(salaryTextView);

            Button payButton = new Button(context);
            payButton.setText("To‘lash");
            payButton.setAllCaps(false);
            payButton.setTextSize(16);
            payButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            payButton.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
            payButton.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
            payButton.setPadding(16, 8, 16, 24);
            layout.addView(payButton);

            return new WorkerViewHolder(layout, nameTextView, salaryTextView, payButton, moneyStillInProgress);
        }

        @Override
        public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
            BalanceOfWorker worker = workerList.get(position);
            holder.workerName.setText("Ishchi : " + worker.username);
            holder.moneyStillInProgress.setText("Ishlovda turgan mablag' : " + TgCrmUtilities.numberFormatter(worker.moneyStillInProcess) + " UZS");

            if (worker.username.equals("#1")) {
                tgCrmUtilities.fetchManagerBonusBalance(c -> {
                    holder.workerSalary.setText("Yechishga tayyor mablag' : " + TgCrmUtilities.numberFormatter(worker.readyToWithdrawn + c) + " UZS");
                });
            } else {
                holder.workerSalary.setText("Yechishga tayyor mablag' : " + TgCrmUtilities.numberFormatter(worker.readyToWithdrawn) + " UZS");
            }

            holder.payButton.setOnClickListener(v -> {
                tgCrmUtilities.getBalanceOfWorkerData(worker.username, c->{
                tgCrmUtilities.clickedSuccessfulPaidButton(worker.username);
                });
            });
        }


        @Override
        public int getItemCount() {
            return workerList.size();
        }

        public class WorkerViewHolder extends RecyclerView.ViewHolder {
            TextView workerName, workerSalary, moneyStillInProgress;
            Button payButton;

            public WorkerViewHolder(@NonNull View itemView, TextView workerName, TextView workerSalary, Button payButton, TextView moneyStillInProgress) {
                super(itemView);
                this.workerName = workerName;
                this.workerSalary = workerSalary;
                this.payButton = payButton;
                this.moneyStillInProgress = moneyStillInProgress;
            }
        }
    }
}
