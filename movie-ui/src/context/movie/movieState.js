import React, { useReducer } from "react";
import _ from "lodash";
import axios, { movieUrls } from "../../axios";
import { MovieContext } from "../context";
import MovieReducer from "./movieReducer";
import {
  GET_MOVIES,
  SET_MOVIES,
  FILTER_MOVIES,
  GET_MOVIE_DETAILS,
  GET_MOVIE_REVIEWS
} from "../actionTypes";

const MovieState = props => {
  const initialState = {
    movies: [],
    originalMovies: [],
    movieDetails: {
      currentPage: 1,
      movie: {},
      videos: [],
      tags: {},
      extraInfo: []
    },
    movieReviews: []
  };

  const [state, dispatch] = useReducer(MovieReducer, initialState);

  const getMovies = async page => {
    if (page < 0) {
      return;
    }
    const res = await axios.get(movieUrls.popular(page || 0));
    console.log("got movies: ", res.data);
    dispatch({
      type: GET_MOVIES,
      payload: res.data,
      currentPage: page || 0
    });
  };

  const searchMovie = async searchText => {
    if (!searchText) {
      return;
    }

    const res = await axios.get(movieUrls.search(searchText));
    console.log("got movies from search: ", res.data);
    dispatch({
      type: SET_MOVIES,
      payload: res.data.results,
      currentPage: res.data.page
    });
  };

  const filterMovies = filterText => {
    dispatch({
      type: FILTER_MOVIES,
      payload: state.originalMovies.filter(m =>
        m.title.toLowerCase().includes(filterText.toLowerCase())
      )
    });
  };

  const getMovieDetails = async movieid => {
    const response = await axios.get(movieUrls.details(movieid));

    const movie = response.data;
    const videos = _.filter(movie.videos.results, {
      site: "YouTube",
      type: "Trailer"
    });

    const country = _.chain(movie.production_countries)
      .map("name")
      .join(", ")
      .value();

    const boxOffice =
      movie.budget / 1000000 + "M/" + movie.revenue / 1000000 + "M";

    const tags = {
      TagLine: movie.tagline,
      Released: movie.release_date,
      Runtime: movie.runtime + " min",
      Genres: _.chain(movie.genres)
        .map("name")
        .join(", ")
        .value()
    };

    const extraInfo = [
      { heading: "Country", subheading: country, isLink: false },
      { heading: "Budget / Revenue", subheading: boxOffice, isLink: false },
      {
        heading: movieUrls.imdbLink(movie.imdb_id),
        subheading: "",
        isLink: true
      }
    ];

    console.log(movie);
    dispatch({
      type: GET_MOVIE_DETAILS,
      payload: { movie, videos, tags, extraInfo }
    });
  };

  const getMovieReviews = async movieId => {
    const response = await axios.get(movieUrls.review(movieId));
    const reviews = response.data.results;

    console.log(reviews);
    dispatch({ type: GET_MOVIE_REVIEWS, payload: reviews });
  };

  const getMoviesTopRated = async () => {
    const movieAxios = [1, 2, 3, 4, 5].map(num =>
      axios.get(movieUrls.toprated(num))
    );
    const [r1, r2, r3, r4, r5] = await Promise.all(movieAxios);

    const moviesToprated = _.chain([r1, r2, r3, r4, r5])
      .flatMap("data.results")
      .map(movie => {
        movie.release_year = movie.release_date.substr(0, 4);
        return movie;
      })
      .value();

    return moviesToprated;
  };

  return (
    <MovieContext.Provider
      value={{
        currentPage: state.currentPage,
        movies: state.movies,
        movieDetails: state.movieDetails,
        movieReviews: state.movieReviews,
        getMovies,
        searchMovie,
        filterMovies,
        getMovieDetails,
        getMovieReviews,
        getMoviesTopRated
      }}
    >
      {props.children}
    </MovieContext.Provider>
  );
};

export default MovieState;
