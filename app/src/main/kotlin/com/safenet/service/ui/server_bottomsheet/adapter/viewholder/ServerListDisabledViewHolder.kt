package com.safenet.service.ui.server_bottomsheet.adapter.viewholder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safenet.service.data.network.dto.Server
import com.safenet.service.databinding.ItemServerBinding
import com.safenet.service.databinding.ItemServerDisabledBinding
import com.safenet.service.ui.server_bottomsheet.ServerListListener

class ServerListDisabledViewHolder(
    val binding: ItemServerDisabledBinding,
    private val listener: ServerListListener,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            listener: ServerListListener,
        ): ServerListDisabledViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemServerDisabledBinding.inflate(inflater, parent, false)
            return ServerListDisabledViewHolder(binding, listener)
        }
    }

    @SuppressLint("SetTextI18n")
    fun bind(server: Server) {
        binding.tvServerName.text = "${server.name}- id: ${server.id}"
        binding.tvAvailability.text = "${server.capacity}% used"
        binding.apply {
            binding.root.setOnClickListener{
                listener.onItemClicked(server.id)
            }
        }
    }

}
