package com.project.internshiptracking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

data class Intern(
    val id: Int,
    val name: String,
    val school: String,
    val startDate: String,
    val endDate: String,
    val salary: String,
    val instructor: String
)

const val SESSION_TIMEOUT_MS = 1 * 60 * 1000L

val PrimaryBlue = Color(0xFF2D4A6D)
val FieldBackground = Color(0xE6FFFFFF)
val TitleTextColor = Color(0xFF1F2D3D)

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = PrimaryBlue.copy(alpha = 0.5f),
    focusedContainerColor = FieldBackground,
    unfocusedContainerColor = FieldBackground,
    focusedLabelColor = PrimaryBlue,
    unfocusedLabelColor = PrimaryBlue.copy(alpha = 0.7f),
    cursorColor = PrimaryBlue,
    focusedTextColor = TitleTextColor,
    unfocusedTextColor = TitleTextColor
)

@Composable
fun appButtonColors() =
    ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White)

val AppButtonShape = RoundedCornerShape(10.dp)

fun isValidName(text: String): Boolean {
    if (text.isBlank()) return false
    return text.all { it.isLetter() || it == ' ' }
}

fun isValidDate(text: String): Boolean {
    val parts = text.split("/")
    if (parts.size != 3) return false
    if (parts[0].length != 2 || parts[1].length != 2 || parts[2].length != 4) return false
    val day = parts[0].toIntOrNull() ?: return false
    val month = parts[1].toIntOrNull() ?: return false
    val year = parts[2].toIntOrNull() ?: return false
    if (day !in 1..31) return false
    if (month !in 1..12) return false
    if (year !in 1900..2100) return false
    return true
}

fun isValidSalary(text: String): Boolean {
    if (text.isBlank()) return true
    return text.all { it.isDigit() }
}

fun dateToComparableValue(text: String): Int {
    val parts = text.split("/")
    val day = parts[0].toInt()
    val month = parts[1].toInt()
    val year = parts[2].toInt()
    return year * 10000 + month * 100 + day
}

fun isEndDateValid(startDate: String, endDate: String): Boolean {
    return dateToComparableValue(endDate) >= dateToComparableValue(startDate)
}

