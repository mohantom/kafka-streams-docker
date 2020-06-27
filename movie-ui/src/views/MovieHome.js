import React, { useContext, useEffect } from "react";
import MovieCard from "../components/MovieCard";
import MovieSearch from "../components/MovieSearch";
import { MovieContext } from "../context/context";

const MovieHome = (props) => {

  const movieContext = useContext(MovieContext);
  const { currentPage, movies, getMovies } = movieContext;

  const showLoadMore = true;

  const loadMoreMovies = () => {
    getMovies(currentPage + 1);
  };

  useEffect(() => {
    getMovies();
  }, []);

  return (
    <div className="container">
      <MovieSearch />

      <div className="columns features is-multiline">
        {movies.map(movie => (
          <MovieCard key={movie.imdbid} movie={movie} />
        ))}
      </div>

      {showLoadMore && (
        <div className="container box has-text-centered">
          <div className="button is-primary" onClick={loadMoreMovies}>
            Load More
          </div>
        </div>
      )}
    </div>
  );
};

export default MovieHome;
