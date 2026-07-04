package com.lpu.fooddeliveryapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

// ─── Data Models ───────────────────────────────────────────
data class Category(val name: String, val icon: ImageVector, val color: Color, val imageUrl: String = "")
data class Offer(val title: String, val subtitle: String, val code: String, val color: Color)
// A single dish shown in the "Popular Near You" row
data class FoodItem(
    val name: String,
    val restaurant: String,
    val price: String,
    val rating: String,
    val time: String,
    val tag: String,
    val imageUrl: String = ""
)

// A restaurant shown in the "All Restaurants" list
data class Restaurant(
    val name: String,
    val cuisine: String,
    val minOrder: String,
    val rating: String,
    val deliveryTime: String,
    val dietTag: String,
    val imageUrl: String = ""
)

// ─── Icon Mapping Helper ───────────────────────────────────
// Returns the most relevant icon for a given food/cuisine name.
// Falls back to a generic Fastfood icon if nothing matches.
fun getIconForFood(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        n.contains("pizza") -> Icons.Default.LocalPizza
        n.contains("burger") -> Icons.Default.LunchDining
        n.contains("pasta") -> Icons.Default.RamenDining
        n.contains("noodle") || n.contains("rice") || n.contains("chinese") -> Icons.Default.RamenDining
        n.contains("biryani") || n.contains("kebab") || n.contains("curry") -> Icons.Default.SetMeal
        n.contains("roll") || n.contains("momo") -> Icons.Default.LunchDining
        n.contains("dosa") || n.contains("idli") || n.contains("south indian") -> Icons.Default.RiceBowl
        n.contains("cake") || n.contains("dessert") || n.contains("pastr") -> Icons.Default.Cake
        n.contains("drink") || n.contains("juice") -> Icons.Default.LocalBar
        else -> Icons.Default.Fastfood
    }
}

// ─── Reusable Image Composable ─────────────────────────────
// Shows a real photo (imageUrl) if provided; otherwise falls back to an icon.
// Using SubcomposeAsyncImage so we can show a fallback icon while loading or on error.
@Composable
fun FoodImage(
    imageUrl: String,
    fallbackIcon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = Color(0xFFFF5722),
    iconSize: Int = 40
) {
    if (imageUrl.isBlank()) {
        Icon(fallbackIcon, contentDescription = null, tint = iconTint,
            modifier = modifier.then(Modifier.size(iconSize.dp)))
    } else {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            loading = {
                Icon(fallbackIcon, contentDescription = null, tint = iconTint,
                    modifier = Modifier.size(iconSize.dp))
            },
            error = {
                Icon(fallbackIcon, contentDescription = null, tint = iconTint,
                    modifier = Modifier.size(iconSize.dp))
            }
        )
    }
}

// ─── Main Activity ─────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodDeliveryAppTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    FoodDeliveryHomeScreen()
                }
            }
        }
    }
}

// ─── HOME SCREEN ───────────────────────────────────────────
@Composable
fun FoodDeliveryHomeScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)) {
        item { TopBar() }
        item { SearchBar() }
        item { OfferBanner() }
        item { SectionTitle("Categories") }
        item { CategoryRow() }
        item { SectionTitle("Popular Near You") }
        item { PopularFoodRow() }
        item { SectionTitle("All Restaurants") }
        items(restaurantList()) { item -> RestaurantCard(item) }
    }
}

// ─── TOP BAR ───────────────────────────────────────────────
@Composable
fun TopBar() {
    Row(modifier = Modifier.fillMaxWidth()
        .background(Color(0xFFFF5722))
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("Deliver to", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Ludhiana, Punjab", color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications",
                tint = Color.White, modifier = Modifier.size(24.dp))
            Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = "Profile",
                    tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ─── SEARCH BAR ────────────────────────────────────────────
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth()
        .background(Color(0xFFFF5722))
        .padding(horizontal = 16.dp)
        .padding(bottom = 20.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search food, restaurants...", fontSize = 14.sp,
                color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null,
                tint = Color(0xFFFF5722)) },
            trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = null,
                tint = Color(0xFFFF5722)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

// ─── OFFER BANNER ──────────────────────────────────────────
@Composable
fun OfferBanner() {
    val offers = listOf(
        Offer("50% OFF", "On your first order", "FIRST50",
            Color(0xFFFF5722)),
        Offer("Free Delivery", "Orders above ₹299", "FREEDEL",
            Color(0xFF6C63FF)),
        Offer("Buy 1 Get 1", "On selected items", "BOGO",
            Color(0xFF00BFA5))
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 12.dp)) {
        items(offers) { offer -> OfferCard(offer) }
    }
}

