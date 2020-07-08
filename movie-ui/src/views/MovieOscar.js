import React, { useState, useContext, useEffect } from "react";
import MovieCard from "../components/MovieCard";
import MovieSearch from "../components/MovieSearch";
import { MovieContext } from "../context/context";

const MovieOscar = (props) => {

  const movieContext = useContext(MovieContext);

  const [bestPics, setBestPics] = useState([]);

  const { getOscar } = movieContext;

  useEffect(() => {
    getOscar().then(response => {
      setBestPics(response.data)
    });
  }, []);

  return (
    <div className="container">
      <MovieSearch />

      <div className="columns features is-multiline">
        {bestPics.map(movie => (
          <MovieCard key={movie.imdbid} movie={movie} />
        ))}
      </div>
    </div>
  );
};

export default MovieOscar;
