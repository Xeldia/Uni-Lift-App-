package com.example.uni_lift.features.settings.main

class SettingsPresenter(
    private val repository: SettingsContract.Repository
) : SettingsContract.Presenter {

    private var view: SettingsContract.View? = null

    override fun attach(view: SettingsContract.View) {
        this.view = view
        view.render(repository.getInitialState())
    }

    override fun detach() {
        view = null
    }

    override fun onBackClicked() {
        view?.navigateBack()
    }

    override fun onEditProfileClicked() {
        view?.navigateToEditProfile()
    }

    override fun onChangePasswordClicked() {
        view?.navigateToChangePassword()
    }
}
