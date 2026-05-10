package com.example.coachfitness_belag.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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

    private lateinit var cameraExecutor: ExecutorService
    private var interpreter: Interpreter? = null
    private val labels = listOf("Ectomorph", "Mesomorph", "Endomorph")
    
    private var detectedMorphology: String? = null
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_morphology_scanner)

        viewFinder = findViewById(R.id.viewFinder)
        tvResultType = findViewById(R.id.tvResultType)
        tvDescription = findViewById(R.id.tvDescription)
        resultCard = findViewById(R.id.resultCard)
        btnConfirm = findViewById(R.id.btnConfirm)
        progressBar = findViewById(R.id.progressBar)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        loadModel()

        btnConfirm.setOnClickListener {
            detectedMorphology?.let { morphology ->
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("FILTER_MORPHOLOGY", morphology)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadModel() {
        try {
            // Note: Make sure model_unquant.tflite is in app/src/main/assets/
            val assetFileDescriptor = assets.openFd("model_unquant.tflite")
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            interpreter = Interpreter(modelBuffer)
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("Scanner", "Error loading model", e)
            // Fallback for testing if file is missing in main/assets
            Toast.makeText(this, "Modèle non trouvé dans assets. Assurez-vous de l'avoir déplacé dans src/main/assets/", Toast.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!isProcessing) {
                            analyzeImage(imageProxy)
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("Scanner", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        val bitmap = viewFinder.bitmap
        if (bitmap == null || interpreter == null) {
            imageProxy.close()
            return
        }

        isProcessing = true
        
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val output = Array(1) { FloatArray(labels.size) }
        interpreter?.run(tensorImage.buffer, output)

        val results = output[0]
        val maxIdx = results.indices.maxByOrNull { results[it] } ?: -1

        if (maxIdx != -1 && results[maxIdx] > 0.7f) { // Seuil de confiance 70%
            val type = labels[maxIdx]
            runOnUiThread {
                showResult(type)
            }
        } else {
            isProcessing = false
        }

        imageProxy.close()
    }

    private fun showResult(type: String) {
        detectedMorphology = type
        resultCard.visibility = View.VISIBLE
        tvResultType.text = type
        
        tvDescription.text = when(type) {
            "Ectomorph" -> "Profil mince, métabolisme rapide. Focus sur la prise de masse (Musculation)."
            "Mesomorph" -> "Profil athlétique, gagne facilement du muscle. Programme équilibré."
            "Endomorph" -> "Profil plus large, métabolisme lent. Focus sur le cardio et l'endurance."
            else -> ""
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions non accordées.", Toast.LENGTH_SHORT).show()
                finish()
            }
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
