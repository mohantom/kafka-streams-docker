import React from 'react';

const MovieDetailsOverview = ({overview}) => {
    return (
        <nav className="panel">
        <p className="panel-heading">
          Overview
        </p>
        <div className="panel-block is-active">
          <span className="panel-icon">
            <i className="fas fa-book" aria-hidden="true"></i>
          </span>
          { overview }
        </div>
      </nav>
    )
}

export default MovieDetailsOverview;

