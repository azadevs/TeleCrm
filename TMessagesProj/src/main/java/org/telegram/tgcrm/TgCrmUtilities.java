package org.telegram.tgcrm;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgcrm.features.deleted_messages.DeletedMessageByWorker;
import org.telegram.tgcrm.model.ActionEntity;
import org.telegram.tgcrm.model.BalanceData;
import org.telegram.tgcrm.model.BalanceOfWorker;
import org.telegram.tgcrm.model.BalanceWithOwner;
import org.telegram.tgcrm.model.TransferUser;
import org.telegram.tgcrm.model.UserActions;
import org.telegram.tgcrm.model.UserCredentials;
import org.telegram.tgcrm.model.UserCredentialsData;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.LayoutHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by : Azamat Kalmurzaev
 * 21/02/25
 */
public class TgCrmUtilities {
    public FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public static List<UserCredentials> credentials = new ArrayList<>();

    public static List<ActionEntity> actions = new ArrayList<>();

    public static List<TransferUser> transfers = new ArrayList<>();

    public static List<MessagesController.DialogFilter> dialogFilters = new ArrayList<>();

    private List<BalanceData> balanceData = new ArrayList<>();

    public ArrayList<BalanceOfWorker> balanceOfWorkers = new ArrayList<>();

    private String storedPhone = "";

    public BalanceOfWorker balanceOfWorker;

    private Long percentOfBalance = 0L;

    private Long managerBonusBalance;

    public TgCrmUtilities() {
        fetchPercentOfBalance();
    }

