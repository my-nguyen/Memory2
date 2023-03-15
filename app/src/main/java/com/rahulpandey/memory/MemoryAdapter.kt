package com.rahulpandey.memory

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.rahulpandey.memory.databinding.MemoryCardBinding
import kotlin.math.min

private const val MARGIN_SIZE = 10
private const val TAG = "MemoryAdapter-Truong"

class MemoryAdapter(
    private val boardSize: BoardSize,
    val cards: List<Card>,
    val listener: ClickListener
) : RecyclerView.Adapter<MemoryAdapter.ViewHolder>() {
    interface ClickListener {
        fun onCardClicked(position: Int)
    }

    inner class ViewHolder(val binding: MemoryCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val card = cards[position]
            val image = if (card.isFaceUp) card.id else R.drawable.ic_launcher_background
            binding.imageButton.setImageResource(image)

            binding.imageButton.alpha = if (card.isMatched) .4f else 1.0f
            val colors = if (card.isMatched) ContextCompat.getColorStateList(
                binding.root.context,
                R.color.color_gray
            ) else null
            ViewCompat.setBackgroundTintList(binding.imageButton, colors)

            binding.imageButton.setOnClickListener {
                Log.d(TAG, "Clicked on position $position")
                listener.onCardClicked(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MemoryCardBinding.inflate(inflater, parent, false)
        val layoutParams = binding.cardView.layoutParams as MarginLayoutParams
        val width = parent.width / boardSize.width() - MARGIN_SIZE * 2
        val height = parent.height / boardSize.height() - MARGIN_SIZE * 2
        val cardSide = min(width, height)
        layoutParams.width = cardSide
        layoutParams.height = cardSide
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(binding)
    }
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun bind(position: Int) {}
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryAdapter.ViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val view = inflater.inflate(R.layout.memory_card, parent, false)
//        return ViewHolder(view)
//    }

    override fun getItemCount() = boardSize.count

    override fun onBindViewHolder(holder: MemoryAdapter.ViewHolder, position: Int) {
        holder.bind(position)
    }
}
