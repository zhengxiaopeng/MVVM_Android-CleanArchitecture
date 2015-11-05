/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fernandocejas.android10.sample.presentation.presenter;

import android.support.annotation.NonNull;

import com.fernandocejas.android10.sample.data.dto.User;
import com.fernandocejas.android10.sample.data.exception.DefaultErrorBundle;
import com.fernandocejas.android10.sample.data.exception.ErrorBundle;
import com.fernandocejas.android10.sample.domain.interactor.DefaultSubscriber;
import com.fernandocejas.android10.sample.domain.interactor.UseCase;
import com.fernandocejas.android10.sample.presentation.exception.ErrorMessageFactory;
import com.fernandocejas.android10.sample.presentation.mapper.UserModelDataMapper;
import com.fernandocejas.android10.sample.presentation.model.UserModel;
import com.fernandocejas.android10.sample.presentation.view.UserListView;

import java.util.Collection;
import java.util.List;

/**
 * {@link Presenter} that controls communication between views and models of the presentation
 * layer.
 */
public class UserListPresenter implements Presenter {

	private UserListView viewListView;

	private final UseCase getUserListUseCase;
	private final UserModelDataMapper userModelDataMapper;

	public UserListPresenter(UseCase getUserListUserCase, UserModelDataMapper userModelDataMapper) {
		this.getUserListUseCase = getUserListUserCase;
		this.userModelDataMapper = userModelDataMapper;
	}

	public void setView(@NonNull UserListView view) {
		this.viewListView = view;
	}

	@Override
	public void resume() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void destroy() {
		this.getUserListUseCase.unsubscribe();
	}

	/**
	 * Initializes the presenter by start retrieving the user list.
	 */
	public void initialize() {
		this.loadUserList();
	}

	/**
	 * Loads all users.
	 */
	private void loadUserList() {
		this.hideViewRetry();
		this.showViewLoading();
		this.getUserList();
	}

	public void onUserClicked(UserModel userModel) {
		this.viewListView.viewUser(userModel);
	}

	private void showViewLoading() {
		this.viewListView.showLoading();
	}

	private void hideViewLoading() {
		this.viewListView.hideLoading();
	}

	private void showViewRetry() {
		this.viewListView.showRetry();
	}

	private void hideViewRetry() {
		this.viewListView.hideRetry();
	}

	private void showErrorMessage(ErrorBundle errorBundle) {
		String errorMessage = ErrorMessageFactory.create(this.viewListView.getContext(),
				errorBundle.getException());
		this.viewListView.showError(errorMessage);
	}

	private void showUsersCollectionInView(Collection<User> usersCollection) {
		final Collection<UserModel> userModelsCollection =
				this.userModelDataMapper.transformUsers(usersCollection);
		this.viewListView.renderUserList(userModelsCollection);
	}

	private void getUserList() {
		this.getUserListUseCase.execute(new UserListSubscriber());
	}

	private final class UserListSubscriber extends DefaultSubscriber<List<User>> {

		@Override
		public void onCompleted() {
			UserListPresenter.this.hideViewLoading();
		}

		@Override
		public void onError(Throwable e) {
			UserListPresenter.this.hideViewLoading();
			UserListPresenter.this.showErrorMessage(new DefaultErrorBundle((Exception) e));
			UserListPresenter.this.showViewRetry();
		}

		@Override
		public void onNext(List<User> users) {
			UserListPresenter.this.showUsersCollectionInView(users);
		}
	}
}
