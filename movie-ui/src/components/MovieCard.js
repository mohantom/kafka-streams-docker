import React from "react";
import { Link } from "react-router-dom";
import "./MovieCard.css";

const basePosterUrl = "http://image.tmdb.org/t/p/w185/";

const movieCard = React.memo(({ movie }) => {

  const truncate = text => {
    return text.length > 50 ? text.substr(0, 50) + '...' : text;
  }

  return (
    <Link to={{
      pathname: `/movie/${movie.imdbid}`,
      movie: movie
      }}>
      <div>
        <div className="card column">
          <div className="card-image">
            <figure className="image is-6by3">
              <img src={movie.poster} alt="poster" />
            </figure>
          </div>
          <div className="card-content">
            <div className="media">
              <div className="media-content">
                <div className="title is-5">
                  <span>{movie.title}</span>
                  <div className="tag has-addon">
                    <span className="tag is-info">{movie.rating}</span>
                    <span className="tag">{movie.year}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="content">{truncate(movie.plot)}</div>
          </div>
        </div>
      </div>
    </Link>
  );
});

export default movieCard;
