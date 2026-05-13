package com.kalavidarabalaga.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kalavidarabalaga.models.Troupe
import com.kalavidarabalaga.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TroupeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection(Constants.TROUPES_COLLECTION)

    fun getAllActiveTroupes(): Flow<List<Troupe>> = callbackFlow {
        val query = collection
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val troupes = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Troupe::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(troupes)
        }

        awaitClose { listener.remove() }
    }

    fun searchTroupes(district: String?, artForm: String?): Flow<List<Troupe>> = callbackFlow {
        var query: Query = collection.whereEqualTo("isActive", true)

        if (!district.isNullOrBlank() && district != "All Districts") {
            query = query.whereEqualTo("district", district)
        }
        if (!artForm.isNullOrBlank() && artForm != "All Art Forms") {
            query = query.whereEqualTo("artForm", artForm)
        }

        query = query.orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val troupes = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Troupe::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(troupes)
        }

        awaitClose { listener.remove() }
    }

    fun getTroupeById(id: String): Flow<Troupe?> = callbackFlow {
        val listener = collection.document(id).addSnapshotListener { doc, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val troupe = doc?.toObject(Troupe::class.java)?.copy(id = doc.id)
            trySend(troupe)
        }
        awaitClose { listener.remove() }
    }

    fun getAllTroupesForAdmin(): Flow<List<Troupe>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val troupes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Troupe::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(troupes)
            }
        awaitClose { listener.remove() }
    }

    fun getTroupeByOwner(ownerId: String): Flow<Troupe?> = callbackFlow {
        val listener = collection
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val troupe = snapshot?.documents?.firstOrNull()?.let { doc ->
                    doc.toObject(Troupe::class.java)?.copy(id = doc.id)
                }
                trySend(troupe)
            }
        awaitClose { listener.remove() }
    }

    fun addTroupe(troupe: Troupe, onResult: (Boolean, String?) -> Unit) {
        val data = mapOf(
            "name" to troupe.name,
            "artForm" to troupe.artForm,
            "district" to troupe.district,
            "leadContactName" to troupe.leadContactName,
            "leadContactPhone" to troupe.leadContactPhone,
            "groupPhotoUrl" to troupe.groupPhotoUrl,
            "portfolioImages" to troupe.portfolioImages,
            "portfolioVideoLinks" to troupe.portfolioVideoLinks,
            "equipmentList" to troupe.equipmentList,
            "serviceArea" to troupe.serviceArea,
            "description" to troupe.description,
            "isActive" to false,
            "ownerId" to troupe.ownerId,
            "createdAt" to Timestamp.now()
        )
        collection.add(data)
            .addOnSuccessListener { ref -> onResult(true, ref.id) }
            .addOnFailureListener { onResult(false, null) }
    }

    fun updateTroupe(id: String, data: Map<String, Any>, onResult: (Boolean) -> Unit) {
        val update = data.toMutableMap()
        update["updatedAt"] = Timestamp.now()
        collection.document(id).update(update)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun approveTroupe(id: String, onResult: (Boolean) -> Unit) {
        collection.document(id).update("isActive", true, "updatedAt", Timestamp.now())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun deactivateTroupe(id: String, onResult: (Boolean) -> Unit) {
        collection.document(id).update("isActive", false, "updatedAt", Timestamp.now())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
