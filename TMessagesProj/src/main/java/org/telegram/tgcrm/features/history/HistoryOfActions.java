package org.telegram.tgcrm.features.history;

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
import org.telegram.tgcrm.model.ActionEntity;
import org.telegram.tgcrm.model.UserActions;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryOfActions extends BaseFragment {

    private AlertDialog progressDialog;

    private ActionsAdapter actionsAdapter;

    public void sortActionListByTime(List<UserActions> actionsWithOwner) {
        Collections.sort(actionsWithOwner, (u1, u2) -> {
            if (!u1.actions.isEmpty() && !u2.actions.isEmpty()) {
                return u2.actions.get(0).actionTime.compareTo(u1.actions.get(0).actionTime);
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

        actionBar.setTitle("History of Actions");

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        actionsAdapter = new ActionsAdapter();
        recyclerView.setAdapter(actionsAdapter);

        getActions();

        linearLayout.addView(recyclerView);

        fragmentView = linearLayout;
        return fragmentView;
    }

    public void getActions() {
        progressDialog.show();
        List<UserActions> actionsWithOwner = new TgCrmUtilities().getActionsWithOwner();
        sortActionListByTime(actionsWithOwner);
        actionsAdapter.updateData(actionsWithOwner);
        progressDialog.dismiss();
    }

    @Override
    public void onFragmentDestroy() {
        fragmentView = null;
        super.onFragmentDestroy();
    }

    private static class ActionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<UserActions> userActionsList = new ArrayList<>();

        private static final int VIEW_TYPE_USER = 0;
        private static final int VIEW_TYPE_ACTION = 1;

        public void updateData(List<UserActions> newData) {
            userActionsList.clear();
            userActionsList.addAll(newData);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            int index = 0;
            for (UserActions userActions : userActionsList) {
                if (position == index) {
                    return VIEW_TYPE_USER;
                }
                index++;
                for (ActionEntity ignored : userActions.actions) {
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
            for (UserActions userActions : userActionsList) {
                count += 1 + userActions.actions.size();
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

        static class ActionViewHolder extends RecyclerView.ViewHolder {
            TextView actionTextView;

            public ActionViewHolder(TextView textView) {
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
                usernameTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                usernameTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                usernameTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                usernameTextView.setPadding(0, 24, 0, 16);
                usernameTextView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
                usernameTextView.setTypeface(null, Typeface.BOLD);
                return new UserViewHolder(usernameTextView);
            } else {
                TextView actionTextView = new TextView(parent.getContext());
                actionTextView.setTextSize(16f);
                actionTextView.setPadding(48, 16, 48, 16);
                actionTextView.setTextColor(Theme.getColor(Theme.key_graySectionText));
                return new ActionViewHolder(actionTextView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int index = 0;
            for (UserActions userActions : userActionsList) {
                if (position == index) {
                    ((UserViewHolder) holder).usernameTextView.setText("👤 " + userActions.username);
                    return;
                }
                index++;

                for (ActionEntity action : userActions.actions) {
                    if (position == index) {
                        ((ActionViewHolder) holder).actionTextView.setText(
                                "💬 Mijoz: " + action.dialogName + "\n" +
                                        "\uD83D\uDCC1 Ko'chirildi: " + (action.fromFolder.isEmpty() ? "All" : action.fromFolder) + " → " + action.toFolder + "\n" +
                                        "🕒 Vaqti: " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(action.actionTime.toDate())
                        );
                        return;
                    }
                    index++;
                }
            }
        }
    }

}
