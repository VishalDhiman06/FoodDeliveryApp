package com.lpu.fooddeliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.lpu.fooddeliveryapp.ui.theme.FoodDeliveryAppTheme

// ─── Models ─────────────────────────────────────────
data class Category(val name: String, val icon: ImageVector, val color: Color, val imageUrl: String = "")
data class Offer(val title: String, val subtitle: String, val code: String, val color: Color)
data class FoodItem(val name: String, val restaurant: String, val price: String, val rating: String, val tag: String, val imageUrl: String = "")
data class Restaurant(
    val name: String, val cuisine: String, val minOrder: String, val rating: String,
    val deliveryTime: String, val dietTag: String, val location: String = "",
    val imageUrl: String = "", val offersAvailable: Boolean = false
)
data class FilterOptions(
    val ratingAbove4_5: Boolean = false, val under30MinOnly: Boolean = false,
    val vegOnly: Boolean = false, val offersOnly: Boolean = false
) {
    fun activeCount() = listOf(ratingAbove4_5, under30MinOnly, vegOnly, offersOnly).count { it }
}

val Orange = Color(0xFFFF5722)

fun iconFor(name: String): ImageVector = when {
    name.contains("pizza", true) -> Icons.Default.LocalPizza
    name.contains("burger", true) -> Icons.Default.LunchDining
    name.contains("pasta", true) || name.contains("noodle", true) || name.contains("chinese", true) -> Icons.Default.RamenDining
    name.contains("biryani", true) || name.contains("kebab", true) || name.contains("curry", true) -> Icons.Default.SetMeal
    name.contains("roll", true) || name.contains("momo", true) -> Icons.Default.LunchDining
    name.contains("dosa", true) || name.contains("idli", true) -> Icons.Default.RiceBowl
    name.contains("cake", true) || name.contains("dessert", true) -> Icons.Default.Cake
    name.contains("drink", true) || name.contains("juice", true) -> Icons.Default.LocalBar
    else -> Icons.Default.Fastfood
}

// ─── Image w/ fallback ──────────────────────────────
@Composable
fun FoodImage(imageUrl: String, icon: ImageVector, modifier: Modifier = Modifier, tint: Color = Orange, size: Int = 40) {
    if (imageUrl.isBlank()) {
        Icon(icon, null, tint = tint, modifier = modifier.then(Modifier.size(size.dp)))
    } else {
        SubcomposeAsyncImage(
            model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier,
            loading = { Icon(icon, null, tint = tint, modifier = Modifier.size(size.dp)) },
            error = { Icon(icon, null, tint = tint, modifier = Modifier.size(size.dp)) }
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodDeliveryAppTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    var selectedCategory by remember { mutableStateOf("Pizza") }
    var selectedFood by remember { mutableStateOf("Pepperoni Pizza") }
    var selectedRestaurant by remember { mutableStateOf("Pizza Hut") }
    var query by remember { mutableStateOf("") }
    var filters by remember { mutableStateOf(FilterOptions()) }
    var showSheet by remember { mutableStateOf(false) }

    val restaurants = remember { restaurantList() }
    val filtered = remember(query, filters) { filterRestaurants(restaurants, query, filters) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFFFF8F2), Color(0xFFFFF3E0), Color.White))
        ),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item { TopBar() }
        item { SearchBar(query, filters.activeCount(), { query = it }) { showSheet = true } }
        if (query.isNotBlank() || filters.activeCount() > 0) {
            item { SearchSummary(query, filtered.size, filters) }
        }
        item { OfferBanner() }
        item { SectionTitle("Categories") }
        item { CategoryRow(selectedCategory) { selectedCategory = it } }
        item { SectionTitle("Popular Near You") }
        item { PopularFoodRow(selectedFood) { selectedFood = it } }
        item { SectionTitle("All Restaurants") }
        items(filtered) { r -> RestaurantCard(r, selectedRestaurant == r.name) { selectedRestaurant = r.name } }
    }

    if (showSheet) {
        FilterSheet(
            current = filters,
            onDismiss = { showSheet = false },
            onApply = { filters = it; showSheet = false },
            onReset = { filters = FilterOptions(); showSheet = false }
        )
    }
}

// ─── Top bar ────────────────────────────────────────
@Composable
fun TopBar() {
    Box(Modifier.fillMaxWidth().background(
        Brush.horizontalGradient(listOf(Color(0xFFFF6F3C), Orange, Color(0xFFE64A19)))
    )) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Deliver to", color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ludhiana, Punjab", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = .2f)), Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().background(Color.White.copy(alpha = .16f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("⚡ Fast delivery", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("24/7 support", color = Color.White.copy(alpha = .9f), fontSize = 12.sp)
            }
        }
    }
}

