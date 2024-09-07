package com.example.codingchallangemovieapp

import com.example.codingchallangemovieapp.ui.FavouriteManager
import com.example.codingchallangemovieapp.vm.MovieListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(
    application = MainApp::class,
    manifest = "src/main/AndroidManifest.xml",
    packageName = "com.example.codingchallangemovieapp"
)
class MovieListViewModelTest : KoinTest {
    companion object {
        const val FULL_TEST_TIMEOUT = 3000L
        const val ACTION_TIMEOUT = 500L

        val defaultDispatcher = Dispatchers.IO
        val uiDispatcher = Dispatchers.Default
    }

    private lateinit var viewModel: MovieListViewModel

    @Before
    fun setUp() {
        val ctx = RuntimeEnvironment.application
        val favouriteManager = FavouriteManager(ctx, defaultDispatcher)
        viewModel = MovieListViewModel(favouriteManager, defaultDispatcher, uiDispatcher)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testSwitchingPages() {
        runBlocking {
            withTimeout(FULL_TEST_TIMEOUT) {
                val list1 =
                    viewModel.currentlyDisplayedMovies.debounce(ACTION_TIMEOUT).first().movies

                println(list1)

                assertTrue(list1.isNotEmpty(), "Downloaded empty list on first page")

                viewModel.seeNextPage()

                val list2 =
                    viewModel.currentlyDisplayedMovies.debounce(ACTION_TIMEOUT).first().movies

                println(list2)

                assertTrue(list2.isNotEmpty(), "Downloaded empty list on second page")

                assertNotEquals(list1, list2, "Page one and two are identical")

                viewModel.seePreviousPage()

                val list3 =
                    viewModel.currentlyDisplayedMovies.debounce(ACTION_TIMEOUT).first().movies

                println(list3)

                assertEquals(list1, list3, "First page fetch error")

            }
        }
    }
}
