package com.varunkumar.wayfinder.features.outdoor.di

import android.content.Context
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.varunkumar.wayfinder.R
import com.varunkumar.wayfinder.features.outdoor.domain.FirestoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object MapModule {
    @Provides
    @ViewModelScoped
    fun provideFirestoreDatabase(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @ViewModelScoped
    fun provideMapApiKey(@ApplicationContext context: Context) : String {
        return context.getString(R.string.google_maps_key)
    }

    @Provides
    @ViewModelScoped
    fun provideBuildingFirestoreReference(db: FirebaseFirestore): CollectionReference {
        return db.collection("buildings")
    }

    @Provides
    @ViewModelScoped
    fun provideFireStoreRepository(ref: CollectionReference): FirestoreRepository {
        return FirestoreRepository(ref)
    }
}