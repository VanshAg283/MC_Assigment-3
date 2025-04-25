package com.example.matrixpert

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable
fun MatrixScreen() {
    // Matrix A dimensions
    var rowsA by remember { mutableStateOf("2") }
    var colsA by remember { mutableStateOf("2") }
    // Matrix B dimensions
    var rowsB by remember { mutableStateOf("2") }
    var colsB by remember { mutableStateOf("2") }

    val rA = rowsA.toIntOrNull() ?: 2
    val cA = colsA.toIntOrNull() ?: 2
    val rB = rowsB.toIntOrNull() ?: 2
    val cB = colsB.toIntOrNull() ?: 2

    var matrixA by remember { mutableStateOf(List(rA * cA) { "" }) }
    var matrixB by remember { mutableStateOf(List(rB * cB) { "" }) }
    var result by remember { mutableStateOf<List<String>>(emptyList()) }
    var resultRows by remember { mutableIntStateOf(0) }
    var resultCols by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    var expanded by remember { mutableStateOf(false) }
    val operations = listOf("Add", "Subtract", "Multiply", "Divide")
    var selectedOperation by remember { mutableStateOf(operations[0]) }

    // Reset matrices when dimensions change
    LaunchedEffect(rA, cA) { matrixA = List(rA * cA) { "" } }
    LaunchedEffect(rB, cB) { matrixB = List(rB * cB) { "" } }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            Text(
                "Matrix Calculator",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            // Operation Dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                OutlinedTextField(
                    value = selectedOperation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Operation") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    operations.forEach { op ->
                        DropdownMenuItem(
                            text = { Text(op) },
                            onClick = {
                                selectedOperation = op
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Matrix A
            Text("Matrix A", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = rowsA,
                    onValueChange = { if (it.isNotEmpty() && it.toIntOrNull() != null) rowsA = it },
                    label = { Text("Rows") },
                    modifier = Modifier.width(90.dp).weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(8.dp)
                )
                Text(" × ", modifier = Modifier.padding(horizontal = 8.dp))
                OutlinedTextField(
                    value = colsA,
                    onValueChange = { if (it.isNotEmpty() && it.toIntOrNull() != null) colsA = it },
                    label = { Text("Columns") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Card(
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    MatrixInput(matrixA, rA, cA) { matrixA = it }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Matrix B
            Text("Matrix B", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = rowsB,
                    onValueChange = { if (it.isNotEmpty() && it.toIntOrNull() != null) rowsB = it },
                    label = { Text("Rows") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(8.dp)
                )
                Text(" × ", modifier = Modifier.padding(horizontal = 8.dp))
                OutlinedTextField(
                    value = colsB,
                    onValueChange = { if (it.isNotEmpty() && it.toIntOrNull() != null) colsB = it },
                    label = { Text("Columns") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Card(
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(modifier = Modifier.padding(6.dp)) {
                    MatrixInput(matrixB, rB, cB) { matrixB = it }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Calculate Button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val a = matrixA.map { it.toDoubleOrNull() ?: 0.0 }.toDoubleArray()
                    val b = matrixB.map { it.toDoubleOrNull() ?: 0.0 }.toDoubleArray()
                    try {
                        when (selectedOperation) {
                            "Add" -> {
                                if (rA != rB || cA != cB) throw Exception("Dimensions must match for addition")
                                val res = MatrixOps.addMatrices(a, b, rA, cA)
                                result = res.map { "%.2f".format(it) }
                                resultRows = rA
                                resultCols = cA
                            }
                            "Subtract" -> {
                                if (rA != rB || cA != cB) throw Exception("Dimensions must match for subtraction")
                                val res = MatrixOps.subtractMatrices(a, b, rA, cA)
                                result = res.map { "%.2f".format(it) }
                                resultRows = rA
                                resultCols = cA
                            }
                            "Multiply" -> {
                                if (cA != rB) throw Exception("A columns must equal B rows for multiplication")
                                val res = MatrixOps.dotProductMatrices(a, b, rA, cA, rB, cB)
                                if (res.isEmpty()) throw Exception("Multiplication failed")
                                result = res.map { "%.2f".format(it) }
                                resultRows = rA
                                resultCols = cB
                            }
                            "Divide" -> {
                                if (cA != rB) throw Exception("A columns must equal B rows for division (A × B⁻¹)")
                                if (rB != cB) throw Exception("Matrix B must be square for inversion")
                                val res = MatrixOps.divideMatrices(a, b, rA, cA, rB, cB)
                                if (res.isEmpty()) throw Exception("Matrix B is not invertible")
                                result = res.map { "%.2f".format(it) }
                                resultRows = rA
                                resultCols = cB
                            }
                        }
                        error = null
                    } catch (e: Exception) {
                        error = e.message
                        result = emptyList()
                    }
                }
            ) {
                Text(
                    "Calculate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),

                ) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (result.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Result Matrix", style = MaterialTheme.typography.titleMedium)
                Card(
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        MatrixOutput(result, resultRows, resultCols)
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixInput(values: List<String>, rows: Int, cols: Int, onChange: (List<String>) -> Unit) {
    Column(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            )  {
                for (j in 0 until cols) {
                    val index = i * cols + j
                    OutlinedTextField(
                        value = values.getOrElse(index) { "" },
                        onValueChange = {
                            val updated = values.toMutableList()
                            if (index < updated.size) updated[index] = it
                            onChange(updated)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .height(54.dp)
                            .width(100.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                        ),
                        shape = RoundedCornerShape(6.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
    }
}

@Composable
fun MatrixOutput(values: List<String>, rows: Int, cols: Int) {
    Column (
//        modifier = Modifier.horizontalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (j in 0 until cols) {
                    val index = i * cols + j
                    OutlinedTextField(
                        value = values.getOrElse(index) { "0" },
                        onValueChange = {},
                        readOnly = true,
                        enabled = true,
                        modifier = Modifier
                            .width(100.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        shape = RoundedCornerShape(6.dp),
                    )
                }
            }
        }
    }
}