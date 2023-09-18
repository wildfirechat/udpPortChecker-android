@file:OptIn(ExperimentalMaterial3Api::class)

package cn.wildfirechat.wfchecker

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import cn.wildfirechat.wfchecker.ui.theme.WFCheckerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class MainActivity : ComponentActivity() {
    val TAG = "WF-Checker"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WFCheckerTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "UDP 端口检测") },
//                            colors = TopAppBarDefaults.smallTopAppBarColors(titleContentColor = Color.Red, containerColor = Color.Blue)
                        )
                    },
                    content = { paddingValues ->
                        Log.d(TAG, "Padding values $paddingValues")
                        InputPage()
                    }
                )

            }
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputPage() {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }

    // Define a modifier to add some padding
    val modifier = Modifier

    Column(
        modifier = modifier.padding(10.dp, top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "请输入主机地址和端口",
            modifier.padding(bottom = 20.dp)
        )

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Host", modifier.width(40.dp))

            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Ascii),
                keyboardActions = KeyboardActions(
                    onNext = {
                        // Define action when "Next" is pressed
                    }
                ),
                modifier = modifier
                    .fillMaxWidth()
            )
        }
        Row(
            modifier = modifier.padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Port", modifier.width(40.dp))
            // Input field 2
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Define action when "Done" is pressed
                    }
                ),
                modifier = modifier
                    .fillMaxWidth()
            )

        }
        // Button
        val composableScope = rememberCoroutineScope()
        val context = LocalContext.current
        Button(
            onClick = {
                // Define action when button is clicked
                if (!isValidHost(host)) {
                    Toast.makeText(context, "Host 不合法", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (!isPositiveInteger(port)) {
                    Toast.makeText(context, "Port 不合法", Toast.LENGTH_LONG).show()
                    return@Button
                }

                isChecking = true
                composableScope.launch(context = Dispatchers.IO) {
                    val res = UDPClient(host.trim(), port.trim().toInt()).connectAndSend("hello")
                    var desc = res
                    if (res == null) {
                        desc = "失败"
                    }
                    withContext(Dispatchers.Main) {
                        isChecking = false
                        testResult = res ?: ""
                        Toast.makeText(context, desc, Toast.LENGTH_LONG).show()
                    }
                }

            },
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            enabled = host.isNotEmpty() && port.isNotEmpty() && !isChecking

        ) {
            Text(text = if (isChecking) "测试中" else "测试")
        }

        Text(text = testResult)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WFCheckerTheme {
        InputPage()
    }
}

fun isValidHost(hostname: String): Boolean {
    // 定义主机名的正则表达式
    val pattern = Pattern.compile(
        "^(?=.{1,255}\$)([A-Za-z0-9_-]+\\.)*[A-Za-z0-9][A-Za-z0-9_-]*\\.([A-Za-z]{2,})\$",
        Pattern.CASE_INSENSITIVE
    )

    // 使用正则表达式验证主机名
    val matcher = pattern.matcher(hostname)
    return matcher.matches()
}

fun isPositiveInteger(str: String): Boolean {
    try {
        val number = str.toInt()
        return number > 0
    } catch (e: NumberFormatException) {
        e.printStackTrace()
    }
    return false
}
