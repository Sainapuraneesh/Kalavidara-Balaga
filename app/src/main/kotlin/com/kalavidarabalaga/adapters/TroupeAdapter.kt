package com.kalavidarabalaga.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kalavidarabalaga.R
import com.kalavidarabalaga.databinding.ItemTroupeCardBinding
import com.kalavidarabalaga.models.Troupe

class TroupeAdapter(
    private val onTroupeClick: (Troupe) -> Unit
) : ListAdapter<Troupe, TroupeAdapter.TroupeViewHolder>(TroupeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TroupeViewHolder {
        val binding = ItemTroupeCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TroupeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TroupeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TroupeViewHolder(
        private val binding: ItemTroupeCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(troupe: Troupe) {
            binding.tvTroupeName.text = troupe.name
            binding.chipArtForm.text = troupe.artForm
            binding.tvDistrict.text = troupe.district
            binding.tvContactName.text = troupe.leadContactName

            Glide.with(binding.root.context)
                .load(troupe.groupPhotoUrl.ifBlank { null })
                .placeholder(R.drawable.placeholder_troupe)
                .error(R.drawable.placeholder_troupe)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.ivGroupPhoto)

            binding.root.setOnClickListener { onTroupeClick(troupe) }
        }
    }

    class TroupeDiffCallback : DiffUtil.ItemCallback<Troupe>() {
        override fun areItemsTheSame(oldItem: Troupe, newItem: Troupe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Troupe, newItem: Troupe) = oldItem == newItem
    }
}
