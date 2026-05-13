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
import com.kalavidarabalaga.models.Troupe

class AdminTroupeAdapter(
    private val onApprove: (Troupe) -> Unit,
    private val onDeactivate: (Troupe) -> Unit,
    private val onClick: (Troupe) -> Unit
) : ListAdapter<Troupe, AdminTroupeAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Troupe>() {
        override fun areItemsTheSame(a: Troupe, b: Troupe) = a.id == b.id
        override fun areContentsTheSame(a: Troupe, b: Troupe) = a == b
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvTroupeName)
        val tvMeta: TextView = view.findViewById(R.id.tvArtFormDistrict)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnApprove: MaterialButton = view.findViewById(R.id.btnApprove)
        val btnDeactivate: MaterialButton = view.findViewById(R.id.btnDeactivate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_troupe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val troupe = getItem(position)
        holder.tvName.text = troupe.name
        holder.tvMeta.text = "${troupe.artForm} • ${troupe.district}"

        if (troupe.isActive) {
            holder.tvStatus.text = "Active"
            holder.tvStatus.setBackgroundResource(R.drawable.badge_active)
            holder.btnApprove.visibility = View.GONE
            holder.btnDeactivate.visibility = View.VISIBLE
        } else {
            holder.tvStatus.text = "Pending"
            holder.tvStatus.setBackgroundResource(R.drawable.badge_pending)
            holder.btnApprove.visibility = View.VISIBLE
            holder.btnDeactivate.visibility = View.GONE
        }

        holder.btnApprove.setOnClickListener { onApprove(troupe) }
        holder.btnDeactivate.setOnClickListener { onDeactivate(troupe) }
        holder.itemView.setOnClickListener { onClick(troupe) }
    }
}
