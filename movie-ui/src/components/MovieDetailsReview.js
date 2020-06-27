import React from 'react'

const MovieDetailsReview = ({review}) => {
    return (
        <div>
        <p class="has-text-info">Author: { review.author }</p>
            <p>
              { review.content }
            </p>
      </div>
    );
};

export default MovieDetailsReview;