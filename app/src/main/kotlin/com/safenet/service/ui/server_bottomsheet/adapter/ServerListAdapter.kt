package com.safenet.service.ui.server_bottomsheet.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.safenet.service.data.network.dto.Server
import com.safenet.service.ui.server_bottomsheet.ServerListListener
import com.safenet.service.ui.server_bottomsheet.adapter.viewholder.ServerListViewHolder


/**
 * Adapter for the [RecyclerView] in [BasketFragment].
 */

class ServerListAdapter(
    private val listener: ServerListListener
) : ListAdapter<Server, ServerListViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ServerListViewHolder.create(parent, listener)

    override fun onBindViewHolder(holder: ServerListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallBack : DiffUtil.ItemCallback<Server>() {
        override fun areItemsTheSame(
            oldItem: Server,
            newItem: Server
        ) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Server, newItem: Server
        ) =
            oldItem == newItem

    }
}