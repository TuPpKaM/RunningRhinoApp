package com.example.runningrhino.running

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.runningrhino.R
import com.example.runningrhino.databinding.FragmentStartRunBinding
import com.example.runningrhino.tracking.TrackingViewModel

class StartRunFragment : Fragment(R.layout.fragment_start_run) {

    private var _binding: FragmentStartRunBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: TrackingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartRunBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}