package com.kalavidarabalaga.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kalavidarabalaga.models.User
import com.kalavidarabalaga.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection(Constants.USERS_COLLECTION)

    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        val data = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "role" to user.role,
            "createdAt" to Timestamp.now()
        )
        collection.document(user.uid).set(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUserRole(uid: String, onResult: (String?) -> Unit) {
        collection.document(uid).get()
            .addOnSuccessListener { doc ->
                onResult(doc?.getString("role"))
            }
            .addOnFailureListener { onResult(null) }
    }

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val users = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(uid = doc.id)
            } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }
}
