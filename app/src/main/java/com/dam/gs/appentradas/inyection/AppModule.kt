package com.dam.gs.appentradas.injection

import com.dam.gs.appentradas.data.repository.TicketRepositoryImpl
import com.dam.gs.appentradas.domain.repository.TicketRepository
import com.dam.gs.appentradas.domain.usecase.CheckInTicket
import com.dam.gs.appentradas.domain.usecase.GetEvents
import com.dam.gs.appentradas.domain.usecase.ValidateTicket
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTicketRepository(): TicketRepositoryImpl = TicketRepositoryImpl()

    @Provides
    @Singleton
    fun provideTicketRepositoryInterface(
        impl: TicketRepositoryImpl
    ): TicketRepository = impl

    @Provides
    fun provideGetEvents(repository: TicketRepository) = GetEvents(repository)

    @Provides
    fun provideValidateTicket(repository: TicketRepository) = ValidateTicket(repository)

    @Provides
    fun provideCheckInTicket(repository: TicketRepository) = CheckInTicket(repository)
}