package dev.lbeernaert.youhavemail.screens

import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.lbeernaert.youhavemail.Log
import dev.lbeernaert.youhavemail.R
import dev.lbeernaert.youhavemail.ServiceException
import dev.lbeernaert.youhavemail.ServiceView
import dev.lbeernaert.youhavemail.components.ActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun Login(serviceView: ServiceView, navController: NavController, backendIndex: Int) {
    val backend = serviceView.getBackends()[backendIndex]
    val service = serviceView.getService()!!

    var email = remember { mutableStateOf(TextFieldValue()) }
    val password = remember { mutableStateOf(TextFieldValue()) }
    val openDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    val onLoginClicked: () -> Unit = {
        openDialog.value = true;
        var account = service.newAccount(backend, email.value.text)
        serviceView.setInLoginAccount(account)

        coroutineScope.launch {
            val exception: ServiceException? = withContext(Dispatchers.IO) {
                var exception: ServiceException? = null
                try {
                    account.login(password.value.text)
                } catch (e: ServiceException) {
                    exception = e
                } finally {
                    openDialog.value = false
                }
                exception
            }

            when (exception) {
                null -> {
                    if (account.isAwaitingTotp()) {
                        navController.navigate(Routes.TOTP.route)
                    } else {
                        try {
                            service.addAccount(account)
                        } catch (e: ServiceException) {
                            Log.e(e.toString())
                        } finally {
                            serviceView.clearInLoginAccount()
                        }

                        serviceView.requiresAccountRefresh()
                        navController.popBackStack(Routes.Main.route, false)
                    }
                }
                else -> {
                    Log.e(exception.toString())
                }
            }
        }
    }


    if (openDialog.value) {
        BackgroundTask(
            text = stringResource(
                R.string.login_to_account,
                email.value.text
            )
        )
    } else {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(text = stringResource(id = R.string.add_account_title))
            },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                })
        }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                Text(text = stringResource(R.string.login_to_account, backend.name()))

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Email") },
                    singleLine = true,
                    value = email.value,
                    onValueChange = { email.value = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Password") },
                    value = password.value,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = { password.value = it },
                    keyboardActions = KeyboardActions(onDone = {
                        onLoginClicked()
                    })
                )

                Spacer(modifier = Modifier.height(20.dp))

                ActionButton(text = stringResource(id = R.string.login), onLoginClicked)
            }
        }
    }
}