import React, { useReducer } from "react";
import _ from "lodash";
import axios, { mongoAxios, movieUrls } from "../../axios";
import { MovieContext } from "../context";
import MovieReducer from "./movieReducer";
import withErrorBoundary from "../../hocs/withErrorBoundary"
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

  // or [movieStates, setMovieState] = useState(initialState);
  const [state, dispatch] = useReducer(MovieReducer, initialState);

  const getMovies = async page => {
    if (page < 0) {
      return;
    }
    const res = await mongoAxios.get(movieUrls.popular(page || 0));
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

    const res = await mongoAxios.get(movieUrls.search(searchText));
    console.log("got movies from search: ", res.data);
    dispatch({
      type: SET_MOVIES,
      payload: res.data,
      currentPage: 0
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
    const response = await mongoAxios.get(movieUrls.toprated(0));
    return response.data;
  };

  const playMovie = async fileurl => {
    await mongoAxios.post(movieUrls.play(), {fileurl: fileurl});
  }

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
        getMoviesTopRated,
        playMovie
      }}
    >
      {props.children}
    </MovieContext.Provider>
  );
};

export default withErrorBoundary(MovieState);