// ─── Search ─────────────────────────────────────────
@Composable
fun SearchBar(query: String, filterCount: Int, onQueryChange: (String) -> Unit, onFilterClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = Orange)
            Spacer(Modifier.width(8.dp))
            TextField(
                value = query, onValueChange = onQueryChange,
                placeholder = { Text("Search food, restaurants or city...", fontSize = 14.sp, color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Orange
                ),
                singleLine = true
            )
            IconButton(onClick = onFilterClick) {
                BadgedBox(badge = {
                    if (filterCount > 0) Badge(containerColor = Orange) { Text("$filterCount", color = Color.White, fontSize = 10.sp) }
                }) { Icon(Icons.Default.FilterList, null, tint = Orange) }
            }
        }
    }
}

@Composable
fun SearchSummary(query: String, count: Int, filters: FilterOptions) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5EE)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            val title = when {
                query.equals("phagwara", true) -> "Phagwara delivery is available"
                query.isBlank() -> "Showing restaurants for your chosen filters"
                else -> "Search results for \"$query\""
            }
            Text(title, fontWeight = FontWeight.Bold, color = Orange)
            Spacer(Modifier.height(4.dp))
            val subtitle = if (count == 0) "No restaurants match your current filters. Try clearing one of the options."
            else "$count restaurant${if (count != 1) "s" else ""} matched your search${if (filters.activeCount() > 0) " and filters" else ""}."
            Text(subtitle, fontSize = 13.sp, color = Color.DarkGray)
        }
    }
}

// ─── Offers ─────────────────────────────────────────
@Composable
fun OfferBanner() {
    val offers = listOf(
        Offer("50% OFF", "On your first order", "FIRST50", Orange),
        Offer("Free Delivery", "Orders above ₹299", "FREEDEL", Color(0xFF6C63FF)),
        Offer("Buy 1 Get 1", "On selected items", "BOGO", Color(0xFF00BFA5))
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 12.dp)) {
        items(offers) { OfferCard(it) }
    }
}