fun isDateBeforeToday(text: String): Boolean {
    val today = Calendar.getInstance()
    val todayValue =
        today.get(Calendar.YEAR) * 10000 + (today.get(Calendar.MONTH) + 1) * 100 + today.get(
            Calendar.DAY_OF_MONTH
        )
    return dateToComparableValue(text) < todayValue
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("login") }
    var loggedInUsername by remember { mutableStateOf("") }
    var selectedIntern by remember { mutableStateOf<Intern?>(null) }
    var lastActionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    fun logout() {
        currentScreen = "login"
        loggedInUsername = ""
        selectedIntern = null
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val isProtectedScreen = currentScreen != "login" && currentScreen != "register"
            if (isProtectedScreen && System.currentTimeMillis() - lastActionTime > SESSION_TIMEOUT_MS) {
                logout()
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    lastActionTime = System.currentTimeMillis()
                }
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        when (currentScreen) {
            "login" -> {
                LoginScreen(
                    onLoginClick = { enteredUsername ->
                        loggedInUsername = enteredUsername
                        currentScreen = "welcome"
                    },
                    onRegisterClick = { currentScreen = "register" }
                )
            }

            "register" -> {
                RegisterScreen(
                    onRegisterClick = { enteredFullName ->
                        loggedInUsername = enteredFullName
                        currentScreen = "welcome"
                    },
                    onBackClick = { currentScreen = "login" }
                )
            }

            "welcome" -> {
                WelcomeScreen(
                    username = loggedInUsername,
                    onContinueClick = { currentScreen = "main" }
                )
            }

            "main" -> {
                MainScreen(
                    onInternClick = { intern ->
                        selectedIntern = intern
                        currentScreen = "detail"
                    },
                    onAddClick = { currentScreen = "add" },
                    onLogoutClick = { logout() }
                )
            }

            "add" -> {
                AddScreen(
                    onSaveClick = { currentScreen = "main" },
                    onCancelClick = { currentScreen = "main" }
                )
            }

            "detail" -> {
                selectedIntern?.let { intern ->
                    DetailScreen(
                        intern = intern,
                        onBackClick = { currentScreen = "main" },
                        onEditClick = { currentScreen = "edit" }
                    )
                }
            }

            "edit" -> {
                selectedIntern?.let { intern ->
                    EditScreen(
                        intern = intern,
                        onSaveClick = { updatedIntern ->
                            selectedIntern = updatedIntern
                            currentScreen = "detail"
                        },
                        onCancelClick = { currentScreen = "detail" }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginClick: (String) -> Unit, onRegisterClick: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginErrorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(72.dp))
            Text(
                text = "Internship Tracker",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TitleTextColor
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                colors = appTextFieldColors(),
                shape = AppButtonShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                colors = appTextFieldColors(),
                shape = AppButtonShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (fullName.isBlank() || password.isBlank()) {
                        loginErrorMessage = "Please fill in both full name and password"
                    } else if (!dbHelper.checkUser(fullName.trim(), password)) {
                        loginErrorMessage = "Incorrect full name or password"
                    } else {
                        loginErrorMessage = ""
                        onLoginClick(fullName.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = appButtonColors(),
                shape = AppButtonShape
            ) {
                Text("Login")
            }
            if (loginErrorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(loginErrorMessage, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onRegisterClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = appButtonColors(),
                shape = AppButtonShape
            ) {
                Text("Register")
            }
        }
    }
}

@Composable
fun RegisterScreen(onRegisterClick: (String) -> Unit, onBackClick: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var registerErrorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(72.dp))
            Text(
                text = "Create Account",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TitleTextColor
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                colors = appTextFieldColors(),
                shape = AppButtonShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                colors = appTextFieldColors(),
                shape = AppButtonShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (fullName.isBlank() || password.isBlank()) {
                        registerErrorMessage = "Please fill in both full name and password"
                    } else if (!isValidName(fullName)) {
                        registerErrorMessage = "Full name must contain only letters"
                    } else {
                        dbHelper.insertUser(fullName.trim(), password)
                        registerErrorMessage = ""
                        onRegisterClick(fullName.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = appButtonColors(),
                shape = AppButtonShape
            ) {
                Text("Register")
            }
            if (registerErrorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(registerErrorMessage, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onBackClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = appButtonColors(),
                shape = AppButtonShape
            ) {
                Text("Back")
            }
        }
    }
}

const val WELCOME_SCREEN_DELAY_MS = 2000L

@Composable
fun WelcomeScreen(username: String, onContinueClick: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(WELCOME_SCREEN_DELAY_MS)
        onContinueClick()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, $username!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TitleTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MainScreen(onInternClick: (Intern) -> Unit, onAddClick: () -> Unit, onLogoutClick: () -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var internList by remember { mutableStateOf(dbHelper.getAllInterns()) }
    val activeInterns = internList.filter { !isDateBeforeToday(it.endDate) }
    val finishedInterns = internList.filter { isDateBeforeToday(it.endDate) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Interns",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TitleTextColor
            )
            Button(
                onClick = { onLogoutClick() },
                colors = appButtonColors(),
                shape = AppButtonShape
            ) {
                Text("Logout")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onAddClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = appButtonColors(),
            shape = AppButtonShape
        ) {
            Text("Add Intern")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Text(
                    text = "Active",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleTextColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(activeInterns, key = { it.id }) { intern ->
                InternRow(
                    intern = intern,
                    onDetailsClick = { onInternClick(intern) },
                    onDeleteClick = {
                        dbHelper.deleteIntern(intern.id)
                        internList = dbHelper.getAllInterns()
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Finished",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleTextColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(finishedInterns, key = { it.id }) { intern ->
                InternRow(
                    intern = intern,
                    onDetailsClick = { onInternClick(intern) },
                    onDeleteClick = {
                        dbHelper.deleteIntern(intern.id)
                        internList = dbHelper.getAllInterns()
                    }
                )
            }
        }
    }
}

@Composable
fun InternRow(intern: Intern, onDetailsClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(FieldBackground, AppButtonShape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = intern.name, fontWeight = FontWeight.Bold, color = TitleTextColor)
            Text(
                text = "Instructor: ${intern.instructor}",
                fontSize = 12.sp,
                color = TitleTextColor
            )
        }
        Button(onClick = { onDetailsClick() }, colors = appButtonColors(), shape = AppButtonShape) {
            Text("Details")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onDeleteClick() }, colors = appButtonColors(), shape = AppButtonShape) {
            Text("Delete")
        }
    }
}

@Composable
fun AddScreen(onSaveClick: () -> Unit, onCancelClick: () -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var name by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }
    var addErrorMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Add Intern",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TitleTextColor
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Intern name") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = school,
            onValueChange = { school = it },
            label = { Text("School") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start date (dd/mm/yyyy)") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("End date (dd/mm/yyyy)") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = salary,
            onValueChange = { salary = it },
            label = { Text("Salary (optional, numbers only)") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = instructor,
            onValueChange = { instructor = it },
            label = { Text("Instructor") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (name.isBlank() || school.isBlank() || startDate.isBlank() || endDate.isBlank() || instructor.isBlank()) {
                    addErrorMessage = "Please fill in all fields except salary"
                } else if (!isValidName(name)) {
                    addErrorMessage = "Intern name must contain only letters"
                } else if (!isValidName(school)) {
                    addErrorMessage = "School must contain only letters"
                } else if (!isValidDate(startDate)) {
                    addErrorMessage = "Start date must be in dd/mm/yyyy format"
                } else if (!isValidDate(endDate)) {
                    addErrorMessage = "End date must be in dd/mm/yyyy format"
                } else if (!isEndDateValid(startDate, endDate)) {
                    addErrorMessage = "End date must be after start date"
                } else if (!isValidSalary(salary)) {
                    addErrorMessage = "Salary must contain only numbers"
                } else if (!isValidName(instructor)) {
                    addErrorMessage = "Instructor must contain only letters"
                } else {
                    dbHelper.insertIntern(name, school, startDate, endDate, salary, instructor)
                    addErrorMessage = ""
                    onSaveClick()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = appButtonColors(),
            shape = AppButtonShape
        ) {
            Text("Save")
        }
        if (addErrorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(addErrorMessage, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onCancelClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = appButtonColors(),
            shape = AppButtonShape
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun DetailScreen(intern: Intern, onBackClick: () -> Unit, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Intern Details",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TitleTextColor
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Name: ${intern.name}", fontSize = 18.sp, color = TitleTextColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "School: ${intern.school}", fontSize = 18.sp, color = TitleTextColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Start Date: ${intern.startDate}", fontSize = 18.sp, color = TitleTextColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "End Date: ${intern.endDate}", fontSize = 18.sp, color = TitleTextColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Salary: ${if (intern.salary.isBlank()) "-" else intern.salary}",
            fontSize = 18.sp,
            color = TitleTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Instructor: ${intern.instructor}", fontSize = 18.sp, color = TitleTextColor)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onBackClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = appButtonColors(),
            shape = AppButtonShape
        ) {
            Text("Back")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onEditClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = appButtonColors(),
            shape = AppButtonShape
        ) {
            Text("Edit")
        }
    }
}

@Composable
fun EditScreen(intern: Intern, onSaveClick: (Intern) -> Unit, onCancelClick: () -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var name by remember { mutableStateOf(intern.name) }
    var school by remember { mutableStateOf(intern.school) }
    var startDate by remember { mutableStateOf(intern.startDate) }
    var endDate by remember { mutableStateOf(intern.endDate) }
    var salary by remember { mutableStateOf(intern.salary) }
    var instructor by remember { mutableStateOf(intern.instructor) }
    var editErrorMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Edit Intern",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TitleTextColor
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Intern name") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = school,
            onValueChange = { school = it },
            label = { Text("School") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start date (dd/mm/yyyy)") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("End date (dd/mm/yyyy)") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = salary,
            onValueChange = { salary = it },
            label = { Text("Salary (optional, numbers only)") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = instructor,
            onValueChange = { instructor = it },
            label = { Text("Instructor") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            shape = AppButtonShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (name.isBlank() || school.isBlank() || startDate.isBlank() || endDate.isBlank() || instructor.isBlank()) {
                    editErrorMessage = "Please fill in all fields except salary"
                } else if (!isValidName(name)) {
                    editErrorMessage = "Intern name must contain only letters"
                } else if (!isValidName(school)) {
                    editErrorMessage = "School must contain only letters"
                } else if (!isValidDate(startDate)) {
                    editErrorMessage = "Start date must be in dd/mm/yyyy format"
                } else if (!isValidDate(endDate)) {
                    editErrorMessage = "End date must be in dd/mm/yyyy format"
                } else if (!isEndDateValid(startDate, endDate)) {
                    editErrorMessage = "End date must be after start date"
                } else if (!isValidSalary(salary)) {
                    editErrorMessage = "Salary must contain only numbers"
                } else if (!isValidName(instructor)) {
                    editErrorMessage = "Instructor must contain only letters"
                } else {
                    dbHelper.updateIntern(
                        intern.id,
                        name,
                        school,
                        startDate,
                        endDate,
                        salary,
                        instructor
                    )
                    editErrorMessage = ""
                    onSaveClick(
                        Intern(
                            intern.id,
                            name,
                            school,
                            startDate,
                            endDate,
                            salary,
                            instructor
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = appButtonColors(),
            shape = AppButtonShape
        ) {
            Text("Save")
        }
        if (editErrorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(editErrorMessage, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onCancelClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = appButtonColors(),
            shape = AppButtonShape
        ) {
            Text("Cancel")
        }
    }
}