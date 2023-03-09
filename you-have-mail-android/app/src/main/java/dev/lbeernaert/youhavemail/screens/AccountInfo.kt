package dev.lbeernaert.youhavemail.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.lbeernaert.youhavemail.ObserverAccountState
import dev.lbeernaert.youhavemail.R
import dev.lbeernaert.youhavemail.components.ActionButton


@Composable
fun AccountInfo(
    accountEmail: String,
    backendName: String,
    accountState: ObserverAccountState,
    onBackClicked: () -> Unit,
    onLogout: suspend () -> Unit,
    onLogin: () -> Unit,
    onDelete: suspend () -> Unit,
) {
    val accountState = remember { mutableStateOf(accountState) }

    AsyncScreen(
        title = stringResource(id = R.string.account_title),
        onBackClicked = onBackClicked
    ) { padding, runTask ->

        val logOutBackgroundLabel = stringResource(id = R.string.logging_out)
        val onLogoutImpl: () -> Unit = {
            runTask(logOutBackgroundLabel) {
                onLogout()
            }
        }

        val onDeleteImpl: () -> Unit = {
            runTask(logOutBackgroundLabel) {
                onDelete()
            }
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {

            Text(
                text = accountEmail,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.h2,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = backendName,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            val statusString = when (accountState.value) {
                ObserverAccountState.OFFLINE -> stringResource(id = R.string.status_offline)
                ObserverAccountState.LOGGED_OUT -> stringResource(id = R.string.status_logged_out)
                ObserverAccountState.ONLINE -> stringResource(id = R.string.status_online)
            }
            Text(
                text = stringResource(id = R.string.status, statusString),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (accountState.value == ObserverAccountState.LOGGED_OUT) {
                ActionButton(
                    text = stringResource(id = R.string.login),
                    onClick = onLogin
                )
            } else {
                ActionButton(
                    text = stringResource(id = R.string.logout),
                    onClick = onLogoutImpl,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ActionButton(text = stringResource(id = R.string.delete_account), onDeleteImpl)
        }

    }
}