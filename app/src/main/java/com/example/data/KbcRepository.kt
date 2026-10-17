package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class KbcRepository(private val kbcDao: KbcDao) {
    val allAccounts: Flow<List<Account>> = kbcDao.getAllAccounts()
    val allTransactions: Flow<List<Transaction>> = kbcDao.getAllTransactions()
    val allCertificates: Flow<List<ApiCertificate>> = kbcDao.getAllCertificates()

    suspend fun getAccountById(id: String): Account? {
        return kbcDao.getAccountById(id)
    }

    suspend fun insertAccount(account: Account) {
        kbcDao.insertAccount(account)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        kbcDao.insertTransaction(transaction)
    }

    suspend fun insertCertificate(certificate: ApiCertificate) {
        kbcDao.insertCertificate(certificate)
    }

    suspend fun deleteCertificateById(id: Long) {
        kbcDao.deleteCertificateById(id)
    }

    suspend fun selectActiveCertificate(id: Long) {
        kbcDao.selectActiveCertificate(id)
    }

    suspend fun executePayment(
        fromAccountId: String,
        amount: Double,
        recipientName: String,
        recipientIban: String,
        description: String,
        apiMode: Boolean,
        activeCertificate: ApiCertificate?
    ): Boolean {
        val account = kbcDao.getAccountById(fromAccountId) ?: return false
        if (account.balance < amount) return false

        // Update balance
        val newBalance = account.balance - amount
        kbcDao.updateAccountBalance(fromAccountId, newBalance)

        // Generate transaction
        val descSuffix = if (apiMode && activeCertificate != null) {
            " [Via API: ${activeCertificate.name}]"
        } else {
            " [Lokaal Boeken]"
        }

        val transaction = Transaction(
            accountId = fromAccountId,
            type = "DEBIT",
            amount = amount,
            recipientName = recipientName,
            recipientIban = recipientIban,
            description = description + descSuffix,
            status = "Voltooid"
        )
        kbcDao.insertTransaction(transaction)
        return true
    }

    suspend fun checkAndPopulateDefaults() {
        val accounts = kbcDao.getAllAccounts().first()
        if (accounts.isEmpty()) {
            // Seed Accounts
            kbcDao.insertAccount(
                Account(
                    id = "zichtrekening",
                    accountName = "KBC Plusrekening (Zicht)",
                    iban = "BE96 4351 0293 8812",
                    balance = 2485.60
                )
            )
            kbcDao.insertAccount(
                Account(
                    id = "spaarrekening",
                    accountName = "KBC Spaarrekening",
                    iban = "BE12 4351 0293 9940",
                    balance = 12450.25
                )
            )

            // Seed Transactions
            kbcDao.insertTransaction(
                Transaction(
                    accountId = "zichtrekening",
                    type = "DEBIT",
                    amount = 42.50,
                    recipientName = "Supermarkt Delhaize",
                    recipientIban = "BE39 9988 7766 5544",
                    description = "Wekelijkse boodschappen"
                )
            )
            kbcDao.insertTransaction(
                Transaction(
                    accountId = "zichtrekening",
                    type = "DEBIT",
                    amount = 89.15,
                    recipientName = "KBC Verzekeringen",
                    recipientIban = "BE55 1122 3344 5566",
                    description = "Autoverzekering juli 2026"
                )
            )
            kbcDao.insertTransaction(
                Transaction(
                    accountId = "zichtrekening",
                    type = "CREDIT",
                    amount = 2450.00,
                    recipientName = "Loon - Moorkens BV",
                    recipientIban = "BE44 5566 7788 9900",
                    description = "Salaris juni 2026"
                )
            )
            kbcDao.insertTransaction(
                Transaction(
                    accountId = "zichtrekening",
                    type = "DEBIT",
                    amount = 18.60,
                    recipientName = "Café de Grote Markt",
                    recipientIban = "BE91 2233 4455 6677",
                    description = "Drankjes weekend"
                )
            )

            // Seed API Certificates
            kbcDao.insertCertificate(
                ApiCertificate(
                    name = "KBC Developer Sandbox (Default)",
                    endpointUrl = "https://api.kbc.be/v1/sandbox",
                    clientId = "kbc-client-8812a",
                    certificateType = "API Key",
                    certificateContent = "apiKey=kbc_sandbox_key_992507498523_active\nenv=sandbox\nversion=v1.2",
                    isActive = true
                )
            )
            kbcDao.insertCertificate(
                ApiCertificate(
                    name = "PSD2 Open Banking QWAC",
                    endpointUrl = "https://api-openbanking.kbc.be/psd2/v2",
                    clientId = "psd2-math-moors-prod",
                    certificateType = "QWAC",
                    certificateContent = "-----BEGIN CERTIFICATE-----\nMIIFdzCCBF+gAwIBAgIQD6S7uH6W7TfE2yZ8pX5kMTANBgkqhkiG9w0BAQsFADBZ\nMQswCQYDVQQGEwJCRTEYMBYGA1UEChMPR2xvYmFsU2lnbiBudi1zYTEkMCIGA1UE\nCxMbR2xvYmFsU2lnbiBPcGVuIEJhbmtpbmcgQ0EwHhcNMjYwMTAxMDAwMDAwWhcN\nMjcwMTAxMjM1OTU5WjBoMQswCQYDVQQGEwJCRTEVMBMGA1UEChMMS0JDIEJhbmsg\nTlYxHzAdBgNVBAsTFlBTRDIgT3BlbiBCYW5raW5nIEFQSTEdMBsGA1UEAxMUYXBp\nLW9wZW5iYW5raW5nLmtiYy5iZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC\nggEBALrZpX...\n-----END CERTIFICATE-----",
                    isActive = false
                )
            )
        }
    }
}
