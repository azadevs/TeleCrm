package org.telegram.tgcrm.features.transfer;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.telegram.tgcrm.model.UserCredentials;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;

public class TransferWorkerBottomSheetDialog {
    private Context context;
    private List<UserCredentials> items;
    private OnItemSelectedListener onItemSelectedListener;
    private BottomSheetDialog dialog;

    public interface OnItemSelectedListener {
        void onItemSelected(String item);
    }

    public TransferWorkerBottomSheetDialog(Context context, List<UserCredentials> items, OnItemSelectedListener listener) {
        this.context = context;
        this.items = items;
        this.onItemSelectedListener = listener;

    }

    public Dialog onCreateDialog() {
        dialog = new BottomSheetDialog(context);
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setPadding(16,16,16,16);
        recyclerView.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new BottomSheetAdapter(items, item -> {
            onItemSelectedListener.onItemSelected(item);
            dialog.dismiss();
        }));
        dialog.setContentView(recyclerView);
        return dialog;
    }

    private class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.ViewHolder> {
        private List<UserCredentials> items;
        private OnItemSelectedListener onClick;

        public BottomSheetAdapter(List<UserCredentials> items, OnItemSelectedListener onClick) {
            this.items = items;
            this.onClick = onClick;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View view, TextView textView) {
                super(view);
                this.textView = textView;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setPadding(16, 16, 16, 16);
            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(12,12,12,12);
            textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            textView.setTextSize(18f);

            layout.addView(textView);

            return new ViewHolder(layout,textView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserCredentials item = items.get(position);
            holder.textView.setText(item.username + "-Ishchi");

            holder.textView.setOnClickListener(v -> {
                notifyDataSetChanged();
                onClick.onItemSelected(item.username);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
