package com.colleagues.austrom.database

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

    override fun getUserByUsername(username: String, activity: AppCompatActivity?) : User? {
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


//        val user = databaseQuery.get()//.await().getValue<User>()
//        databaseQuery.get().addOnSuccessListener { dataSnapshot ->
//            if (dataSnapshot.exists()) {
//                val userList = dataSnapshot.children.mapNotNull { it.getValue(User::class.java) }
//                userList.forEach { user ->
//                    if (user.username == username) {
//
//                    }
//                }
//            } else {
//
//            }
//        }.addOnFailureListener { error ->
//        }
        return null
    }
}