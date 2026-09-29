// tools_run: skip  (module imported by 14_node_runs_ts.ts)
export interface Rect { kind: 'rect'; w: number; h: number }
export interface Circle { kind: 'circle'; r: number }
export type Shape = Rect | Circle;

export function area(s: Shape): number {
  return s.kind === 'rect' ? s.w * s.h : Math.PI * s.r ** 2;
}
