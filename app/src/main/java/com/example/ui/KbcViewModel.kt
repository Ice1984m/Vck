package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApiCertificate
import com.example.data.KbcDatabase
import com.example.data.KbcRepository
import com.example.data.Transaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KbcViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KbcRepository

    val accounts: StateFlow<List<com.example.data.Account>>
    val transactions: StateFlow<List<Transaction>>
    val certificates: StateFlow<List<ApiCertificate>>

    private val _selectedAccountId = MutableStateFlow("zichtrekening")
    val selectedAccountId = _selectedAccountId.asStateFlow()

    private val _isCheckingApi = MutableStateFlow(false)
    val isCheckingApi = _isCheckingApi.asStateFlow()

    private val _apiTestResult = MutableStateFlow<String?>(null)
    val apiTestResult = _apiTestResult.asStateFlow()

    private val _paymentResultEvent = MutableSharedFlow<PaymentResult>()
    val paymentResultEvent = _paymentResultEvent.asSharedFlow()

    init {
        val database = KbcDatabase.getDatabase(application)
        repository = KbcRepository(database.kbcDao())

        accounts = repository.allAccounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        certificates = repository.allCertificates.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.checkAndPopulateDefaults()
        }
    }

    val activeCertificate: StateFlow<ApiCertificate?> = certificates.map { list ->
        list.find { it.isActive }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun selectAccount(id: String) {
        _selectedAccountId.value = id
    }

    fun makePayment(
        recipientName: String,
        recipientIban: String,
        amount: Double,
        description: String,
        useApi: Boolean
    ) {
        viewModelScope.launch {
            if (recipientName.isBlank() || recipientIban.isBlank() || amount <= 0.0) {
                _paymentResultEvent.emit(PaymentResult.Error("Vul alle verplichte velden in met geldige waarden."))
                return@launch
            }

            val currentCert = activeCertificate.value
            if (useApi && currentCert == null) {
                _paymentResultEvent.emit(PaymentResult.Error("Geen actieve API certificaat geselecteerd in de certificaten-sectie."))
                return@launch
            }

            val success = repository.executePayment(
                fromAccountId = _selectedAccountId.value,
                amount = amount,
                recipientName = recipientName,
                recipientIban = recipientIban,
                description = description,
                apiMode = useApi,
                activeCertificate = currentCert
            )

            if (success) {
                _paymentResultEvent.emit(PaymentResult.Success)
            } else {
                _paymentResultEvent.emit(PaymentResult.Error("Onvoldoende saldo op de geselecteerde rekening."))
            }
        }
    }

    fun toggleCertificate(certId: Long) {
        viewModelScope.launch {
            repository.selectActiveCertificate(certId)
        }
    }

    fun addCertificate(
        name: String,
        endpoint: String,
        clientId: String,
        type: String,
        content: String
    ) {
        viewModelScope.launch {
            if (name.isBlank() || endpoint.isBlank() || clientId.isBlank() || content.isBlank()) {
                return@launch
            }
            val newCert = ApiCertificate(
                name = name,
                endpointUrl = endpoint,
                clientId = clientId,
                certificateType = type,
                certificateContent = content,
                isActive = false
            )
            repository.insertCertificate(newCert)
        }
    }

    fun deleteCertificate(certId: Long) {
        viewModelScope.launch {
            repository.deleteCertificateById(certId)
        }
    }

    fun testCertificateConnection(cert: ApiCertificate) {
        viewModelScope.launch {
            _isCheckingApi.value = true
            _apiTestResult.value = null
            // Simulate networking and handshake
            delay(1800)
            _isCheckingApi.value = false
            _apiTestResult.value = """
                🟢 VERBONDEN MET KBC GATEWAY
                Status: 200 OK
                Handshake: ECDHE-RSA-AES256-GCM-SHA384
                Endpoint: ${cert.endpointUrl}
                Client ID: ${cert.clientId}
                Certificaat Type: ${cert.certificateType}
                Sleutel Validatie: Geverifieerd door KBC Sandbox CA
            """.trimIndent()
        }
    }

    fun clearApiTestResult() {
        _apiTestResult.value = null
    }

    sealed interface PaymentResult {
        object Success : PaymentResult
        data class Error(val message: String) : PaymentResult
    }
}
