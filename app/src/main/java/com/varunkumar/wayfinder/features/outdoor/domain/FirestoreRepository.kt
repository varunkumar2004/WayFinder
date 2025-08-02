package com.varunkumar.wayfinder.features.outdoor.domain

import com.google.firebase.firestore.CollectionReference
import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
import com.varunkumar.wayfinder.features.outdoor.data.BuildingCategory
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirestoreRepository(
    private val ref: CollectionReference
) {
    suspend fun getBuildingCategory(): List<BuildingCategory> {
        return try {
            val result = ref.get().await()

            result.documents.mapNotNull {
                BuildingCategory(name = it.id)
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBuildingsByCategory(
        collectionName: String
    ): List<BuildingByCategory> {
        return try {
            val result = ref
                .document(collectionName)
                .collection("${collectionName}_")
                .get()
                .await()

            result.documents.mapNotNull { snap ->
                val name = snap.getString("name")

                name?.let {
                    BuildingByCategory(
                        name = name.capitalize(Locale.ROOT),
                        lat = snap.getDouble("lat") ?: 0.0,
                        long = snap.getDouble("long") ?: 0.0
                    )
                }
            }
        } catch (e:Exception) {
            emptyList()
        }
    }
}