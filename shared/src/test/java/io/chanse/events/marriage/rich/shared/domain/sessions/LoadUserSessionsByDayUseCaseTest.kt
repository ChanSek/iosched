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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import io.chanse.events.marriage.rich.androidtest.util.LiveDataTestUtil
import io.chanse.events.marriage.rich.model.Session
import io.chanse.events.marriage.rich.model.SessionId
import io.chanse.events.marriage.rich.shared.data.session.DefaultSessionRepository
import io.chanse.events.marriage.rich.shared.data.session.SessionRepository
import io.chanse.events.marriage.rich.shared.data.userevent.DefaultSessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.data.userevent.UserEventMessage
import io.chanse.events.marriage.rich.shared.data.userevent.UserEventMessageChangeType
import io.chanse.events.marriage.rich.shared.data.userevent.UserEventsResult
import io.chanse.events.marriage.rich.shared.domain.repository.TestUserEventDataSource
import io.chanse.events.marriage.rich.shared.model.TestDataRepository
import io.chanse.events.marriage.rich.shared.result.Result
import io.chanse.events.marriage.rich.shared.schedule.UserSessionMatcher
import io.chanse.events.marriage.rich.shared.util.SyncExecutorRule
import io.chanse.events.marriage.rich.shared.util.TimeUtils.ConferenceDays
import io.chanse.events.marriage.rich.test.data.TestData
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [LoadUserSessionsByDayUseCase]
 */
class LoadUserSessionsByDayUseCaseTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule var syncExecutorRule = SyncExecutorRule()

    @Test
    fun returnsMapOfSessions() {

        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()

        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(userEventsResult),
            DefaultSessionRepository(TestDataRepository)
        )
        val useCase = LoadUserSessionsByDayUseCase(testUserEventRepository)

        val resultLiveData = useCase.observe()

        useCase.execute(LoadUserSessionsByDayUseCaseParameters(UserSessionMatcher(), "user1"))

        val result = LiveDataTestUtil.getValue(resultLiveData)
            as Result.Success<LoadUserSessionsByDayUseCaseResult>

        assertThat(
            TestData.userSessionMap,
            `is`(equalTo(result.data.userSessionsPerDay))
        )
    }

    @Test
    fun userEventsMessage() {

        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()

        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(userEventsResult),
            DefaultSessionRepository(TestDataRepository)
        )
        val useCase = LoadUserSessionsByDayUseCase(testUserEventRepository)

        val resultLiveData = useCase.observe()

        useCase.execute(LoadUserSessionsByDayUseCaseParameters(UserSessionMatcher(), "user1"))

        userEventsResult.postValue(
            UserEventsResult(
                userEventsMessage = UserEventMessage(
                    UserEventMessageChangeType.CHANGES_IN_RESERVATIONS
                ),
                userEvents = TestData.userEvents
            )
        )

        val result = LiveDataTestUtil.getValue(resultLiveData)
            as Result.Success<LoadUserSessionsByDayUseCaseResult>

        assertThat(
            TestData.userSessionMap,
            `is`(equalTo(result.data.userSessionsPerDay))
        )

        assertThat(
            UserEventMessage(UserEventMessageChangeType.CHANGES_IN_RESERVATIONS),
            `is`(equalTo(result.data.userMessage))
        )
    }

    @Test
    fun errorCase() {

        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()

        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(userEventsResult),
            FailingSessionRepository
        )

        val useCase = LoadUserSessionsByDayUseCase(testUserEventRepository)

        val resultLiveData = useCase.observe()

        useCase.execute(LoadUserSessionsByDayUseCaseParameters(UserSessionMatcher(), "user1"))

        val result = LiveDataTestUtil.getValue(resultLiveData)

        assertThat(result, `is`(instanceOf(Result.Error::class.java)))
    }

    @Test
    fun returnsCurrentEventIndex() {
        // Given the use case
        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()

        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(userEventsResult),
            DefaultSessionRepository(TestDataRepository)
        )
        val useCase = LoadUserSessionsByDayUseCase(testUserEventRepository)
        val resultLiveData = useCase.observe()

        // When we execute it, passing Day 2 +3hrs as the current time
        val now = ConferenceDays.first().start.plusHours(3L)
        useCase.execute(LoadUserSessionsByDayUseCaseParameters(UserSessionMatcher(), "user1", now))

        // Then the expected indexes are returned
        val result = LiveDataTestUtil.getValue(resultLiveData)
            as Result.Success<LoadUserSessionsByDayUseCaseResult>
        assertThat(
            EventLocation(0, 0),
            `is`(equalTo(result.data.firstUnfinishedSession))
        )
    }

    @Test
    fun midConference_afterDayEnd_returnsCurrentEventIndex() {
        // Given the use case
        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()

        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(userEventsResult),
            DefaultSessionRepository(TestDataRepository)
        )
        val useCase = LoadUserSessionsByDayUseCase(testUserEventRepository)
        val resultLiveData = useCase.observe()

        // When we execute it, passing Day 2 *after the end of day*
        val now = ConferenceDays[1].end.plusHours(3L)
        useCase.execute(LoadUserSessionsByDayUseCaseParameters(UserSessionMatcher(), "user1", now))

        // Then returns the index of the first session the next day
        val result = LiveDataTestUtil.getValue(resultLiveData)
            as Result.Success<LoadUserSessionsByDayUseCaseResult>
        assertThat(
            EventLocation(2, 0),
            `is`(equalTo(result.data.firstUnfinishedSession))
        )
    }

    @Test
    fun beforeConference_returnsNoCurrentEventIndex() {
        // Given the use case
        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()

        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(userEventsResult),
            DefaultSessionRepository(TestDataRepository)
        )
        val useCase = LoadUserSessionsByDayUseCase(testUserEventRepository)
        val resultLiveData = useCase.observe()

        // When we execute it, passing a current time *before* the conference
        val now = ConferenceDays.first().start.minusDays(2L)
        useCase.execute(LoadUserSessionsByDayUseCaseParameters(UserSessionMatcher(), "user1", now))

        // Then the expected indexes are returned
        val result = LiveDataTestUtil.getValue(resultLiveData)
            as Result.Success<LoadUserSessionsByDayUseCaseResult>
        assertThat(
            null,
            `is`(equalTo(result.data.firstUnfinishedSession))
        )
    }

    @Test
    fun afterConference_returnsNoCurrentEventIndex() {
        // Given the use case
        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()

        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(userEventsResult),
            DefaultSessionRepository(TestDataRepository)
        )
        val useCase = LoadUserSessionsByDayUseCase(testUserEventRepository)
        val resultLiveData = useCase.observe()

        // When we execute it, passing a current time *after* the conference
        val now = ConferenceDays.last().end.plusHours(2L)
        useCase.execute(LoadUserSessionsByDayUseCaseParameters(UserSessionMatcher(), "user1", now))

        // Then the expected indexes are returned
        val result = LiveDataTestUtil.getValue(resultLiveData)
            as Result.Success<LoadUserSessionsByDayUseCaseResult>
        assertThat(
            null,
            `is`(equalTo(result.data.firstUnfinishedSession))
        )
    }
}

object FailingSessionRepository : SessionRepository {
    override fun getSessions(): List<Session> {
        throw Exception("test")
    }

    override fun getSession(eventId: SessionId): Session {
        throw Exception("test")
    }
}
