package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ApiCertificate
import com.example.data.Account
import com.example.data.Transaction
import com.example.ui.theme.KbcBluePrimary
import com.example.ui.theme.KbcBlueDark
import com.example.ui.theme.KbcCyanSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KbcApp(viewModel: KbcViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val certificates by viewModel.certificates.collectAsState()
    val activeCertificate by viewModel.activeCertificate.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()
    val isCheckingApi by viewModel.isCheckingApi.collectAsState()
    val apiTestResult by viewModel.apiTestResult.collectAsState()

    var currentTab by remember { mutableStateOf(0) }
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }
    var paymentErrorMessage by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current

    // Listen to payment events
    LaunchedEffect(Unit) {
        viewModel.paymentResultEvent.collect { result ->
            when (result) {
                is KbcViewModel.PaymentResult.Success -> {
                    showPaymentSuccessDialog = true
                }
                is KbcViewModel.PaymentResult.Error -> {
                    paymentErrorMessage = result.message
                }
            }
        }
    }

    val selectedAccount = accounts.find { it.id == selectedAccountId }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = KbcBlueDark,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(KbcCyanSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "KBC Mobile",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Veilig & Snel Betalen",
                            color = KbcCyanSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    if (activeCertificate != null) {
                        Surface(
                            color = KbcCyanSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Green, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "API Actief",
                                    color = Color.Green,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp,
                modifier = Modifier.testTag("kbc_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Saldo") },
                    label = { Text("Saldo", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KbcBluePrimary,
                        indicatorColor = KbcBluePrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Payment, contentDescription = "Betalen") },
                    label = { Text("Betalen", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KbcBluePrimary,
                        indicatorColor = KbcBluePrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Certificaten") },
                    label = { Text("API & Certen", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KbcBluePrimary,
                        indicatorColor = KbcBluePrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    accounts = accounts,
                    transactions = transactions,
                    selectedAccountId = selectedAccountId,
                    onSelectAccount = { viewModel.selectAccount(it) },
                    onCopyToClipboard = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                    }
                )
                1 -> PaymentScreen(
                    accounts = accounts,
                    selectedAccountId = selectedAccountId,
                    activeCertificate = activeCertificate,
                    onSelectAccount = { viewModel.selectAccount(it) },
                    onPay = { name, iban, amount, desc, useApi ->
                        viewModel.makePayment(name, iban, amount, desc, useApi)
                    },
                    errorMessage = paymentErrorMessage,
                    onClearError = { paymentErrorMessage = null }
                )
                2 -> CertificatesScreen(
                    certificates = certificates,
                    activeCertificate = activeCertificate,
                    isChecking = isCheckingApi,
                    testResult = apiTestResult,
                    onToggleActive = { viewModel.toggleCertificate(it) },
                    onDelete = { viewModel.deleteCertificate(it) },
                    onTest = { viewModel.testCertificateConnection(it) },
                    onAdd = { name, endpoint, clientId, certType, content ->
                        viewModel.addCertificate(name, endpoint, clientId, certType, content)
                    },
                    onClearTestResult = { viewModel.clearApiTestResult() }
                )
            }

            // Payment success dialog
            if (showPaymentSuccessDialog) {
                Dialog(onDismissRequest = { showPaymentSuccessDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Succes",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Betaling Voltooid",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = KbcBlueDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Je betaling is succesvol verwerkt en geregistreerd in het grootboek.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    showPaymentSuccessDialog = false
                                    currentTab = 0 // Go back to balance
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KbcBluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("success_confirm_button")
                            ) {
                                Text("Terug naar Saldo", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    accounts: List<Account>,
    transactions: List<Transaction>,
    selectedAccountId: String,
    onSelectAccount: (String) -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = WindowInsets.safeDrawing.asPaddingValues()
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welkom terug,",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                text = "Math Moors",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = KbcBlueDark,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Horizontal account list/carousel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mijn Rekeningen",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = KbcBluePrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                accounts.forEach { account ->
                    val isSelected = account.id == selectedAccountId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clickable { onSelectAccount(account.id) }
                            .testTag("account_card_${account.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) KbcBluePrimary else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = account.accountName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else KbcBlueDark
                                )
                                if (isSelected) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Geselecteerd",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = account.iban,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Kopieer IBAN",
                                    tint = if (isSelected) Color.White.copy(alpha = 0.8f) else KbcBluePrimary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onCopyToClipboard(account.iban) }
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Beschikbaar saldo",
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                            Text(
                                text = formatCurrency(account.balance),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else KbcBluePrimary,
                                modifier = Modifier.testTag("balance_text_${account.id}")
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Transactions Header
        item {
            Text(
                text = "Recente Transacties",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KbcBlueDark,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Transactions list
        val filteredTransactions = transactions.filter { it.accountId == selectedAccountId }
        if (filteredTransactions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Geen recente transacties gevonden voor deze rekening.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                TransactionRow(tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
    val isDebit = tx.type == "DEBIT"
    val colorAccent = if (isDebit) Color(0xFFC62828) else Color(0xFF2E7D32)
    val prefix = if (isDebit) "-" else "+"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isDebit) Color(0xFFFEEBEE) else Color(0xFFE8F5E9),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDebit) Icons.Default.Send else Icons.Default.CheckCircle,
                    contentDescription = tx.type,
                    tint = colorAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tx.recipientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = KbcBlueDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tx.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Text(
                    text = formatDate(tx.timestamp),
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$prefix ${formatCurrency(tx.amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colorAccent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    accounts: List<Account>,
    selectedAccountId: String,
    activeCertificate: ApiCertificate?,
    onSelectAccount: (String) -> Unit,
    onPay: (String, String, Double, String, Boolean) -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit
) {
    var recipientName by remember { mutableStateOf("") }
    var recipientIban by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var useApi by remember { mutableStateOf(false) }

    val selectedAccount = accounts.find { it.id == selectedAccountId }

    // Auto-enable API mode if active cert is present, as a cool convenience
    LaunchedEffect(activeCertificate) {
        if (activeCertificate != null) {
            useApi = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("payment_screen")
    ) {
        Text(
            text = "Nieuwe Overschrijving",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = KbcBlueDark
        )
        Text(
            text = "Verstuur direct geld naar een andere rekening.",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Account Selector Row
        if (selectedAccount != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KbcBluePrimary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = KbcBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Debiteren van: ${selectedAccount.accountName}",
                            fontSize = 12.sp,
                            color = KbcBlueDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Saldo: ${formatCurrency(selectedAccount.balance)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = KbcBluePrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recipient Name
        OutlinedTextField(
            value = recipientName,
            onValueChange = { recipientName = it },
            label = { Text("Naam begunstigde") },
            placeholder = { Text("b.v. Jan Peeters") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recipient_name_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KbcBluePrimary,
                focusedLabelColor = KbcBluePrimary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Recipient IBAN
        OutlinedTextField(
            value = recipientIban,
            onValueChange = { recipientIban = it.uppercase() },
            label = { Text("IBAN van begunstigde") },
            placeholder = { Text("b.v. BE96 0012 3456 7890") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recipient_iban_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KbcBluePrimary,
                focusedLabelColor = KbcBluePrimary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Amount
        OutlinedTextField(
            value = amountString,
            onValueChange = { amountString = it },
            label = { Text("Bedrag (€)") },
            placeholder = { Text("0,00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("amount_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KbcBluePrimary,
                focusedLabelColor = KbcBluePrimary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Vrije mededeling") },
            placeholder = { Text("b.v. Terugbetaling etentje") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("description_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KbcBluePrimary,
                focusedLabelColor = KbcBluePrimary
            )
        )
        Spacer(modifier = Modifier.height(20.dp))

        // API Mode Switcher
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "API Keys",
                                tint = KbcCyanSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KBC Developer API",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = KbcBlueDark
                            )
                        }
                        Text(
                            text = "Boek deze transactie via het geselecteerde PSD2 open banking certificaat.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = useApi,
                        onCheckedChange = { useApi = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = KbcBluePrimary
                        ),
                        modifier = Modifier.testTag("api_payment_switch")
                    )
                }

                if (useApi) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (activeCertificate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Green, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verbinden met: ${activeCertificate.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Geen actieve API sleutel. Ga naar 'API & Certen' tab.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error message banner
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFF9B1C1C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sluit fout",
                                tint = Color(0xFF9B1C1C)
                            )
                        }
                    }
                }
            }
        }

        // Action Button
        Button(
            onClick = {
                val amt = amountString.replace(",", ".").toDoubleOrNull() ?: 0.0
                onPay(recipientName, recipientIban, amt, description, useApi)
            },
            colors = ButtonDefaults.buttonColors(containerColor = KbcBluePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_payment_button")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Verstuur",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Betaling Bevestigen",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CertificatesScreen(
    certificates: List<ApiCertificate>,
    activeCertificate: ApiCertificate?,
    isChecking: Boolean,
    testResult: String?,
    onToggleActive: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onTest: (ApiCertificate) -> Unit,
    onAdd: (String, String, String, String, String) -> Unit,
    onClearTestResult: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("certificates_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "API Certificaten",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = KbcBlueDark
                )
                Text(
                    text = "Beheer open banking PSD2 certificaten.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = KbcBluePrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier.testTag("add_certificate_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Voeg Cert toe", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Voeg toe", fontSize = 12.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // API test result display
        if (isChecking || testResult != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KbcBlueDark),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KBC Gateway Handshake",
                            color = KbcCyanSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (!isChecking) {
                            TextButton(onClick = onClearTestResult) {
                                Text("Sluit", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isChecking) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = KbcCyanSecondary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "SSL Handshake en Handtekening verifiëren...",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    } else if (testResult != null) {
                        Text(
                            text = testResult,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // List of certificates
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (certificates.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Geen certificaten opgeslagen. Voeg een API certificaat toe om te beginnen.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(certificates) { cert ->
                    CertificateItemRow(
                        cert = cert,
                        isActive = cert.id == activeCertificate?.id,
                        onActivate = { onToggleActive(cert.id) },
                        onDelete = { onDelete(cert.id) },
                        onTest = { onTest(cert) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddCertificateDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, endpoint, clientId, certType, content ->
                    onAdd(name, endpoint, clientId, certType, content)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun CertificateItemRow(
    cert: ApiCertificate,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("certificate_item_${cert.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) KbcBluePrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, KbcBluePrimary) else null,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Cert",
                            tint = if (isActive) KbcBluePrimary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cert.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = KbcBlueDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Endpoint: ${cert.endpointUrl}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                    Text(
                        text = "Client ID: ${cert.clientId} • Type: ${cert.certificateType}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Delete button (only for custom ones, say id > 2, or just allow deleting all custom entries)
                if (cert.id > 2) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Verwijder",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Activate Toggle / Indicator
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onActivate() }
                        .padding(vertical = 6.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (isActive) Color(0xFF2E7D32) else Color.LightGray,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) "Geselecteerd" else "Kies als actief",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) Color(0xFF2E7D32) else Color.Gray
                    )
                }

                // Connection Test Button
                OutlinedButton(
                    onClick = onTest,
                    border = androidx.compose.foundation.BorderStroke(1.dp, KbcCyanSecondary),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KbcCyanSecondary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Test",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gateway Testen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddCertificateDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("https://sandbox.api.kbc.be/v1") }
    var clientId by remember { mutableStateOf("") }
    var certType by remember { mutableStateOf("API Key") }
    var content by remember { mutableStateOf("") }

    val types = listOf("API Key", "QWAC (OAuth)", "QSealC", "Client SSL Cert")
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Nieuw Certificaat Toevoegen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KbcBlueDark
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Certificaat Naam") },
                    placeholder = { Text("b.v. KBC Production Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("API Endpoint URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientId,
                    onValueChange = { clientId = it },
                    label = { Text("Client ID (eIDAS)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Certificate Type Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = certType,
                        onValueChange = {},
                        label = { Text("Certificaat Type") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.Lock, contentDescription = "Lijst openen")
                            }
                        }
                    )
                    androidx.compose.material3.DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    certType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Certificaat Sleutel (PEM / JSON)") },
                    placeholder = { Text("-----BEGIN PRIVATE KEY-----\n...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuleren", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && endpoint.isNotBlank() && clientId.isNotBlank() && content.isNotBlank()) {
                                onAdd(name, endpoint, clientId, certType, content)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KbcBluePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Opslaan", color = Color.White)
                    }
                }
            }
        }
    }
}

// Helpers
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("nl", "BE"))
    return format.format(amount)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("nl", "BE"))
    return sdf.format(Date(timestamp))
}
