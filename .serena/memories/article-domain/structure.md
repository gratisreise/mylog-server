# Article Domain Architecture

## Package Structure
```
com.mylog.domain.article/
├── ArticleController.java       # REST API endpoints
├── ArticleService.java          # Service facade coordinating operations
├── entity/
│   ├── Article.java            # Main article entity
│   ├── Tag.java                # Tag entity
│   ├── ArticleTag.java         # Article-Tag relationship (composite key)
│   └── ArticleTagId.java       # Composite key for ArticleTag
├── dto/
│   ├── request/
│   │   ├── ArticleCreateRequest.java
│   │   ├── ArticleUpdateRequest.java
│   │   └── ArticleSearchRequest.java
│   └── response/
│       ├── ArticleResponse.java
│       └── ArticleTestResponse.java
├── repository/
│   ├── ArticleRepository.java
│   ├── ArticleRepositoryCustom.java
│   ├── ArticleRepositoryImpl.java
│   ├── ArticleTagRepository.java
│   ├── TagRepository.java
│   └── TagRepositoryCustom.java
└── service/
    ├── ArticleReader.java      # Query operations (read-only)
    ├── ArticleWriter.java      # Command operations
    ├── TagReader.java          # Tag query operations
    └── TagWriter.java          # Tag command operations
```

## Key Components

### Controller Layer
**ArticleController**
- Dependencies: `articleService`
- Endpoints:
  - `createArticle` - POST /api/articles
  - `updateArticle` - PUT /api/articles/{id}
  - `deleteArticle` - DELETE /api/articles/{id}
  - `getArticle` - GET /api/articles/{id}
  - `getArticles` - GET /api/articles
  - `getMyArticles` - GET /api/articles/my

### Service Layer
**ArticleService** (Facade)
- Dependencies: `articleReader`, `articleWriter`, `s3Service`
- Methods:
  - `createArticle` - Create new article
  - `updateArticle` - Update existing article
  - `deleteArticle` - Delete article
  - `getArticle` - Get single article
  - `getArticles` - Get article list (2 overloads)
  - `searchArticles` - Search articles with criteria
  - `searchMyArticles` - Search current user's articles

**ArticleReader** (CQRS Query Service)
- Annotations: `@Transactional(readOnly = true)`
- Dependencies: `articleRepository`, `tagReader`, `memberReader`
- Methods:
  - `getArticles` - Paginated article list (2 overloads)
  - `search` - Search with criteria
  - `searchAll` - Search all articles
  - `searchMine` - Search user's articles
  - `getArticle` - Get single article by ID
  - `isExists` - Check existence
  - `getArticlesTest` - Test endpoint data
  - `getArticleById` - Get by ID (internal)

**ArticleWriter** (CQRS Command Service)
- Annotations: `@Transactional`
- Dependencies: `articleRepository`, `memberReader`, `categoryReader`, `tagWriter`
- Methods:
  - `create` - Create article
  - `update` - Update article
  - `delete` - Delete article

**TagReader**
- Annotations: `@Transactional(readOnly = true)`
- Dependencies: `tagRepository`
- Methods:
  - `getTags` - Get tags by IDs
  - `getTagByTagName` - Get tag by name

**TagWriter**
- Annotations: `@Transactional`
- Dependencies: `tagRepository`, `tagReader`
- Methods:
  - `saveTag` - Create or retrieve tag

### Entity Layer
**Article**
- Fields:
  - `id` - Primary key
  - `member` - Author (Many-to-One)
  - `category` - Category (Many-to-One)
  - `title` - Article title
  - `content` - Article content
  - `articleImg` - Image URL
  - `aiSummary` - AI-generated summary
  - `aiSummaryStatus` - Summary status enum
- Methods:
  - `update` - Update article fields
  - `updateAiSummary` - Update AI summary
  - `markAiSummaryFailed` - Mark summary as failed
- Inner class: `ArticleBuilder`

**Tag**
- Fields:
  - `id` - Primary key
  - `tagName` - Tag name (unique)
- Methods:
  - `from` - Static factory method
- Inner class: `TagBuilder`

**ArticleTag** (Join Entity)
- Fields:
  - `article` - Article reference
  - `tag` - Tag reference
- Methods:
  - `equals`, `hashCode` - Composite key equality
- Inner class: `ArticleTagBuilder`

## Design Patterns

### CQRS Pattern
- **Reader services**: Read-only operations with `@Transactional(readOnly = true)`
- **Writer services**: Command operations with `@Transactional`
- Benefits: Separation of concerns, optimized queries, clearer transaction boundaries

### Service Facade Pattern
- `ArticleService` acts as facade coordinating between Reader/Writer services
- Controllers only interact with facade, not directly with Reader/Writer

### Repository Pattern
- Standard Spring Data JPA repositories
- Custom repository implementations for complex queries
- `ArticleRepositoryCustom` + `ArticleRepositoryImpl` pattern

## Relationships
- Article → Member (Many-to-One)
- Article → Category (Many-to-One)
- Article ↔ Tag (Many-to-Many via ArticleTag)
- ArticleTag uses composite key (ArticleTagId)

## Transaction Boundaries
- **Reader methods**: Read-only, optimized for queries
- **Writer methods**: Write operations, full transaction support
- **Service facade**: Delegates to appropriate Reader/Writer

## Common Usage Patterns

### Creating an Article
```java
// Controller
@PostMapping
public ResponseEntity createArticle(@MemberId Long memberId, @RequestBody ArticleCreateRequest request)

// Service Facade
public Long createArticle(Long memberId, ArticleCreateRequest request)

// Writer
public Article create(Long memberId, ArticleCreateRequest request)
```

### Querying Articles
```java
// Controller
@GetMapping
public ResponseEntity getArticles(Pageable pageable)

// Service Facade
public Page<ArticleResponse> getArticles(Pageable pageable)

// Reader
public Page<Article> getArticles(Pageable pageable)
```

## Key Dependencies
- Member domain (author information)
- Category domain (article categorization)
- S3 Service (image upload)
- Common infrastructure (security, response, exceptions)