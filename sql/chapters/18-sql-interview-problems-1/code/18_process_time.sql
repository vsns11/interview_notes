-- P18 Average time per process on each machine (end ts minus start ts), 3 decimals
CREATE TABLE machine_event (machine_id int, process_id int,
  kind text CHECK (kind IN ('start','end')), ts numeric,
  PRIMARY KEY (machine_id, process_id, kind));
INSERT INTO machine_event VALUES
 (0,0,'start',0.712),(0,0,'end',1.520),(0,1,'start',3.140),(0,1,'end',4.120),
 (1,0,'start',0.550),(1,0,'end',1.550),(1,1,'start',0.430),(1,1,'end',1.420),
 (2,0,'start',4.100),(2,0,'end',4.512),(2,1,'start',2.500),(2,1,'end',5.000),
 (2,2,'start',9.000);                                  -- never finished
\echo '--- solution: self-join start row to end row'
SELECT s.machine_id, round(avg(e.ts - s.ts), 3) AS processing_time
FROM machine_event s
JOIN machine_event e
  ON e.machine_id = s.machine_id AND e.process_id = s.process_id
 AND s.kind = 'start' AND e.kind = 'end'
GROUP BY s.machine_id
ORDER BY s.machine_id;
\echo '--- alternative: pivot with FILTER, then average (unfinished process -> NULL -> skipped by avg)'
SELECT machine_id, round(avg(finish - begin_ts), 3) AS processing_time
FROM (SELECT machine_id, process_id,
             max(ts) FILTER (WHERE kind = 'start') AS begin_ts,
             max(ts) FILTER (WHERE kind = 'end')   AS finish
      FROM machine_event GROUP BY machine_id, process_id) AS p
GROUP BY machine_id
ORDER BY machine_id;
