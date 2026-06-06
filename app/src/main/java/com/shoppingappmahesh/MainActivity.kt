package com.shoppingappmahesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shoppingappmahesh.data.util.FirebaseDataSeeder
import com.shoppingappmahesh.ui.navigation.NavGraph
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.theme.ShoppingAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var dataSeeder: FirebaseDataSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Seed initial data
        dataSeeder.seedData()

        enableEdgeToEdge()
        setContent {
            ShoppingAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val showBottomBar = when (currentDestination?.route) {
                    Screen.Home.route, Screen.Search.route, Screen.Cart.route, 
                    Screen.Profile.route, Screen.OrderHistory.route -> true
                    else -> false
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            LuxFloatingBottomBar(
                                currentRoute = currentDestination?.route,
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun LuxFloatingBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(70.dp),
            shape = RoundedCornerShape(35.dp),
            color = Color.Black,
            shadowElevation = 15.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LuxNavItem(
                    icon = Icons.Outlined.Home,
                    label = "Home",
                    selected = currentRoute == Screen.Home.route,
                    onClick = { onNavigate(Screen.Home) }
                )
                LuxNavItem(
                    icon = Icons.Outlined.Search,
                    label = "Search",
                    selected = currentRoute == Screen.Search.route,
                    onClick = { onNavigate(Screen.Search) }
                )
                LuxNavItem(
                    icon = Icons.Outlined.LocalMall,
                    label = "Cart",
                    selected = currentRoute == Screen.Cart.route,
                    onClick = { onNavigate(Screen.Cart) }
                )
                LuxNavItem(
                    icon = Icons.Default.History,
                    label = "History",
                    selected = currentRoute == Screen.OrderHistory.route,
                    onClick = { onNavigate(Screen.OrderHistory) }
                )
                LuxNavItem(
                    icon = Icons.Outlined.Person,
                    label = "Profile",
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { onNavigate(Screen.Profile) }
                )
            }
        }
    }
}

@Composable
fun LuxNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = if (selected) Color.White else Color.Transparent,
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}