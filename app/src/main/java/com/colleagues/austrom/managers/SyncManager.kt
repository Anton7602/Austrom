package com.colleagues.austrom.managers

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider

class SyncManager(val localDBProvider: LocalDatabaseProvider, val remoteDBProvider: IRemoteDatabaseProvider) {

    fun sync() {

    }

    private fun syncAssetsLocalToRemote() {

    }

    private fun syncAssetsRemoteToLocal() {

    }

    @Entity(tableName = "change_log")
    data class AssetChangeLog(
        @PrimaryKey(autoGenerate = true) val logId: Long = 0,
        val entityId: Long,
        val action: String,
        val currentVersion: Int
    )
}

