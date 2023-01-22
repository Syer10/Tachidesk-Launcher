package suwayomi.tachidesk.launcher.settings

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job

class LauncherPreference<T>(
    private val key: String,
    private val default: T,
    private val settings: ObservableSettings,
    private val adapter: LauncherAdapter<T>
) {

    fun get(): T {
        return adapter[settings, key, default]
    }

    fun set(value: T) {
        adapter[settings, key] = value
    }

    fun asStateFlow(scope: CoroutineScope): MutableStateFlow<T> {
        val flow = MutableStateFlow(get())
        val listener = adapter.addListener(settings, key, get()) {
            if (it != flow.value) {
                flow.value = it
            }
        }
        scope.coroutineContext.job.invokeOnCompletion {
            listener.deactivate()
        }
        flow.drop(1)
            .onEach { set(it) }
            .launchIn(scope)
        return flow
    }
}
