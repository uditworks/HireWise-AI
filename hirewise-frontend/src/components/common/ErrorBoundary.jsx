import React from 'react';
import Button from './Button';
import { AlertCircle, RotateCcw, Home } from 'lucide-react';

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    // Suppress console spam while capturing for diagnostics
    if (process.env.NODE_ENV === 'development') {
      console.warn('ErrorBoundary caught error:', error, errorInfo);
    }
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center p-4 bg-slate-50">
          <div className="max-w-md w-full bg-white rounded-2xl border border-red-200 shadow-xl p-8 text-center space-y-4">
            <div className="w-14 h-14 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center mx-auto">
              <AlertCircle className="w-7 h-7" />
            </div>
            <div className="space-y-1">
              <h2 className="text-xl font-extrabold text-navy-950">Something went wrong</h2>
              <p className="text-xs text-slate-500 leading-relaxed">
                An unexpected interface error occurred. You can reload the application or return to the home page.
              </p>
            </div>
            <div className="pt-2 flex items-center justify-center gap-3">
              <Button
                variant="outline"
                size="sm"
                onClick={() => window.location.reload()}
              >
                <RotateCcw className="w-4 h-4" />
                <span>Reload Page</span>
              </Button>
              <Button
                variant="navy"
                size="sm"
                onClick={this.handleReset}
              >
                <Home className="w-4 h-4" />
                <span>Go to Home</span>
              </Button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
