package com.colleagues.austrom.managers

import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider

class SyncManager(val localDBProvider: LocalDatabaseProvider, val remoteDBProvider: IRemoteDatabaseProvider) {

    fun sync() {

    }

    private fun syncAssetsLocalToRemote() {

    }

    private fun syncAssetsRemoteToLocal() {

    }
}

