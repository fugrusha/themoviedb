# MovieDataBase API Service

### Stack of technologies:
- **Spring Boot**
- **Spring Security** to implement basic authentication
- **Spring Test** to write unit and integration tests
- **Hibernate** to map entities to database tables
- **PostgreSQL** to store data
- **hsqldb** - in-memory database for tests
- **Liquibase** to manage database migrations
- **Maven** to manage dependencies, to create a package
- **OpenFeign** - HTTP Client to perform requests to external API
- **Swagger** to create API documentation
- **Object-Translator** to facilitate the translation of objects of different types but similar structure.
- **Bean-Randomizer** - tool for the generation of random beans for tests
- **Query-Builder** - tool for building dynamic SQL, JPA or hibernate queries
- **Jacoco Maven plugin** to get know about test coverage 
- **PMD Maven plugin** - tool, that produces a report on both code rule violations and detected copy and paste fragments
- **Checkstyle Maven plugin** - code was written according to google checkstyle

### Domain model diagram
![diagram](https://github.com/fugrusha/themoviedb/blob/master/domain-model-diagrams/Backend%20class%20diagram%20v2.png)

### Application features
- Store information about the movie, its actors and creators (director, producer, etc). 
- Classification of movies by genre
- Film statuses: not yet released, released
- Store information about the person and its cast's or crew's participation.
- Store information about registered users.
- User types: admin, moderator, content manager, registered user, unregistered user

#### Unregistered users can:
- to create account
- to view movies, its casts, its crews and reviews(comments)
- to view articles and comments
- to view actors pages

#### Registered users can:
- to rate movie
- to add movie review(comment)
- to rate movie cast and crew
- to add cast or crew review(comment)
- to mark which parts of the comment contain plot spoiler
- to find users with similar tastes (at personal page)
- to set like/dislike mark to other user's comments
- to set like/dislike mark to articles(news)
- to cancel his like/dislike
- to edit own profile
- to create a watchlist and add/remove movies from it
- to send a complaint to a moderator about spam, spoiler, etc. in articles, comments, movies/cast/crew/person description
- to send a complaint to the content manager about typos(misprints) in articles, movies/cast/crew/person description
- the blocked user cannot rate movies, write comments. Only view.

#### Admin can:
- assign access rights to a user
- ban/unban a user


#### Moderator can:
- to ban/unban a user
- to moderate user's complaints
- to moderate user's complaints about other comments. Similar complaints automatically close. Moderator can manually correct the comment, or delete it. Or in the worst case, block the author of the comment. It may also turn out that everything is OK with the comment, then the moderator can punish the user who gave the false signal, or lower his level of trust.
- to moderate user's comments
- to change user's trust level (users with trust level higher than 5 can write comments without moderation)

#### Content Manager can:
- to perform CRUD operations a movie
- to perform CRUD operations a the genre
- to add genres to movie
- to perform CRUD operations with a person
- to perform CRUD operations with a movie cast
- to perform CRUD operations with a movie crew
- to perform CRUD operations with article
- to moderate misprint complaints (similar complaints automatically close)
- to import new movie from TheMovieDb

#### Calculated ratings by scheduled jobs
- average rating of a movie
- average rating of a movie cast
- average rating of a movie crew
- average rating of a person by its casts
- average rating of a person by its movies
- predicted average rating of an unreleased movie based on the actor's ratings

#### Additional scheduled jobs
- updating of isReleased movie status
- finding users with similar tastes
- one-off async job to import movies from TheMovieDb