@Composable
fun OfferCard(offer: Offer) {
    Card(modifier = Modifier.width(260.dp).height(110.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.horizontalGradient(
                listOf(offer.color, offer.color.copy(alpha = 0.7f))))) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(offer.title, color = Color.White,
                        fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text(offer.subtitle, color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use: ", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Box(modifier = Modifier.background(Color.White.copy(alpha = 0.25f),
                        RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(offer.code, color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Icon(Icons.Default.LocalOffer, contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(80.dp).align(Alignment.CenterEnd)
                    .padding(end = 8.dp))
        }
    }
}

// ─── CATEGORIES ────────────────────────────────────────────
@Composable
fun CategoryRow() {
    val categories = listOf(
        Category("Pizza", Icons.Default.LocalPizza, Color(0xFFFF7043),
            "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200"),
        Category("Burgers", Icons.Default.LunchDining, Color(0xFF8D6E63),
            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200"),
        Category("Sushi", Icons.Default.SetMeal, Color(0xFF26C6DA),
            "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=200"),
        Category("Drinks", Icons.Default.LocalBar, Color(0xFF7E57C2),
            "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=200"),
        Category("Desserts", Icons.Default.Cake, Color(0xFFEC407A),
            "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=200"),
        Category("Salads", Icons.Default.SoupKitchen, Color(0xFF66BB6A),
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=200"),
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)) {
        items(categories) { cat -> CategoryChip(cat) }
    }
}

@Composable
fun CategoryChip(category: Category) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape)
            .background(category.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center) {
            FoodImage(
                imageUrl = category.imageUrl,
                fallbackIcon = category.icon,
                iconTint = category.color,
                iconSize = 28,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(category.name, fontSize = 11.sp, fontWeight = FontWeight.Medium,
            color = Color(0xFF333333), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ─── POPULAR FOOD ROW ──────────────────────────────────────
@Composable
fun PopularFoodRow() {
    val items = popularFoodList()
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)) {
        items(items) { food -> PopularFoodCard(food) }
    }
}

@Composable
fun PopularFoodCard(food: FoodItem) {
    Card(modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp)
                .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center) {
                // Real photo if food.imageUrl is set, otherwise falls back to a matching icon
                FoodImage(
                    imageUrl = food.imageUrl,
                    fallbackIcon = getIconForFood(food.name),
                    iconSize = 48,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .background(Color(0xFFFF5722), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(food.tag, color = Color.White,
                        fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(food.name, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(food.restaurant, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(food.price, fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp, color = Color(0xFFFF5722))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null,
                            tint = Color(0xFFFFC107), modifier = Modifier.size(13.dp))
                        Text(food.rating, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ─── RESTAURANT CARD ───────────────────────────────────────
// Now takes a Restaurant instead of a reused FoodItem
@Composable
fun RestaurantCard(restaurant: Restaurant) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center) {
                // Real photo if restaurant.imageUrl is set, otherwise falls back to a matching icon
                FoodImage(
                    imageUrl = restaurant.imageUrl,
                    fallbackIcon = getIconForFood(restaurant.cuisine),
                    iconSize = 36,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.background(Color(0xFFE8F5E9),
                        RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(restaurant.dietTag, color = Color(0xFF2E7D32),
                            fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(restaurant.cuisine, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null,
                            tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(restaurant.rating, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null,
                            tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(restaurant.deliveryTime, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(restaurant.minOrder, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))
                }
            }
        }
    }
}

// ─── SECTION TITLE ─────────────────────────────────────────
@Composable
fun SectionTitle(title: String) {
    Row(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp, color = Color(0xFF1A1A1A))
        Text("See All", fontSize = 13.sp,
            color = Color(0xFFFF5722), fontWeight = FontWeight.Medium)
    }
}

// ─── DATA LISTS ────────────────────────────────────────────
fun popularFoodList() = listOf(
    FoodItem("Pepperoni Pizza", "Pizza Hut", "₹299", "4.8", "25 min", "🔥 Hot",
        "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=300"),
    FoodItem("Classic Burger", "Burger King", "₹149", "4.5", "20 min", "⭐ Top",
        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300"),
    FoodItem("Pasta Alfredo", "Dominos", "₹249", "4.6", "30 min", "New",
        "https://images.unsplash.com/photo-1645112411341-6c4fd023714a?w=300"),
    FoodItem("Fried Rice", "Chinese Wok", "₹179", "4.3", "35 min", "Popular",
        "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=300"),
)

fun restaurantList() = listOf(
    Restaurant("Pizza Hut", "Pizza, Pasta, Garlic Bread", "₹200 min", "4.8", "25-30 min", "Pure Veg",
        "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200"),
    Restaurant("Burger King", "Burgers, Fries, Wraps", "₹150 min", "4.5", "20-25 min", "Non-Veg",
        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200"),
    Restaurant("Behrouz Biryani", "Biryani, Kebabs, Curries", "₹300 min", "4.7", "35-40 min", "Non-Veg",
        "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=200"),
    Restaurant("Wow Momo", "Rolls, Momos, Noodles", "₹100 min", "4.2", "20-30 min", "Veg",
        "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=200"),
    Restaurant("Saravana Bhavan", "South Indian, Dosa, Idli", "₹150 min", "4.6", "30-35 min", "Pure Veg",
        "https://images.unsplash.com/photo-1630383249896-424e482df921?w=200"),
    Restaurant("Baskin Robbins", "Cakes, Pastries, Desserts", "₹200 min", "4.4", "15-20 min", "Veg",
        "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=200"),
)