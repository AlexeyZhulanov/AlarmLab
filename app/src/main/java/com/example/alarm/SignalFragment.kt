package com.example.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alarm.databinding.FragmentSignalBinding


class SignalFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSignalBinding.inflate(inflater, container, false)
        binding.pulsator.start()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}