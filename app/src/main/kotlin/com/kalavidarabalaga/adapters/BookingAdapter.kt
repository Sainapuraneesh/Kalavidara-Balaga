package com.kalavidarabalaga.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kalavidarabalaga.R
import com.kalavidarabalaga.models.Booking

class BookingAdapter(
    private val onAccept: (Booking) -> Unit,
    private val onReject: (Booking) -> Unit
) : ListAdapter<Booking, BookingAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(a: Booking, b: Booking) = a.id == b.id
        override fun areContentsTheSame(a: Booking, b: Booking) = a == b
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvEventDate: TextView = view.findViewById(R.id.tvEventDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val layoutActions: View = view.findViewById(R.id.layoutActions)
        val btnAccept: MaterialButton = view.findViewById(R.id.btnAccept)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = getItem(position)
        holder.tvUserName.text = booking.userName.ifBlank { booking.userEmail }
        holder.tvEventDate.text = "Event Date: ${booking.eventDate.ifBlank { "Not specified" }}"
        holder.tvMessage.text = booking.message

        when (booking.status) {
            "accepted" -> {
                holder.tvStatus.text = "Accepted"
                holder.tvStatus.setBackgroundResource(R.drawable.badge_active)
                holder.layoutActions.visibility = View.GONE
            }
            "rejected" -> {
                holder.tvStatus.text = "Rejected"
                holder.tvStatus.setBackgroundResource(R.drawable.badge_pending)
                holder.layoutActions.visibility = View.GONE
            }
            else -> {
                holder.tvStatus.text = "Pending"
                holder.tvStatus.setBackgroundResource(R.drawable.badge_pending)
                holder.layoutActions.visibility = View.VISIBLE
            }
        }

        holder.btnAccept.setOnClickListener { onAccept(booking) }
        holder.btnReject.setOnClickListener { onReject(booking) }
    }
}
