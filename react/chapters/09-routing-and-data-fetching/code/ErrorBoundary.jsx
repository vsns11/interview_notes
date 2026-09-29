// tools_run: skip  (component module) — the minimal error boundary. It must be a class component:
// function components have no equivalent of getDerivedStateFromError yet. (Or use the react-error-boundary package.)
import { Component } from 'react';

export class ErrorBoundary extends Component {
  state = { error: null };
  static getDerivedStateFromError(error) { return { error }; }       // render the fallback next
  componentDidCatch(error, info) { this.props.onError?.(error, info); } // log it (Sentry, console…)
  reset = () => this.setState({ error: null });
  render() {
    if (this.state.error) return this.props.fallback({ error: this.state.error, reset: this.reset });
    return this.props.children;
  }
}
