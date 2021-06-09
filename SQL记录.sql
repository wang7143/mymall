SELECT
    DATE_FORMAT( date, "%Y-%m-%d" ) as time,
MIN(date) as earliest_time
FROM test
GROUP BY DATE_FORMAT( date, "%Y-%m-%d" );


select * from news where daytime = date_add(curdate(), interval - 1 day) and status in (1,2);
select * from news where daytime = date_add(curdate(), interval - 1 day) and (status = 1 or status = 2);