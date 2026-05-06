package dev.vive.kdelauncher.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dev.vive.kdelauncher.ui.components.SearchBar
import org.junit.Rule
import org.junit.Test

class SearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_displaysSearchIcon() {
        composeTestRule.setContent {
            SearchBar(query = "", onQueryChange = {})
        }

        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun searchBar_showsEmptyQuery() {
        composeTestRule.setContent {
            SearchBar(query = "", onQueryChange = {})
        }

        onNodeWithText("").assertIsDisplayed()
    }

    @Test
    fun searchBar_showsQueryText() {
        composeTestRule.setContent {
            SearchBar(query = "test", onQueryChange = {})
        }

        onNodeWithText("test").assertIsDisplayed()
    }

    @Test
    fun searchBar_showsClearButtonWhenQueryNotEmpty() {
        composeTestRule.setContent {
            SearchBar(query = "test", onQueryChange = {})
        }

        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }

    @Test
    fun searchBar_hidesClearButtonWhenQueryEmpty() {
        composeTestRule.setContent {
            SearchBar(query = "", onQueryChange = {})
        }

        composeTestRule.onNodeWithContentDescription("Clear search").assertDoesNotExist()
    }

    @Test
    fun searchBar_clearButtonClearsQuery() {
        var currentQuery = "test"

        composeTestRule.setContent {
            SearchBar(query = currentQuery, onQueryChange = { currentQuery = it })
        }

        composeTestRule.onNodeWithContentDescription("Clear search").performClick()

        assert(currentQuery == "")
    }

    @Test
    fun searchBar_callsOnQueryChangeWhenTextEntered() {
        val results = mutableListOf<String>()

        composeTestRule.setContent {
            SearchBar(query = "", onQueryChange = { results.add(it) })
        }

        composeTestRule.onNodeWithText("")
            .performTextInput("hello")

        assert(results.contains("hello"))
    }

    private fun onNodeWithText(text: String) = composeTestRule.onNodeWithText(text)
}
