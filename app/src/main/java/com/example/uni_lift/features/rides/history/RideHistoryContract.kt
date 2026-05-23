package com.example.uni_lift.features.rides.history

import com.example.uni_lift.core.models.RideRecord

interface RideHistoryContract {

    data class UiState(
        val rides: List<RideRecord> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
    }

    interface Repository {
        suspend fun getRides(token: String): Result<List<RideRecord>>
    }
}
