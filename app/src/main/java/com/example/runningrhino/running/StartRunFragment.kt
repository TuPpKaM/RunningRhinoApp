package com.example.runningrhino.running

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.runningrhino.R
import com.example.runningrhino.databinding.FragmentStartRunBinding
import com.example.runningrhino.tracking.TrackingViewModel


class StartRunFragment : Fragment(R.layout.fragment_start_run) {

    private var _binding: FragmentStartRunBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())

    private val sharedViewModel: TrackingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartRunBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val startButton: Button = view.findViewById(R.id.start_button)
        startButton.setOnClickListener {
            Log.d("GPS", "start button")
            startRun()
            val navController = findNavController()
            navController.navigate(R.id.action_startRunFragment_to_mapsFragment)
        }
    }

    private fun startRun() {
        Log.d("GPS", "Startrun() ${sharedViewModel.fix.value}")
        sharedViewModel.startTracking()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}