package com.safenet.service.ui.server_bottomsheet.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.safenet.service.data.network.dto.Server
import com.safenet.service.ui.server_bottomsheet.ServerListListener
import com.safenet.service.ui.server_bottomsheet.adapter.viewholder.ServerListDisabledViewHolder
import com.safenet.service.ui.server_bottomsheet.adapter.viewholder.ServerListViewHolder


/**
 * Adapter for the [RecyclerView] in [BasketFragment].
 */

class ServerListAdapter(
    private val listener: ServerListListener
) : ListAdapter<Server, RecyclerView.ViewHolder>(DiffCallBack()) {

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position).enable){
            true -> 0
            false -> 1
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when(viewType){
            0 -> ServerListViewHolder.create(parent = parent,listener)
            else -> ServerListDisabledViewHolder.create(parent, listener)
        }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        when(getItem(position).enable){
            true -> (holder as ServerListViewHolder).bind(getItem(position))
            false -> (holder as ServerListDisabledViewHolder).bind(getItem(position))
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