package com.example.coachfitness_belag.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.models.Exercise

class ExerciseAdapter(private val onExerciseClick: (Exercise) -> Unit) :
    ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view, onExerciseClick)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExerciseViewHolder(
        itemView: View,
        private val onExerciseClick: (Exercise) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvExerciseName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val ivExercise: ImageView = itemView.findViewById(R.id.ivExercise)

        fun bind(exercise: Exercise) {
            tvName.text = exercise.name
            tvCategory.text = exercise.category
            tvDuration.text = "${exercise.duration} min"
            
            val nameLower = exercise.name.lowercase()

            // Attribution de l'image correspondante
            when {
                nameLower.contains("yoga") -> {
                    ivExercise.setImageResource(R.drawable.yoga)
                    ivExercise.scaleType = ImageView.ScaleType.CENTER_CROP
                    ivExercise.setPadding(0, 0, 0, 0)
                    ivExercise.background = null
                }
                nameLower.contains("pompe") -> {
                    ivExercise.setImageResource(R.drawable.pompes)
                    ivExercise.scaleType = ImageView.ScaleType.CENTER_CROP
                    ivExercise.setPadding(0, 0, 0, 0)
                    ivExercise.background = null
                }
                nameLower.contains("squat") -> {
                    ivExercise.setImageResource(R.drawable.squats)
                    ivExercise.scaleType = ImageView.ScaleType.CENTER_CROP
                    ivExercise.setPadding(0, 0, 0, 0)
                    ivExercise.background = null
                }
                nameLower.contains("burpee") -> {
                    ivExercise.setImageResource(R.drawable.burpees)
                    ivExercise.scaleType = ImageView.ScaleType.CENTER_CROP
                    ivExercise.setPadding(0, 0, 0, 0)
                    ivExercise.background = null
                }
                nameLower.contains("course") || nameLower.contains("run") -> {
                    ivExercise.setImageResource(R.drawable.course)
                    ivExercise.scaleType = ImageView.ScaleType.CENTER_CROP
                    ivExercise.setPadding(0, 0, 0, 0)
                    ivExercise.background = null
                }
                else -> {
                    ivExercise.setImageResource(R.drawable.ic_exercise)
                    ivExercise.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    ivExercise.setPadding(24, 24, 24, 24)
                    ivExercise.setBackgroundResource(R.drawable.bg_circle_red)
                }
            }

            itemView.setOnClickListener {
                onExerciseClick(exercise)
            }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }
}
