package com.codingempire.adminpse.Factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codingempire.adminpse.ViewModel.WithdrawViewModel
import com.codingempire.adminpse.repository.WithdrawRepository

class WithdrawViewModelFactory(private val application: Application, private val repository: WithdrawRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WithdrawViewModel(application, repository) as T
    }
}