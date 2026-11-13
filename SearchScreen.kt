package sa.pricecompare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sa.pricecompare.R
import sa.pricecompare.data.Store

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(vm: SearchViewModel) {
    val state by vm.state.collectAsState()
    val focus = LocalFocusManager.current
    val items = state.visible

    Column(Modifier.fillMaxSize().background(Surface0)) {
        Column(
            Modifier.fillMaxWidth().background(Navy)
                .statusBarsPadding().padding(16.dp)
        ) {
            Text(
                stringResource(R.string.app_name),
                color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::setQuery,
                singleLine = true,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focus.clearFocus(); vm.search(state.query)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Navy, unfocusedTextColor = Navy
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Store.values().toList()) { store ->
                val st = state.statuses[store]
                val label = buildString {
                    append(store.nameAr)
                    if (st != null && !st.loading) append(if (st.failed) " ⚠" else " (${st.count})")
                }
                FilterChip(
                    selected = store in state.selected,
                    onClick = { vm.toggleStore(store) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Orange)

        when {
            !state.searched -> Hint(stringResource(R.string.start_hint))
            items.isEmpty() && !state.loading -> Hint(stringResource(R.string.no_results))
            else -> LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.store.name + it.url + it.price }) { p ->
                    ProductCard(p, isCheapest = p == items.first())
                }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = Navy.copy(alpha = 0.6f), modifier = Modifier.padding(24.dp))
    }
}
