package com.andresestevez.kirabi.data.server

import android.support.v4.media.MediaMetadataCompat
import com.andresestevez.kirabi.data.toMediaMetadataCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val MEDIA_COLLECTION = "media"

class MediaDatabase {

    private val db = Firebase.firestore

    private val mediaCollection = db.collection(MEDIA_COLLECTION)

    private suspend fun getAll(): List<MediaDto> = withContext(Dispatchers.IO) {
        try {
            mediaCollection.get().await().toObjects(MediaDto::class.java)
        } catch (t: Throwable) {
            emptyList()
        }
    }

    suspend fun getAllMediaMetadataCompat(): List<MediaMetadataCompat> =
        getAll().map { mediaDto -> mediaDto.toMediaMetadataCompat() }
}