package com.example.fastcontacts

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val onClick: (Contact) -> Unit,
    private val onCallClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.VH>() {

    private var items: List<Contact> = emptyList()

    init {
        setHasStableIds(true)
    }

    fun submit(list: List<Contact>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.name.text = c.displayName
        holder.avatar.text = c.initial
        holder.starBadge.visibility = if (c.isStarred) View.VISIBLE else View.GONE

        val bg = holder.avatar.background.mutate() as GradientDrawable
        bg.setColor(PALETTE[((c.id % PALETTE.size) + PALETTE.size).toInt() % PALETTE.size])

        if (c.phones.isEmpty()) {
            holder.preview.visibility = View.GONE
            holder.btnQuickCall.visibility = View.GONE
        } else {
            holder.preview.visibility = View.VISIBLE
            holder.btnQuickCall.visibility = View.VISIBLE
            val extra = c.phones.size - 1
            holder.preview.text =
                if (extra > 0) c.phones[0].number + "  +" + extra + " more" else c.phones[0].number
        }

        holder.itemView.setOnClickListener { onClick(c) }
        holder.btnQuickCall.setOnClickListener { onCallClick(c) }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val avatar: TextView = v.findViewById(R.id.avatar)
        val starBadge: TextView = v.findViewById(R.id.starBadge)
        val name: TextView = v.findViewById(R.id.name)
        val preview: TextView = v.findViewById(R.id.preview)
        val btnQuickCall: ImageButton = v.findViewById(R.id.btnQuickCall)
    }

    private companion object {
        val PALETTE = intArrayOf(
            0xFF1A73E8.toInt(), 0xFF188038.toInt(), 0xFFD93025.toInt(), 0xFFE37400.toInt(),
            0xFF9334E6.toInt(), 0xFF007B83.toInt(), 0xFFC2185B.toInt(), 0xFF5F6368.toInt()
        )
    }
}
