import * as React from 'react'
import Error from './Error'

export default function withErrorBoundary(Component) {
    class WithErrorBoundary extends React.Component {
        constructor(props) {
            super(props);
            this.state = {hasError: false};
        }

        componentDidCatch(error, errorInfo) {
            this.setState( {hasError: true} );
        }

        render() {
            if (this.state.hasError) {
                return (
                    <div className="error-boundary">
                        <Error message="Something went wrong, please refresh page." />
                    </div>
                )
            }

            return <Component {...this.props} />;
        }
    }

    return WithErrorBoundary;
}