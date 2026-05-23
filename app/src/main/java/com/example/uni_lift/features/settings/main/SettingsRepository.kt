package com.example.uni_lift.features.settings.main

class SettingsRepository : SettingsContract.Repository {
    override fun getInitialState(): SettingsContract.UiState {
        return SettingsContract.UiState()
    }
}
