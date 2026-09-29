// Testing custom Hooks: renderHook, a tiny test component, and fake timers.
import '../../../../tools_dom.mjs';
import { test, afterEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { useState, useEffect, useCallback } from 'react';
import { render, renderHook, act, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

// --- Hooks under test ---
function useCounter(initial = 0, { min = -Infinity, max = Infinity } = {}) {
  const [count, setCount] = useState(initial);
  const increment = useCallback(() => setCount(c => Math.min(max, c + 1)), [max]);
  const decrement = useCallback(() => setCount(c => Math.max(min, c - 1)), [min]);
  const reset = useCallback(() => setCount(initial), [initial]);
  return { count, increment, decrement, reset };
}
function useDebounce(value, delay) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(id);
  }, [value, delay]);
  return debounced;
}

afterEach(() => { cleanup(); mock.timers.reset(); });

// 1. renderHook: renders a hidden test component that calls the Hook; result.current = latest return value.
test('useCounter: increments, respects max, resets', () => {
  const { result } = renderHook(() => useCounter(8, { max: 10 }));
  assert.equal(result.current.count, 8);
  act(() => result.current.increment());               // state updates must be wrapped in act()
  act(() => result.current.increment());
  act(() => result.current.increment());               // would be 11, capped at 10
  assert.equal(result.current.count, 10);
  act(() => result.current.reset());
  assert.equal(result.current.count, 8);
});

test('useCounter: rerender with new arguments', () => {
  const { result, rerender } = renderHook(({ max }) => useCounter(0, { max }), { initialProps: { max: 1 } });
  act(() => { result.current.increment(); result.current.increment(); });
  assert.equal(result.current.count, 1);
  rerender({ max: 5 });                                 // same Hook instance, new props
  act(() => result.current.increment());
  assert.equal(result.current.count, 2);
});

// 2. Fake timers: control time instead of waiting for it.
test('useDebounce: only the last value within the delay gets through', () => {
  mock.timers.enable({ apis: ['setTimeout'] });
  const { result, rerender } = renderHook(({ v }) => useDebounce(v, 300), { initialProps: { v: 'r' } });
  rerender({ v: 're' });
  act(() => mock.timers.tick(200));
  rerender({ v: 'rea' });                               // restarts the 300 ms timer
  act(() => mock.timers.tick(299));
  assert.equal(result.current, 'r');                    // nothing has settled yet
  act(() => mock.timers.tick(1));
  assert.equal(result.current, 'rea');                  // 're' was skipped entirely
});

// 3. Often better: test the Hook through a small component, the way users meet it.
function Stepper() {
  const { count, increment, decrement } = useCounter(0, { min: 0 });
  return <><button onClick={decrement}>-</button><output aria-label="count">{count}</output><button onClick={increment}>+</button></>;
}
test('useCounter through a component: cannot go below min', async () => {
  const user = userEvent.setup();
  render(<Stepper />);
  await user.click(screen.getByRole('button', { name: '-' }));
  await user.click(screen.getByRole('button', { name: '+' }));
  await user.click(screen.getByRole('button', { name: '+' }));
  assert.equal(screen.getByLabelText('count').textContent, '2');
});
