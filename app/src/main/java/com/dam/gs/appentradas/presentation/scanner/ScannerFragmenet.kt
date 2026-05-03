package com.dam.gs.appentradas.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dam.gs.appentradas.R
import com.dam.gs.appentradas.databinding.FragmentScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by viewModels()
    private val args: ScannerFragmentArgs by navArgs()
    private lateinit var cameraExecutor: ExecutorService
    private var ultimoCodigo: String = ""
    private var ultimoTiempo: Long = 0



    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        observeViewModel()
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val image = InputImage.fromMediaImage(
                            imageProxy.image!!, imageProxy.imageInfo.rotationDegrees
                        )
                        BarcodeScanning.getClient().process(image)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                                    ?.rawValue?.let { code ->
                                        viewModel.handleScan(code, args.eventId, args.eventName)
                                    }
                            } .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                                    ?.rawValue?.let { code ->
                                        val ahora = System.currentTimeMillis()
                                        if (code != ultimoCodigo || ahora - ultimoTiempo > 3000) {
                                            ultimoCodigo = code
                                            ultimoTiempo = ahora
                                            viewModel.handleScan(code, args.eventId, args.eventName)
                                        }
                                    }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun observeViewModel() {
        viewModel.scanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScannerViewModel.ScanState.Ready -> {
                    binding.tvResultado.text = getString(R.string.scanner_listo)
                    binding.tvCliente.text = ""
                    binding.llResultado.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.primary_dark)
                    )
                }
                is ScannerViewModel.ScanState.Valid -> {
                    binding.tvResultado.text = getString(R.string.scanner_valido)
                    binding.tvCliente.text = "${state.nombre} — ${state.cliente}"
                    binding.llResultado.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.valid)
                    )
                }
                is ScannerViewModel.ScanState.AlreadyUsed -> {
                    binding.tvResultado.text = getString(R.string.scanner_usado)
                    binding.tvCliente.text = ""
                    binding.llResultado.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.used)
                    )
                }
                is ScannerViewModel.ScanState.Invalid -> {
                    binding.tvResultado.text = getString(R.string.scanner_invalido)
                    binding.tvCliente.text = ""
                    binding.llResultado.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.invalid)
                    )
                }
                is ScannerViewModel.ScanState.Error -> {
                    binding.tvResultado.text = "Error: ${state.message}"
                    binding.tvCliente.text = ""
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}