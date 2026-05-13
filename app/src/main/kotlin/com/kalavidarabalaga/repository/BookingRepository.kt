package com.kalavidarabalaga.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kalavidarabalaga.models.Booking
import com.kalavidarabalaga.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BookingRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection(Constants.BOOKINGS_COLLECTION)

    fun createBooking(booking: Booking, onResult: (Boolean) -> Unit) {
        val data = mapOf(
            "userId" to booking.userId,
            "userName" to booking.userName,
            "userEmail" to booking.userEmail,
            "troupeId" to booking.troupeId,
            "troupeName" to booking.troupeName,
            "troupeLeaderId" to booking.troupeLeaderId,
            "eventDate" to booking.eventDate,
            "message" to booking.message,
            "status" to "pending",
            "createdAt" to Timestamp.now()
        )
        collection.add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getBookingsForLeader(leaderId: String): Flow<List<Booking>> = callbackFlow {
        val listener = collection
            .whereEqualTo("troupeLeaderId", leaderId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    fun getBookingsForUser(userId: String): Flow<List<Booking>> = callbackFlow {
        val listener = collection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    fun updateBookingStatus(bookingId: String, status: String, onResult: (Boolean) -> Unit) {
        collection.document(bookingId).update("status", status)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
