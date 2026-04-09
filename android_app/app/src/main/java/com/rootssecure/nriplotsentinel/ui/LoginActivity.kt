package com.rootssecure.nriplotsentinel.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rootssecure.nriplotsentinel.api.ApiClient
import com.rootssecure.nriplotsentinel.databinding.ActivityLoginBinding
import com.rootssecure.nriplotsentinel.repository.AuthRepository
import com.rootssecure.nriplotsentinel.storage.TokenDataStore
import com.rootssecure.nriplotsentinel.viewmodel.LoginUiState
import com.rootssecure.nriplotsentinel.viewmodel.LoginViewModel
import com.rootssecure.nriplotsentinel.viewmodel.LoginViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var tokenDataStore: TokenDataStore

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            AuthRepository(
                authApiService = ApiClient.authService,
                tokenDataStore = TokenDataStore(this)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenDataStore = TokenDataStore(this)

        lifecycleScope.launch {
            val token = tokenDataStore.tokenFlow.first()
            if (!token.isNullOrBlank()) {
                openHome()
            }
        }

        binding.loginButton.setOnClickListener {
            viewModel.login(
                email = binding.emailInput.text?.toString().orEmpty(),
                password = binding.passwordInput.text?.toString().orEmpty()
            )
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginUiState.Idle -> {
                    binding.progressBar.hide()
                    binding.errorText.text = ""
                }
                is LoginUiState.Loading -> {
                    binding.progressBar.show()
                    binding.errorText.text = ""
                }
                is LoginUiState.Success -> {
                    binding.progressBar.hide()
                    Toast.makeText(
                        this,
                        state.response.message ?: "Login successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    openHome()
                }
                is LoginUiState.Error -> {
                    binding.progressBar.hide()
                    binding.errorText.text = state.message
                }
            }
        }
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
