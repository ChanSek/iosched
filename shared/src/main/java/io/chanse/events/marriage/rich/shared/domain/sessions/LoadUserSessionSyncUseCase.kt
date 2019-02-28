/*
 * Copyright 2018 Google LLC
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

package io.chanse.events.marriage.rich.shared.domain.sessions

import io.chanse.events.marriage.rich.model.SessionId
import io.chanse.events.marriage.rich.model.userdata.UserSession
import io.chanse.events.marriage.rich.shared.data.userevent.DefaultSessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.domain.UseCase
import javax.inject.Inject

/**
 * A [UseCase] that returns the [UserSession]s for a user.
 */
class LoadUserSessionSyncUseCase @Inject constructor(
        private val userEventRepository: DefaultSessionAndUserEventRepository
) : UseCase<Pair<String, SessionId>, UserSession>() {

    override fun execute(parameters: Pair<String, SessionId>): UserSession {
        val (userId, eventId) = parameters

        return userEventRepository.getUserSession(userId, eventId)
    }
}