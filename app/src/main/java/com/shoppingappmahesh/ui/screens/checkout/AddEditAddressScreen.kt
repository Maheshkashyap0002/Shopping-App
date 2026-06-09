package com.shoppingappmahesh.ui.screens.checkout

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppingappmahesh.domain.model.Address
import com.shoppingappmahesh.ui.screens.checkout.viewmodel.AddressUiState
import com.shoppingappmahesh.ui.screens.checkout.viewmodel.AddressViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressScreen(
    navController: NavHostController,
    addressId: String? = null,
    viewModel: AddressViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var altPhone by remember { mutableStateOf("") }
    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("India") }
    var addressType by remember { mutableStateOf("Home") }
    var isDefault by remember { mutableStateOf(false) }

    // Load existing address if editing
    LaunchedEffect(addressId) {
        if (addressId != null) {
            viewModel.addresses.first().find { it.id == addressId }?.let { addr ->
                fullName = addr.fullName
                phone = addr.phone
                altPhone = addr.altPhone
                houseNo = addr.houseNo
                street = addr.street
                landmark = addr.landmark
                city = addr.city
                state = addr.state
                pincode = addr.pincode
                country = addr.country
                addressType = addr.addressType
                isDefault = addr.isDefault
            }
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AddressUiState.Success -> {
                Toast.makeText(context, (uiState as AddressUiState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.navigateUp()
            }
            is AddressUiState.Error -> {
                Toast.makeText(context, (uiState as AddressUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (addressId == null) "Add New Address" else "Edit Address", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Contact Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            PremiumTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name*")
            PremiumTextField(value = phone, onValueChange = { phone = it }, label = "Mobile Number*", keyboardType = KeyboardType.Phone)
            PremiumTextField(value = altPhone, onValueChange = { altPhone = it }, label = "Alternate Mobile (Optional)", keyboardType = KeyboardType.Phone)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Address Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            PremiumTextField(value = houseNo, onValueChange = { houseNo = it }, label = "House No. / Flat / Building*")
            PremiumTextField(value = street, onValueChange = { street = it }, label = "Street / Area / Colony*")
            PremiumTextField(value = landmark, onValueChange = { landmark = it }, label = "Landmark (Optional)")
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumTextField(value = city, onValueChange = { city = it }, label = "City*", modifier = Modifier.weight(1f))
                PremiumTextField(value = pincode, onValueChange = { pincode = it }, label = "PIN Code*", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumTextField(value = state, onValueChange = { state = it }, label = "State*", modifier = Modifier.weight(1f))
                PremiumTextField(value = country, onValueChange = { country = it }, label = "Country*", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Address Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Home", "Work", "Other").forEach { type ->
                    val isSelected = addressType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { addressType = type },
                        label = { Text(type) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = when(type) {
                                        "Home" -> Icons.Default.Home
                                        "Work" -> Icons.Default.Work
                                        else -> Icons.Default.LocationOn
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Set as Default Address", fontWeight = FontWeight.Bold)
                    Text("Use this as my primary delivery address", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Switch(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color.Black)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validate(fullName, phone, houseNo, street, city, state, pincode, context)) {
                        val address = Address(
                            id = addressId ?: "",
                            fullName = fullName,
                            phone = phone,
                            altPhone = altPhone,
                            houseNo = houseNo,
                            street = street,
                            landmark = landmark,
                            city = city,
                            state = state,
                            pincode = pincode,
                            country = country,
                            addressType = addressType,
                            isDefault = isDefault
                        )
                        if (addressId == null) viewModel.addAddress(address)
                        else viewModel.updateAddress(address)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = uiState !is AddressUiState.Loading
            ) {
                if (uiState is AddressUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (addressId == null) "Save Address" else "Update Address", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedLabelColor = Color.Blue
        )
    )
}

private fun validate(
    name: String, phone: String, house: String, street: String, 
    city: String, state: String, pincode: String, context: android.content.Context
): Boolean {
    return when {
        name.isBlank() -> { Toast.makeText(context, "Enter full name", Toast.LENGTH_SHORT).show(); false }
        phone.length < 10 -> { Toast.makeText(context, "Enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show(); false }
        house.isBlank() -> { Toast.makeText(context, "Enter house/flat no", Toast.LENGTH_SHORT).show(); false }
        street.isBlank() -> { Toast.makeText(context, "Enter street/area", Toast.LENGTH_SHORT).show(); false }
        city.isBlank() -> { Toast.makeText(context, "Enter city", Toast.LENGTH_SHORT).show(); false }
        state.isBlank() -> { Toast.makeText(context, "Enter state", Toast.LENGTH_SHORT).show(); false }
        pincode.length < 6 -> { Toast.makeText(context, "Enter valid 6-digit PIN code", Toast.LENGTH_SHORT).show(); false }
        else -> true
    }
}
