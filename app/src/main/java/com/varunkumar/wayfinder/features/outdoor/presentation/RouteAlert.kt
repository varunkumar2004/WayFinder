package com.varunkumar.wayfinder.features.outdoor.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
import com.varunkumar.wayfinder.features.outdoor.data.BuildingCategory
import com.varunkumar.wayfinder.features.outdoor.data.FirestoreState
import java.util.Locale

@Composable
fun RouteAlert(
    modifier: Modifier = Modifier,
    firestoreState: FirestoreState,
    onDismissRequest: () -> Unit,
    onCategorySelect: (BuildingCategory) -> Unit,
    onDestinationSelect: (BuildingByCategory) -> Unit, // Updated function signature
    currentLocation: LatLng, // New parameter
    mapsApi: String // New parameter
) {
    var selectedDestination by remember {
        mutableStateOf<BuildingByCategory?>(null)
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "Choose Destination"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Text(
                        text = "Categories: ",
                        style = MaterialTheme.typography.bodySmall
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(firestoreState.buildingCategory) { category ->
                            FilterChip(
                                selected = firestoreState.selectedBuildingCategory == category,
                                onClick = {
                                    onCategorySelect(category)
                                },
                                label = { Text(text = category.name.capitalize(Locale.ROOT)) }
                            )
                        }
                    }
                }

                if (firestoreState.selectedBuildingCategory != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Buildings: ",
                            style = MaterialTheme.typography.bodySmall
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            verticalArrangement = Arrangement.spacedBy(0.5.dp)
                        ) {

                            itemsIndexed(firestoreState.buildingByCategory) { index, item ->
                                ListItem(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .clickable { selectedDestination = item },
                                    headlineContent = {
                                        Text(
                                            text = item.name.capitalize(Locale.ROOT),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = if (selectedDestination == item) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.primaryContainer,
                                        headlineColor =
                                            if (selectedDestination == item) MaterialTheme.colorScheme.surface
                                            else MaterialTheme.colorScheme.secondary
                                    )
                                )
                            }
                        }
                    }
                }

            }
        },
        confirmButton = {
            Button(
                enabled = selectedDestination != null,
                onClick = {
                    selectedDestination?.let {
                        // Pass the required parameters to the ViewModel function
                        onDestinationSelect(it)
                        onDismissRequest()
                    }
                }
            ) {
                Text(text = "Start WayFinder")
            }
        }
    )
}