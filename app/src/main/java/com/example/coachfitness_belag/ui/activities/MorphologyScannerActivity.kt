package com.example.coachfitness_belag.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.coachfitness_belag.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MorphologyScannerActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var tvResultType: TextView
    private lateinit var tvDescription: TextView
    private lateinit var resultCard: View
    private lateinit var btnConfirm: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvInstructions: TextView

    private lateinit var cameraExecutor: ExecutorService
    private var interpreter: Interpreter? = null
    private val labels = listOf("Ectomorph", "Mesomorph", "Endomorph")
    
    private var detectedMorphology: String? = null
    private var isProcessing = false
    private var canAnalyze = true // Analyse immédiate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_morphology_scanner)

        viewFinder = findViewById(R.id.viewFinder)
        tvResultType = findViewById(R.id.tvResultType)
        tvDescription = findViewById(R.id.tvDescription)
        resultCard = findViewById(R.id.resultCard)
        btnConfirm = findViewById(R.id.btnConfirm)
        progressBar = findViewById(R.id.progressBar)
        tvInstructions = findViewById(R.id.tvInstructions)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        loadModel()
        
        tvInstructions.text = "Analyse en cours... Tenez-vous bien devant la caméra"

        btnConfirm.setOnClickListener {
            redirectToExercises()
        }
    }

    private fun loadModel() {
        try {
            val assetFileDescriptor = assets.openFd("model_unquant.tflite")
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            interpreter = Interpreter(modelBuffer)
            progressBar.visibility = View.GONE
            Log.d("Scanner", "Modèle chargé avec succès")
        } catch (e: Exception) {
            Log.e("Scanner", "Erreur lors du chargement du modèle", e)
            Toast.makeText(this, "Erreur : modèle introuvable.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (canAnalyze && !isProcessing) {
                                analyzeImage(imageProxy)
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                // Priorité à la caméra frontale pour l'utilisateur
                val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("Scanner", "Erreur CameraX", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        // Accès au bitmap sur le thread UI
        runOnUiThread {
            val bitmap = viewFinder.bitmap
            if (bitmap == null || interpreter == null) {
                imageProxy.close()
                isProcessing = false
                return@runOnUiThread
            }

            isProcessing = true

            cameraExecutor.execute {
                try {
                    val imageProcessor = ImageProcessor.Builder()
                        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                        .add(NormalizeOp(127.5f, 127.5f)) // Normalisation standard Teachable Machine
                        .build()

                    var tensorImage = TensorImage(DataType.FLOAT32)
                    tensorImage.load(bitmap)
                    tensorImage = imageProcessor.process(tensorImage)

                    val output = Array(1) { FloatArray(labels.size) }
                    interpreter?.run(tensorImage.buffer, output)

                    val results = output[0]
                    val maxIdx = results.indices.maxByOrNull { results[it] } ?: -1
                    
                    // Log des probabilités pour voir ce que l'IA détecte
                    Log.d("Scanner", "Scores: Ecto=${results[0]}, Meso=${results[1]}, Endo=${results[2]}")

                    // Seuil abaissé à 0.4 (40%) pour faciliter la détection
                    if (maxIdx != -1 && results[maxIdx] > 0.4f) { 
                        val type = labels[maxIdx]
                        runOnUiThread {
                            showResult(type)
                        }
                    } else {
                        isProcessing = false
                    }
                } catch (e: Exception) {
                    Log.e("Scanner", "Erreur analyse", e)
                    isProcessing = false
                } finally {
                    imageProxy.close()
                }
            }
        }
    }

    private fun showResult(type: String) {
        canAnalyze = false 
        detectedMorphology = type
        resultCard.visibility = View.VISIBLE
        tvResultType.text = type
        tvInstructions.text = "Morphologie détectée !"
        
        tvDescription.text = when(type) {
            "Ectomorph" -> "Profil mince. Redirection vers vos exercices personnalisés..."
            "Mesomorph" -> "Profil athlétique. Redirection vers vos exercices personnalisés..."
            "Endomorph" -> "Profil large. Redirection vers vos exercices personnalisés..."
            else -> ""
        }

        // Redirection automatique après 2 secondes
        Handler(Looper.getMainLooper()).postDelayed({
            redirectToExercises()
        }, 2000)
    }

    private fun redirectToExercises() {
        detectedMorphology?.let { morphology ->
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("FILTER_MORPHOLOGY", morphology)
            startActivity(intent)
            finish()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        interpreter?.close()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
