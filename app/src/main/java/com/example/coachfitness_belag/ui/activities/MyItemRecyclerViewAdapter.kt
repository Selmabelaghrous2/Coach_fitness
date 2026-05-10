package com.example.coachfitness_belag.ui.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coachfitness_belag.databinding.FragmentItemBinding
import com.example.coachfitness_belag.placeholder.PlaceholderContent

/**
 * [androidx.recyclerview.widget.RecyclerView.Adapter] that can display a [com.example.coachfitness_belag.placeholder.PlaceholderContent.PlaceholderItem].
 */
class MyItemRecyclerViewAdapter(
    private val values: List<PlaceholderContent.PlaceholderItem>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.tvExerciseName.text = item.content
        holder.tvCategory.text = item.id
        holder.tvDuration.text = "30 min"
        holder.tvCalories.text = "200 cal"
        holder.tvDifficulty.text = "Débutant"
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvExerciseName: TextView = binding.tvExerciseName
        val tvCategory: TextView = binding.tvCategory
        val tvDuration: TextView = binding.tvDuration
        val tvCalories: TextView = binding.tvCalories
        val tvDifficulty: TextView = binding.tvDifficulty

        override fun toString(): String {
            return super.toString() + " '" + tvExerciseName.text + "'"
        }
    }
}
