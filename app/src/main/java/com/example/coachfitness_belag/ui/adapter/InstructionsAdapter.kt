package com.example.coachfitness_belag.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coachfitness_belag.R

class InstructionsAdapter(private val instructions: List<String>) :
    RecyclerView.Adapter<InstructionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStepNumber: TextView = view.findViewById(R.id.tvStepNumber)
        val tvInstruction: TextView = view.findViewById(R.id.tvInstruction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instruction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvStepNumber.text = (position + 1).toString()
        holder.tvInstruction.text = instructions[position]
    }

    override fun getItemCount() = instructions.size
}
