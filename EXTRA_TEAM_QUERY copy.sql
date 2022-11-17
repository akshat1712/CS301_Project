select dept.departure_day start_day, dept.train_no train_1,dept.station_departure from_station, dept.departure_time, dept.station_arrival stopping_station,  
dept.arrival_time arrival_train1, arrv.train_no train_2, arrv.departure_time departure_train2, arrv.arrival_time, arrv.station_arrival , dept.arrival_day+arrv.arrival_day - arrv.departure_day + day_check(dept.arrival_time,arrv.departure_time) end_day
from 
    (select * from service where station_departure = 'CHANDIGARH') dept,
    (select * from service where station_arrival = 'NEW DELHI') arrv
    
    where dept.station_arrival = arrv.station_departure 
    and ( 
            ( ((arrv.departure_time - dept.arrival_time ) >=  interval '1 minute') and ((arrv.departure_time - dept.arrival_time ) <=  interval ' 5 hour') )
            or 
            ( ((arrv.departure_time - dept.arrival_time  + interval '24 hour') >=  interval '1 minute') and ((arrv.departure_time - dept.arrival_time + interval '24 hour') <=  interval ' 4 hour') )
        )
    and dept.train_no != arrv.train_no
    UNION
    select departure_day,train_no,station_departure,departure_time,NULL,NULL,NULL,NULL,arrival_time,station_arrival ,arrival_day
    from service where station_arrival = 'NEW DELHI' and station_departure = 'CHANDIGARH';


create or replace function day_check(arrival_time time, departure_time time) 
returns integer as $$
declare
    day integer;
begin
    if arrival_time > departure_time then
        day := 1;
    else
        day := 0;
    end if;
    return day;
end;
$$ language plpgsql;



CREATE OR REPLACE FUNCTION get_film (from_stat VARCHAR, to_stat VARCHAR) 
    RETURNS TABLE (
        start_day INT,
        train_1 varchar,
        from_station varchar,
        departure_time time,
        stopping_station varchar,
        arrival_train1 time,
        train_2 varchar,
        departure_train2 time,
        arrival_time time,
        station_arrival varchar,
        end_day int
) 
AS $$
BEGIN
    RETURN QUERY select dept.departure_day start_day, dept.train_no train_1,dept.station_departure from_station, dept.departure_time, dept.station_arrival stopping_station,  
dept.arrival_time arrival_train1, arrv.train_no train_2, arrv.departure_time departure_train2, arrv.arrival_time, arrv.station_arrival , dept.arrival_day+arrv.arrival_day - arrv.departure_day + day_check(dept.arrival_time,arrv.departure_time) end_day
from 
    (select * from service where station_departure = from_stat) dept,
    (select * from service where station_arrival = to_stat) arrv
    
    where dept.station_arrival = arrv.station_departure 
    and ( 
            ( ((arrv.departure_time - dept.arrival_time ) >=  interval '1 minute') and ((arrv.departure_time - dept.arrival_time ) <=  interval ' 5 hour') )
            or 
            ( ((arrv.departure_time - dept.arrival_time  + interval '24 hour') >=  interval '1 minute') and ((arrv.departure_time - dept.arrival_time + interval '24 hour') <=  interval ' 4 hour') )
        )
    and dept.train_no != arrv.train_no
    UNION
    select departure_day,train_no,station_departure,departure_time,NULL,NULL,NULL,NULL,arrival_time,station_arrival ,arrival_day
    from service where station_arrival = to_stat and station_departure = from_stat;
END; $$ 

LANGUAGE 'plpgsql';
