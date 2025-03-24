package org.telegram.tgcrm.features.history;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import org.telegram.messenger.R;
import org.telegram.tgcrm.TgCrmUtilities;
import org.telegram.tgcrm.model.ActionEntity;
import org.telegram.tgcrm.model.TransferUser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class Actions extends BaseFragment {

    private AlertDialog progressDialog;
    private RecyclerView recyclerView;

    private Long dialogId;

    public Actions(Long dialogId) {
        this.dialogId = dialogId;
    }

    private ActionsAdapter actionsAdapter;

    ArrayList<Object> actionEntities = new ArrayList<>();

    TgCrmUtilities tgCrmUtilities;

    public static <T> void sortListByTime(List<T> list, Function<T, Timestamp> timeExtractor) {
        list.sort((o1, o2) -> {
            Timestamp t1 = timeExtractor.apply(o1);
            Timestamp t2 = timeExtractor.apply(o2);

            if (t1 == null || t2 == null) {
                return 0;
            }

            return t2.toDate().compareTo(t1.toDate());
        });
    }

    @Override
    public View createView(Context context) {
        tgCrmUtilities = new TgCrmUtilities();
        progressDialog = new AlertDialog(getContext(), AlertDialog.ALERT_TYPE_SPINNER);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                finishFragment();
            }
        });

        actionBar.setTitle("Actions");

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        actionsAdapter = new ActionsAdapter(actionEntities);
        recyclerView.setAdapter(actionsAdapter);

        getActions();

        linearLayout.addView(recyclerView);

        fragmentView = linearLayout;
        return fragmentView;
    }

    public void getActions() {
        progressDialog.show();
        actionEntities.clear();

        tgCrmUtilities.getActionsByDialogId(dialogId, actions -> {
            sortListByTime(actions,actionEntity -> actionEntity.actionTime);
            actionEntities.addAll(actions);

            tgCrmUtilities.getTransferredActions(dialogId, transfers -> {
                sortListByTime(transfers, transfer -> transfer.transferTime);
                actionEntities.addAll(transfers);
                actionsAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            });
        });
    }


    @Override
    public void onFragmentDestroy() {
        clearViews();
        recyclerView = null;
        parentDialog = null;
        progressDialog = null;
        fragmentView = null;
        super.onFragmentDestroy();
    }

    private static class ActionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Object> items;

        private static final int TYPE_ACTION = 0;
        private static final int TYPE_TRANSFER = 1;

        public ActionsAdapter(List<Object> items) {
            this.items = items;
        }

        static class ActionViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ActionViewHolder(TextView textView) {
                super(textView);
                this.textView = textView;
            }
        }

        static class TransferViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public TransferViewHolder(TextView textView) {
                super(textView);
                this.textView = textView;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (items.get(position) instanceof ActionEntity) {
                return TYPE_ACTION;
            } else {
                return TYPE_TRANSFER;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(16f);
            textView.setPadding(32, 24, 32, 24);
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            if (viewType == TYPE_ACTION) {
                textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
                return new ActionViewHolder(textView);
            } else {
                textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText));
                return new TransferViewHolder(textView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

            if (holder.getItemViewType() == TYPE_ACTION) {
                ActionEntity action = (ActionEntity) items.get(position);
                Date date = action.actionTime.toDate();
                String time = sdf.format(date);

                ((ActionViewHolder) holder).textView.setText("Ko'chirildi: " + action.fromFolder + " -> " + action.toFolder +
                        "\nIshchi: " + action.actionOwner +
                        "\nVaqti: " + time);
            } else {
                TransferUser transfer = (TransferUser) items.get(position);
                Date date = transfer.transferTime.toDate();
                String time = sdf.format(date);

                ((TransferViewHolder) holder).textView.setText("Kimga yo'naltirildi: " + transfer.otherUser +
                        "\nKim tomonidan: " + transfer.currentUser +
                        "\nVaqti: " + time);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

}
