package com.example.alarm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alarm.model.AlarmService

class ViewModelFactory(
    private val alarmsService: AlarmService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: AlarmViewModel = when(modelClass) {
         AlarmViewModel::class.java -> AlarmViewModel(alarmsService)
            else -> {
                throw IllegalStateException("Unknown ViewModel class")
            }
        }
        return viewModel as T
        }
    }