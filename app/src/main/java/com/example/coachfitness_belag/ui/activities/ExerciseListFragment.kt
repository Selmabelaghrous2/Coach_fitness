package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.api.RetrofitInstance
import com.example.coachfitness_belag.data.models.Exercise
import com.example.coachfitness_belag.data.repository.AppRepository
import com.example.coachfitness_belag.ui.adapter.ExerciseAdapter
import com.example.coachfitness_belag.ui.viewmodel.MainViewModel
import com.google.android.material.chip.Chip

class ExerciseListFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var rvExercises: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var categoryChips: LinearLayout
    private lateinit var exerciseAdapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupViewModel()
        setupCategoryFilters()
        
        // Récupérer la morphologie passée en argument
        val morphology = arguments?.getString("MORPHOLOGY")
        if (morphology != null) {
            applyMorphologyFilter(morphology)
        } else {
            loadExercises()
        }
    }

    private fun initViews(view: View) {
        rvExercises = view.findViewById(R.id.rvExercises)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        categoryChips = view.findViewById(R.id.categoryChips)
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter { exercise ->
            navigateToExerciseDetail(exercise)
        }
        rvExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
        }
    }

    private fun setupViewModel() {
        val repository = AppRepository(RetrofitInstance.apiService)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            updateExerciseList(exercises ?: emptyList())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                rvExercises.visibility = View.GONE
                tvEmpty.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                tvEmpty.text = "Erreur de connexion : $it\nVérifiez l'adresse IP du serveur."
                tvEmpty.visibility = View.VISIBLE
                rvExercises.visibility = View.GONE
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupCategoryFilters() {
        val categories = listOf("Tous", "Cardio", "Musculation", "Yoga", "Étirements")
        categoryChips.removeAllViews()
        for (category in categories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setOnClickListener { filterByCategory(category) }
            }
            categoryChips.addView(chip)
        }
    }

    private fun applyMorphologyFilter(morphology: String) {
        Toast.makeText(requireContext(), "Programmes pour $morphology", Toast.LENGTH_LONG).show()
        when (morphology) {
            "Ectomorph" -> viewModel.loadExercisesByCategory("musculation")
            "Endomorph" -> viewModel.loadExercisesByCategory("cardio")
            "Mesomorph" -> viewModel.loadAllExercises()
            else -> viewModel.loadAllExercises()
        }
    }

    private fun filterByCategory(category: String) {
        when (category) {
            "Tous" -> viewModel.loadAllExercises()
            "Cardio" -> viewModel.loadExercisesByCategory("cardio")
            "Musculation" -> viewModel.loadExercisesByCategory("musculation")
            "Yoga" -> viewModel.loadExercisesByCategory("yoga")
            "Étirements" -> viewModel.loadExercisesByCategory("stretching")
        }
    }

    private fun updateExerciseList(exercises: List<Exercise>) {
        if (exercises.isEmpty()) {
            rvExercises.visibility = View.GONE
            tvEmpty.text = "Aucun exercice disponible"
            tvEmpty.visibility = View.VISIBLE
        } else {
            rvExercises.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            exerciseAdapter.submitList(exercises)
        }
    }

    fun loadExercises() {
        viewModel.loadAllExercises()
    }

    private fun navigateToExerciseDetail(exercise: Exercise) {
        val intent = Intent(requireContext(), ExerciseDetailActivity::class.java)
        intent.putExtra("exercise", exercise)
        startActivity(intent)
    }
}
