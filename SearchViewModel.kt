package sa.pricecompare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import sa.pricecompare.data.Product
import sa.pricecompare.data.Store
import sa.pricecompare.data.StoreSources

data class StoreStatus(val loading: Boolean, val count: Int, val failed: Boolean)

data class UiState(
    val query: String = "",
    val searched: Boolean = false,
    val loading: Boolean = false,
    val results: List<Product> = emptyList(),
    val selected: Set<Store> = Store.values().toSet(),
    val statuses: Map<Store, StoreStatus> = emptyMap()
) {
    /** Store-filtered, sorted lowest -> highest price. */
    val visible: List<Product>
        get() = results.filter { it.store in selected }.sortedBy { it.price }
}

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    private var job: Job? = null

    fun search(raw: String) {
        val q = raw.trim()
        if (q.isEmpty()) return
        job?.cancel()
        _state.update {
            it.copy(
                query = q, searched = true, loading = true, results = emptyList(),
                statuses = StoreSources.all.associate { s -> s.store to StoreStatus(true, 0, false) }
            )
        }
        job = viewModelScope.launch {
            StoreSources.all.map { src ->
                launch {
                    val found: List<Product>
                    try {
                        found = src.search(q)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _state.update { it.copy(statuses = it.statuses + (src.store to StoreStatus(false, 0, true))) }
                        return@launch
                    }
                    _state.update {
                        it.copy(
                            results = it.results + found,
                            statuses = it.statuses + (src.store to StoreStatus(false, found.size, false))
                        )
                    }
                }
            }.joinAll()
            _state.update { it.copy(loading = false) }
        }
    }

    fun toggleStore(store: Store) = _state.update {
        val s = if (store in it.selected) it.selected - store else it.selected + store
        it.copy(selected = s)
    }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }
}
