import slick.jdbc
import slick.jdbc.GetResult
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


trait Schema{

  case class Movie(movieId: Int, title: String, genres: String)
  class MoviesTable(tag: Tag) extends Table[Movie](tag, Some("public"), "movies") {
    override def * = (movieId, title, genres) <> (Movie.tupled, Movie.unapply)
    val movieId : Rep[Int] = column[Int]("movieId", O.AutoInc, O.PrimaryKey)
    val title: Rep[String] = column[String]("title")
    val genres : Rep[String] = column[String]("genres")
  }
  lazy val movies = TableQuery[MoviesTable]

  case class Rating(userId: Int, movieId: Int, rating: Double, timestamp: Long)
  class RatingsTable(tag: Tag) extends Table[Rating](tag, Some("public"), "ratings") {
    override def * = (userId, movieId, rating, timestamp) <> (Rating.tupled, Rating.unapply)
    val userId : Rep[Int] = column[Int]("userId")
    val movieId: Rep[Int] = column[Int]("movieId")
    val rating : Rep[Double] = column[Double]("rating")
    val timestamp : Rep[Long] = column[Long]("timestamp")
  }
  lazy val ratings = TableQuery[RatingsTable]
}

trait Queries extends Schema{

  val db: jdbc.H2Profile.backend.Database

  def selectHowManyMovies(): Future[Unit] = {
    val query = movies.length.result

    println("How many movies are in data set?")
    db.run(query).map { r =>
      println(r)
      println()
    }
  }

  def selectMostCommonGenre(): Future[Unit] = {

    case class GenreWithOccurences(genre: String, c: Int)

    implicit val getGenreWithOccurencesResult: AnyRef with GetResult[GenreWithOccurences] = GetResult(r => GenreWithOccurences(r.nextString, r.nextInt))
    val query =
      sql"""
        SELECT unnest(STRING_TO_ARRAY("genres", '|')) as genre
        , count(*) as c
      FROM public.movies
      group by 1
      order by 2 desc
      limit 1
         """.as[GenreWithOccurences]

    println("What is the most common genre of movie?")
    db.run(query).map { r =>
      r.foreach(x => println(s"Genre: ${x.genre}"))
      println()
    }

  }

  def selectTop10MoviesWithHighestRate(): Future[Unit] = {

    val query = ratings
      .joinLeft(movies).on(_.movieId === _.movieId)
      .groupBy{case (_,m) => m.map(_.title)}
      .map{case (title, group) => (title, group.length, group.map(_._1.rating).avg)}
        .sortBy(_._2.desc).sortBy(_._3.desc).map(c => (c._1, c._3)).take(10).result

    println("What are top 10 movies with highest rate ?")
    db.run(query).map { r =>
      r.foreach(x => println(s"Title: ${x._1.get}, Rating: ${x._2.get}"))
      println()
    }
  }

  def selectTop5MostOftenRatingUsers(): Future[Unit] = {
    val query = ratings.groupBy(_.userId).map{
      case (userId, group) => (userId, group.map(_.timestamp).max - group.map(_.timestamp).min, group.map(_.rating).length)
    }.map(c => (c._1, c._3.asColumnOf[Float]/c._2.asColumnOf[Float])).sortBy(_._2.desc).take(num=5).result


    println("What are 5 most often rating users ?")
    println("Generated query:")
    db.run(query).map { r =>
      r.foreach(x => println(s"User: ${x._1}"))
      println()
    }
  }

  def selectFirstAndLastRate(): Future[Unit] = {

    val toTimestamp = SimpleFunction.unary[Long, String]("to_timestamp")

    val timestampOfFirstRating = ratings.map(_.timestamp).min
    val timestampOfLastRating = ratings.map(_.timestamp).max

    val selectedMovies = movies.map(c => (c.movieId, c.title))
    val queryEarliest = ratings.filter(_.timestamp === timestampOfFirstRating).map(c => (c.movieId, c.timestamp,  LiteralColumn("earliest")))
      .joinLeft(selectedMovies).on((a,b) => a._1 === b._1).map(a => (toTimestamp(a._1._2), a._1._3, a._2.map(_._2)))

    val queryLatest = ratings.filter(_.timestamp === timestampOfLastRating).map(c => (c.movieId, c.timestamp,  LiteralColumn("latest")))
      .joinLeft(selectedMovies).on((a,b) => a._1 === b._1).map(a => (toTimestamp(a._1._2), a._1._3, a._2.map(_._2)))

    val query = queryEarliest.unionAll(queryLatest).result

    println("When was done first and last rate included in data set and what was the rated movie tittle?")
    db.run(query).map { r =>
      r.foreach(x => println(s"Label: ${x._2}, Title: ${x._3.get}, timestamp: ${x._1}"))
      println()
    }
  }

  def selectMoviesFrom1990(): Future[Unit] = {

    case class MovieFrom1990(name: String)

    implicit val getMovieFrom1990Result: AnyRef with GetResult[MovieFrom1990] = GetResult(r => MovieFrom1990(r.nextString))
    val query =
      sql"""
      select title
      from movies
      where substring("title" from '\((\d{4})\)') = '1990'
         """.as[MovieFrom1990]

    println("Find all movies released in 1990")
    db.run(query).map { r =>
      r.foreach(x => println(s"Title: ${x.name}"))
      println()
    }

  }

}


object Main extends App with Schema with Queries {


  val db = Database.forURL(sys.env("DATABASE_URL"), driver="org.postgresql.ds.PGSimpleDataSource", user = sys.env("USER"), password = sys.env("PASSWORD"))


  try {
    val queries = for {
      _ <- selectHowManyMovies()
      _ <- selectMostCommonGenre()
      _ <- selectTop10MoviesWithHighestRate()
      _ <- selectTop5MostOftenRatingUsers()
      _ <- selectFirstAndLastRate()
      _ <- selectMoviesFrom1990()
    } yield ()

    Await.result(queries, 1.minute)
  } finally db.close()
}
