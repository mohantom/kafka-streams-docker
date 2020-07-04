import * as React from 'react'

export default Error = ({ message }) => (
    <div className="error">
        <i className="material-icons">cancel</i>
        <span className="error-icons">ERROR:</span>
        {message}
    </div>
)