# API Connect App

An Android news search application that demonstrates API integration, data fetching, and modern Android development practices.

## Which API Did You Choose and Why?

**NewsAPI (https://newsapi.org/)**

I chose NewsAPI for several compelling reasons:

- **Comprehensive News Coverage**: Provides access to articles from over 80,000 news sources worldwide, making it ideal for a search-based news application
- **Well-Documented REST API**: Clear, straightforward documentation with predictable endpoints and response structures
- **Free Tier Available**: Offers a developer-friendly free tier (100 requests/day) perfect for learning and prototyping
- **Rich Data Structure**: Returns detailed article information including title, description, author, source, images, and publication dates
- **Flexible Search**: Supports keyword search with filtering options (language, sort order, date ranges) enabling powerful query capabilities
- **Real-World Relevance**: Working with news data provides a practical, relatable use case that demonstrates real-world API integration patterns

## How Did You Implement Data Fetching and JSON Parsing?

### Architecture Overview

The implementation follows a clean, layered architecture:

**1. Network Layer (Retrofit + OkHttp)**
```kotlin
// NewsApiClient.kt - Singleton object managing Retrofit instance
- OkHttpClient with logging interceptor for debugging
- Retrofit with GsonConverterFactory for automatic JSON parsing
- Base URL configured via BuildConfig
```

**2. API Service Interface**
```kotlin
// NewsApiService.kt - Defines API endpoints
interface NewsApiService {
    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): Response<NewsResponse>
}
```

**3. Data Models**
```kotlin
// NewsModels.kt - Kotlin data classes for JSON mapping
data class NewsResponse(
    val status: String?,
    val totalResults: Int?,
    val articles: List<Article> = emptyList()
)

data class Article(
    val source: Source?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)
```

**4. Repository Pattern**
```kotlin
// NewsRepository.kt - Handles business logic and error handling
- Validates API key before making requests
- Wraps network calls in try-catch blocks
- Maps HTTP status codes to user-friendly error messages
- Filters out invalid articles (missing title/URL)
- Returns sealed class results (Success/Error)
```

**5. ViewModel + StateFlow**
```kotlin
// DashboardViewModel.kt - Manages UI state
- Uses Kotlin Coroutines for async operations
- StateFlow for reactive UI updates
- Handles loading states, error messages, and data
```

### JSON Parsing Strategy

- **Automatic Parsing**: Gson automatically converts JSON responses to Kotlin data classes
- **Null Safety**: All fields are nullable with safe defaults to handle incomplete data
- **Data Validation**: Repository filters articles with missing critical fields (title, URL)
- **Type Safety**: Strongly-typed models prevent runtime errors

### Key Implementation Details

- **Coroutines**: All network calls use `suspend` functions for non-blocking async execution
- **Dependency Injection**: Repository accepts service and API key as constructor parameters for testability
- **Configuration Management**: API key stored in `local.properties` and injected via BuildConfig
- **Logging**: HTTP logging interceptor enabled in debug builds for troubleshooting

## What Challenges Did You Face When Handling Errors or Slow Connections?

### Challenge 1: Network Connectivity Detection

**Problem**: Users might attempt searches without an active internet connection, leading to confusing timeout errors.

**Solution**: 
- Implemented proactive network checking using `ConnectivityManager` before API calls
- Check for active network with internet capability
- Display user-friendly "No connection" message with retry action
- Prevents unnecessary API calls and provides immediate feedback

```kotlin
private fun hasNetworkConnection(): Boolean {
    val connectivityManager = requireContext().getSystemService<ConnectivityManager>()
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
```

### Challenge 2: HTTP Error Code Handling

**Problem**: NewsAPI returns various HTTP status codes (400, 401, 426, 429) that need context-specific error messages.

**Solution**:
- Mapped each status code to actionable user messages:
  - `400`: "Invalid query. Please refine your keyword and try again."
  - `401`: "Unauthorized response from NewsAPI. Check that your API key is valid."
  - `426`: "NewsAPI plan upgrade required for this request."
  - `429`: "Rate limit reached. Please wait a minute and retry."
- Provides clear guidance on what went wrong and how to fix it

### Challenge 3: Exception Handling

**Problem**: Multiple failure points (network errors, parsing errors, unexpected exceptions) need graceful handling.

**Solution**:
- Wrapped API calls in try-catch with specific exception types:
  - `IOException`: Network-related errors (timeouts, connection failures)
  - `Exception`: Catch-all for unexpected errors
- Used sealed class `NewsRepositoryResult` to represent success/failure states
- Prevents app crashes and provides meaningful error feedback

### Challenge 4: Loading State Management

**Problem**: Users need feedback during slow network requests to know the app is working.

**Solution**:
- Implemented comprehensive UI state management:
  - `isLoading`: Shows progress indicator during API calls
  - `showStatus`: Controls visibility of status messages
  - `transientMessage`: Displays temporary error notifications via Snackbar
- Loading indicator appears immediately when search starts
- Status messages update based on results (empty, error, success)

### Challenge 5: API Key Security

**Problem**: API keys shouldn't be hardcoded in source code or committed to version control.

**Solution**:
- Store API key in `local.properties` (gitignored)
- Inject key via BuildConfig at compile time
- Validate key presence before making requests
- Provide helpful error message if key is missing

### Challenge 6: Incomplete or Invalid Data

**Problem**: API sometimes returns articles with missing titles, URLs, or images.

**Solution**:
- Filter articles in repository layer:
  ```kotlin
  val articles = body?.articles.orEmpty().filter {
      !it.title.isNullOrBlank() && !it.url.isNullOrBlank()
  }
  ```
- Use nullable types with safe defaults in data models
- Gracefully handle missing images in UI (placeholder or hide)

### Challenge 7: User Experience During Errors

**Problem**: Errors shouldn't completely block the user experience.

**Solution**:
- Snackbar notifications for transient errors (dismissible, non-intrusive)
- Retry actions on network errors
- Preserve previous search results when possible
- Clear, actionable error messages instead of technical jargon

## How Would You Improve Your App's UI or Performance in Future Versions?

### UI Improvements

**1. Enhanced Visual Design**
- Implement Material Design 3 (Material You) with dynamic theming
- Add card-based article layouts with elevation and shadows
- Include article thumbnail images with proper aspect ratios
- Add pull-to-refresh gesture for updating results
- Implement smooth animations for list updates and transitions

**2. Advanced Search Features**
- Add filter chips for date ranges, sources, and categories
- Implement search suggestions/autocomplete
- Add voice search capability
- Include advanced search options (author, domain filtering)
- Save and manage favorite searches

**3. Article Reading Experience**
- In-app web view for reading articles without leaving the app
- Reader mode with text extraction and formatting
- Bookmark/save articles for offline reading
- Share articles via Android share sheet
- Add "read later" functionality

**4. Improved Empty/Error States**
- Custom illustrations for different error types
- Animated empty state graphics
- Contextual help tips for first-time users
- Better onboarding experience

**5. Accessibility Enhancements**
- Improve content descriptions for screen readers
- Add larger touch targets for buttons
- Support dynamic text sizing
- Ensure proper color contrast ratios
- Add keyboard navigation support

### Performance Improvements

**1. Caching Strategy**
- Implement Room database for offline article storage
- Cache search results with expiration timestamps
- Add memory cache (LRU) for images
- Implement cache-first, network-fallback strategy
- Reduce redundant API calls for repeated searches

**2. Image Loading Optimization**
- Integrate Coil or Glide for efficient image loading
- Implement progressive image loading with placeholders
- Add image caching and memory management
- Lazy load images as user scrolls
- Compress and resize images appropriately

**3. Pagination**
- Implement infinite scroll with pagination
- Load articles in batches (20-30 at a time)
- Add "Load More" button as fallback
- Prefetch next page when user nears bottom
- Reduce initial load time and memory usage

**4. Background Processing**
- Use WorkManager for periodic news updates
- Implement background sync for saved searches
- Add notifications for breaking news (opt-in)
- Offload heavy processing to background threads

**5. Network Optimization**
- Implement request debouncing for search input
- Add request cancellation when user navigates away
- Use HTTP caching headers effectively
- Compress network payloads
- Implement exponential backoff for retries

**6. Code Architecture**
- Migrate to Jetpack Compose for modern, declarative UI
- Implement proper dependency injection (Hilt/Koin)
- Add comprehensive unit and integration tests
- Use Kotlin Flow for reactive data streams
- Implement proper error handling with Result types

**7. Monitoring & Analytics**
- Add crash reporting (Firebase Crashlytics)
- Implement performance monitoring
- Track user engagement metrics
- Monitor API response times
- Add logging for debugging production issues

**8. Additional Features**
- Dark mode support (already partially implemented)
- Multi-language support with localization
- Personalized news feed based on interests
- Social features (comments, discussions)
- Offline mode with cached content
- Widget for home screen news updates

### Technical Debt & Refactoring

- Extract hardcoded strings to resources
- Improve test coverage (currently minimal)
- Add ProGuard rules for release builds
- Implement proper error tracking
- Add CI/CD pipeline for automated testing
- Document code with KDoc comments
- Create comprehensive user documentation

## Features

- **News Search**: Search for news articles by keyword
- **Recent Searches**: View and reuse recent search queries
- **Article Preview**: See article title, description, source, and publication date
- **External Links**: Open full articles in browser
- **Error Handling**: Graceful handling of network errors and API issues
- **Loading States**: Visual feedback during data fetching
- **Network Detection**: Proactive connectivity checking

## Setup Instructions

1. Clone the repository
2. Get a free API key from [NewsAPI.org](https://newsapi.org/)
3. Create `local.properties` in the project root (if it doesn't exist)
4. Add your API key: `newsApiKey=YOUR_API_KEY_HERE`
5. Sync the project with Gradle
6. Run the app on an emulator or physical device

## Technologies Used

- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Networking**: Retrofit 2 + OkHttp
- **JSON Parsing**: Gson
- **Async**: Kotlin Coroutines + Flow
- **UI**: View Binding, RecyclerView, Material Components
- **Navigation**: Jetpack Navigation Component
- **Lifecycle**: ViewModel, StateFlow

## Project Structure

```
app/src/main/java/com/example/apiconnectapp/
├── data/
│   ├── news/
│   │   ├── NewsApiClient.kt      # Retrofit configuration
│   │   ├── NewsApiService.kt     # API endpoints
│   │   ├── NewsModels.kt         # Data classes
│   │   └── NewsRepository.kt     # Business logic
│   └── recent/
│       └── RecentSearchRepository.kt
├── ui/
│   ├── dashboard/
│   │   ├── DashboardFragment.kt  # Search UI
│   │   ├── DashboardViewModel.kt # State management
│   │   └── NewsAdapter.kt        # RecyclerView adapter
│   └── home/
│       ├── HomeFragment.kt       # Recent searches UI
│       └── RecentAdapter.kt
└── MainActivity.kt
```

## Screenshots

See the `act6/` folder for app screenshots demonstrating:
- Successful news fetching
- Recent searches on home screen
- No connection error handling

## License

This project is for educational purposes.