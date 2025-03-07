package com.example.cantona

import android.graphics.drawable.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

class NavigationTools {
    @Composable
    fun NavigationBar(viewModel: CantonaViewModel, navController: NavController, modifier: Modifier = Modifier){
        @Composable
        fun NavItem(iconRes: Int, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
            Column(modifier=modifier.clickable{onClick()}, horizontalAlignment = Alignment.CenterHorizontally)
            {
                IconButton(onClick = {}) {
                    Icon(painterResource(iconRes), title)
                    Text(title)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()){
            NavItem(R.drawable.baseline_album_24, "Albums", {})
            NavItem(R.drawable.baseline_search_24, "Search", {})
        }
    }
}