package com.colleagues.austrom.database

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseProvider : IDatabaseProvider{
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun writeNewUser(user: User) {
        val reference = database.getReference("users")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the user")
            return
        }
        reference.child(key).setValue(user)
        Log.w("Debug", "New user added to DB with key: $key")
    }

    override fun getUserByUsername(username: String, activity: FragmentActivity?) : User? {
        var user : User? = null
        activity?.lifecycleScope?.launch {
            user = getUserByUsernameAsync(username)
        }
        return user
    }

    private suspend fun getUserByUsernameAsync(username: String) : User? {
        val reference = database.getReference("users")
        val databaseQuery = reference.orderByChild("username").equalTo(username).limitToFirst(1)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                val userList = mutableListOf<User>()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    user?.userID = child.key
                    user?.let { userList.add(it) }
                }
                if (userList.isNotEmpty()) {
                    userList[0]
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun writeNewBudget(budget: Budget) {
        val reference = database.getReference("budgets")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the budget")
            return
        }
        reference.child(key).setValue(budget)
        Log.w("Debug", "New budget added to DB with key: $key")
    }

    override fun writeNewAsset(asset: Asset) {
        val reference = database.getReference("assets")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the asset")
            return
        }
        reference.child(key).setValue(asset)
        Log.w("Debug", "New asset added to DB with key: $key")
    }

    override fun getAssetsOfUser(user: User, activity: FragmentActivity?): MutableList<Asset>? {
        var assets : MutableList<Asset>? = ArrayList()
        activity?.lifecycleScope?.launch {
            assets = getAssetsOfUserAsync(user)
        }
        return assets
    }


    private fun getAssetsOfUserAsync(user: User) : MutableList<Asset>? {
        val reference = database.getReference("assets")
        val databaseQuery = reference.orderByChild("user_id").equalTo(user.userID)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                val assetsList = mutableListOf<Asset>()
                for (child in snapshot.children) {
                    val asset = child.getValue(Asset::class.java)
                    asset?.let { assetsList.add(it) }
                }
                assetsList.ifEmpty {
                    null
                }
            }
            catch (e: Exception) {
                null
            }
        }
    }
}