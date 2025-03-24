package org.telegram.tgcrm.features.warning

import com.google.firebase.firestore.FirebaseFirestore
import org.telegram.tgcrm.TgCrmUtilities
import org.telegram.tgcrm.features.check_unread_msgs.Warning
import org.telegram.tgcrm.model.UserCredentialsData

object WarningRemote {

    fun setWarning(firestore: FirebaseFirestore) {
        var batch = firestore.batch();
        var actionsRef = firestore.collection("worker_warning");

        var warning = Warning(TgCrmUtilities.getCachedUserData(UserCredentialsData.USERNAME))
        var docRef = actionsRef.document ()
        batch.set(docRef, warning)
        batch.commit()
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }

    fun getWarning(firestore: FirebaseFirestore, callback: (List<Warning>) -> Unit){
        firestore.collection("worker_warning")
            .whereEqualTo("worker", TgCrmUtilities.getCachedUserData(UserCredentialsData.USERNAME))
            .addSnapshotListener { queryDocumentSnapshots, e ->
                if (e != null || queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty) {
                    callback(emptyList())
                    return@addSnapshotListener
                }

                var warnings = ArrayList<Warning>()

                for (queryDocumentSnapshot in queryDocumentSnapshots) {
                    try {
                        val warning = queryDocumentSnapshot.toObject(Warning::class.java)
                        warnings.add(warning)
                    }catch (e: Exception){}
                }

                callback(warnings.sortedByDescending { it.warningTime })
            }
    }

}