    // Check if phone number exists in database
    public Task<Boolean> checkPhoneNumber(String phone) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean exists = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            storedPhone = document.getString("phone");
                            String workerId = document.getString("workerId");
                            if (storedPhone != null && workerId != null && storedPhone.equals(phone) && workerId.equals(getCachedUserData(UserCredentialsData.WORKER_ID))) {
                                exists = true;
                                break;
                            }
                        }
                        taskCompletionSource.setResult(exists);
                    } else {
                        taskCompletionSource.setResult(false);
                    }
                });

        return taskCompletionSource.getTask();
    }

    // Save deleted messages
    public void saveDeletedMessage(Long dialogId, TLRPC.User user, TLRPC.Chat chat, boolean forAll, ArrayList<MessageObject> selectedMessages) {
        WriteBatch batch = firestore.batch();
        CollectionReference deletedMessagesByWorkersRef = firestore.collection("deleted_messages_by_workers");

        String name = "";

        if (user != null) {
            name = user.first_name + user.last_name;
        } else if (chat != null) {
            name = chat.title;
        }

        ArrayList<DeletedMessageByWorker> deletedMessages = new ArrayList<>();

        for (MessageObject messageObject : selectedMessages) {
            deletedMessages.add(new DeletedMessageByWorker(name, getCachedUserData(UserCredentialsData.USERNAME), dialogId, messageObject.getId(), forAll ? 2 : 1, messageObject.messageText.toString(), Timestamp.now()));
        }

        for (DeletedMessageByWorker deletedMessage : deletedMessages) {
            DocumentReference docRef = deletedMessagesByWorkersRef.document();
            batch.set(docRef, deletedMessage);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(e -> {
                });
    }

    // Get deleted messages
    public void getDeletedMessages(boolean ascending, FirestoreCallback<ArrayList<DeletedMessageByWorker>> callback) {
        CollectionReference deletedMessagesByWorkersRef = firestore.collection("deleted_messages_by_workers");

        deletedMessagesByWorkersRef
                .get()
                .addOnCompleteListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isSuccessful()) {
                        ArrayList<DeletedMessageByWorker> deletedMessages = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getResult()) {
                            deletedMessages.add(doc.toObject(DeletedMessageByWorker.class));
                        }
                        callback.onCallback(deletedMessages);
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    public void showNameInputDialog(Context context, View.OnClickListener onClick) {
        if (context == null || !getCachedUserData(UserCredentialsData.USERNAME).isEmpty()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText username = new EditText(context);
        username.setInputType(InputType.TYPE_CLASS_TEXT);
        username.setHint("Enter your username");
        layout.addView(username, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 30, 16, 30, 0));

        final EditText password = new EditText(context);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint("Enter your password");
        layout.addView(password, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 30, 16, 30, 0));

        builder.setView(layout);
        builder.setCancelable(false);

        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                    boolean exists = checkUserCredentials(username.getText().toString(), password.getText().toString(), context);
                    if (exists) {
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();
                        if (onClick != null) {
                            onClick.onClick(dialog.getListView());
                        }
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Invalid credentials!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Credentials cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    // Get all users
    public void getAllUserCredentials() {
        firestore.collection("workers")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    credentials.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        UserCredentials action = queryDocumentSnapshot.toObject(UserCredentials.class);
                        credentials.add(action);
                    }
                });

    }

    // Check if user credentials are correct
    private boolean checkUserCredentials(String username, String password, Context context) {
        for (UserCredentials credential : credentials) {
            if (credential.username.equals(username) && credential.password.equals(password)) {
                cacheUserCredentials(username, password, context, credential.id);
                return true;
            }
        }
        return false;
    }

    private void cacheUserCredentials(String username, String password, Context context, String userId) {
        context.getSharedPreferences("credentials", Context.MODE_PRIVATE)
                .edit()
                .putString("phone", storedPhone)
                .putString("username", username)
                .putString("password", password)
                .putString("userId", userId)
                .apply();
    }

    public void saveAction(String fromFolder, String toFolder, ArrayList<Long> dialogIds, int folderPosition, MessagesController messagesController, SaveActionCallback callback) {
        WriteBatch batch = firestore.batch();
        CollectionReference actionsRef = firestore.collection("actions");

        for (Long dialogId : dialogIds) {
            ActionEntity actionEntity = new ActionEntity(fromFolder, toFolder, getCachedUserData(UserCredentialsData.USERNAME), dialogId, messagesController.getFullName(dialogId), folderPosition);
            DocumentReference docRef = actionsRef.document();
            batch.set(docRef, actionEntity);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    callback.onCallback();
                })
                .addOnFailureListener(e -> {
                });
    }

    public void saveTransferredActions(
            String currentUser,
            String otherUser,
            long dialogId
    ) {
        WriteBatch batch = firestore.batch();
        CollectionReference actionsRef = firestore.collection("transfer");

        TransferUser transferUser = new TransferUser(otherUser, currentUser, dialogId);
        DocumentReference docRef = actionsRef.document();
        batch.set(docRef, transferUser);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    public void getTransferredActions(long dialogId, FirestoreCallback<ArrayList<TransferUser>> callback) {
        ArrayList<TransferUser> transfers = new ArrayList<>();
        firestore.collection("transfer")
                .whereEqualTo("dialog_id", dialogId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            TransferUser action = queryDocumentSnapshot.toObject(TransferUser.class);
                            transfers.add(action);
                        }
                        callback.onCallback(transfers);
                    } else {
                        callback.onCallback(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(new ArrayList<>());
                });

    }

    public void getActionsByDialogId(long dialogId, FirestoreCallback<ArrayList<ActionEntity>> callback) {
        ArrayList<ActionEntity> actions = new ArrayList<>();

        firestore.collection("actions")
                .whereEqualTo("dialog_id", dialogId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            ActionEntity action = queryDocumentSnapshot.toObject(ActionEntity.class);
                            actions.add(action);
                        }
                        callback.onCallback(actions);
                    } else {
                        callback.onCallback(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }

    public interface FirestoreCallback<T> {
        void onCallback(T actions);
    }

    public static String getCachedUserData(UserCredentialsData userCredentialsData) {
        String result;
        switch (userCredentialsData) {
            case USERNAME:
                result = ApplicationLoader.applicationContext.getSharedPreferences("credentials", Context.MODE_PRIVATE).getString("username", "");
                break;
            case PASSWORD:
                result = ApplicationLoader.applicationContext.getSharedPreferences("credentials", Context.MODE_PRIVATE).getString("password", "");
                break;
            case WORKER_ID:
                result = ApplicationLoader.applicationContext.getSharedPreferences("credentials", Context.MODE_PRIVATE).getString("userId", "");
                break;
            case PHONE:
                result = ApplicationLoader.applicationContext.getSharedPreferences("credentials", Context.MODE_PRIVATE).getString("phone", "");
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

    public void getAllActions() {
        firestore.collection("actions")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    actions.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        ActionEntity action = queryDocumentSnapshot.toObject(ActionEntity.class);
                        actions.add(action);
                    }

                    Collections.sort(actions, (a1, a2) -> a2.actionTime.compareTo(a1.actionTime));
                });
    }

    public void getAllTransfers() {
        if (firestore == null) {
            return;
        }

        firestore.collection("transfer")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    transfers.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        TransferUser action = queryDocumentSnapshot.toObject(TransferUser.class);
                        transfers.add(action);
                    }

                    Collections.sort(transfers, (a1, a2) -> a2.transferTime.compareTo(a1.transferTime));
                });
    }

    public interface SaveActionCallback {
        void onCallback();
    }

    public List<UserActions> getActionsWithOwner() {
        LinkedHashMap<String, List<ActionEntity>> groupedActions = new LinkedHashMap<>();
        for (ActionEntity action : actions) {
            if (!dialogFilters.isEmpty() && action.folderPosition >= dialogFilters.size() - 1) {
                String username = action.actionOwner;
                if (!groupedActions.containsKey(username)) {
                    groupedActions.put(username, new ArrayList<>());
                }
                groupedActions.get(username).add(action);
            }
        }
        List<UserActions> userActionsList = new ArrayList<>();
        for (Map.Entry<String, List<ActionEntity>> entry : groupedActions.entrySet()) {
            userActionsList.add(new UserActions(entry.getKey(), entry.getValue()));
        }
        return userActionsList;
    }

    public static void setDialogFilter(List<MessagesController.DialogFilter> newDialogFilters) {
        dialogFilters.clear();
        dialogFilters.addAll(newDialogFilters);
    }

    public static String getOwnerName(Long id) {
        for (ActionEntity action : actions) {
            if (action.dialogId == id) {
                return action.actionOwner + "->" + " ";
            }
        }
        return "";
    }

    public static Boolean compareTime(Long id) {
        Long transferTime = null;
        Long actionTime = null;
        for (TransferUser transfer : transfers) {
            if (transfer.dialogId == id) {
                transferTime = transfer.transferTime.toDate().getTime();
                break;
            }
        }
        for (ActionEntity action : actions) {
            if (action.dialogId == id) {
                actionTime = action.actionTime.toDate().getTime();
                break;
            }
        }
        return actionTime != null && transferTime != null && actionTime > transferTime;
    }

    public static String getIfTransfer(Long id) {
        for (TransferUser transfer : transfers) {
            if (transfer.dialogId == id) {
                return transfer.otherUser + "->" + " ";
            }
        }
        return "";
    }

    public static void clearCache(Context context) {
        context.getSharedPreferences("credentials", Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static List<UserCredentials> getRemovedOwnCredentials() {
        List<UserCredentials> credentials = new ArrayList<>(TgCrmUtilities.credentials);
        credentials.removeIf(userCredentials ->
                userCredentials.username.equals(getCachedUserData(UserCredentialsData.USERNAME)) || userCredentials.username.equals("Boshliq")
        );
        return credentials;
    }

    public void saveBalanceData(BalanceData balanceData) {
        WriteBatch batch = firestore.batch();
        DocumentReference docRef = firestore.collection("balance").document();
        batch.set(docRef, balanceData);
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Price data saved");
                    fetchSavedBalancesByUsername();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to save price data", e));
    }

    public void getBalanceData(long dialogId, FirestoreCallback<Integer> callback) {
        firestore.collection("balance")
                .whereEqualTo("dialogId", dialogId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<BalanceData> balanceList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            BalanceData balanceData = document.toObject(BalanceData.class);
                            balanceList.add(balanceData);
                        }

                        // Timestamp bo‘yicha tartiblash (oxirgisini olish uchun)
                        Collections.sort(balanceList, (o1, o2) -> o2.timestamp.compareTo(o1.timestamp));

                        // Oxirgi saqlangan qiymatni olish
                        BalanceData latestBalance = balanceList.get(0);

                        callback.onCallback(latestBalance.price);
                    } else {
                        callback.onCallback(0);
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(0));
    }

    public void fetchSavedBalancesByUsername() {
        balanceData.clear();
        firestore.collection("balance")
                .whereEqualTo("owner", getCachedUserData(UserCredentialsData.USERNAME))
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Failed to fetch balances", e);
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        balanceData.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            balanceData.add(doc.toObject(BalanceData.class));
                        }
                        Log.d("Firestore", "Fetched " + balanceData.size() + " balances.");
                    }
                });
    }

    public Task<List<BalanceWithOwner>> getBalancesWithOwner() {
        TaskCompletionSource<List<BalanceWithOwner>> task = new TaskCompletionSource<>();

        firestore.collection("balance")
                .get()
                .addOnCompleteListener(e -> {
                    if (e.isSuccessful()) {
                        Map<String, List<BalanceData>> groupedBalances = new HashMap<>();
                        List<BalanceWithOwner> balanceList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : e.getResult()) {
                            BalanceData balance = document.toObject(BalanceData.class);
                            groupedBalances.computeIfAbsent(balance.owner, k -> new ArrayList<>()).add(balance);
                        }

                        for (Map.Entry<String, List<BalanceData>> entry : groupedBalances.entrySet()) {
                            balanceList.add(new BalanceWithOwner(entry.getKey(), entry.getValue()));
                        }

                        task.setResult(balanceList);
                    } else {
                        task.setException(e.getException());
                    }
                })
                .addOnFailureListener(task::setException);

        return task.getTask();
    }


    public void getBalanceOfWorkerData(String username, FirestoreCallback<BalanceOfWorker> callback) {
        firestore.collection("balance_of_worker")
                .document(username)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Failed to fetch balance of worker", e);
                        return;
                    }
                    if (documentSnapshot != null) {
                        balanceOfWorker = documentSnapshot.toObject(BalanceOfWorker.class);
                        callback.onCallback(documentSnapshot.toObject(BalanceOfWorker.class));
                    }
                });
    }

    public void saveBalanceByUserName() {
        long totalPrice = 0;
        for (BalanceData balance : balanceData) {
            if (balance.owner.equals(getCachedUserData(UserCredentialsData.USERNAME))) {
                totalPrice += balance.price;
            }
        }
        long fullPaid = (balanceOfWorker != null) ? balanceOfWorker.fullPaid : 0;
        long withDrawMoney = (balanceOfWorker != null) ? balanceOfWorker.readyToWithdrawn : 0;

        BalanceOfWorker balanceOfWorker = new BalanceOfWorker(fullPaid, totalPrice, withDrawMoney, getCachedUserData(UserCredentialsData.USERNAME));

        WriteBatch batch = firestore.batch();
        DocumentReference docRef = firestore.collection("balance_of_worker").document(getCachedUserData(UserCredentialsData.USERNAME));
        batch.set(docRef, balanceOfWorker);
        batch.commit().addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Balance data updated");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to save price data");
                });
    }

    public void saveBalanceOfWorkerSuccessful(long dialogId) {

        long readyToWithdraw = 0;

        if (this.balanceOfWorker == null) {
            Log.e("Firestore", "balanceOfWorker is NULL!");
            return;
        }

        for (BalanceData balance : balanceData) {
            if (balance.dialogId == dialogId) {
                readyToWithdraw += balance.price;
                saveManagerBonusBalance(balance.price);
            }
        }

        BalanceOfWorker balanceOfWorker = new BalanceOfWorker(
                this.balanceOfWorker.fullPaid,
                Math.abs(this.balanceOfWorker.moneyStillInProcess - readyToWithdraw),
                this.balanceOfWorker.readyToWithdrawn + ((readyToWithdraw * percentOfBalance / 100)),
                getCachedUserData(UserCredentialsData.USERNAME)
        );

        DocumentReference successfulBalance = firestore.collection("balance_of_worker")
                .document(getCachedUserData(UserCredentialsData.USERNAME));

        firestore.runTransaction(transaction -> {
            transaction.set(successfulBalance, balanceOfWorker);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Balance successfully updated");
            removeBalanceDataByDialogId(dialogId);
            fetchBalanceOfWorkers(v -> {
            });
        }).addOnFailureListener(e -> Log.e("Firestore", "Failed to update balance", e));
    }

    private void saveManagerBonusBalance(long balance) {
        WriteBatch batch = firestore.batch();
        DocumentReference docRef = firestore.collection("manager_bonus_balance").document("#1");
        Map<String, Object> data = new HashMap<>();
        data.put("balance", (managerBonusBalance != null) ? managerBonusBalance + balance * 3 / 100 : balance * 3 / 100);
        batch.set(docRef, data);
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Balance data updated");
                    fetchSavedBalancesByUsername();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to save price data", e));

    }

    public void fetchManagerBonusBalance(FirestoreCallback<Long> firestoreCallback) {
        firestore.collection("manager_bonus_balance")
                .document("#1")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        managerBonusBalance = documentSnapshot.getLong("balance");
                        firestoreCallback.onCallback(documentSnapshot.getLong("balance"));
                    }
                });
    }

    private void removeBalanceDataByDialogId(
            long dialogId
    ) {
        firestore.collection("balance")
                .whereEqualTo("dialogId", dialogId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        firestore.collection("balance").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        System.out.println("Removed document: " + document.getId()))
                                .addOnFailureListener(e ->
                                        System.err.println("Failed to remove document: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e ->
                        System.err.println("Failed to fetch documents: " + e.getMessage()));
    }

    private void fetchPercentOfBalance() {
        firestore.collection("percent")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        percentOfBalance = querySnapshot.getDocuments().get(0).get("percent", Long.class);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch percent", e));
    }

    public void fetchBalanceOfWorkers(FirestoreCallback<ArrayList<BalanceOfWorker>> callback) {
        firestore.collection("balance_of_worker")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null || queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        callback.onCallback(new ArrayList<>());
                        return;
                    }
                    balanceOfWorkers.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        balanceOfWorkers.add(doc.toObject(BalanceOfWorker.class));
                    }
                    callback.onCallback(balanceOfWorkers);
                });
    }

    public void clickedSuccessfulPaidButton(String username) {
        if (this.balanceOfWorker == null) {
            Log.e("Firestore", "balanceOfWorker is NULL!");
            return;
        }

        BalanceOfWorker balanceOfWorker = new BalanceOfWorker(
                this.balanceOfWorker.fullPaid + this.balanceOfWorker.readyToWithdrawn,
                Math.abs(this.balanceOfWorker.moneyStillInProcess),
                0,
                username
        );

        DocumentReference successfulBalance = firestore.collection("balance_of_worker")
                .document(username);

        firestore.runTransaction(transaction -> {
            transaction.set(successfulBalance, balanceOfWorker);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Balance successfully updated");
            getBalanceOfWorkerData(username, v -> {
                fetchBalanceOfWorkers(c -> {
                });
            });
        }).addOnFailureListener(e -> Log.e("Firestore", "Failed to update balance", e));
    }

    public static String numberFormatter(long number) {
        NumberFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }

    public void removeActionsByDialogId(long dialogId) {
        firestore.collection("actions")
                .whereEqualTo("dialog_id", dialogId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = firestore.batch();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> System.out.println("All documents removed successfully"))
                            .addOnFailureListener(e -> System.err.println("Failed to remove documents: " + e.getMessage()));
                })
                .addOnFailureListener(e -> System.err.println("Failed to fetch documents: " + e.getMessage()));
    }

}
