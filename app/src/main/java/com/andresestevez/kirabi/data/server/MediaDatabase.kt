package com.andresestevez.kirabi.data.server

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MediaDatabase {

    companion object {
        private const val MEDIA_COLLECTION = "media"
    }

    private val db = Firebase.firestore

    private val mediaCollection = db.collection(MEDIA_COLLECTION)

    suspend fun getAll(): List<MediaDto> {
        return try {
            mediaCollection.get().await().toObjects(MediaDto::class.java)
        } catch (t: Throwable) {
            emptyList()
        }
    }

}