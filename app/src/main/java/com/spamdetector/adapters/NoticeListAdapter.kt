package com.spamdetector.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spamdetector.R
import com.spamdetector.databinding.ItemNoticeBinding
import com.spamdetector.model.data.NoticeInfo

class NoticeListAdapter : RecyclerView.Adapter<NoticeViewHolder>() {

    private val notices = mutableListOf<NoticeInfo>()
    var onClick: (NoticeInfo) -> Unit = {}

    fun update(menus: List<NoticeInfo>) {
        this.notices.clear()
        this.notices.addAll(menus)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notice, parent, false)
        val binding = ItemNoticeBinding.bind(view)
        return NoticeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val lp = holder.itemView.layoutParams
        lp.height

        val notice = notices[position]
        holder.bind(notice)
        holder.itemView.setOnClickListener { onClick(notice) }
    }

    override fun getItemCount(): Int = notices.size
}

class NoticeViewHolder(binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root) {


    fun bind(data: NoticeInfo) {

    }
}