@Composable
fun OfferCard(offer: Offer) {
    Card(modifier = Modifier.width(250.dp).height(118.dp), shape = RoundedCornerShape(18.dp)) {
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(offer.color, offer.color.copy(alpha = .75f))))) {
            Column(Modifier.padding(16.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(offer.title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text(offer.subtitle, color = Color.White.copy(alpha = .9f), fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use: ", color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
                    Box(Modifier.background(Color.White.copy(alpha = .2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(offer.code, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Icon(Icons.Default.LocalOffer, null, tint = Color.White.copy(alpha = .15f),
                modifier = Modifier.size(78.dp).align(Alignment.CenterEnd).padding(end = 8.dp))
        }
    }
}

// ─── Categories ─────────────────────────────────────
@Composable
fun CategoryRow(selected: String, onSelected: (String) -> Unit) {
    val categories = listOf(
        Category("Pizza", Icons.Default.LocalPizza, Color(0xFFFF7043), "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200"),
        Category("Burgers", Icons.Default.LunchDining, Color(0xFF8D6E63), "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200"),
        Category("Sushi", Icons.Default.SetMeal, Color(0xFF26C6DA), "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=200"),
        Category("Drinks", Icons.Default.LocalBar, Color(0xFF7E57C2), "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=200"),
        Category("Desserts", Icons.Default.Cake, Color(0xFFEC407A), "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=200"),
        Category("Salads", Icons.Default.SoupKitchen, Color(0xFF66BB6A), "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=200"),
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        items(categories) { cat -> CategoryChip(cat, selected == cat.name) { onSelected(cat.name) } }
    }
}

@Composable
fun CategoryChip(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp).clickable { onClick() }) {
        Box(
            Modifier.size(58.dp).clip(CircleShape)
                .background(if (isSelected) category.color.copy(alpha = .16f) else Color(0xFFF5F5F5))
                .border(if (isSelected) 1.5.dp else 0.dp, if (isSelected) category.color else Color.Transparent, CircleShape),
            Alignment.Center
        ) {
            FoodImage(category.imageUrl, category.icon, tint = category.color, size = 28, modifier = Modifier.fillMaxSize().clip(CircleShape))
        }
        Spacer(Modifier.height(6.dp))
        Text(category.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) category.color else Color(0xFF333333), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ─── Popular food ───────────────────────────────────
@Composable
fun PopularFoodRow(selected: String, onSelected: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        items(popularFoodList()) { food -> PopularFoodCard(food, selected == food.name) { onSelected(food.name) } }
    }
}

@Composable
fun PopularFoodCard(food: FoodItem, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(170.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFFFF5EE) else Color.White),
        border = BorderStroke(1.dp, if (isSelected) Orange else Color.Transparent)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(106.dp).background(Color(0xFFFFF3E0)), Alignment.Center) {
                FoodImage(food.imageUrl, iconFor(food.name), size = 48, modifier = Modifier.fillMaxSize())
                Box(
                    Modifier.align(Alignment.TopEnd).padding(8.dp).background(Orange, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) { Text(food.tag, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
            }
            Column(Modifier.padding(10.dp)) {
                Text(food.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(food.restaurant, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(food.price, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Orange)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(13.dp))
                        Text(food.rating, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ─── Restaurant card ────────────────────────────────
@Composable
fun RestaurantCard(restaurant: Restaurant, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFFFF8F2) else Color.White),
        border = BorderStroke(1.dp, if (isSelected) Orange else Color.Transparent)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF3E0)), Alignment.Center) {
                FoodImage(restaurant.imageUrl, iconFor(restaurant.cuisine), size = 36, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Box(Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(restaurant.dietTag, color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(restaurant.cuisine, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Text(restaurant.rating, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(restaurant.deliveryTime, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(restaurant.minOrder, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Orange)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1A1A1A))
        Text("See All", fontSize = 13.sp, color = Orange, fontWeight = FontWeight.Medium)
    }
}

// ─── Filter sheet ───────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(current: FilterOptions, onDismiss: () -> Unit, onApply: (FilterOptions) -> Unit, onReset: () -> Unit) {
    var f by remember(current) { mutableStateOf(current) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Filter restaurants", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                TextButton(onClick = onReset) { Text("Reset") }
            }
            Spacer(Modifier.height(12.dp))
            Text("Quick filters", fontWeight = FontWeight.SemiBold, color = Color(0xFF444444))
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(f.ratingAbove4_5, { f = f.copy(ratingAbove4_5 = !f.ratingAbove4_5) }, { Text("Rating 4.5+") })
                FilterChip(f.under30MinOnly, { f = f.copy(under30MinOnly = !f.under30MinOnly) }, { Text("Under 30 min") })
                FilterChip(f.vegOnly, { f = f.copy(vegOnly = !f.vegOnly) }, { Text("Veg only") })
                FilterChip(f.offersOnly, { f = f.copy(offersOnly = !f.offersOnly) }, { Text("Offers available") })
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onApply(f) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
                Text("Apply filters", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Filtering helpers ──────────────────────────────
fun filterRestaurants(restaurants: List<Restaurant>, query: String, filters: FilterOptions = FilterOptions()): List<Restaurant> {
    val q = query.trim().lowercase()
    return restaurants.filter { r ->
        val matchesQuery = q.isEmpty() || listOf(r.name, r.cuisine, r.dietTag, r.location).any { it.lowercase().contains(q) }
        val matchesRating = !filters.ratingAbove4_5 || (r.rating.toDoubleOrNull() ?: 0.0) >= 4.5
        val matchesTime = !filters.under30MinOnly || parseDeliveryMinutes(r.deliveryTime) <= 30
        val matchesVeg = !filters.vegOnly || r.dietTag.contains("veg", true)
        val matchesOffers = !filters.offersOnly || r.offersAvailable
        matchesQuery && matchesRating && matchesTime && matchesVeg && matchesOffers
    }
}

fun parseDeliveryMinutes(deliveryTime: String): Int =
    Regex("\\d+").findAll(deliveryTime).map { it.value.toInt() }.firstOrNull() ?: Int.MAX_VALUE

// ─── Sample data ────────────────────────────────────
fun popularFoodList() = listOf(
    FoodItem("Pepperoni Pizza", "Pizza Hut", "₹299", "4.8", "🔥 Hot", "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=300"),
    FoodItem("Classic Burger", "Burger King", "₹149", "4.5", "⭐ Top", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300"),
    FoodItem("Pasta Alfredo", "Dominos", "₹249", "4.6", "New", "https://images.unsplash.com/photo-1645112411341-6c4fd023714a?w=300"),
    FoodItem("Fried Rice", "Chinese Wok", "₹179", "4.3", "Popular", "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=300"),
)

fun restaurantList() = listOf(
    Restaurant("Pizza Hut", "Pizza, Pasta, Garlic Bread", "₹200 min", "4.8", "25-30 min", "Pure Veg", "Phagwara", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200", true),
    Restaurant("Burger King", "Burgers, Fries, Wraps", "₹150 min", "4.5", "20-25 min", "Non-Veg", "Ludhiana", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200", false),
    Restaurant("Behrouz Biryani", "Biryani, Kebabs, Curries", "₹300 min", "4.7", "35-40 min", "Non-Veg", "Phagwara", "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=200", true),
    Restaurant("Wow Momo", "Rolls, Momos, Noodles", "₹100 min", "4.2", "20-30 min", "Veg", "Jalandhar", "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=200", true),
    Restaurant("Saravana Bhavan", "South Indian, Dosa, Idli", "₹150 min", "4.6", "30-35 min", "Pure Veg", "Phagwara", "https://images.unsplash.com/photo-1630383249896-424e482df921?w=200", true),
    Restaurant("Baskin Robbins", "Cakes, Pastries, Desserts", "₹200 min", "4.4", "15-20 min", "Veg", "Ludhiana", "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=200", false),
)
