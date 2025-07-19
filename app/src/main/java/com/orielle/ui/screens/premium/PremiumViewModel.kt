package com.orielle.ui.screens.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.BillingManager
import com.orielle.domain.manager.PurchaseResult
import com.orielle.domain.manager.RestoreResult
import com.orielle.domain.manager.SubscriptionProduct
import com.orielle.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class PremiumUiState(
    val isLoading: Boolean = false,
    val products: List<SubscriptionProduct> = emptyList(),
    val selectedProductId: String? = null,
    val isPremium: Boolean = false
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadProducts()
        observePremiumStatus()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val products = billingManager.getAvailableProducts()
                _uiState.value = _uiState.value.copy(
                    products = products,
                    selectedProductId = products.firstOrNull()?.productId,
                    isLoading = false
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading products")
                _uiState.value = _uiState.value.copy(isLoading = false)
                _eventFlow.emit(UiEvent.ShowSnackbar("Failed to load subscription options"))
            }
        }
    }

    private fun observePremiumStatus() {
        viewModelScope.launch {
            billingManager.isPremium.collect { isPremium ->
                _uiState.value = _uiState.value.copy(isPremium = isPremium)
            }
        }
    }

    fun selectProduct(productId: String) {
        _uiState.value = _uiState.value.copy(selectedProductId = productId)
    }

    fun purchasePremium() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = billingManager.purchasePremium()

                when (result) {
                    is PurchaseResult.Success -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Premium upgrade successful!"))
                        // Navigate back or to main screen
                    }
                    is PurchaseResult.Cancelled -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Purchase cancelled"))
                    }
                    is PurchaseResult.Error -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Purchase failed: ${result.message}"))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during purchase")
                _eventFlow.emit(UiEvent.ShowSnackbar("Purchase failed: ${e.message}"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = billingManager.restorePurchases()

                when (result) {
                    is RestoreResult.Success -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Purchases restored successfully!"))
                    }
                    is RestoreResult.NoPurchasesFound -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar("No previous purchases found"))
                    }
                    is RestoreResult.Error -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Restore failed: ${result.message}"))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error restoring purchases")
                _eventFlow.emit(UiEvent.ShowSnackbar("Restore failed: ${e.message}"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}