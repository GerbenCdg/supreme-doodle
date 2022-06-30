package fr.gerben.supreme_doodle

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import fr.gerben.supreme_doodle.ui.theme.AppColors
import fr.gerben.supreme_doodle.ui.theme.SupremeDoodleTheme

class MainActivity : ComponentActivity() {

    lateinit var dataManager: DoodleDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SupremeDoodleTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = AppColors.White) {
                    // Greeting("Android")

                    Row{
                        Column(Modifier.background(AppColors.Purple200)) {

                            Row(Modifier.padding(8.dp)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_undo_white_24dp),
                                    contentDescription = "Undo",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable {
                                            dataManager.undo()
                                        }
                                )
                            }
                            Spacer(modifier = Modifier.width(32.dp))
                            Row(Modifier.padding(8.dp)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_redo_white_24dp),
                                    contentDescription = "Redo",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable {
                                            dataManager.redo()
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(Modifier
                            .background(AppColors.Purple500)) {

                            AndroidView(factory = {
                                getDoodleCanvas(it)
                            })
                        }
                    }

                }
            }
        }
    }


    private fun getDoodleCanvas(context: Context) = DoodleCanvas(context).apply {
        this@MainActivity.dataManager = data
    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SupremeDoodleTheme {
        Greeting("Android")
    }
}


