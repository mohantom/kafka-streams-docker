import React, { useEffect, useContext } from "react";
import axios, { movieUrls } from "../axios";
import { MovieContext } from "../context/context";
import MovieDetailsInfo from "../components/MovieDetailsInfo";
import MovieDetailsOverview from "../components/MovieDetailsOverview";
import MovieDetailsPoster from "../components/MovieDetailsPoster";
import MovieDetailsReview from "../components/MovieDetailsReview";
import MovieDetailsTag from "../components/MovieDetailsTag";
import MovieDetailsTrailer from "../components/MovieDetailsTrailer";

const MovieDetails = props => {
  const movieContext = useContext(MovieContext);
  const imdbid = props.match.params.imdbid;
  const { fileurl } = props.location.state.mongoMovie;

  const {
    movieDetails,
    movieReviews,
    getMovieDetails,
    getMovieReviews,
    playMovie,
  } = movieContext;

  const { movie, videos, tags, extraInfo } = movieDetails;

  useEffect(() => {
    axios.get(movieUrls.findMovieId(imdbid))
    .then(response => {
      const tmdbid = response.data.movie_results[0].id;
      getMovieDetails(tmdbid);
      getMovieReviews(tmdbid);
    })
  }, []);

  return (
    <div>
      <div className="hero">
        <div className="hero-body">
          <div className="container">
            <div className="box has-background-grey-light">
              <h1 className="title is-1 has-text-centered">
                {movie.title}
                <span className="tag is-success">{movie.vote_average}</span>
              </h1>
            </div>
            <div className="columns is-multiline">
              <MovieDetailsPoster
                posterPath={movie.poster_path}
                posterSize="medium"
              />

              <div
                className="column is-7 has-vertically-aligned-content"
                data-aos="fade-right"
              >
                <MovieDetailsOverview overview={movie.overview} />

                {Object.keys(tags).map(tagName => {
                  return (
                    <MovieDetailsTag
                      key={tagName}
                      tagName={tagName}
                      tagValue={tags[tagName]}
                    />
                  );
                })}

                <br />

                <div className="columns about-links">
                  {extraInfo.map(info => {
                    return (
                      <MovieDetailsInfo
                        className="column about-links"
                        key={info.heading}
                        heading={info.heading}
                        subheading={info.subheading}
                        isLink={info.isLink}
                      />
                    );
                  })}
                </div>

                <div className="columns">
                  {videos.map(video => (
                    <MovieDetailsTrailer key={video.id} video={video} />
                  ))}
                </div>

                <div>
                  <button className="" onClick={() => playMovie(fileurl)}>Play</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container">
        <h1 className="title is-2">Reviews</h1>
        {movieReviews.map(review => {
          return <MovieDetailsReview review={review} />;
        })}
      </div>
    </div>
  );
};

export default MovieDetails;
