/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.chanse.events.marriage.rich.ui.sessioncommon

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import io.chanse.events.marriage.rich.R
import io.chanse.events.marriage.rich.model.SessionId
import io.chanse.events.marriage.rich.model.userdata.UserSession
import io.chanse.events.marriage.rich.shared.domain.users.StarEventAndNotifyUseCase
import io.chanse.events.marriage.rich.shared.domain.users.StarEventParameter
import io.chanse.events.marriage.rich.shared.result.Event
import io.chanse.events.marriage.rich.ui.SnackbarMessage
import io.chanse.events.marriage.rich.ui.messages.SnackbarMessageManager
import io.chanse.events.marriage.rich.ui.signin.SignInViewModelDelegate
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * A delegate providing common functionality for displaying a list of events and responding to
 * actions performed on them.
 */
interface EventActionsViewModelDelegate : EventActions {
    val navigateToEventAction: LiveData<Event<SessionId>>
    val navigateToSignInDialogAction: LiveData<Event<Unit>>
    val snackBarMessage: LiveData<Event<SnackbarMessage>>
}

class DefaultEventActionsViewModelDelegate @Inject constructor(
    signInViewModelDelegate: SignInViewModelDelegate,
    private val starEventUseCase: StarEventAndNotifyUseCase,
    private val snackbarMessageManager: SnackbarMessageManager
) : EventActionsViewModelDelegate, SignInViewModelDelegate by signInViewModelDelegate {

    private val _navigateToEventAction = MutableLiveData<Event<SessionId>>()
    override val navigateToEventAction: LiveData<Event<SessionId>>
        get() = _navigateToEventAction

    private val _navigateToSignInDialogAction = MutableLiveData<Event<Unit>>()
    override val navigateToSignInDialogAction: LiveData<Event<Unit>>
        get() = _navigateToSignInDialogAction

    private val _snackBarMessage = MediatorLiveData<Event<SnackbarMessage>>()
    override val snackBarMessage: LiveData<Event<SnackbarMessage>>
        get() = _snackBarMessage

    override fun openEventDetail(id: SessionId) {
        _navigateToEventAction.value = Event(id)
    }

    override fun onStarClicked(userSession: UserSession) {
        if (!isSignedIn()) {
            Timber.d("Showing Sign-in dialog after star click")
            _navigateToSignInDialogAction.value = Event(Unit)
            return
        }
        val newIsStarredState = !userSession.userEvent.isStarred

        // Update the snackbar message optimistically.
        val stringResId = if (newIsStarredState) {
            R.string.event_starred
        } else {
            R.string.event_unstarred
        }
        snackbarMessageManager.addMessage(
            SnackbarMessage(
                messageId = stringResId,
                actionId = R.string.dont_show,
                requestChangeId = UUID.randomUUID().toString()
            )
        )

        getUserId()?.let {
            starEventUseCase.execute(
                StarEventParameter(
                    it, userSession.copy(
                        userEvent = userSession.userEvent.copy(isStarred = newIsStarredState))
                )
            )
        }
    }
}