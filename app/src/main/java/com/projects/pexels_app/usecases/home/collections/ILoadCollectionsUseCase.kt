package com.projects.pexels_app.usecases.home.collections

import com.projects.pexels_app.domain.models.Collection

interface ILoadCollectionsUseCase {
    suspend fun loadCollections() : List<Collection>
}