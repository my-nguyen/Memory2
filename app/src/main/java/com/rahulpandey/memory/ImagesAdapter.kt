package com.rahulpandey.memory

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rahulpandey.memory.databinding.CardImageBinding
import kotlin.math.min

class ImagesAdapter(
    val imageUris: List<Uri>,
    val boardSize: BoardSize,
    val listener: ClickListener
) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    interface ClickListener {
        fun onPlaceholderClicked()
    }

    inner class ViewHolder(val binding: CardImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            binding.image.setImageURI(uri)
            // once an image is picked, user cannot pick another image by clicking on the square
            binding.image.setOnClickListener(null)
        }

        fun bind() {
            binding.image.setOnClickListener {
                listener.onPlaceholderClicked()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardImageBinding.inflate(inflater, parent, false)

        val cardWidth = parent.width / boardSize.width()
        val cardHeight = parent.height / boardSize.height()
        val cardSide = min(cardWidth, cardHeight)
        val layoutParams = binding.image.layoutParams
        layoutParams.width = cardSide
        layoutParams.height = cardSide

        return ViewHolder(binding)
    }

    override fun getItemCount() = boardSize.pairs()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < imageUris.size) {
            holder.bind(imageUris[position])
        } else {
            holder.bind()
        }
    }
}
