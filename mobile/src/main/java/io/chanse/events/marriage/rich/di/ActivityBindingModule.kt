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
package io.chanse.events.marriage.rich.di

import io.chanse.events.marriage.rich.shared.di.ActivityScoped
import io.chanse.events.marriage.rich.ui.LaunchModule
import io.chanse.events.marriage.rich.ui.LauncherActivity
import io.chanse.events.marriage.rich.ui.MainActivity
import io.chanse.events.marriage.rich.ui.info.InfoModule
import io.chanse.events.marriage.rich.ui.map.MapActivity
import io.chanse.events.marriage.rich.ui.map.MapModule
import io.chanse.events.marriage.rich.ui.onboarding.OnboardingActivity
import io.chanse.events.marriage.rich.ui.onboarding.OnboardingModule
import io.chanse.events.marriage.rich.ui.prefs.PreferenceModule
import io.chanse.events.marriage.rich.ui.reservation.ReservationModule
import io.chanse.events.marriage.rich.ui.schedule.ScheduleModule
import io.chanse.events.marriage.rich.ui.sessioncommon.EventActionsViewModelDelegateModule
import io.chanse.events.marriage.rich.ui.sessiondetail.SessionDetailActivity
import io.chanse.events.marriage.rich.ui.sessiondetail.SessionDetailModule
import io.chanse.events.marriage.rich.ui.signin.SignInDialogModule
import io.chanse.events.marriage.rich.ui.speaker.SpeakerActivity
import io.chanse.events.marriage.rich.ui.speaker.SpeakerModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * We want Dagger.Android to create a Subcomponent which has a parent Component of whichever module
 * ActivityBindingModule is on, in our case that will be [AppComponent]. You never
 * need to tell [AppComponent] that it is going to have all these subcomponents
 * nor do you need to tell these subcomponents that [AppComponent] exists.
 * We are also telling Dagger.Android that this generated SubComponent needs to include the
 * specified modules and be aware of a scope annotation [@ActivityScoped].
 * When Dagger.Android annotation processor runs it will create 2 subcomponents for us.
 */
@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LaunchModule::class])
    internal abstract fun launcherActivity(): LauncherActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [OnboardingModule::class])
    internal abstract fun onboardingActivity(): OnboardingActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            ScheduleModule::class,
            MapModule::class,
            InfoModule::class,
            SignInDialogModule::class,
            ReservationModule::class,
            PreferenceModule::class
        ]
    )
    internal abstract fun mainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SessionDetailModule::class,
            SignInDialogModule::class,
            ReservationModule::class,
            PreferenceModule::class
        ]
    )
    internal abstract fun sessionDetailActivity(): SessionDetailActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SpeakerModule::class,
            SignInDialogModule::class,
            EventActionsViewModelDelegateModule::class,
            PreferenceModule::class
        ]
    )
    internal abstract fun speakerActivity(): SpeakerActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            MapModule::class,
            PreferenceModule::class
        ]
    )
    internal abstract fun mapActivity(): MapActivity
}
