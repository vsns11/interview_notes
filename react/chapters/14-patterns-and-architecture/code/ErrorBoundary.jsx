// tools_run: skip  (component module) — a reusable error boundary. Still a class component in React 19:
// there is no Hook equivalent of getDerivedStateFromError / componentDidCatch.
import { Component } from 'react';

export class ErrorBoundary extends Component {
  state = { error: null };

  static getDerivedStateFromError(error) {          // during render: switch to the fallback
    return { error };
  }

  componentDidCatch(error, info) {                   // after commit: side effects such as logging
    this.props.onError?.(error, info);
  }

  componentDidUpdate(prevProps) {                    // reset automatically when resetKeys change
    const { resetKeys = [] } = this.props;
    if (this.state.error && resetKeys.some((k, i) => !Object.is(k, prevProps.resetKeys?.[i]))) {
      this.setState({ error: null });
    }
  }

  reset = () => this.setState({ error: null });

  render() {
    if (this.state.error) {
      return this.props.fallback({ error: this.state.error, reset: this.reset });
    }
    return this.props.children;
  }
}
