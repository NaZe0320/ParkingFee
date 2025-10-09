package com.naze.parkingfee.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 하단 네비게이션 바 컴포넌트
 * React 디자인의 5개 탭 구조를 구현합니다.
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NavigationItem(
            route = "home",
            icon = Icons.Default.LocationOn,
            label = "홈",
            selectedIcon = Icons.Default.LocationOn
        ),
        NavigationItem(
            route = "add_parking_lot",
            icon = Icons.Default.Add,
            label = "추가",
            selectedIcon = Icons.Default.Add
        ),
        NavigationItem(
            route = "vehicles/list",
            icon = Icons.Filled.DirectionsCar,
            label = "차량",
            selectedIcon = Icons.Filled.DirectionsCar
        ),
        NavigationItem(
            route = "history",
            icon = Icons.Filled.History,
            label = "기록",
            selectedIcon = Icons.Filled.History
        ),
        NavigationItem(
            route = "settings",
            icon = Icons.Default.Settings,
            label = "설정",
            selectedIcon = Icons.Default.Settings
        )
    )

    Surface(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationItems.forEach { item ->
                val isSelected = currentRoute == item.route || 
                               (item.route == "home" && currentRoute == "home") ||
                               (item.route == "add_parking_lot" && currentRoute.startsWith("add_parking_lot")) ||
                               (item.route == "vehicles/list" && currentRoute.startsWith("vehicles/")) ||
                               (item.route == "history" && currentRoute == "history") ||
                               (item.route == "settings" && currentRoute == "settings")

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else androidx.compose.ui.graphics.Color.Transparent
                        )
                        .clickable { onNavigate(item.route) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * 네비게이션 아이템 데이터 클래스
 */
data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val selectedIcon: ImageVector
)
