package org.samarthya.collect.android.utilities

import org.samarthya.collect.shared.locks.ChangeLock
import org.samarthya.collect.shared.locks.ReentrantLockChangeLock
import javax.inject.Singleton

@Singleton
class ChangeLockProvider {

    private val locks: MutableMap<String, ChangeLock> = mutableMapOf()

    fun getFormLock(projectId: String): ChangeLock {
        return locks.getOrPut("form:$projectId") { ReentrantLockChangeLock() }
    }

    fun getInstanceLock(projectId: String): ChangeLock {
        return locks.getOrPut("instance:$projectId") { ReentrantLockChangeLock() }
    }
}
