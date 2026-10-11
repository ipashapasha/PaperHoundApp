package com.example.paperhoundapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.paperhoundapp.data.Shelf
import com.example.paperhoundapp.ui.addbook.AddBookScreen
import com.example.paperhoundapp.ui.home.HomeScreen
import com.example.paperhoundapp.ui.shelf.ShelfScreen

private const val HOME_ROUTE = "home"
private const val ADD_BOOK_ROUTE = "add_book"
private const val SHELF_ARG = "shelf"
private const val SHELF_ROUTE = "shelf/{$SHELF_ARG}"

@Composable
fun PaperHoundNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = HOME_ROUTE, modifier = modifier) {
        composable(HOME_ROUTE) {
            HomeScreen(
                onOpenShelf = { shelf -> navController.navigate("shelf/${shelf.name}") },
                onAddBook = { navController.navigate(ADD_BOOK_ROUTE) },
            )
        }
        composable(
            SHELF_ROUTE,
            arguments = listOf(navArgument(SHELF_ARG) { type = NavType.StringType }),
        ) { entry ->
            val shelf = Shelf.valueOf(requireNotNull(entry.arguments?.getString(SHELF_ARG)))
            ShelfScreen(shelf, onBack = { navController.popBackStack() })
        }
        composable(ADD_BOOK_ROUTE) {
            AddBookScreen(onClose = { navController.popBackStack() })
        }
    }
}
