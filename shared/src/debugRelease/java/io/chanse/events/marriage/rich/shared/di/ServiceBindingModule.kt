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

package io.chanse.events.marriage.rich.shared.di

import io.chanse.events.marriage.rich.shared.data.job.ConferenceDataService
import io.chanse.events.marriage.rich.shared.fcm.AdsschedFirebaseInstanceIDService
import io.chanse.events.marriage.rich.shared.fcm.AdsschedFirebaseMessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * This is the debug/release version of ServiceBindingModule.
 */
@Module
abstract class ServiceBindingModule {
    @ServiceScoped
    @ContributesAndroidInjector
    internal abstract fun adsschedMessagingService(): AdsschedFirebaseMessagingService

    @ServiceScoped
    @ContributesAndroidInjector
    internal abstract fun provideFirebaseInstanceIDService(): AdsschedFirebaseInstanceIDService

    @ServiceScoped
    @ContributesAndroidInjector
    internal abstract fun provideConferenceDataService(): ConferenceDataService
}